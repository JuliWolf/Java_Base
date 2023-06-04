package com.example.reactive.part01;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture05FromSupplier {
  public static void main(String[] args) {
    // use just only when you have data already
//    Mono<String> mono = Mono.just(getName()); // Generating name..

    Mono<String> mono = Mono.fromSupplier(() -> getName()); // nothing happen
    mono.subscribe(
        Util.onNext() // return name
    );


    Supplier<String> stringSupplier = () -> getName();
    Callable<String> stringCallable = () -> getName();
    Mono.fromCallable(stringCallable)
        .subscribe(
            Util.onNext() // Received: Mrs. Fanny Buckridge
        );
  }

  private static String getName () {
    System.out.println("Generating name..");
    return Util.faker().name().fullName();
  }
}
