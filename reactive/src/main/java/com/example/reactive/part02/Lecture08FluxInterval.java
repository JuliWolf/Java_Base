package com.example.reactive.part02;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture08FluxInterval {
  public static void main(String[] args) {
    // like range
    // will publish items periodically
    // in non-blocking async way
    Flux.interval(Duration.ofSeconds(1))
        .subscribe(Util.onNext());

    Util.sleepSeconds(5);

  }
}
