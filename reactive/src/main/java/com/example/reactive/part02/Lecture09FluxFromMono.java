package com.example.reactive.part02;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture09FluxFromMono {
  public static void main(String[] args) {
    Mono<String> mono = Mono.just("a");
    Flux<String> flux = Flux.from(mono);
    flux.subscribe(
        Util.onNext()
    );
  }

  private static void doSomething (Flux<String> flux) {

  }
}
