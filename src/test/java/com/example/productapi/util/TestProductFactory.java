package com.example.productapi.util;

import com.example.productapi.dto.ProductDto;

public class TestProductFactory {
    public static ProductDto validLaptopDto() {
        return new ProductDto(null, "Laptop", "High performance laptop", 999.99, 10);
    }

    public static ProductDto invalidProductDto() {
        return new ProductDto(null, "", "", -10.0, -1); // triggers validation errors
    }
}
