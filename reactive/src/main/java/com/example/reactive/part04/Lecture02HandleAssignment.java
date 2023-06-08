package com.example.reactive.part04;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.util.Locale;

/**
 * @author JuliWolf
 * @date 08.06.2023
 */
public class Lecture02HandleAssignment {
  public static void main(String[] args) {
    Flux.generate(synchronousSink -> synchronousSink.next(Util.faker().country().name()))
        .map(Object::toString)
        .handle(((s, synchronousSink) -> {
          synchronousSink.next(s);
          if (s.equalsIgnoreCase("canada")) {
            synchronousSink.complete();
          }
        }))
        .subscribe(Util.subscriber());
  }
}
