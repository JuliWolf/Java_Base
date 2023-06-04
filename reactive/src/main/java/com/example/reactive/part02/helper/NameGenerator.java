package com.example.reactive.part02.helper;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class NameGenerator {
//  public static List<String> getNames (int count) {
//    List<String> list = new ArrayList<>(count);
//    for (int i = 0; i < count; i++) {
//      list.add(getName());
//    }
//
//    return list;
//  }

  public static Flux<String> getNames (int count) {
    return Flux.range(0, count)
        .map(i -> getName());
  }

  private static String getName () {
    Util.sleepSeconds(1);
    return Util.faker().name().fullName();
  }
}
