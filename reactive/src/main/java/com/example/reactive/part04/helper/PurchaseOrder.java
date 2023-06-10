package com.example.reactive.part04.helper;

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
  private String price;
  private int userId;

  public PurchaseOrder (int userId) {
    this.userId = userId;
    this.item = Util.faker().commerce().productName();
    this.price = Util.faker().commerce().price();
  }
}
