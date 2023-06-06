package com.example.reactive.part03;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author JuliWolf
 * @date 06.06.2023
 */
public class Lecture07FluxGenerateCounter {
  public static void main(String[] args) {
    // canada
    // max = 10
    Flux.generate(
        () -> 1,
        (counter, sink) -> {
          String country = Util.faker().country().name();
          sink.next(country);

          if (country.equalsIgnoreCase("canada") || counter >= 10) {
            sink.complete();
          }
          return counter + 1;
        }
    )
        .subscribe(Util.subscriber());
  }
}
