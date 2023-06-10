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
public class InventoryService {
  private Map<String, Integer> db = new HashMap<>();

  public InventoryService () {
    db.put("Kids", 100);
    db.put("Automotive", 100);
  }

  public Consumer<PurchaseOrder> subscribeOrderStream () {
    return p -> db.computeIfPresent(
        p.getCategory(),
        (key, value) -> value - p.getQuantity()
    );
  }

  public Flux<String> inventoryStream () {
    return Flux.interval(Duration.ofSeconds(2))
        .map(i -> db.toString());
  }
}
