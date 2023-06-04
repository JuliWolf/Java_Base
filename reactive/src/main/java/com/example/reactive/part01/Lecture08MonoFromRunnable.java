package com.example.reactive.part01;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Mono;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture08MonoFromRunnable {
  public static void main(String[] args) {
    Mono.fromRunnable(timeConsumingProcess())
        .subscribe(
            Util.onNext(),
            Util.onError(),
            () -> {
              System.out.println("process is done. Sending emails...");
            }
        );
  }

  private static Runnable timeConsumingProcess () {
    return () -> {
      Util.sleepSeconds(3);
      System.out.println("Operation completed");
    };
  }
}
