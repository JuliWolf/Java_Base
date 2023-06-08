package com.example.reactive.part04;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 08.06.2023
 */
public class Lecture04LimitRate {
  public static void main(String[] args) {
    Flux.range(1, 1000)
        .log()
        .limitRate(100, 99)
        .subscribe(Util.subscriber());
    // 100
    // 175
  }
}
