package com.example.webflux.service;

import com.example.webflux.dto.ProductDto;
import com.example.webflux.entity.Product;
import com.example.webflux.repository.ProductRepository;
import com.example.webflux.utils.AppUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.stream.Stream;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    public Flux<ProductDto> getProducts(){
        return repository.findAll().map(AppUtils::entityToDto)
                //.delayElements(Duration.ofSeconds(1));
                .flatMap(productDto -> Flux
                        .zip(Flux.interval(Duration.ofSeconds(2)),
                                Flux.fromStream(Stream.generate(() -> productDto))
                        )
                        .map(Tuple2::getT2).log()
                );
    }

    public Mono<ProductDto> getProduct(String id) {
        return repository.findById(id).map(AppUtils::entityToDto);
    }

    public Flux<ProductDto> getProductInRange(double min, double max) {
        return repository.findByPriceBetween(Range.closed(min, max));
    }

    public Mono<ProductDto> saveProduct(Mono<ProductDto> productDtoMono) {
      return productDtoMono.map(AppUtils::dtoToEntity)
               .flatMap(repository::insert)
               .map(AppUtils::entityToDto);
    }

    public Mono<ProductDto> updateProduct(Mono<ProductDto> productDtoMono, String id) {
      return  repository.findById(id)
                .flatMap(p -> productDtoMono.map(AppUtils::dtoToEntity))
                .doOnNext(e -> e.setId(id))
                .flatMap(repository::save)
                .map(AppUtils::entityToDto);
    }

    public Mono<Void> deleteProduct(String id) {
        return repository.deleteById(id);
    }

}
