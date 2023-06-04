package com.example.reactive.part02;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture01FluxIntro {
  public static void main(String[] args) {
    // can have more than 1 item
//    Flux<Integer> flux = Flux.just(1, 2, 3,4);

    // empty flux
    Flux<Object> flux = Flux.empty();

    // complete will be return once
    flux.subscribe(
        Util.onNext(),
        Util.onError(),
        Util.onComplete()
    );
  }
}
