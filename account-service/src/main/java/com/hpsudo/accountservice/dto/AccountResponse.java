package com.hpsudo.accountservice.dto;

import com.hpsudo.accountservice.model.AccountStatus;
import com.hpsudo.accountservice.model.AccountType;

import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String accountHolder,
        Double balance,
        Long customerId,
        AccountType type,
        AccountStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
