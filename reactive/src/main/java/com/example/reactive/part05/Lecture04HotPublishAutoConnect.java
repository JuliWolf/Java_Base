package com.example.reactive.part05;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.stream.Stream;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class Lecture04HotPublishAutoConnect {
  public static void main(String[] args) {
    // share = publish().refCount(1)
    Flux<String> movieStream = Flux.fromStream(() -> getMovie())
        .delayElements(Duration.ofSeconds(1))
        // Активирует горячую подписку
        .publish()
        .autoConnect(0);

    Util.sleepSeconds(3);

    movieStream
        .subscribe(Util.subscriber("sam"));

    Util.sleepSeconds(10);

    System.out.println("Mike if about to join");

    movieStream
        .subscribe(Util.subscriber("mike"));

    Util.sleepSeconds(60);
  }

  // movie theatre
  private static Stream<String> getMovie () {
    System.out.println("got the movie streaming req");
    return Stream.of(
        "Scene 1",
        "Scene 2",
        "Scene 3",
        "Scene 4",
        "Scene 5",
        "Scene 6",
        "Scene 7"
    );
  }
}
