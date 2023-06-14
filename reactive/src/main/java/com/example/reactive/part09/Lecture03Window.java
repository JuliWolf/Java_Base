package com.example.reactive.part09;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author JuliWolf
 * @date 14.06.2023
 */
public class Lecture03Window {
  private static AtomicInteger atomicInteger = new AtomicInteger(1);

  public static void main(String[] args) {
    eventStream()
        .window(Duration.ofSeconds(2))
        .flatMap(flux -> saveEvents(flux))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<String> eventStream () {
    return Flux.interval(Duration.ofMillis(500))
        .map(i -> "event" + i);
  }

  private static Mono<Integer> saveEvents (Flux<String> flux) {
    return flux
        .doOnNext(e -> System.out.println("saving " + e))
        .doOnComplete(() -> {
          System.out.println("saved this batch");
          System.out.println("----------------");
        })
        .then(Mono.just(atomicInteger.getAndIncrement()));
  }
}
