package com.example.reactive.part02;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture03FluxFromArrayOrList {
  public static void main(String[] args) {
    List<String> strings = Arrays.asList("a", "b", "c");

    // works like just
    // Use when data is exists
    Flux.fromIterable(strings)
        .subscribe(Util.onNext());

    Integer[] arr = {2,5,7,8};
    Flux.fromArray(arr)
        .subscribe(Util.onNext());
  }
}
