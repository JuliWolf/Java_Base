package com.example.reactive.part11;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture05SinkMultiDirectAll {
  public static void main(String[] args) {
    System.setProperty("reactor.bufferSize.small", "16");

    Sinks.Many<Object> sink = Sinks.many()
        .multicast()
        .directBestEffort();

    Flux<Object> flux = sink.asFlux();

    flux.subscribe(Util.subscriber("Sam"));
    flux
        .delayElements(Duration.ofMillis(100))
        .subscribe(Util.subscriber("Mike"));

    for (int i = 0; i < 100; i++) {
      sink.tryEmitNext(i);
    }

    Util.sleepSeconds(10);
  }
}
