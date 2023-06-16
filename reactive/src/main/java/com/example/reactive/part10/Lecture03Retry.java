package com.example.reactive.part10;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author JuliWolf
 * @date 16.06.2023
 */
public class Lecture03Retry {
  public static void main(String[] args) {
    getIntegers()
        .retry(2)
        .subscribe(Util.subscriber());
  }

  private static Flux<Integer> getIntegers () {
    return Flux.range(1, 3)
        .doOnSubscribe(s -> System.out.println("Subscribed"))
        .doOnComplete(() -> System.out.println("---Completed"))
        .map(i -> i / (Util.faker().random().nextInt(1, 5) > 3 ? 0 : 1))
        .doOnError(err -> System.out.println("---error"));
  }
}
