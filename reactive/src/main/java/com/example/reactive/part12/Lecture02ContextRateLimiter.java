package com.example.reactive.part12;

import com.example.reactive.part12.helper.BookService;
import com.example.reactive.part12.helper.UserService;
import com.example.reactive.utils.Util;
import reactor.util.context.Context;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
public class Lecture02ContextRateLimiter {
  public static void main(String[] args) {
    BookService.getBook()
        .repeat(2)
        .contextWrite(UserService.userCategoryContext())
        .contextWrite(Context.of("user", "mike"))
        .subscribe(Util.subscriber());
  }
}
