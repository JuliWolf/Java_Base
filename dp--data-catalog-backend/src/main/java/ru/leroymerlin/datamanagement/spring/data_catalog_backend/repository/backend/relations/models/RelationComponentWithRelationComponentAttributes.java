package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models;

import java.util.UUID;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RelationComponentWithRelationComponentAttributes {
  private UUID relationComponentId;

  private UUID assetId;

  private String assetDisplayName;

  private UUID attributeTypeId;

  private String attributeTypeName;

  private AttributeKindType attributeKind;

  private String validationMask;

  private Object allowedValuesObject;

  private UUID relationComponentAttributeId;

  private String value;

  private HierarchyRole hierarchyRole;

  private ResponsibilityInheritanceRole responsibilityInheritanceRole;

  private UUID relationTypeComponentId;

  private String relationTypeComponentName;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  public String[] getAllowedValues () {
    String stringAllowedValues = (String) allowedValuesObject;
    return StringUtils.isNotEmpty(stringAllowedValues) ? stringAllowedValues.split(";") : new String[]{};
  }
}
