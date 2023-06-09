package com.example.reactive.part04;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.util.concurrent.Queues;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 09.06.2023
 */
public class Lecture05Delay {
  public static void main(String[] args) {

    Flux.range(1, 100) // request 1
        .log()
        .delayElements(Duration.ofSeconds(1))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(50);
  }
}
