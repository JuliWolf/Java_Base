package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models;

import java.sql.Timestamp;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * @author juliwolf
 */


public interface AssetTypeAttributeTypeAssignmentResponse {
  String getAssetTypeAttributeTypeAssignmentIdText();

  default UUID getAssetTypeAttributeTypeAssignmentId () {
    if (StringUtils.isEmpty(getAssetTypeAttributeTypeAssignmentIdText())) return null;

    return UUID.fromString(getAssetTypeAttributeTypeAssignmentIdText());
  };

  String getAssetTypeIdText();

  default UUID getAssetTypeId() {
    if (StringUtils.isEmpty(getAssetTypeIdText())) return null;

    return UUID.fromString(getAssetTypeIdText());
  };

  String getAssetTypeName();

  String getAttributeTypeIdText();

  default UUID getAttributeTypeId() {
    if (StringUtils.isEmpty(getAttributeTypeIdText())) return null;

    return UUID.fromString(getAttributeTypeIdText());
  };

  String getAttributeTypeName();

  Long getCount();

  Boolean getIsInherited();

  String getParentAssetTypeIdText();

  default UUID getParentAssetTypeId() {
    if (StringUtils.isEmpty(getParentAssetTypeIdText())) return null;

    return UUID.fromString(getParentAssetTypeIdText());
  };

  String getParentAssetTypeName();

  Timestamp getCreatedOn();

  String getCreatedByText();

  default UUID getCreatedBy() {
    if (StringUtils.isEmpty(getCreatedByText())) return null;

    return UUID.fromString(getCreatedByText());
  };
}
