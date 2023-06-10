package com.example.reactive.part05.assignment;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class RevenueService {
  private Map<String, Double> db = new HashMap<>();

  public RevenueService () {
    db.put("Kids", 0.0);
    db.put("Automotive", 0.0);
  }

  public Consumer<PurchaseOrder> subscribeOrderStream () {
    return p -> db.computeIfPresent(
        p.getCategory(),
        (key, value) -> value + p.getPrice()
    );
  }

  public Flux<String> revenueStream () {
    return Flux.interval(Duration.ofSeconds(2))
        .map(i -> db.toString());
  }
}
