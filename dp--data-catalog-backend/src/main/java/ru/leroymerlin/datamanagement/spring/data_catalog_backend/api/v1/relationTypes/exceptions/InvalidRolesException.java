package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions;

/**
 * @author JuliWolf
 */
public class InvalidRolesException extends RuntimeException {
  public InvalidRolesException () {
    super("Invalid roles");
  }
}
