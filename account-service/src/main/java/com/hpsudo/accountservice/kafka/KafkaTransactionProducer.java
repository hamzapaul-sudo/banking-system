package com.hpsudo.accountservice.kafka;

import com.hpsudo.protobuf.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTransactionProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final String TOPIC = "transaction-events";

    public void publish(TransactionEvent event) {
        try {
            byte[] data = event.toByteArray(); // manually serialize
            kafkaTemplate.send(TOPIC, data);
            log.info("✅ Published event to Kafka: {}", event);
        } catch (Exception e) {
            log.error("❌ Failed to publish event", e);
        }
    }
}