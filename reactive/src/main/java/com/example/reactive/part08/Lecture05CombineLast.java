package com.example.reactive.part08;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class Lecture05CombineLast {
  public static void main(String[] args) {
    Flux.combineLatest(
        getString(),
        getNumber(),
        (stringStream, numberStream) -> stringStream + numberStream
    ).subscribe(Util.subscriber());

    Util.sleepSeconds(10);
  }

  private static Flux<String> getString () {
    return Flux.just("A", "B", "C", "D")
        .delayElements(Duration.ofSeconds(1));
  }

  private static Flux<String> getNumber () {
    return Flux.just("1", "2", "3")
        .delayElements(Duration.ofSeconds(3));
  }
}
