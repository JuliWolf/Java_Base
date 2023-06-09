package com.example.reactive.part04;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 09.06.2023
 */
public class Lecture08DefaultIfEmpty {
  public static void main(String[] args) {
    getOrderNumbers()
        .filter(i -> i > 10)
        .defaultIfEmpty(-100)
        .subscribe(Util.subscriber());
  }

  private static Flux<Integer> getOrderNumbers () {
    return Flux.range(1, 10);
  }
}
