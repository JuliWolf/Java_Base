package com.example.reactive.utils;

import com.github.javafaker.Faker;
import org.reactivestreams.Subscriber;

import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Util {

  private static Faker FAKER = Faker.instance();

  public static Consumer<Object> onNext () {
    return o -> System.out.println("Received: " + o);
  }

  public static Consumer<Throwable> onError () {
    return e -> System.out.println("Error: " + e.getMessage());
  }

  public static Runnable onComplete () {
    return () -> System.out.println("Completed");
  }

  public static Faker faker () {
    return FAKER;
  }

  public static void sleepSeconds (int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static Subscriber<Object> subscriber () {
    return new DefaultSubscriber();
  }

  public static Subscriber<Object> subscriber (String name) {
    return new DefaultSubscriber(name);
  }
}
