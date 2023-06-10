package com.example.reactive.part06;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class Lecture01ThreadDemo {
  public static void main(String[] args) {
    Flux<Object> flux = Flux.create(fluxSink -> {
          printThreadName("create");
          fluxSink.next(1);
        })
        .doOnNext(i -> printThreadName("next " + i));

    Runnable runnable = () -> flux.subscribe(v -> printThreadName("sub "+ v));
    for (int i = 0; i < 2; i++) {
      new Thread(runnable).start();
    }

    Util.sleepSeconds(5);

  }

  private static void printThreadName (String msg) {
    System.out.println(msg + "\t\t: Thread:" + Thread.currentThread().getName());
  }
}
