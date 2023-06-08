package com.example.reactive.part04;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 08.06.2023
 */
public class Lecture01Handle {
  public static void main(String[] args) {
    // handle = filter +map
    Flux.range(1, 20)
        .handle(((integer, synchronousSink) -> {
          if (integer == 7) {
            synchronousSink.complete();
          }

          if (integer % 2 == 0) {
            synchronousSink.next(integer); // filter
          } else {
            synchronousSink.next(integer + "a"); // map
          }
        }))
        .subscribe(Util.subscriber());
  }
}
