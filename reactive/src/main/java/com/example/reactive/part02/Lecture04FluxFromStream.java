package com.example.reactive.part02;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture04FluxFromStream {
  public static void main(String[] args) {

    List<Integer> list = List.of(1, 2, 3, 4, 5);
    Stream<Integer> stream = list.stream();

//    Flux<Integer> integerFlux = Flux.fromStream(stream);
//    integerFlux.subscribe(
//        Util.onNext(),
//        Util.onError(),
//        Util.onComplete()
//    );
//
//    // Will get error
//    // Error: stream has already been operated upon or closed
//    integerFlux.subscribe(
//        Util.onNext(),
//        Util.onError(),
//        Util.onComplete()
//    );


    // In order to connect multiple subscribers we need to use Stream suppliers
    Flux<Integer> supplierStream = Flux.fromStream(() -> list.stream());

    supplierStream.subscribe(
        Util.onNext(),
        Util.onError(),
        Util.onComplete()
    );

    supplierStream.subscribe(
        Util.onNext(),
        Util.onError(),
        Util.onComplete()
    );
  }
}
