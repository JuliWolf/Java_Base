package com.example.reactive.part09;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 14.06.2023
 */
public class Lecture04GroupBy {
  public static void main(String[] args) {
    Flux.range(1, 30)
        .delayElements(Duration.ofSeconds(1))
        .groupBy(i -> i % 2) // key 0, 1
        .subscribe(groupedFlux -> process(groupedFlux, groupedFlux.key()));

    Util.sleepSeconds(60);
  }

  private static void process (Flux<Integer> flux, int key) {
    flux.subscribe( i -> System.out.println("Key: " + key + ", Item: " + i));
  }
}
