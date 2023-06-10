package com.example.reactive.part04;

import com.example.reactive.part04.helper.OrderService;
import com.example.reactive.part04.helper.UserService;
import com.example.reactive.utils.Util;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class Lecture12FlatMap {
  public static void main(String[] args) {
    UserService.getUsers()
        .flatMap(user -> OrderService.getOrders(user.getUserId())) // Flux
        .subscribe(Util.subscriber());
  }
}
