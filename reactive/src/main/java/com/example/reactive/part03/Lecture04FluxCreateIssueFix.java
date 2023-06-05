package com.example.reactive.part03;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 05.06.2023
 */
public class Lecture04FluxCreateIssueFix {
  public static void main(String[] args) {
    Flux.create(fluxSink -> {
      String country;
      do {
        country = Util.faker().country().name();
        System.out.println("emitting : " + country);
        fluxSink.next(country);
      } while (!country.equalsIgnoreCase("canada") && !fluxSink.isCancelled());
      fluxSink.complete();

    })
        .take(3)
        .subscribe(Util.onNext());
  }
}
