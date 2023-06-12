package com.example.reactive.part08;

import com.example.reactive.part08.helper.AmericanAirlines;
import com.example.reactive.part08.helper.EmirateFlights;
import com.example.reactive.part08.helper.QatarFlights;
import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 12.06.2023
 */
public class Lecture03Merge {
  public static void main(String[] args) {
    Flux<String> mergeFlux = Flux.merge(
        QatarFlights.getFlights(),
        EmirateFlights.getFlights(),
        AmericanAirlines.getFlights()
    );

    mergeFlux.subscribe(Util.subscriber());

    Util.sleepSeconds(10);

  }
}
