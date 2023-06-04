package com.example.reactive.part01;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Mono;

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
    getName()
        .subscribe(
            Util.onNext()
        );

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
