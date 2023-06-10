package com.example.reactive.part05.assignment;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Objects;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class OrderService {

  private Flux<PurchaseOrder> flux;

  public Flux<PurchaseOrder> orderStream () {
    if (Objects.isNull(flux)) {
      flux = getOrderStream();
    }

    return flux;
  }

  private Flux<PurchaseOrder> getOrderStream () {
    return Flux.interval(Duration.ofMillis(100))
        .map(i -> new PurchaseOrder())
        .publish()
        .refCount(2);
  }
}
