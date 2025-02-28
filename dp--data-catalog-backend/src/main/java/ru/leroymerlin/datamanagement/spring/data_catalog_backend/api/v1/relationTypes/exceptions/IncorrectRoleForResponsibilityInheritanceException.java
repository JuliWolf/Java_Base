package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions;

/**
 * @author JuliWolf
 */
public class IncorrectRoleForResponsibilityInheritanceException extends RuntimeException {
  public IncorrectRoleForResponsibilityInheritanceException () {
    super("Incorrect roles for responsibility inheritance.");
  }
}
