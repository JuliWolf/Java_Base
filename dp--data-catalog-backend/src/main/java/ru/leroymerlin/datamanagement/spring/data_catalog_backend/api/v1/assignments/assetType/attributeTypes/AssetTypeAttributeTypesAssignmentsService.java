package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AttributeTypeIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.GetAssetTypeAttributeTypeAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.GetAssetTypeAttributeTypesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributeTypesAssignmentsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;

public interface AssetTypeAttributeTypesAssignmentsService {
  PostAssetTypeAttributesAssignmentsResponse createAssetTypeAttributeTypesAssignments (
    UUID assetTypeId,
    PostAssetTypeAttributeTypesAssignmentsRequest request,
    User user
  ) throws AssetTypeNotFoundException, AttributeTypeNotFoundException;

  @Deprecated
  GetAssetTypeAttributeTypesAssignmentsResponse getAssetTypeAttributeTypesAssignmentsByAssetTypeId (UUID assetTypeId);

  void deleteAssetTypeAttributeTypeAssignmentById (
    UUID assetTypeAttributeTypeAssignmentId,
    User user
  ) throws
    AttributeTypeIsUsedForAssetException,
    AssetTypeAttributeTypeAssignmentNotFoundException,
    AssetTypeAttributeTypeAssignmentIsInheritedException;

  GetAssetTypeAttributeTypeAssignmentsResponse getAssetTypeAttributeTypesAssignmentsByParams (
    String assetTypeId,
    String attributeTypeId,
    SortField sortField,
    SortOrder sortOrder,
    Integer pageNumber,
    Integer pageSize
  ) throws
    IllegalArgumentException,
    AssetTypeNotFoundException,
    AttributeTypeNotFoundException;
}
