package com.example.reactive.part06;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class Lecture08FluxInterval {
  public static void main(String[] args) {
    Flux.interval(Duration.ofSeconds(1))
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }
}
