package com.example.reactive.part01;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture06SupplierRefactoring {
  public static void main(String[] args) {
    // Thread will not be blocked because there are no subscribers
    // pipeline was build
    getName(); // return pipeline
    getName();


    // execute pipeline code
    String s = getName()
        // make process async (will be canceled when main thread is closed)
        .subscribeOn(Schedulers.boundedElastic())
        // block main thread until the end of the process
        .block();
    System.out.println(s);

    getName();
  }

  private static Mono<String> getName () {
    System.out.println("entered getName method");
    return Mono.fromSupplier(() -> {
      System.out.println("Generating name..");
      Util.sleepSeconds(3);
      return Util.faker().name().fullName();
    }).map(String::toUpperCase);

  }
}
