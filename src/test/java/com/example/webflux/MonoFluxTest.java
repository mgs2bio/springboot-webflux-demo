package com.example.webflux;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.*;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MonoFluxTest {

    @Test
    public void testMono(){
        Mono<?> monoString = Mono.just("ABC").
                then(Mono.error(new RuntimeException("Exception occurred"))).log();
        monoString.subscribe(System.out::println, System.out::println);
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
    public void testFluxInterval(){
        Flux<String> fluxInterval = Flux.interval(Duration.ofSeconds(2))
                .take(5)
                .map(aLong -> String.format("event%d", aLong))
                .onBackpressureBuffer(2, BufferOverflowStrategy.ERROR);
        fluxInterval.doOnSubscribe(s -> System.out.println("Started reading"))
                .subscribe(System.out::println, Throwable::printStackTrace);
        //Thread has to wait more than 2 secs for message to print
        try {
            Thread.sleep(13000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testStreamGenerate(){
        Stream.generate(() -> 1).limit(3).forEach(System.out::println);
    }

    @Test
    public void testFluxZip(){
        long start = System.currentTimeMillis();
        Flux.just("one", "two", "three")
                .flatMap(str -> Flux.zip(Flux.interval(Duration.ofSeconds(2)),
                        Flux.fromStream(Stream.generate(() -> str))))
                .map(Tuple2::getT2)
                .log().subscribe(System.out::println);
        try {
            Thread.sleep(23000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
