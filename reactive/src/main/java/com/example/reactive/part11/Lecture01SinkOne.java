package com.example.reactive.part11;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * @author JuliWolf
 * @date 17.06.2023
 */
public class Lecture01SinkOne {
  public static void main(String[] args) {
    // Mono 1 value / empty / error
    Sinks.One<Object> sink = Sinks.one();

    Mono<Object> mono = sink.asMono();

    mono.subscribe(Util.subscriber("Sam"));
    mono.subscribe(Util.subscriber("Mike"));

    sink.tryEmitValue("hi");
//    sink.tryEmitEmpty();
//    sink.tryEmitError(new RuntimeException("sam"));

//    sink.emitValue("hi", (signalType, emitResult) -> {
//      System.out.println(signalType.name());
//      System.out.println(emitResult.name());
//      return false;
//    });
//
//    sink.emitValue("hello", (signalType, emitResult) -> {
//      System.out.println(signalType.name());
//      System.out.println(emitResult.name());
//      // Вернуть true если нужно повторить попытку
//      return false;
//    });
  }
}
