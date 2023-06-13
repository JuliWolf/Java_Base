package com.example.reactive.part09;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 13.06.2023
 */
public class Lecture02OverlapAndDrop {
  public static void main(String[] args) {
    eventStream()
        .buffer(3, 2)
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Flux<String> eventStream () {
    return Flux.interval(Duration.ofMillis(300))
        .map(i -> "event" + i);
  }
}
