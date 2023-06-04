package com.example.reactive.part02;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture10MonoFromFlux {
  public static void main(String[] args) {
    Flux.range(1, 10)
        .filter(i -> i > 3)
        .next()
        .subscribe(
            Util.onNext()
        );
  }
}
