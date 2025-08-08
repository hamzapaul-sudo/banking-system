package com.hpsudo.transactionservice.dto;

import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long accountId,
        String type,
        Double amount,
        String description,
        LocalDateTime timestamp
) {}
