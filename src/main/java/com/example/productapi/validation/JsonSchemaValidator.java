package com.example.productapi.validation;

import com.example.productapi.dto.KafkaMessage;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class JsonSchemaValidator {

    private final Schema schema;

    public JsonSchemaValidator() throws IOException {
        try (InputStream inputStream = new ClassPathResource("kafka-message-schema.json").getInputStream()) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            this.schema = SchemaLoader.load(rawSchema);
        }
    }

    public void validateKafkaMessage(KafkaMessage message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            schema.validate(jsonObject);
        } catch (ValidationException e) {
            throw new ValidationException("Invalid Kafka message: " + e.getMessage());
        }
    }

    public void validateKafkaMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            schema.validate(jsonObject);
        } catch (ValidationException e) {
            throw new ValidationException("Invalid Kafka message: " + e.getMessage());
        }
    }
}