package com.hpsudo.transactionservice.dto;

import jakarta.validation.constraints.*;

public record TransactionRequest(
        @NotNull(message = "Account ID is required")
        Long accountId,

        @NotBlank(message = "Transaction type is required")
        String type,

        @NotNull(message = "Amount is required")
        @PositiveOrZero(message = "Amount must be positive")
        Double amount,

        @NotBlank(message = "Description is required")
        String description
) {}
