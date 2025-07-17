package com.example.productapi.kafka;

import com.example.productapi.dto.KafkaMessage;
import com.example.productapi.validation.JsonSchemaValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonSchemaValidator jsonSchemaValidator;
    private final ObjectMapper objectMapper;

    public void sendProductEvent(String eventType, Long productId, String productName) {
        KafkaMessage message = new KafkaMessage(eventType, productId, productName,
                "Product " + productName + " with ID " + productId + " was " + eventType.toLowerCase());

        jsonSchemaValidator.validateKafkaMessage(message);

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("product-events", String.valueOf(productId), jsonMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize KafkaMessage", e);
        }
    }
}