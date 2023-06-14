package com.example.reactive.part09.assignment.assignment02;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author JuliWolf
 * @date 14.06.2023
 */
public class Assignment02 {
  public static void main(String[] args) {
    Map<String, Function<Flux<PurchaseOrder>, Flux<PurchaseOrder>>> map = Map.of(
        "Kids", OrderProcessor.kidsProcessing(),
        "Automotive", OrderProcessor.automotiveProcessing()
    );

    Set<String> set = map.keySet();

    OrderService.orderStream()
        .filter(p -> set.contains(p.getCategory()))
        .groupBy(PurchaseOrder::getCategory) // 2 keys
        .flatMap(groupedFlux -> map.get(groupedFlux.key()).apply(groupedFlux)) // flux
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }
}
