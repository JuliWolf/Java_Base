package com.example.reactive.part01;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Mono;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture04MonoEmptyOnError {
  public static void main(String[] args) {
    // return name
    userRepository(1)
        .subscribe(
            Util.onNext(),
            Util.onError(),
            Util.onComplete()
        );

    // return only Completed because result if empty
    userRepository(2)
        .subscribe(
            Util.onNext(),
            Util.onError(),
            Util.onComplete()
        );

    // Return error: "Error: Not in the allowed range"
    userRepository(20)
        .subscribe(
            Util.onNext(),
            Util.onError(),
            Util.onComplete()
        );
  }

  private static Mono<String> userRepository (int userId) {
    // return data only if userId == 1
    if (userId == 1) {
      return Mono.just(Util.faker().name().firstName());
    } else if (userId == 2) {
      return Mono.empty();
    }

    return Mono.error(new RuntimeException("Not in the allowed range"));
  }
}
