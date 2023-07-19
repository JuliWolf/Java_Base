package com.example.reactive.part08.helper;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class QatarFlights {
  public static Flux<String> getFlights () {
    return Flux.range(1, Util.faker().random().nextInt(1, 5))
        .delayElements(Duration.ofSeconds(1))
        .map(i -> "Qatar " + Util.faker().random().nextInt(100, 999))
        .filter(i -> Util.faker().random().nextBoolean());
  }
}