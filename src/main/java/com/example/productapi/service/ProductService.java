package com.example.productapi.service;

import com.example.productapi.dto.ProductDto;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(ProductDto productDto);
    ProductDto getProductById(Long id);
    List<ProductDto> getAllProducts();
    ProductDto updateProduct(Long id, ProductDto productDto);
    ProductDto partialUpdateProduct(Long id, ProductDto productDto);
    void deleteProduct(Long id);
}