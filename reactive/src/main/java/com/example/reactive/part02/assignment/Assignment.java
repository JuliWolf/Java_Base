package com.example.reactive.part02.assignment;

import com.example.reactive.utils.Util;
import lombok.SneakyThrows;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

/**
 * @author JuliWolf
 * @date 04.06.2023
 */
public class Assignment {
  @SneakyThrows
  public static void main(String[] args) {
    CountDownLatch latch = new CountDownLatch(1);

    StockPricePublisher.getPrice()
        .subscribeWith(new Subscriber<Integer>() {
          private Subscription subscription;

          @Override
          public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            subscription.request(Long.MAX_VALUE);
          }

          @Override
          public void onNext(Integer price) {
            System.out.println(LocalDateTime.now() + " : Price : " + price);

            if (price > 110 || price < 90) {
              this.subscription.cancel();
              latch.countDown();
            }

          }

          @Override
          public void onError(Throwable throwable) {
            latch.countDown();
          }

          @Override
          public void onComplete() {
            latch.countDown();
          }
        });

    latch.await();
  }
}
