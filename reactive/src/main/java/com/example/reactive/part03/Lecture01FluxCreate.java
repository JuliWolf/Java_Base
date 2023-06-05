package com.example.reactive.part03;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.util.Locale;

/**
 * @author JuliWolf
 * @date 05.06.2023
 */
public class Lecture01FluxCreate {
  public static void main(String[] args) {
    // Полностью контролируем те данные, которые будут возвращаться
    // Контролируем процесс, когда завершить процесс или выкинуть ошибку
    Flux.create(fluxSink -> {
          fluxSink.next(1);
          fluxSink.next(2);
          fluxSink.complete();
      }).subscribe(Util.onNext());


    Flux.create(fluxSink -> {
      String country;
      do {
        country = Util.faker().country().name();
        fluxSink.next(country);
      } while (!country.toLowerCase().equals("canada"));
      fluxSink.complete();

    }).subscribe(Util.onNext());
  }
}
