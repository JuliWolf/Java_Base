package com.example.reactive.part02.assignment;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class StockPricePublisher {
  public static Flux<Integer> getPrice () {
    AtomicInteger atomicInteger = new AtomicInteger(100);

    return Flux.interval(Duration.ofSeconds(1))
        .map(i -> atomicInteger.getAndAccumulate(
            Util.faker().random().nextInt(-5, 5),
            (a, b) -> a + b
        ));
  }
}
