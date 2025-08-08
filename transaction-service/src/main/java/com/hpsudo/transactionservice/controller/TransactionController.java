package com.hpsudo.transactionservice.controller;

import com.hpsudo.transactionservice.dto.TransactionRequest;
import com.hpsudo.transactionservice.dto.TransactionResponse;
import com.hpsudo.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    public TransactionResponse log(@RequestBody @Valid TransactionRequest request) {
        return service.log(request);
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionResponse> getByAccount(@PathVariable Long accountId) {
        return service.getByAccount(accountId);
    }
}
