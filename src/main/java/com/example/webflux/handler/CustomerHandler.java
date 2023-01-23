package com.example.webflux.handler;

import com.example.webflux.dao.CustomerDao;
import com.example.webflux.dto.Customer;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class CustomerHandler {

    final private CustomerDao dao;

    public Mono<ServerResponse> loadCustomers(ServerRequest request) {
       Flux<Customer> customerList = dao.getCustomersNoDelay();
       return ServerResponse.ok().body(customerList, Customer.class);
    }

    public Mono<ServerResponse> loadCustomerStream(ServerRequest request) {
        Flux<Customer> customerList = dao.getCustomers();
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(customerList, Customer.class);
    }

    public Mono<ServerResponse> loadCustomer(ServerRequest request) {
        int customerId = Integer.valueOf(request.pathVariable("input"));
        Mono<Customer> customerMono = dao.getCustomers()
                .filter(c -> c.getId() == customerId)
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("No Customer found with id " + customerId)));
        return ServerResponse.ok()
                .body(customerMono, Customer.class);
    }

    public Mono<ServerResponse> saveCustomer(ServerRequest request) {
        Mono<Customer> customerMono = request.bodyToMono(Customer.class);
        Mono<String> saveResp = customerMono.map(dto -> dto.getId() + ":" + dto.getName());
        return ServerResponse.ok()
                .body(saveResp, String.class);
    }


}
