package com.example.reactive.part12;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture01Context {
  public static void main(String[] args) {
    // Команды для добавления данных в контекст читаются снизу вверх
    // Поэтому значение "jake" будет последним
    getWelcomeMessage()
        .contextWrite(ctx -> ctx.put("user", ctx.get("user").toString().toUpperCase()))
        .contextWrite(Context.of("user", "Sam"))
        .subscribe(Util.subscriber());
  }

  private static Mono<String> getWelcomeMessage () {
    return Mono.deferContextual(ctx -> {
      if (ctx.hasKey("user")) {
        return Mono.just("Welcome " + ctx.get("user"));
      } else {
        return Mono.error(new RuntimeException("unauthenticated"));
      }
    });
  }
}
