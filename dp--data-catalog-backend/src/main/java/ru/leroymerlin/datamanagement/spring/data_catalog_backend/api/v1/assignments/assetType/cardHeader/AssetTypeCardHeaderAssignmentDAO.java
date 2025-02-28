package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.models.AssetTypeCardHeaderAssignmentResponsible;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetTypeCardHeaderAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeCardHeaderAssignmentNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class AssetTypeCardHeaderAssignmentDAO {
  @Autowired
  protected AssetTypeCardHeaderAssignmentRepository assetTypeCardHeaderAssignmentRepository;

  public AssetTypeCardHeaderAssignment findAssetTypeCardHeaderAssignmentById (UUID assetTypeCardHeaderAssignmentId) throws AssetTypeCardHeaderAssignmentNotFoundException {
    Optional<AssetTypeCardHeaderAssignment> assetTypeCardHeaderAssignment = assetTypeCardHeaderAssignmentRepository.findById(assetTypeCardHeaderAssignmentId);

    if (assetTypeCardHeaderAssignment.isEmpty()) {
      throw new AssetTypeCardHeaderAssignmentNotFoundException();
    }

    if (assetTypeCardHeaderAssignment.get().getIsDeleted()) {
      throw new AssetTypeCardHeaderAssignmentNotFoundException();
    }

    return assetTypeCardHeaderAssignment.get();
  }

  public void deleteAssetTypeCardHeaderAssignmentByParams (UUID roleId, UUID assetTypeId, UUID attributeTypeId, User user) {
    assetTypeCardHeaderAssignmentRepository.deleteAssetTypeCardHeaderAssignmentByParams(roleId, assetTypeId, attributeTypeId, user.getUserId());
  }

  public List<AssetTypeCardHeaderAssignmentResponsible> findAllAssetCardHeaderResponsibleByAssetId (UUID assetId) {
    UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000005040");
    return assetTypeCardHeaderAssignmentRepository.findAllAssetCardHeaderResponsibleByAssetId(assetId, roleId);
  }
}
