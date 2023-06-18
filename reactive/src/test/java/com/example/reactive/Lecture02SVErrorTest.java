package com.example.reactive;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture02SVErrorTest {
  @Test
  public void testWithError () {
    Flux<Integer> just = Flux.just(1, 2, 3);
    Flux<Integer> error = Flux.error(new RuntimeException("oops"));
    Flux<Integer> concat = Flux.concat(just, error);

    StepVerifier.create(concat)
        .expectNext(1, 2, 3)
        .verifyError();
  }

  @Test
  public void testWithExactError () {
    Flux<Integer> just = Flux.just(1, 2, 3);
    Flux<Integer> error = Flux.error(new RuntimeException("oops"));
    Flux<Integer> concat = Flux.concat(just, error);

    StepVerifier.create(concat)
        .expectNext(1, 2, 3)
        .verifyError(RuntimeException.class);
  }

  @Test
  public void testWithExactMessageError () {
    Flux<Integer> just = Flux.just(1, 2, 3);
    Flux<Integer> error = Flux.error(new RuntimeException("oops"));
    Flux<Integer> concat = Flux.concat(just, error);

    StepVerifier.create(concat)
        .expectNext(1, 2, 3)
        .verifyErrorMessage("oops");
  }
}
