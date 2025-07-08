package com.example.productapi.kafka;


import com.example.productapi.validation.JsonSchemaValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumer {

    private final JsonSchemaValidator jsonSchemaValidator;

    public KafkaConsumer(JsonSchemaValidator jsonSchemaValidator) {
        this.jsonSchemaValidator = jsonSchemaValidator;
    }

    @KafkaListener(topics = "${app.kafka.topic.product-events}", groupId = "product-group")
    public void listen(String message) {
        try {
            // Validate the incoming message against JSON schema
            //jsonSchemaValidator.validateKafkaMessage(message);

            log.info("Received Message: {}", message);
            // Process the message here
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage());
        }
    }

}