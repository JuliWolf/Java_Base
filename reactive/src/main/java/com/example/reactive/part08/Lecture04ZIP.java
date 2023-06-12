package com.example.reactive.part08;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class Lecture04ZIP {
  public static void main(String[] args) {
    // return Tuple
    // Если больше 8, тьо нужно будет передать свою ByFunction для объединения данных
    Flux.zip(
        getBody(),
        getEngine(),
        getTires()
    )
      .subscribe(Util.subscriber());
  }

  private static Flux<String> getBody () {
    return Flux.range(1, 5)
        .map(i -> "body");
  }

  private static Flux<String> getEngine () {
    return Flux.range(1, 2)
        .map(i -> "engine");
  }

  private static Flux<String> getTires () {
    return Flux.range(1, 8)
        .map(i -> "tires");
  }
}
