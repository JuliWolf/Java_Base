package com.example.reactive.part02;

import com.example.reactive.utils.Util;
import jdk.swing.interop.SwingInterOpUtils;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Lecture06Subscription {
  public static void main(String[] args) {
    AtomicReference<Subscription> atomicReference = new AtomicReference<>();

    Flux.range(1, 20)
        .log()
        .subscribeWith(new Subscriber<Integer>() {
          @Override
          public void onSubscribe(Subscription subscription) {
            System.out.println("Received Sub: " + subscription);
            atomicReference.set(subscription);
          }

          @Override
          public void onNext(Integer integer) {
            System.out.println("onNext: " + integer);
          }

          @Override
          public void onError(Throwable throwable) {
            System.out.println("onError: " + throwable.getMessage());
          }

          @Override
          public void onComplete() {
            System.out.println("onComplete");
          }
        });

    Util.sleepSeconds(3);
    atomicReference.get().request(3);
    Util.sleepSeconds(5);
    atomicReference.get().request(3);
    Util.sleepSeconds(5);
    System.out.println("Going to cancel");
    atomicReference.get().cancel();
    Util.sleepSeconds(3);
    atomicReference.get().request(4);
    Util.sleepSeconds(3);
  }
}
