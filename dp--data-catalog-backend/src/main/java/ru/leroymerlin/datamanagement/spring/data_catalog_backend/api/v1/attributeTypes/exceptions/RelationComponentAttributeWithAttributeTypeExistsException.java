package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions;

/**
 * @author JuliWolf
 */
public class RelationComponentAttributeWithAttributeTypeExistsException extends RuntimeException {
  public RelationComponentAttributeWithAttributeTypeExistsException () {
    super("Relation component attribute with this attribute type still exists.");
  }

}
