package com.example.reactive.part03;

import com.example.reactive.part03.helper.NameProducer;
import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 05.06.2023
 */
public class Lecture02FluxCreateRefactoring {

  public static void main(String[] args) {
    NameProducer nameProducer = new NameProducer();

    Flux.create(nameProducer)
        .subscribe(Util.subscriber());

    Runnable runnable = nameProducer::produce;

    for (int i = 0; i < 10; i++) {
      new Thread(runnable).start();
    }
  }
}
