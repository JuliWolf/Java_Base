package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.RelationTypeComponentAssetTypeAssignmentAssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponentAssetTypeAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationTypeComponentAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentNotFound;

/**
 * @author juliwolf
 */

@Service
public class RelationTypeComponentAssetTypesAssignmentsDAO {
  protected final RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;

  public RelationTypeComponentAssetTypesAssignmentsDAO (
    RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository
  ) {
    this.relationTypeComponentAssetTypeAssignmentRepository = relationTypeComponentAssetTypeAssignmentRepository;
  }

  public RelationTypeComponentAssetTypeAssignment findRelationTypeComponentAssetTypeAssignmentById (
    UUID relationTypeComponentAssetTypeAssignmentId,
    boolean isJoinFetch
  ) throws RelationTypeComponentAssetTypeAssignmentNotFound {
    Optional<RelationTypeComponentAssetTypeAssignment> foundRelationTypeComponentAssetTypeAssignment;

    if (isJoinFetch) {
      foundRelationTypeComponentAssetTypeAssignment = relationTypeComponentAssetTypeAssignmentRepository.findByIdWithJoinedTables(relationTypeComponentAssetTypeAssignmentId);
    } else {
      foundRelationTypeComponentAssetTypeAssignment = relationTypeComponentAssetTypeAssignmentRepository.findById(relationTypeComponentAssetTypeAssignmentId);
    }

    if (foundRelationTypeComponentAssetTypeAssignment.isEmpty()) {
      throw new RelationTypeComponentAssetTypeAssignmentNotFound();
    }

    if (foundRelationTypeComponentAssetTypeAssignment.get().getIsDeleted()) {
      throw new RelationTypeComponentAssetTypeAssignmentNotFound();
    }

    return foundRelationTypeComponentAssetTypeAssignment.get();
  }

   public Set<RelationTypeComponentAsset> findAllByRelationTypeComponentAndAsset (List<UUID> assetIds, List<UUID> relationTypeComponentIds) {
    return relationTypeComponentAssetTypeAssignmentRepository.findAllByRelationTypeComponentAndAsset(assetIds, relationTypeComponentIds);
  }

  public List<RelationTypeComponentAssetTypeAssignmentAssetType> findAllByRelationTypeComponentIds (List<UUID> relationTypeComponentIds) {
    return relationTypeComponentAssetTypeAssignmentRepository.findAllByRelationTypeComponentIds(relationTypeComponentIds);
  }

  public void deleteAllByRelationTypeComponentIds (List<UUID> relationTypeComponentIds, User user) {
    relationTypeComponentAssetTypeAssignmentRepository.deleteAllByRelationTypeComponentIds(relationTypeComponentIds, user.getUserId());
  }

  public void deleteAllByAssetTypeIds (UUID assetTypeId, User user) {
    relationTypeComponentAssetTypeAssignmentRepository.deleteAllByAssetTypeIds(assetTypeId, user.getUserId());
  }
}
