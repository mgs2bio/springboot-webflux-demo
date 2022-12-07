package com.example.webflux.controller;


import com.example.webflux.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/client")
@Slf4j
public class ReactiveClientController {

    @GetMapping(value = "/products", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductDto> getProducts(){
        WebClient webClient = WebClient.create("http://localhost:8080/");
        long start = System.currentTimeMillis();
        Flux<ProductDto> products = webClient.get()
                .uri("/products")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(ProductDto.class)
                ;
        products.subscribe(p -> {
            System.out.println("Receiving Products: " + p);
        });
        System.out.println("getProducts taking time:" + (System.currentTimeMillis() - start));
        return products;
    }
}
