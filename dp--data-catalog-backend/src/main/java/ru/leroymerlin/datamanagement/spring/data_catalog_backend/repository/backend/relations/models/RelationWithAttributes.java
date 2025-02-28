package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models;

import java.util.UUID;
import io.micrometer.common.util.StringUtils;
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
@Setter
@Getter
public class RelationWithAttributes {
  private UUID relationId;

  private UUID relationTypeId;

  private String relationTypeName;

  private Boolean responsibilityInheritanceFlag;

  private Boolean hierarchyFlag;

  private UUID attributeTypeId;

  private String attributeTypeName;

  private AttributeKindType attributeKind;

  private String validationMask;

  private Object allowedValuesObject;

  private UUID relationAttributeId;

  private String value;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  public String[] getAllowedValues () {
    String stringAllowedValues = (String) allowedValuesObject;
    return StringUtils.isNotEmpty(stringAllowedValues) ? stringAllowedValues.split(";") : new String[]{};
  }
}
