package com.example.reactive;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture06ScenarioNameTest {
  @Test
  public void scenarioNameTest () {
    Flux<String> flux = Flux.just("a", "b", "c");

    StepVerifierOptions scenarioName = StepVerifierOptions.create().scenarioName("alphabets-test");

    StepVerifier.create(flux, scenarioName)
        .expectNextCount(12)
        .verifyComplete();
  }

  @Test
  public void scenarioNameAsTest () {
    Flux<String> flux = Flux.just("a", "b1", "c");


    StepVerifier.create(flux)
        .expectNext("a")
        .as("a-test")
        .expectNext("b")
        .as("b-test")
        .expectNext("c")
        .as("c-test")
        .verifyComplete();
  }
}
