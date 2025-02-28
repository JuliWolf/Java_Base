package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.AssetTypeStatusAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetTypeStatusAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;

@Service
public class AssetTypeStatusesAssignmentsDAO {
  @Autowired
  protected AssetTypeStatusAssignmentRepository assetTypeStatusAssignmentRepository;

  public AssetTypeStatusAssignment findAssetTypeStatusAssignmentsById (UUID assetTypeStatusAssignmentId) throws AssetTypeStatusAssignmentNotFoundException {
    Optional<AssetTypeStatusAssignment> foundAssetTypeStatusAssignment = assetTypeStatusAssignmentRepository.findById(assetTypeStatusAssignmentId);

    if (foundAssetTypeStatusAssignment.isEmpty()) {
      throw new AssetTypeStatusAssignmentNotFoundException();
    }

    if (foundAssetTypeStatusAssignment.get().getIsDeleted()) {
      throw new AssetTypeStatusAssignmentNotFoundException();
    }

    return foundAssetTypeStatusAssignment.get();
  }

  public Boolean isAssignmentsExistsByStatusIdAndAssignmentStatusType (UUID statusId, AssignmentStatusType assignmentStatusType) {
    return assetTypeStatusAssignmentRepository.isExistsAssetTypeStatusAssignmentsByStatusIdAndAssignmentStatusType(
      statusId,
      assignmentStatusType.toString()
    );
  }

  public Set<UUID> getAssetTypeStatusAssignmentsByStatusIdAndAssignmentStatusType (List<UUID> statusIds, AssignmentStatusType assignmentStatusType) {
    return assetTypeStatusAssignmentRepository.getAssetTypeStatusAssignmentsByStatusIdAndAssignmentStatusType(
      statusIds,
      assignmentStatusType
    );
  }

  public AssetTypeStatusAssignment saveAssetTypeAttributeTypesAssignment (AssetTypeStatusAssignment assetTypeStatusAssignment) {
    return assetTypeStatusAssignmentRepository.save(assetTypeStatusAssignment);
  }

  public List<AssetTypeStatusAssignment> findAllByAssetTypeIdAndDeletedIsFalse (UUID assetTypeId) {
    return assetTypeStatusAssignmentRepository.findAllByAssetTypeIdWithJoinedTables(assetTypeId);
  }

  public void deleteAllByAssetTypeId (UUID assetTypeId, User user) {
    assetTypeStatusAssignmentRepository.deleteByAssetTypeId(assetTypeId, user.getUserId());
  }
}
