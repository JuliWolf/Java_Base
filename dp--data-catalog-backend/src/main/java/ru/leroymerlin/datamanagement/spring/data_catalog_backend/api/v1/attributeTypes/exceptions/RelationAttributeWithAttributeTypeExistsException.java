package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions;

/**
 * @author JuliWolf
 */
public class RelationAttributeWithAttributeTypeExistsException extends RuntimeException {
  public RelationAttributeWithAttributeTypeExistsException () {
    super("Relation attribute with this attribute type still exists.");
  }

}
