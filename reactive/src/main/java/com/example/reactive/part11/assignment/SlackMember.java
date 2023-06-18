package com.example.reactive.part11.assignment;

import lombok.Data;

import java.util.function.Consumer;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
@Data
public class SlackMember {
  private String name;
  private Consumer<String> messageConsumer;

  public SlackMember(String name) {
    this.name = name;
  }

  public void receives (String message) {
    System.out.println(message);
  }

  public void says (String message) {
    this.messageConsumer.accept(message);
  }


}
