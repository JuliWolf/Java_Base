package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions;

/**
 * @author JuliWolf
 */
public class InvalidCronExpressionException extends RuntimeException {
  public InvalidCronExpressionException () {
    super("Invalid cron expression");
  }
}
