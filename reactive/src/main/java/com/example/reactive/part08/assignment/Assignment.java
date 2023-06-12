package com.example.reactive.part08.assignment;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class Assignment {
  public static void main(String[] args) {
    final int carPrice = 10000;

    Flux.combineLatest(
        monthStream(),
        demandString(),
        (month, demand) -> (carPrice - (month * 100)) * demand
    ).subscribe(Util.subscriber());

    Util.sleepSeconds(20);
  }

  private static Flux<Long> monthStream () {
    return Flux.interval(Duration.ZERO, Duration.ofSeconds(1));
  }

  private static Flux<Double> demandString () {
    return Flux.interval(Duration.ofSeconds(3))
        .map(i -> Util.faker().random().nextInt(80, 120) / 100d)
        .startWith(1d);
  }
}
