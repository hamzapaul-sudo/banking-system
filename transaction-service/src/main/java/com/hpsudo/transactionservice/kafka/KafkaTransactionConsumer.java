package com.hpsudo.transactionservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hpsudo.protobuf.TransactionEvent;
import com.hpsudo.transactionservice.model.Transaction;
import com.hpsudo.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionConsumer {

    private final TransactionRepository repository;

    @KafkaListener(topics = "transaction-events", groupId = "transaction-service")
    public void consume(ConsumerRecord<String, byte[]> record) {
        try {
            TransactionEvent event = TransactionEvent.parseFrom(record.value());
            log.info("📥 Consumed event from Kafka: {}", event);

            Transaction tx = Transaction.builder()
                    .accountId(event.getAccountId())
                    .type(event.getType())
                    .amount(event.getAmount())
                    .description(event.getDescription())
                    .timestamp(OffsetDateTime.parse(event.getTimestamp()).toLocalDateTime())
                    .build();

            repository.save(tx);
            log.info("✅ Transaction saved: {}", tx);

        } catch (InvalidProtocolBufferException e) {
            log.error("❌ Failed to parse Protobuf message", e);
        } catch (Exception e) {
            log.error("❌ Error while saving transaction", e);
        }
    }
}
