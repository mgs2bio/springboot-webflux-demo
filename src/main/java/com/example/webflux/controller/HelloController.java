package com.example.webflux.controller;

import com.example.webflux.dto.ProductDto;
import com.example.webflux.repository.ProductRepository;
import com.example.webflux.utils.AppUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class HelloController {

    @Autowired
    private ProductRepository repository;

    @GetMapping("/hello")
    public String hello() {
        long start = System.currentTimeMillis();
        String helloStr = getHelloStr();
        System.out.println("普通接口耗时：" + (System.currentTimeMillis() - start));
        return helloStr;
    }

    @GetMapping("/hello2")
    public Mono<String> hello2() {
        long start = System.currentTimeMillis();
        Mono<String> hello2 = Mono.fromSupplier(() -> getHelloStr());
        System.out.println("WebFlux 接口耗时：" + (System.currentTimeMillis() - start));
        return hello2;
    }

    @GetMapping(value = "/flux",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> flux() {
        return Flux.fromArray(new String[]{"javaboy","itboyhub","www.javaboy.org","itboyhub.com"})
                .map(s -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return "my->data->" + s;
                    });
    }

    @GetMapping(value = "/fluxProducts",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductDto> fluxProducts() {
         return repository.findAll().map(AppUtils::entityToDto)
                .map(s -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return s;
                });
    }

    private String getHelloStr() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    }
}
