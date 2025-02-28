package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions;

/**
 * @author JuliWolf
 */
public class InvalidNumberOfComponentsException extends RuntimeException {
  public InvalidNumberOfComponentsException () {
    super("Invalid number of components");
  }
}
