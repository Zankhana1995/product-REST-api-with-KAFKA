package com.example.productapi.service;

import com.example.productapi.dto.ProductDto;
import com.example.productapi.exception.ResourceNotFoundException;
import com.example.productapi.kafka.KafkaProducer;
import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        Long productId = 1L;
        Product product = new Product(productId, "Test Product", "Description", 100.0, 10);
        ProductDto productDto = new ProductDto(productId, "Test Product", "Description", 100.0, 10);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        // Act
        ProductDto result = productService.getProductById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
    }

    @Test
    void getProductById_ShouldThrowException_WhenProductNotExists() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(productId));
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() {
        // Arrange
        ProductDto inputDto = new ProductDto(null, "New Product", "Description", 200.0, 5);
        Product savedProduct = new Product(1L, "New Product", "Description", 200.0, 5);
        ProductDto expectedDto = new ProductDto(1L, "New Product", "Description", 200.0, 5);

        when(modelMapper.map(inputDto, Product.class)).thenReturn(new Product(null, "New Product", "Description", 200.0, 5));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(modelMapper.map(savedProduct, ProductDto.class)).thenReturn(expectedDto);

        // Act
        ProductDto result = productService.createProduct(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(kafkaProducer, times(1)).sendProductEvent(anyString(), anyLong(), anyString());
    }
}