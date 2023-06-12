package com.example.reactive.part08;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class Lecture02Concat {
  public static void main(String[] args) {
    Flux<String> flux1 = Flux.just("a", "b");
    Flux<String> flux2 = Flux.just("c", "d", "e");
    Flux<String> flux3 = Flux.just("f", "g", "h");
    Flux<String> fluxError = Flux.error(new RuntimeException("oops"));

//    Flux<String> fluxConcatWith = flux1.concatWith(flux2);
//    fluxConcatWith.subscribe(Util.subscriber());
//
//    Flux<String> fluxConcat = Flux.concat(flux1, flux2, flux3);
//    fluxConcat.subscribe(Util.subscriber());

    Flux<String> fluxConcatWithError = Flux.concatDelayError(flux2, flux3, fluxError);
    fluxConcatWithError.subscribe(Util.subscriber());
  }
}
