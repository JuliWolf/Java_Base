package com.example.reactive.part10;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author JuliWolf
 * @date 16.06.2023
 */
public class Lecture01Repeat {
  private static AtomicInteger atomicInteger = new AtomicInteger(1);

  public static void main(String[] args) {
    getIntegers()
        .repeat(2)
        .subscribe(Util.subscriber());
  }

  private static Flux<Integer> getIntegers () {
    return Flux.range(1, 3)
        .doOnSubscribe(s -> System.out.println("Subscribed"))
        .doOnComplete(() -> System.out.println("---Completed"))
        .map(i -> atomicInteger.getAndIncrement());
  }
}
