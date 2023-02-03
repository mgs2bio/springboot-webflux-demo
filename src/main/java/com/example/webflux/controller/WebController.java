package com.example.webflux.controller;

import com.example.webflux.dto.Tweet;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RestController
public class WebController {

    private static final int DEFAULT_PORT = 8080;

    @Setter
    private int serverPort = DEFAULT_PORT;

    @Autowired
    private WebClient webClient;

    @GetMapping("/tweets-blocking")
    public List<Tweet> getTweetsBlocking() {
        log.info("Starting BLOCKING Controller!");
        final String uri = getSlowServiceUri();

        var restTemplate = new RestTemplate();
        ResponseEntity<List<Tweet>> response = restTemplate.exchange(
                uri, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Tweet>>(){});

        List<Tweet> result = response.getBody();
        result.forEach(tweet -> log.info(tweet.toString()));
        log.info("Exiting BLOCKING Controller!");
        return result;
    }

    @GetMapping(value = "/tweets-non-blocking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> getTweetsNonBlocking(@RequestParam(required = false) String id) {
        String threadName = Thread.currentThread().getName();
        log.info("Starting NON-BLOCKING Controller with id {} and thread {}", id, threadName);
        var tweetFlux = webClient
                .get()
                .uri(getSlowServiceUri())
                .retrieve()
                .bodyToFlux(Tweet.class)
                .doOnNext(t -> log.info("checking thread: id {}, current thread: {}, same thread: {}",
                        id,
                        Thread.currentThread().getName(),
                        threadName.equals(Thread.currentThread().getName())))
                .log();
       // var tweetList = tweetFlux.collectList().share().block();
         log.info("Exiting NON-BLOCKING Controller with id {}", id);
       // return tweetList;
        return tweetFlux;
    }

    @GetMapping(value = "/fluxInterval", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> fluxInterval() {
        log.info("Starting NON-BLOCKING Controller!");
        var fluxInterval = Flux.interval(Duration.ofSeconds(2)).take(3)
                .map(aLong -> String.format("event%d", aLong));
        fluxInterval.subscribe(log::info, System.out::println);
        log.info("Exiting NON-BLOCKING Controller!");
        return fluxInterval;
    }

    private String getSlowServiceUri() {
        return "http://localhost:" + serverPort + "/slow-service-tweets";
    }


}