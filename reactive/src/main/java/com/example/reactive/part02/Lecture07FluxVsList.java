package com.example.reactive.part02;

import com.example.reactive.part02.helper.NameGenerator;
import com.example.reactive.utils.Util;

import java.util.List;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture07FluxVsList {
  public static void main(String[] args) {
//    List<String> names = NameGenerator.getNames(5);
//    // wait 5 seconds
//    System.out.println(names);

    // when item is ready publisher will give it
    NameGenerator.getNames(5)
        .subscribe(
            Util.onNext()
        );
  }
}
