package com.example.reactive.part05.assignment;

import com.example.reactive.utils.Util;
import lombok.Data;
import lombok.ToString;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
@Data
@ToString
public class PurchaseOrder {
  private String item;
  private Double price;
  private String category;
  private int quantity;

  public PurchaseOrder() {
    this.item = Util.faker().commerce().productName();
    this.price = Double.parseDouble(Util.faker().commerce().price());
    this.category = Util.faker().commerce().department();
    this.quantity = Util.faker().random().nextInt(1, 10);
  }
}
