package com.example.reactive.part07;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class Lecture05BufferWithSize {
  public static void main(String[] args) {

    Flux.create(fluxSink -> {
      for (int i = 1; i < 201 && !fluxSink.isCancelled(); i++) {
        fluxSink.next(i);
        System.out.println("Pushed: " + i);
        Util.sleepMillis(1);
      }

      fluxSink.complete();
    })
      .onBackpressureBuffer(20, o -> System.out.println("Dropped: " + o))
      .publishOn(Schedulers.boundedElastic())
      .doOnNext(i -> {
        Util.sleepMillis(10);
      })
      .subscribe(Util.subscriber());

    Util.sleepSeconds(10);
  }
}
