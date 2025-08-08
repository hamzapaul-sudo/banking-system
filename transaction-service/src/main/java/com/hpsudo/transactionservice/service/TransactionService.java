package com.hpsudo.transactionservice.service;

import com.hpsudo.transactionservice.dto.TransactionRequest;
import com.hpsudo.transactionservice.dto.TransactionResponse;
import com.hpsudo.transactionservice.model.Transaction;
import com.hpsudo.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionResponse log(TransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .accountId(request.accountId())
                .type(request.type())
                .amount(request.amount())
                .description(request.description())
                .timestamp(LocalDateTime.now())
                .build();

        return toResponse(repository.save(transaction));
    }

    public List<TransactionResponse> getByAccount(Long accountId) {
        return repository.findByAccountIdOrderByTimestampDesc(accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getAccountId(),
                tx.getType(),
                tx.getAmount(),
                tx.getDescription(),
                tx.getTimestamp()
        );
    }
}
