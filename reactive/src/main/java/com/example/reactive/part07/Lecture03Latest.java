package com.example.reactive.part07;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class Lecture03Latest {
  public static void main(String[] args) {
    System.setProperty("reactor.bufferSize.small", "16");

    Flux.create(fluxSink -> {
          for (int i = 1; i < 201; i++) {
            fluxSink.next(i);
            System.out.println("Pushed: " + i);
            Util.sleepMillis(1);
          }

          fluxSink.complete();
        })
        .onBackpressureLatest()
        .publishOn(Schedulers.boundedElastic())
        .doOnNext(i -> {
          Util.sleepMillis(10);
        })
        .subscribe(Util.subscriber());

    Util.sleepSeconds(10);
  }
}
