package com.example.reactive.part01;

import com.github.javafaker.Faker;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class FakerDemo {
  public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
      System.out.println(Faker.instance().name().fullName());
    }
  }
}
