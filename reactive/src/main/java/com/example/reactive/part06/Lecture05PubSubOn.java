package com.example.reactive.part06;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class Lecture05PubSubOn {
  public static void main(String[] args) {
    Flux<Object> flux = Flux.create(fluxSink -> {
          printThreadName("create");
          for (int i = 0; i < 4; i++) {
            fluxSink.next(i);
          }

          fluxSink.complete();
        })
        .doOnNext(i -> printThreadName("next " + i));

    flux
        .publishOn(Schedulers.parallel())
        .doOnNext(i -> printThreadName("next " + i))
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe(v -> printThreadName("sub "+ v));

    Util.sleepSeconds(5);

  }

  private static void printThreadName (String msg) {
    System.out.println(msg + "\t\t: Thread:" + Thread.currentThread().getName());
  }
}
