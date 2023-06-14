package com.example.reactive.part09.assignment.assignment02;

import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 14.06.2023
 */
public class OrderService {
  public static Flux<PurchaseOrder> orderStream () {
    return Flux.interval(Duration.ofMillis(100))
        .map(i -> new PurchaseOrder());
  }
}
