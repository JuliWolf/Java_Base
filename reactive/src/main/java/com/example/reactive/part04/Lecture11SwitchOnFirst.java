package com.example.reactive.part04;

import com.example.reactive.part04.helper.Person;
import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.util.function.Function;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class Lecture11SwitchOnFirst {
  public static void main(String[] args) {
    getPerson()
        .switchOnFirst(((signal, personFlux) -> {
          if (signal.isOnNext() && signal.get().getAge() > 10) {
            return personFlux;
          }

          return applyFilterMap().apply(personFlux);
        }))
        .subscribe(Util.subscriber());
  }

  public static Flux<Person> getPerson () {
    return Flux.range(1, 10)
        .map(i -> new Person());
  }

  public static Function<Flux<Person>, Flux<Person>> applyFilterMap () {
    return flux -> flux
        .filter(p -> p.getAge() > 10)
        .doOnNext(p -> p.setName(p.getName().toUpperCase()))
        .doOnDiscard(Person.class, p -> System.out.println("Not allowing: " + p));
  }
}
