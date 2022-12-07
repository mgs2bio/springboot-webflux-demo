package com.example.webflux.dao;

import com.example.webflux.dto.Customer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class CustomerDao {

    public Flux<Customer> getCustomers(){
       return Flux.range(1, 10)
               .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> System.out.println("processing count : " + i))
                .map(i -> new Customer(i, "customer"+i));
    }

    public Flux<Customer> getCustomersNoDelay(){
        return Flux.range(1, 10)
                .doOnNext(i -> System.out.println("processing count : " + i))
                .map(i -> new Customer(i, "customer"+i));
    }
}
