package com.example.reactive.part07;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class Lecture01Demo {
  public static void main(String[] args) {
    Flux.create(fluxSink -> {
      for (int i = 0; i < 501; i++) {
        fluxSink.next(i);
        System.out.println("Pushed: " + i);
      }

      fluxSink.complete();
    })
        .publishOn(Schedulers.boundedElastic())
        .doOnNext(i -> {
          Util.sleepMillis(10);
        })
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }
}
