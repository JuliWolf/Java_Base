package ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions;

/**
 * @author juliwolf
 */

public class InsufficientPrivilegesException extends RuntimeException {
  public InsufficientPrivilegesException () {
    super("User's privileges are insufficient to use this method");
  }
}
