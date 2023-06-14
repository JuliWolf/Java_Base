package com.example.reactive.part09.assignment.assignment02;

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


  public PurchaseOrder() {
    this.item = Util.faker().commerce().productName();
    this.price = Double.parseDouble(Util.faker().commerce().price());
    this.category = Util.faker().commerce().department();
  }
}
