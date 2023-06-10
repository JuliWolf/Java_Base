package com.example.reactive.part05.assignment;


import com.example.reactive.utils.Util;

/**
 * @author JuliWolf
 * @date 10.06.2023
 */
public class Assignment {
  public static void main(String[] args) {
    OrderService orderService = new OrderService();
    RevenueService revenueService = new RevenueService();
    InventoryService inventoryService = new InventoryService();

    // revenue and inv - observe the order stream
    orderService.orderStream().subscribe(revenueService.subscribeOrderStream());
    orderService.orderStream().subscribe(inventoryService.subscribeOrderStream());

    inventoryService.inventoryStream().subscribe(Util.subscriber("inventory"));
    revenueService.revenueStream().subscribe(Util.subscriber("revenue"));

    Util.sleepSeconds(60);


  }
}
