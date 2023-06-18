package com.example.reactive.part11.assignment;

import lombok.Data;

/**
 * @author JuliWolf
 * @date 18.06.2023
 */
@Data
public class SlackMessage {
  private static final String FORMAT = "[%s -> %s] : %s";

  private String sender;
  private String receiver;
  private String message;

  @Override
  public String toString() {
    return String.format(FORMAT, this.sender, this.receiver, this.message);
  }
}
