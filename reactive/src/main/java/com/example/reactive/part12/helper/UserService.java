package com.example.reactive.part12.helper;

import reactor.util.context.Context;
import java.util.Map;
import java.util.function.Function;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class UserService {
  private static final Map<String, String> MAP = Map.of(
      "sam", "std",
      "mike", "prime"
  );

  public static Function<Context, Context> userCategoryContext () {
    return ctx -> {
      String user = ctx.get("user").toString();
      String category = MAP.get(user);
      return ctx.put("category", category);
    };
  }
}
