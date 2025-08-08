package com.hpsudo.transactionservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    private String type; // DEPOSIT, WITHDRAW, TRANSFER

    private Double amount;

    private String description;

    private LocalDateTime timestamp;
}
