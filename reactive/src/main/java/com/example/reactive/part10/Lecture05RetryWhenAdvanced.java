package com.example.reactive.part10;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 16.06.2023
 */
public class Lecture05RetryWhenAdvanced {
  public static void main(String[] args) {
    orderService(Util.faker().business().creditCardNumber())
        .doOnError(err -> System.out.println(err.getMessage()))
        .retryWhen(Retry.from(
            flux -> flux
              .doOnNext(retrySignal -> {
                System.out.println(retrySignal.totalRetries());
                System.out.println(retrySignal.failure());
              })
              .handle((retrySignal, synchronousSink) -> {
                if (retrySignal.failure().getMessage().equals("500")) {
                  synchronousSink.next(1);
                } else {
                  synchronousSink.error(retrySignal.failure());
                }
              })
              .delayElements(Duration.ofSeconds(1))
        ))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static Mono<String> orderService (String ccNumber) {
    return Mono.fromSupplier(() -> {
      processPayment(ccNumber);
      return Util.faker().idNumber().valid();
    });
  }

  // payment service
  private static void processPayment (String ccNumber) {
    int random = Util.faker().random().nextInt(1, 10);
    if (random < 8) {
      throw new RuntimeException("500");
    } else if (random < 10) {
      throw new RuntimeException("404");
    }
  }
}
