package com.hpsudo.accountservice.controller;

import com.hpsudo.accountservice.dto.AccountRequest;
import com.hpsudo.accountservice.dto.AccountResponse;
import com.hpsudo.accountservice.dto.AmountRequest;
import com.hpsudo.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public AccountResponse createAccount(@RequestBody @Valid AccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @GetMapping
    public Page<AccountResponse> getAllAccounts(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return accountService.getAllAccounts(PageRequest.of(page, size));
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }

    @PostMapping("/deposit")
    public AccountResponse deposit(@RequestBody @Valid AmountRequest request) {
        return accountService.deposit(request);
    }

    @PostMapping("/withdraw")
    public AccountResponse withdraw(@RequestBody @Valid AmountRequest request) {
        return accountService.withdraw(request);
    }
}
