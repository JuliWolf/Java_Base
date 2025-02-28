package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.models;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.AuthConstants;

/**
 * @author juliwolf
 */

public interface AssetReport {
  UUID getReportId();

  String getReportName();

  UUID getAssetStewardshipStatusId();

  UUID getReportDataCatalogId();

  String getReportDescription();

  String getReportLink();

  String getReportTemplate();

  String getReviewStatus();

  String getReportConfidentiality();

  String getReportTrainingMaterials();

  Long getUniqueUsersLastMonth();

  java.sql.Timestamp getReportLastModifiedDate();

  String getTechnicalOwner();

  String getReportReviewRequestId();

  default boolean getHasDataCatalogDescription () {
    if (getAssetStewardshipStatusId() == null) {
      return false;
    }

    return getAssetStewardshipStatusId().equals(UUID.fromString("55a86990-84bd-4c95-af43-eb015224ba74"));
  }

  default String getReportDataCatalogLink () {
    if (getReportDataCatalogId() == null) {
      return null;
    }

    return "https://" + AuthConstants.SITE_DOMAIN + "/asset/" + getReportDataCatalogId().toString();
  }
}
