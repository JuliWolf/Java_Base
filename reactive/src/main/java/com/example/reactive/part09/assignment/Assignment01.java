package com.example.reactive.part09.assignment;

import com.example.reactive.utils.Util;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author JuliWolf
 * @date 13.06.2023
 */
public class Assignment01 {
  public static void main(String[] args) {
    Set<String> allowedCategories = Set.of(
        "Science fiction",
        "Fantasy",
        "Suspense/Thriller"
    );

    bookStream()
        .filter(book -> allowedCategories.contains(book.getCategory()))
        .buffer(Duration.ofSeconds(5))
        .map(list -> revenueCalculator(list))
        .subscribe(Util.subscriber());

    Util.sleepSeconds(60);
  }

  private static RevenueReport revenueCalculator (List<BookOrder> books) {
    Map<String, Double> map = books.stream()
        .collect(
            Collectors.groupingBy(
                BookOrder::getCategory,
                Collectors.summingDouble(BookOrder::getPrice)
            )
        );
    return new RevenueReport(map);
  }

  private static Flux<BookOrder> bookStream() {
    return Flux.interval(Duration.ofMillis(200))
        .map(i -> new BookOrder());
  }
}
