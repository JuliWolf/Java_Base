package com.example.reactive.part03.assignment;

import com.example.reactive.utils.Util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author JuliWolf
 * @date 06.06.2023
 */
public class Assignment {
  public static void main(String[] args) {
    FileReader fileReader = new FileReader();
    Path path = Paths.get("reactive/src/main/resources/assignment.part03/file01.txt");
    fileReader.read(path)
        .take(5)
        .subscribe(Util.subscriber());
  }
}
