package com.example.reactive;

import com.example.reactive.part09.assignment.assignment01.BookOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture05VirtualTimeTest {
  @Test
  public void assertVirtualTimeTest () {
    StepVerifier.withVirtualTime(() -> timeConsumingFlux())
        .thenAwait(Duration.ofSeconds(30))
        .expectNext("1a", "2a", "3a", "4a")
        .verifyComplete();
  }

  @Test
  public void assertVirtualTimeNoEventTest () {
    StepVerifier.withVirtualTime(() -> timeConsumingFlux())
        .expectSubscription() // sub is an event
        .expectNoEvent(Duration.ofSeconds(4))
        .thenAwait(Duration.ofSeconds(20))
        .expectNext("1a", "2a", "3a", "4a")
        .verifyComplete();
  }

  private Flux<String> timeConsumingFlux () {
    return Flux.range(1, 4)
        .delayElements(Duration.ofSeconds(5))
        .map(i -> i + "a");
  }
}
