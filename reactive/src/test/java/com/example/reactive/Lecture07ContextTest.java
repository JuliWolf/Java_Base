package com.example.reactive;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import reactor.util.context.Context;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture07ContextTest {
  @Test
  public void contextErrorTest () {
    StepVerifier.create(getWelcomeMessage())
        .verifyError(RuntimeException.class);
  }

  @Test
  public void contextTest () {
    StepVerifierOptions stepVerifierOptions = StepVerifierOptions.create().withInitialContext(Context.of("user", "sam"));

    StepVerifier.create(getWelcomeMessage(), stepVerifierOptions)
        .expectNext("Welcome sam")
        .verifyComplete();
  }

  private Mono<String> getWelcomeMessage () {
    return Mono.deferContextual(ctx -> {
      if (ctx.hasKey("user")) {
        return Mono.just("Welcome " + ctx.get("user"));
      } else {
        return Mono.error(new RuntimeException("unauthenticated"));
      }
    });
  }
}
