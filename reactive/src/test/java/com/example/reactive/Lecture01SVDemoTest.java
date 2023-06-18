package com.example.reactive;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture01SVDemoTest {
  @Test
  public void justTestWithExpectNextSingle () {
    Flux<Integer> just = Flux.just(1, 2, 3);

    StepVerifier.create(just)
        .expectNext(1)
        .expectNext(2)
        .expectNext(3)
        .expectNext(4)
        .verifyComplete();
  }

  @Test
  public void justTestWithExpectNextMultiple () {
    Flux<Integer> just = Flux.just(1, 2, 3);

    StepVerifier.create(just)
        .expectNext(1, 2, 3)
        .verifyComplete();
  }
}
