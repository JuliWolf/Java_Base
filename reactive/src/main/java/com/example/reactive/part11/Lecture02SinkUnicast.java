package com.example.reactive.part11;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * @author JuliWolf
 * @date 17.06.2023
 */
public class Lecture02SinkUnicast {
  public static void main(String[] args) {
    // Метод для отправки данных
    Sinks.Many<Object> sink = Sinks.many()
        .unicast()
        .onBackpressureBuffer();

    // Метод для получения данных подписчиками
    Flux<Object> flux = sink.asFlux();

    flux.subscribe(Util.subscriber("Sam"));


    sink.tryEmitNext("hi");
    sink.tryEmitNext("how are you");
    sink.tryEmitNext("?");
  }
}
