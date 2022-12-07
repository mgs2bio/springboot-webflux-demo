package com.example.webflux.utils;

import com.example.webflux.dto.ProductDto;
import com.example.webflux.entity.Product;
import org.springframework.beans.BeanUtils;

public class AppUtils {


    public static ProductDto entityToDto(Product product) {
        ProductDto productDto = new ProductDto();
        BeanUtils.copyProperties(product, productDto);
        return productDto;
    }

    public static Product dtoToEntity(ProductDto productDto) {
        Product product= new Product();
        BeanUtils.copyProperties(productDto, product);
        return product;
    }
}
