package com.hpsudo.accountservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AmountRequest(
        @NotNull(message = "Account ID must not be null")
        Long accountId,

        @NotNull(message = "Amount must not be null")
        @Positive(message = "Amount must be positive")
        Double amount
) {}
