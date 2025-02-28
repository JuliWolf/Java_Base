package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models;

/**
 * @author juliwolf
 */

@FunctionalInterface
public interface TransactionalProcess {
  void execute();
}
