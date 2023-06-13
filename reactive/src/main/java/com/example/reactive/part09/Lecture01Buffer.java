package com.example.reactive.part09;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 13.06.2023
 */
public class Lecture01Buffer {
  public static void main(String[] args) {
    eventStream()
        .bufferTimeout(5, Duration.ofSeconds(2))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<String> eventStream () {
    return Flux.interval(Duration.ofMillis(800))
        .map(i -> "event" + i);
  }
}
