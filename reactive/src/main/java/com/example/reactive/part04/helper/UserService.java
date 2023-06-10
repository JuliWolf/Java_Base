package com.example.reactive.part04.helper;

import reactor.core.publisher.Flux;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class UserService {
  public static Flux<User> getUsers () {
    return Flux.range(1, 2)
        .map(i -> new User(i));
  }
}
