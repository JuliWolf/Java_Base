package com.example.reactive.part04;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 08.06.2023
 */
public class Lecture03DoCallbacks {
  public static void main(String[] args) {
    Flux.create(fluxSink -> {
      System.out.println("inside create");
      for (int i = 0; i < 5; i++) {
        fluxSink.next(i);
      }

//      fluxSink.error(new RuntimeException("oops"));
      fluxSink.complete();
      System.out.println("--completed");
    })
        .doFirst(() -> System.out.println("doFirst 1")) // order 1
        .doOnSubscribe(s -> System.out.println("doOnSubscribe 1: " + s)) // order 2
        .doOnComplete(() -> System.out.println("doOnComplete")) // order 5
        .doOnNext(o -> System.out.println("doOnNext: " + o)) // order 4
        .doOnRequest(l -> System.out.println("doOnRequest: " + l)) // order 3
        .doOnError(err -> System.out.println("doOnError: " + err.getMessage()))
        .doOnTerminate(() -> System.out.println("doOnTerminate")) // order 6
        .doOnCancel(() -> System.out.println("doOnCancel"))
        .doFinally(signal -> System.out.println("doFinally 1: " + signal)) // order 7
        .doOnDiscard(Object.class, o -> System.out.println("doOnDiscard: " + o))
        .take(2)
        .doFinally(signal -> System.out.println("doFinally 2: " + signal)) // order 7
        .subscribe(Util.subscriber());

    // Success way
    // все события исполняются снизу вверх, начиная от subscribe и далше вверх
    // doFirst - выполняются сверху вниз (subscriber -> publisher)
    // doOnSubscribe - Выполняются сверху вниз (от publisher -> subscriber)
    // doOnRequest - subscriber запрашивает данные (subscriber -> publisher)
    // Запускается внутренняя часть fluxSink
    // doOnNext
    // doOnComplete - При завершении работы
    // doOnTerminate
    // выполняется код, который реализован в подписчике в методе onComplete
    // doFinally
    // Выполняется код после fluxSink.complete();

    // Error way
    // все события исполняются снизу вверх, начиная от subscribe и далше вверх
    // doFirst - выполняются сверху вниз (subscriber -> publisher)
    // doOnSubscribe - Выполняются сверху вниз (от publisher -> subscriber)
    // doOnRequest - subscriber запрашивает данные (subscriber -> publisher)
    // Запускается внутренняя часть fluxSink
    // doOnNext
    // doOnError - При получении ошибки
    // doOnTerminate
    // выполняется код, который реализован в подписчике в методе onError
    // doFinally
    // Выполняется код после fluxSink.error();

    // Cancel way
    // все события исполняются снизу вверх, начиная от subscribe и далше вверх
    // doFirst - выполняются сверху вниз (subscriber -> publisher)
    // doOnSubscribe - Выполняются сверху вниз (от publisher -> subscriber)
    // doOnRequest - subscriber запрашивает данные (subscriber -> publisher)
    // Запускается внутренняя часть fluxSink
    // doOnNext
    // doOnCancel - Когда желаемое количество элементов получено
    // doFinally
    // выполняется код, который реализован в подписчике в методе onComplete
    // doOnDiscard - выполняется для всех элементов, который не были получены
    // Выполняется код после fluxSink.complete();
  }
}
