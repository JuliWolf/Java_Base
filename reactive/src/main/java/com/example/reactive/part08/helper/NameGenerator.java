package com.example.reactive.part08.helper;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class NameGenerator {
  private List<String> list = new ArrayList<>();

  public Flux<String> generateNames () {
    return Flux.generate(stringSynchronousSink -> {
      System.out.println("generator fresh");
      Util.sleepSeconds(1);
      String name = Util.faker().name().firstName();
      list.add(name);
      stringSynchronousSink.next(name);
    })
        .cast(String.class)
        .startWith(getFromCache());
  }

  private Flux<String> getFromCache () {
    return Flux.fromIterable(list);
  }
}
