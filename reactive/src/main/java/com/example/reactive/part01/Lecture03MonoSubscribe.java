package com.example.reactive.part01;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Mono;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture03MonoSubscribe {
  public static void main(String[] args) {
    // publisher
    Mono<Integer> mono = Mono.just("ball")
        .map(String::length)
        .map(l -> l / 1);

    // 1 start process
//    mono.subscribe();

    // 2 provide consumer
//    mono.subscribe(
//        item -> System.out.println(item), // ball
//        err -> System.out.println(err.getMessage()), // / by zero
//        () -> System.out.println("Completed") // Completed
//    );

    // 3 if we do not provide error handler
//    mono.subscribe(
//        item -> System.out.println(item) // throw error
//    );

    // 4 Use utils
    mono.subscribe(
        Util.onNext(),
        Util.onError(),
        Util.onComplete()
    );
  }
}
