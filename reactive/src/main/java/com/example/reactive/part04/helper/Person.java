package com.example.reactive.part04.helper;

import com.example.reactive.utils.Util;
import lombok.Data;
import lombok.ToString;

/**
 * @author JuliWolf
 * @date 09.06.2023
 */
@Data
@ToString
public class Person {
  private String name;
  private int age;

  public Person() {
    this.name = Util.faker().name().firstName();
    this.age = Util.faker().random().nextInt(1, 30);
  }
}
