package com.example.webflux.controller;

import com.example.webflux.dto.Tweet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
public class TweetsSlowServiceController {

    @GetMapping("/slow-service-tweets")
    private List<Tweet> getAllTweets() throws Exception {
        Thread.sleep(1000L); // delay
        return Arrays.asList(
                new Tweet("RestTemplate rules", "@user1"),
                new Tweet("WebClient is better", "@user2"),
                new Tweet("OK, both are useful", "@user1"));
    }

    @GetMapping(value = "/tweetsFlux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    private Flux<Tweet> getAllTweetsFlux() throws Exception {
        log.info("/tweetsFlux called.");
        Thread.sleep(5000L); // delay
        return Flux.just(
                new Tweet("RestTemplate rules", "@user1"),
                new Tweet("WebClient is better", "@user2"),
                new Tweet("OK, both are useful", "@user1"));
    }


}