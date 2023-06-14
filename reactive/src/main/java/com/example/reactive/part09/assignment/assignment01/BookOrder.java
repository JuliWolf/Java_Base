package com.example.reactive.part09.assignment.assignment01;

import com.example.reactive.utils.Util;
import com.github.javafaker.Book;
import lombok.Data;
import lombok.ToString;

/**
 * @author JuliWolf
 * @date 13.06.2023
 */
@Data
@ToString
public class BookOrder {
  private String title;
  private String author;
  private String category;
  private double price;

  public BookOrder() {
    Book book = Util.faker().book();
    this.title = book.title();
    this.author = book.author();
    this.category = book.genre();
    this.price = Double.parseDouble(Util.faker().commerce().price());
  }
}
