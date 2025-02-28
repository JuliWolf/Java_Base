package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models.AssetTypeIdAttributeTypeIdAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetTypeAttributeTypeAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentNotFoundException;

@Service
public class AssetTypeAttributeTypesAssignmentsDAO {
  protected final AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;

  public AssetTypeAttributeTypesAssignmentsDAO (
    AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository
  ) {
    this.assetTypeAttributeTypeAssignmentRepository = assetTypeAttributeTypeAssignmentRepository;
  }
  
  public AssetTypeAttributeTypeAssignment findAssetTypeAttributeTypeAssignmentById (
    UUID assetTypeAttributeTypeAssignmentId,
    boolean isJoinFetch
  ) throws AssetTypeAttributeTypeAssignmentNotFoundException {
    Optional<AssetTypeAttributeTypeAssignment> foundAssetTypeAttributeTypeAssignment;

      if (isJoinFetch) {
        foundAssetTypeAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.findByIdWithJoinedTables(assetTypeAttributeTypeAssignmentId);
      } else {
        foundAssetTypeAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.findById(assetTypeAttributeTypeAssignmentId);
      }


    if (foundAssetTypeAttributeTypeAssignment.isEmpty()) {
      throw new AssetTypeAttributeTypeAssignmentNotFoundException();
    }

    if (foundAssetTypeAttributeTypeAssignment.get().getIsDeleted()) {
      throw new AssetTypeAttributeTypeAssignmentNotFoundException();
    }

    return foundAssetTypeAttributeTypeAssignment.get();
  }

  public Boolean isAssignmentsExistingByAssetTypeIdAndAttributeTypeId (UUID assetTypeId, UUID attributeTypeId) {
    return assetTypeAttributeTypeAssignmentRepository.isAssetTypeAttributeTypeAssignmentExistsByAssetTypeIdAndAttributeTypeId(assetTypeId, attributeTypeId);
  }

  public AssetTypeAttributeTypeAssignment saveAssetTypeAttributeTypesAssignment (AssetTypeAttributeTypeAssignment assetTypeAttributeTypeAssignment) {
    return assetTypeAttributeTypeAssignmentRepository.save(assetTypeAttributeTypeAssignment);
  }

  public List<AssetTypeAttributeTypeAssignment> findAllByAssetTypeId (UUID assetTypeId) {
    return assetTypeAttributeTypeAssignmentRepository.findAllByAssetTypeId(assetTypeId);
  }

  public void deleteAllByParams (UUID assetTypeId, UUID attributeTypeId, User user) {
    assetTypeAttributeTypeAssignmentRepository.deleteByParams(assetTypeId, attributeTypeId, user.getUserId());
  }

  public List<AssetTypeIdAttributeTypeIdAssignment> findAllAssetTypeAttributeTypeAssignmentsByAttributeTypeIdsAndAssetIds (List<UUID> attributeTypeIds, List<UUID> assetTypeIds) {
    return assetTypeAttributeTypeAssignmentRepository.findAllAssetTypeAttributeTypeAssignmentsByAttributeTypeIdsAndAssetIds(attributeTypeIds, assetTypeIds);
  }
}
