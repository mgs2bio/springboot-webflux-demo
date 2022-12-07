package com.example.webflux;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Spring webflux demo",
        version = "1.0",
        description = "Spring webflux demo"
))
public class SpringbootWebfluxDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootWebfluxDemoApplication.class, args);
    }

}
