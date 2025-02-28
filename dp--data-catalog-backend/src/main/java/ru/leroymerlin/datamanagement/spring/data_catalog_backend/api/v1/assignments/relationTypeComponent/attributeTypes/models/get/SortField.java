package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get;

import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public enum SortField {
  RELATION_TYPE_COMPONENT_NAME("rtc.relation_type_component_name"),

  ATTRIBUTE_TYPE_NAME("at.attribute_type_name"),

  RELATION_TYPE_COMPONENT_ATTRIBUTE_TYPE_USAGE_COUNT("count");

  private String value;

  SortField (String value) {
    this.value = value;
  }
}
