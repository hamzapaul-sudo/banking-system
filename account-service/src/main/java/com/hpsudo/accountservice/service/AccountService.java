package com.hpsudo.accountservice.service;

import com.hpsudo.accountservice.dto.AccountRequest;
import com.hpsudo.accountservice.dto.AccountResponse;
import com.hpsudo.accountservice.dto.AmountRequest;
import com.hpsudo.accountservice.exception.AccountNotFoundException;
import com.hpsudo.accountservice.kafka.KafkaTransactionProducer;
import com.hpsudo.accountservice.model.Account;
import com.hpsudo.accountservice.model.AccountStatus;
import com.hpsudo.accountservice.repository.AccountRepository;
import com.hpsudo.protobuf.TransactionEvent;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final KafkaTransactionProducer kafkaProducer;

    public AccountResponse createAccount(AccountRequest request) {
        Account account = Account.builder()
                .accountHolder(request.accountHolder())
                .balance(request.balance())
                .customerId(request.customerId())
                .type(request.type())
                .status(AccountStatus.ACTIVE)
                .deleted(false)
                .build();

        log.info("Creating account for customerId={}, type={}", request.customerId(), request.type());
        return toResponse(accountRepository.save(account));
    }

    public AccountResponse getAccount(Long id) {
        return accountRepository.findById(id)
                .filter(acc -> !acc.isDeleted())
                .map(this::toResponse)
                .orElseThrow(() -> new AccountNotFoundException("Account ID " + id + " not found"));
    }

    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountRepository.findAllByDeletedFalse(pageable)
                .map(this::toResponse);
    }

    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .filter(acc -> !acc.isDeleted())
                .orElseThrow(() -> new AccountNotFoundException("Account ID " + id + " not found"));

        account.setDeleted(true);
        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);

        log.info("Soft-deleted account ID {}", id);
    }

    public AccountResponse deposit(AmountRequest request) {
        return retryUpdate(request.accountId(), request.amount(), false);
    }

    public AccountResponse withdraw(AmountRequest request) {
        return retryUpdate(request.accountId(), request.amount(), true);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountHolder(),
                account.getBalance(),
                account.getCustomerId(),
                account.getType(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private void validateAccountIsActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE || account.isDeleted()) {
            throw new AccountNotFoundException("Account is not active or has been deleted");
        }
    }

    private AccountResponse retryUpdate(Long accountId, double amount, boolean isWithdraw) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                Account account = accountRepository.findById(accountId)
                        .filter(acc -> !acc.isDeleted())
                        .orElseThrow(() -> new AccountNotFoundException("Account ID " + accountId + " not found"));

                validateAccountIsActive(account);

                double updated = isWithdraw ? account.getBalance() - amount : account.getBalance() + amount;

                if (isWithdraw && updated < 0) {
                    throw new IllegalArgumentException("Insufficient funds");
                }

                account.setBalance(updated);
                Account savedAccount = accountRepository.save(account);
                AccountResponse accountResponse = toResponse(savedAccount);

                // Construct the transaction event
                String type = isWithdraw ? "WITHDRAW" : "DEPOSIT";
                String description = isWithdraw ? "Withdraw from account" : "Deposit to account";

                TransactionEvent event = TransactionEvent.newBuilder()
                        .setAccountId(accountResponse.id())
                        .setAmount(amount)
                        .setType(type)
                        .setDescription(description)
                        .setTimestamp(OffsetDateTime.now().toString())
                        .build();

                kafkaProducer.publish(event);
                return accountResponse;

            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
                log.warn("Version conflict detected on attempt {}/{} for accountId {}", i + 1, maxRetries, accountId);
                try {
                    Thread.sleep(50); // small delay before retrying
                } catch (InterruptedException ignored) {}
            }
        }
        throw new IllegalStateException("Could not complete operation after retries due to concurrent update");
    }

}
