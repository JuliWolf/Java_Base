package com.example.reactive;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture03SVRangeTest {
  @Test
  public void rangeTest () {
    Flux<Integer> range = Flux.range(1, 50);

    StepVerifier.create(range)
        .expectNextCount(50)
        .verifyComplete();
  }

  @Test
  public void rangeCheckConditionTest () {
    Flux<Integer> range = Flux.range(1, 50);

    StepVerifier.create(range)
        .thenConsumeWhile(i -> i < 100)
        .verifyComplete();
  }
}
