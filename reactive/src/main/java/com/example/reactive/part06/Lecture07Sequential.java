package com.example.reactive.part06;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class Lecture07Sequential {
  public static void main(String[] args) {
    Flux.range(1, 10)
        .parallel(2)
        .runOn(Schedulers.boundedElastic())
        .doOnNext(i -> printThreadName("next " + i))
        .sequential()
        .subscribe(v -> printThreadName("sub " + v));

    Util.sleepSeconds(5);

  }

  private static void printThreadName (String msg) {
    System.out.println(msg + "\t\t: Thread:" + Thread.currentThread().getName());
  }
}
