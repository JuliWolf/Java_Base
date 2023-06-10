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
public class User {
  private int userId;
  private String name;

  public User(int userId) {
    this.userId = userId;
    this.name = Util.faker().name().fullName();
  }
}
