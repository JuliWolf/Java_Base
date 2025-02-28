package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions;

/**
 * @author JuliWolf
 */
public class IncorrectRoleInHierarchyException extends RuntimeException {
  public IncorrectRoleInHierarchyException () {
    super("Incorrect roles in hierarchy");
  }
}
