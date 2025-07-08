package com.example.productapi.service;

import com.example.productapi.dto.ProductDto;
import com.example.productapi.exception.ResourceNotFoundException;
import com.example.productapi.kafka.KafkaProducer;
import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final KafkaProducer kafkaProducer;

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = modelMapper.map(productDto, Product.class);
        Product savedProduct = productRepository.save(product);

        // Send Kafka event
        kafkaProducer.sendProductEvent("PRODUCT_CREATED", savedProduct.getId(), savedProduct.getName());

        return modelMapper.map(savedProduct, ProductDto.class);
    }

    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return modelMapper.map(product, ProductDto.class);
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        modelMapper.map(productDto, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);

        // Send Kafka event
        kafkaProducer.sendProductEvent("PRODUCT_UPDATED", updatedProduct.getId(), updatedProduct.getName());

        return modelMapper.map(updatedProduct, ProductDto.class);
    }

    @Override
    @Transactional
    public ProductDto partialUpdateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (productDto.getName() != null) {
            existingProduct.setName(productDto.getName());
        }
        if (productDto.getDescription() != null) {
            existingProduct.setDescription(productDto.getDescription());
        }
        if (productDto.getPrice() != null) {
            existingProduct.setPrice(productDto.getPrice());
        }
        if (productDto.getQuantity() != null) {
            existingProduct.setQuantity(productDto.getQuantity());
        }

        Product updatedProduct = productRepository.save(existingProduct);

        // Send Kafka event
        kafkaProducer.sendProductEvent("PRODUCT_UPDATED_PARTIAL", updatedProduct.getId(), updatedProduct.getName());

        return modelMapper.map(updatedProduct, ProductDto.class);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);

        // Send Kafka event
        kafkaProducer.sendProductEvent("PRODUCT_DELETED", product.getId(), product.getName());
    }
}