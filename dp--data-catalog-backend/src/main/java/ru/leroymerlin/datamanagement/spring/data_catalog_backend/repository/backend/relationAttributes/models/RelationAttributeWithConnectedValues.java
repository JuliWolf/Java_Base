package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RelationAttributeWithConnectedValues {
  private UUID relationAttributeId;

  private UUID attributeTypeId;

  private String attributeTypeName;

  private AttributeKindType attributeKind;

  private UUID relationId;

  private String value;

  private Boolean isInteger;

  private Double valueNumeric;

  private Boolean valueBool;

  private java.sql.Timestamp valueDatetime;

  private String sourceLanguage;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  private java.sql.Timestamp lastModifiedOn;

  private UUID lastModifiedBy;
}
