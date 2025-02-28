package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
@Getter
@Setter
public class AssetTypeAttributeTypeAssignmentWithAllowedValues {
  private UUID assetTypeId;

  private String assetTypeName;

  private UUID assetTypeAttributeTypeAssignmentId;

  private UUID attributeTypeId;

  private String attributeTypeName;

  private String attributeTypeDescription;

  private AttributeKindType attributeKindType;

  private String validationMask;

  private Object allowedValues;

  private Boolean isInherited;

  private UUID parentAssetTypeId;

  private String parentAssetTypeName;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  public List<String> getAllowedValues () {
    String stringAllowedValues = (String) allowedValues;
    return StringUtils.isNotEmpty(stringAllowedValues) ? Arrays.stream(stringAllowedValues.split(";")).sorted().toList() : new ArrayList<>();
  }
}
