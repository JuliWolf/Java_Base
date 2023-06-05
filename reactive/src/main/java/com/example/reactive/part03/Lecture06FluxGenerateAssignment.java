package com.example.reactive.part03;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 05.06.2023
 */
public class Lecture06FluxGenerateAssignment {
  public static void main(String[] args) {
    Flux.generate(synchronousSink -> {
          String country = Util.faker().country().name();
          synchronousSink.next(country);

          if (country.equalsIgnoreCase("canada")) {
            synchronousSink.complete();
          }
        })
        .subscribe(Util.subscriber());
  }
}
