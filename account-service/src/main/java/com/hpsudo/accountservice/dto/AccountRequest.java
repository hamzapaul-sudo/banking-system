package com.hpsudo.accountservice.dto;

import com.hpsudo.accountservice.model.AccountType;
import jakarta.validation.constraints.*;

public record AccountRequest(
        @NotBlank(message = "Account holder name must not be blank")
        String accountHolder,

        @NotNull(message = "Balance must not be null")
        @PositiveOrZero(message = "Initial balance cannot be negative")
        Double balance,

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotNull(message = "Account type must be provided")
        AccountType type
) {}
