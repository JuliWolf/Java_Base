package com.example.reactive.part04;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 09.06.2023
 */
public class Lecture09SwitchIfEmpty {
  public static void main(String[] args) {
    getOrderNumbers()
        .filter(i -> i > 10)
        .switchIfEmpty(fallback())
        .subscribe(Util.subscriber());
  }

  private static Flux<Integer> getOrderNumbers () {
    return Flux.range(1, 10);
  }

  private static Flux<Integer> fallback () {
    return Flux.range(20, 5);
  }
}
