package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.StatusTypeIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;

public interface AssetTypeStatusesAssignmentsService {
  PostAssetTypeStatusesAssignmentsResponse createAssetTypeStatusesAssignments (
    UUID assetTypeId,
    PostAssetTypeStatusesAssignmentsRequest request,
    User user
  ) throws AssetTypeNotFoundException, StatusNotFoundException;

  GetAssetTypeStatusesAssignmentsResponse getAssetTypeStatusesAssignmentsByParams (UUID assetTypeId, AssignmentStatusType statusType);

  void deleteAssetTypeStatusAssignmentById (
    UUID assetTypeId,
    User user
  ) throws StatusTypeIsUsedForAssetException, AssetTypeStatusAssignmentNotFoundException;

  GetAssetTypeStatusesResponse getAssetTypeStatusesByParams (UUID statusId, AssignmentStatusType statusType, UUID assetTypeId, Integer pageNumber, Integer pageSize);
}
