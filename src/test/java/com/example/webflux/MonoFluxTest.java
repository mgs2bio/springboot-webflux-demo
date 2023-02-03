package com.example.webflux;

import com.example.webflux.dto.Tweet;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.*;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

public class MonoFluxTest {


    @Test
    public void testMonoBlock(){
        System.out.println("testMonoBlock begins.");
        Mono<String> monoString = Mono.delay(Duration.ofSeconds(2))
                .then(Mono.just("ABC")).log();
        //block is strongly discouraged
        String text = monoString.block();
        System.out.println("received text: " + text);
        assertEquals("ABC", text);
    }

    @Test
    public void testWebClientBlock(){
        var tweetFlux = WebClient.create()
                .get()
                .uri("http://localhost:8080/slow-service-tweets")
                .retrieve()
                .bodyToFlux(Tweet.class).log();
        var tweets = tweetFlux.collectList().block();
        System.out.println("received tweets: " + tweets);
        System.out.println("current thread: " + Thread.currentThread());
    }

    @Test
    public void testWebClientConcurrentCall() throws InterruptedException {
        Random rd = new Random();
        CountDownLatch cdl = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            var tweetFlux = WebClient.create()
                    .get()
                    .uri("http://localhost:8080/tweets-non-blocking?id="+ UUID.randomUUID().toString().substring(0,6))
                    .retrieve()
                    .bodyToFlux(Tweet.class);
            tweetFlux.doOnComplete(cdl::countDown).subscribe(t -> {
                System.out.println("received: " + t);
            });
        }
        cdl.await();
    }

    @Test
    public void testMono(){
        Mono<?> monoString = Mono.just("ABC").
                then(Mono.error(new RuntimeException("Exception occurred"))).log();
        monoString.subscribe(System.out::println, System.out::println);
    }

    @Test
    public void testCountDownLatch() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(5);
        List<String> bucket = Collections.synchronizedList(new ArrayList<>());
        Stream.generate(() -> new Thread(() -> {
           bucket.add("hello");
           cdl.countDown();
        })).limit(5).forEach(Thread::start);
        //DON'T use wait(), USE await()!!!
        cdl.await();
        bucket.add("released");
        assertThat(bucket).containsExactly("hello",
                "hello",
                "hello",
                "hello",
                "hello",
                "released"
        );
    }

    @Test
    public void testFlux(){
      Flux<?>  fluxString = Flux.just("Spring", "Summer", "Fall", "Winter").
              concatWithValues("Season")
             // .concatWith(Flux.error(new RuntimeException("Exception occurred in Flux")))
              .concatWithValues("cloud")
              .log();
      fluxString.subscribe(System.out::println, System.out::println);
    }

    @Test
    public void testFluxInterval() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(5);
        Flux<String> fluxInterval = Flux.interval(Duration.ofSeconds(2))
                .take(5)
                .map(aLong -> String.format("event%d", aLong))
                .onBackpressureBuffer(2, BufferOverflowStrategy.ERROR);
        fluxInterval.doOnSubscribe(s -> System.out.println("Started reading"))
                .doOnComplete(cdl::countDown)
                .subscribe(System.out::println, Throwable::printStackTrace);
        cdl.await();
    }

    @Test
    public void testStreamGenerate(){
        Stream.generate(() -> 1).limit(3).forEach(System.out::println);
    }

    @Test
    public void testFluxZip() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(3);
        Flux.just("one", "two", "three")
                .flatMap(str -> Flux.zip(Flux.interval(Duration.ofSeconds(2)),
                        Flux.fromStream(Stream.generate(() -> str))))
                .map(Tuple2::getT2)
                .log().doOnComplete(cdl::countDown)
                .subscribe(System.out::println);
        cdl.await();
    }

    @Test
    public void testFluxZip2(){
        Flux<String> flux1 = Flux.just(" {1} ","{2} ","{3} ","{4} " );
        Flux<String> flux2 = Flux.just(" |A|"," |B| "," |C| ");
        Flux.zip(flux1, flux2)
                .map(tuple -> {
                    System.out.println("T1:" + tuple.getT1()
                    + "| T2:" + tuple.getT2() + ".");
                    return tuple.getT2();
                })
                .subscribe(System.out::print);
    }

    @Test
    @DisplayName("Flux Pull模式 Integer.MAX_VALUE")
    public void testFluxPull() {
        Flux.generate((Consumer<SynchronousSink<Integer>>) sink -> {
                    int k = (int) (Math.random() * 10);
                    sink.next(k);
                })
                .take(3)
                // 默认获取 request(Integer.MAX_VALUE)
                .subscribe(integer -> System.out.println("Pull:" + integer));
    }

    @Test
    @DisplayName("Flux Push模式")
    public void testFluxPush() {
        Flux.create((Consumer<FluxSink<Integer>>) sink -> {
            int k = (int) (Math.random() * 10);
            sink.next(k);
        }).subscribe(integer -> System.out.println("Push:" + integer));
    }

    @Test
    @DisplayName("Mono Push模式")
    public void testMonoPush() {
        Mono.create(monoSink -> {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                monoSink.error(new RuntimeException(e));
                return;
            }
            monoSink.success("JayChou");
        }).subscribe(integer -> System.out.println("Mono Push:" + integer));
    }

    @Test
    @DisplayName("Flux Pull模式 request调用一次,则调用Sink生产一次")
    public void testFluxPullTwo() {
        Flux.generate((Consumer<SynchronousSink<Integer>>) sink -> {
                    int k = (int) (Math.random() * 10);
                    sink.next(k);
                })
                .subscribe(new Subscriber<Integer>() {
                    Subscription subscription;

                    private int count;

                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        // 订阅时候,生产1条数据
                        this.subscription.request(1);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        count++;
                        System.out.println("处理:" + integer);
                        // 在处理1次，当第二次处理时候,就不拉数据了
                        if (count < 2) {
                            this.subscription.request(1);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.out.println("onError");
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");
                    }
                });
    }



}
