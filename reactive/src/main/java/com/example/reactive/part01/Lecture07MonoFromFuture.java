package com.example.reactive.part01;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture07MonoFromFuture {
  public static void main(String[] args) {
    Mono.fromFuture(getName())
        .subscribe(
            Util.onNext()
        );

    Util.sleepSeconds(1);
  }

  private static CompletableFuture<String> getName () {
    return CompletableFuture.supplyAsync(() -> Util.faker().name().fullName());
  }
}
