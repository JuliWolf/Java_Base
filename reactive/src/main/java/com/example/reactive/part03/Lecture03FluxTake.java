package com.example.reactive.part03;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 05.06.2023
 */
public class Lecture03FluxTake {
  public static void main(String[] args) {
    // map
    // filter
    Flux.range(1, 10)
        .log()
        .take(3)
        .log()
        .subscribe(Util.subscriber());
  }
}
