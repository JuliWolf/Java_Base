package com.example.reactive.part01.assignment;

import com.example.reactive.utils.Util;

import java.io.File;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Assignment {
  public static void main(String[] args) {
    FileService.read("file03.txt")
        .subscribe(
            Util.onNext(),
            Util.onError(),
            Util.onComplete()
        );

//    FileService.write("file03.txt", "This is file3")
//        .subscribe(
//            Util.onNext(),
//            Util.onError(),
//            Util.onComplete()
//        );

        FileService.delete("file03.txt")
        .subscribe(
            Util.onNext(),
            Util.onError(),
            Util.onComplete()
        );

  }
}
