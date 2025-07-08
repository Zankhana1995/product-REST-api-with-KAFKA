package com.example.productapi.kafka;

import com.example.productapi.dto.KafkaMessage;
import com.example.productapi.validation.JsonSchemaValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonSchemaValidator jsonSchemaValidator;

    public void sendProductEvent(String eventType, Long productId, String productName) {
        KafkaMessage message = new KafkaMessage(eventType, productId, productName,
                "Product " + productName + " with ID " + productId + " was " + eventType.toLowerCase());

        // Validate against JSON schema before sending
        jsonSchemaValidator.validateKafkaMessage(message);

        kafkaTemplate.send("product-events", String.valueOf(message.getProductId()), message.toString());
    }
}