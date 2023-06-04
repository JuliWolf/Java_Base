package com.example.reactive.part01;

import reactor.core.publisher.Mono;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture02MonoJust {
  public static void main(String[] args) {
    // publisher
    Mono<Integer> mono = Mono.just(1);

    System.out.println(mono);  // MonoJust

    // subscribe
    mono.subscribe(i -> System.out.println("Received: " + i));
  }
}
