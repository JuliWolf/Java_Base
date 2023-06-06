package com.example.reactive.part03;

import com.example.reactive.part03.helper.NameProducer;
import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 06.06.2023
 */
public class Lecture08FluxPush {
  public static void main(String[] args) {
    NameProducer nameProducer = new NameProducer();

    Flux.push(nameProducer)
        .subscribe(Util.subscriber());

    Runnable runnable = nameProducer::produce;

    for (int i = 0; i < 10; i++) {
      new Thread(runnable).start();
    }
  }
}
