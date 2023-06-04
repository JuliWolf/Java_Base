package com.example.reactive.part01;

import java.util.stream.Stream;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture01Stream {
  public static void main(String[] args) {
    Stream<Integer> stream = Stream.of(1)
        .map(i -> {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          return i *2;
        });
    stream.forEach(System.out::println);
  }
}
