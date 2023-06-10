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
public class Lecture06Parallel {
  public static void main(String[] args) {
    List<Integer> list = new ArrayList<>();

    Flux.range(1, 999)
        .parallel(2)
        .runOn(Schedulers.parallel())
//        .doOnNext(i -> printThreadName("next " + i))
        .subscribe(v -> list.add(v));
//        .subscribe(v -> printThreadName("sub "+ v));

    Util.sleepSeconds(5);
    System.out.println(list.size());

  }

  private static void printThreadName (String msg) {
    System.out.println(msg + "\t\t: Thread:" + Thread.currentThread().getName());
  }
}
