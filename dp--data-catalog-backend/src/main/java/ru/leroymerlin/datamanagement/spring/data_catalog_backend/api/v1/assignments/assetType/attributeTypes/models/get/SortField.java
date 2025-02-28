package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get;

import lombok.Getter;

/**
 * @author juliwolf
 */

@Getter
public enum SortField {
  ASSET_TYPE_NAME("ast.asset_type_name"),

  ATTRIBUTE_TYPE_NAME("atbt.attribute_type_name"),

  ASSET_TYPE_ATTRIBUTE_TYPE_USAGE_COUNT("count");

  private String value;

  SortField (String value) {
    this.value = value;
  }
}
