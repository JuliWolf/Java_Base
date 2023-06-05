package com.example.reactive.part03;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 05.06.2023
 */
public class Lecture05FluxGenerate {
  public static void main(String[] args) {
    // Можно передавать только 1 значение
    // Запускает бесконечный цикл
    // Для каждой итерации будет передаваться отдельный инстанс synchronousSink
    Flux.generate(synchronousSink -> {
      System.out.println("emitting");
      synchronousSink.next(Util.faker().country().name());
      synchronousSink.complete();
    })
        .take(2)
        .subscribe(Util.subscriber());
  }
}
