package com.example.reactive.part06;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class Lecture02SubscribeOnDemo {
  public static void main(String[] args) {
    Flux<Object> flux = Flux.create(fluxSink -> {
          printThreadName("create");
          fluxSink.next(1);
        })
        // Выполняться все будет в этом потоке
        .subscribeOn(Schedulers.newParallel("vins"))
        .doOnNext(i -> printThreadName("next " + i));

    Runnable runnable = () -> flux
        .doFirst(() -> printThreadName("first2"))
        .subscribeOn(Schedulers.boundedElastic())
        .doFirst(() -> printThreadName("first1"))
        .subscribe(v -> printThreadName("sub "+ v));

    for (int i = 0; i < 2; i++) {
      new Thread(runnable).start();
    }

    Util.sleepSeconds(5);

  }

  private static void printThreadName (String msg) {
    System.out.println(msg + "\t\t: Thread:" + Thread.currentThread().getName());
  }
}
