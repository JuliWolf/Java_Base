package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.models;

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

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RelationTypeComponentAttributeTypeAssignmentWithConnectedValues {
  private UUID relationTypeComponentId;

  private String relationTypeComponentName;

  private UUID relationTypeComponentAttributeTypeId;

  private UUID attributeTypeId;

  private String attributeTypeName;

  private AttributeKindType attributeKind;

  private String validationMask;

  private Object allowedValuesObject;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  public String[] getAllowedValues () {
    String allowedValues = (String) allowedValuesObject;

    return StringUtils.isNotEmpty(allowedValues) ? allowedValues.split(";") : new String[] {};
  }

}
