package com.example.reactive.part08;

import com.example.reactive.part08.helper.NameGenerator;
import com.example.reactive.utils.Util;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class Lecture01StartWith {
  public static void main(String[] args) {
    NameGenerator generator = new NameGenerator();
    generator.generateNames()
        .take(2)
        .subscribe(Util.subscriber("sam"));

    generator.generateNames()
        .take(2)
        .subscribe(Util.subscriber("mike"));

    generator.generateNames()
        .take(3)
        .subscribe(Util.subscriber("Jake"));

    generator.generateNames()
        .filter(n -> n.startsWith("A"))
        .take(1)
        .subscribe(Util.subscriber("Jake"));
  }
}
