package ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.models;

/**
 * @author juliwolf
 */

public interface AttributeTypeKind {
  void setValue(String value);

  void setIsInteger(Boolean isInteger);

  void setValueNumeric(Double valueNumeric);

  void setValueBoolean(Boolean valueBoolean);

  void setValueDatetime(java.sql.Timestamp valueDatetime);
}
