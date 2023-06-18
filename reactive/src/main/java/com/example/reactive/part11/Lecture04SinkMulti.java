package com.example.reactive.part11;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture04SinkMulti {
  public static void main(String[] args) {
    Sinks.Many<Object> sink = Sinks.many()
        .multicast()
        .onBackpressureBuffer();

    Flux<Object> flux = sink.asFlux();

    sink.tryEmitNext("hi");
    sink.tryEmitNext("how are you");

    flux.subscribe(Util.subscriber("Sam"));
    flux.subscribe(Util.subscriber("Mike"));

    sink.tryEmitNext("?");
    flux.subscribe(Util.subscriber("Jake"));
    sink.tryEmitNext("new msg");
  }
}
