package com.example.reactive.part02;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture05FluxRange {
  public static void main(String[] args) {
    // Will create from 1 2 3 4 5 6 7 8 9 10 elements
//    Flux.range(1, 10)
//        .subscribe(
//            Util.onNext()
//        );
//
//    // 3 4 5 6 7 8 9 10 11 12
//    Flux.range(3, 10)
//        .subscribe(
//            Util.onNext()
//        );

    // will receive 10 names
    Flux.range(3, 10)
        .log()
        .map(i -> Util.faker().name().fullName())
        .log()
        .subscribe(
            Util.onNext()
        );
  }
}
