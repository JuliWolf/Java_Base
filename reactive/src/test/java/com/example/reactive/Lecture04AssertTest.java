package com.example.reactive;

import com.example.reactive.part09.assignment.assignment01.BookOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture04AssertTest {
  @Test
  public void assertNotNullTest () {
    Mono<BookOrder> mono = Mono.fromSupplier(() -> new BookOrder());

    StepVerifier.create(mono)
        .assertNext(b -> Assertions.assertNotNull(b.getAuthor()))
        .verifyComplete();
  }

  @Test
  public void assertDelayTest () {
    Mono<BookOrder> mono = Mono.fromSupplier(() -> new BookOrder())
            .delayElement(Duration.ofSeconds(3));

    StepVerifier.create(mono)
        .assertNext(b -> Assertions.assertNotNull(b.getAuthor()))
        .expectComplete()
        .verify(Duration.ofSeconds(2));
  }
}
