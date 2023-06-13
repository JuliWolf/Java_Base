package com.example.reactive.part09.assignment;

import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author JuliWolf
 * @date 13.06.2023
 */
@ToString
public class RevenueReport {
  private LocalDateTime localDateTime = LocalDateTime.now();
  private Map<String, Double> revenue;

  public RevenueReport(Map<String, Double> revenue) {
    this.revenue = revenue;
  }
}
