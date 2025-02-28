package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AttributeWithConnectedValues {
  private UUID attributeId;

  private UUID attributeTypeId;

  private String attributeTypeName;

  private AttributeKindType attributeKind;

  private UUID assetId;

  private String assetDisplayName;

  private String assetFullName;

  private String value;

  private Boolean isInteger;

  private Double valueNumeric;

  private Boolean valueBoolean;

  private java.sql.Timestamp valueDatetime;

  private String languageName;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  private java.sql.Timestamp lastModifiedOn;

  private UUID lastModifiedBy;
}
