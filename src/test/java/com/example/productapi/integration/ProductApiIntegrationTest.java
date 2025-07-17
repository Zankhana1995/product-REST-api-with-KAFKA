package com.example.productapi.integration;

import com.example.productapi.dto.KafkaMessage;
import com.example.productapi.dto.ProductDto;
import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import com.example.productapi.util.TestProductFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = { "product-events" }, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;


    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        // Create Kafka Consumer
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "testGroup", "true", embeddedKafkaBroker);
        consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps, new StringDeserializer(), new StringDeserializer()
        ).createConsumer();

        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    @Order(1)
    void createProduct_shouldProduceKafkaEvent() throws Exception {
        ProductDto productDto = new ProductDto(
                null,
                "Laptop",
                "A high-performance laptop",
                1500.0,
                10
        );

        mockMvc.perform(post("/api/products")  // fixed URL
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated());

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, "product-events", Duration.ofSeconds(5));

        assertThat(record).isNotNull();
        System.out.println("Kafka message: " + record.value());

        KafkaMessage kafkaMessage = objectMapper.readValue(record.value(), KafkaMessage.class);
        assertThat(kafkaMessage.getProductName()).isEqualTo("Laptop");
        assertThat(kafkaMessage.getEventType()).isEqualTo("PRODUCT_CREATED");
    }

    @Test
    @Order(2)
    void createProduct_shouldReturnCreatedProduct() throws Exception {
        ProductDto productDto = TestProductFactory.validLaptopDto();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    @Order(3)
    void getProductById_shouldReturnProduct() throws Exception {
        Product saved = productRepository.save(
                new Product(null, "Smartphone", "Latest smartphone model", 699.99, 20));

        mockMvc.perform(get("/api/products/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smartphone")); // Fixed value to match saved
    }

    @Test
    @Order(4)
    void getAllProducts_shouldReturnList() throws Exception {
        productRepository.save(new Product(null, "Headphones", "Noise cancelling headphones", 199.99, 30));
        productRepository.save(new Product(null, "Prod2", "Desc2", 60.0, 20));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Order(5)
    void updateProduct_shouldUpdate() throws Exception {
        Product saved = productRepository.save(new Product(null, "Old Name", "Old Desc", 10.0, 2));
        saved.setName("New Name");

        mockMvc.perform(put("/api/products/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));

        assertThat(productRepository.findById(saved.getId()).get().getName()).isEqualTo("New Name");
    }

    @Test
    @Order(6)
    void partialUpdateProduct_shouldPatch() throws Exception {
        Product saved = productRepository.save(new Product(null, "Prod", "Desc", 99.0, 9));
        String patchJson = "{\"price\": 199.99}";

        mockMvc.perform(patch("/api/products/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(199.99));
    }

    @Test
    @Order(7)
    void deleteProduct_shouldRemoveProduct() throws Exception {
        Product saved = productRepository.save(
                new Product(null, "Laptop", "High performance laptop", 999.99, 10));

        mockMvc.perform(delete("/api/products/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }
}