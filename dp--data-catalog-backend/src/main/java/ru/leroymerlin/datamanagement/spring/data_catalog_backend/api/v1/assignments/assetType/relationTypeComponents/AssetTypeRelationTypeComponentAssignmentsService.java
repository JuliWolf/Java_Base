package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents.models.get.GetAssetTypeRelationTypeComponentAssignments;

/**
 * @author juliwolf
 */

public interface AssetTypeRelationTypeComponentAssignmentsService {
  GetAssetTypeRelationTypeComponentAssignments getAssetTypeRelationTypeComponentAssignments (
    UUID assetTypeId,
    HierarchyRole hierarchyRole,
    ResponsibilityInheritanceRole responsibilityInheritanceRole,
    String relationTypeComponentName,
    Integer pageNumber,
    Integer pageSize
  ) throws AssetTypeNotFoundException;
}
