package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post.PostAssetTypeCardHeaderAssignmentRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post.PostAssetTypeCardHeaderAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeCardHeaderAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

/**
 * @author juliwolf
 */

public interface AssetTypeCardHeaderAssignmentService {
  PostAssetTypeCardHeaderAssignmentResponse createAssetTypeCardHeaderAssignment (
    UUID assetTypeId,
    PostAssetTypeCardHeaderAssignmentRequest assetTypeRequest,
    User user
  )
    throws
    RoleNotFoundException,
    AssetTypeNotFoundException,
    AttributeTypeNotFoundException;

  void deleteAssetTypeCardHeaderAssignment (UUID assetTypeCardHeaderAssignmentId, User user) throws AssetTypeCardHeaderAssignmentNotFoundException;
}
