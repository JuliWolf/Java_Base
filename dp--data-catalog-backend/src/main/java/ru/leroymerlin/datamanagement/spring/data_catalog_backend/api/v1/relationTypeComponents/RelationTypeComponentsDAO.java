package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypeComponents;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponent;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.models.RelationTypeComponentWithRelationType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.RelationTypeComponentAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class RelationTypeComponentsDAO {
  @Autowired
  protected RelationTypeComponentRepository relationTypeComponentRepository;

  public RelationTypeComponent findRelationTypeComponentById (UUID relationTypeComponentId, boolean isJoinFetch) {
    Optional<RelationTypeComponent> foundRelationTypeComponent;

    if (isJoinFetch) {
      foundRelationTypeComponent = relationTypeComponentRepository.findByIdWithJoinedTables(relationTypeComponentId);
    } else {
      foundRelationTypeComponent = relationTypeComponentRepository.findById(relationTypeComponentId);
    }

    if (foundRelationTypeComponent.isEmpty()) {
      throw new RelationTypeComponentNotFoundException();
    }

    if (foundRelationTypeComponent.get().getIsDeleted()) {
      throw new RelationTypeComponentNotFoundException();
    }

    return foundRelationTypeComponent.get();
  }

  public RelationTypeComponentWithRelationType findRelationTypeComponentByRelationTypeComponentAssetTypeAssignmentId (UUID relationTypeComponentAssetTypeAssignmentId) throws RelationTypeComponentAssetTypeAssignmentNotFound {
    Optional<RelationTypeComponentWithRelationType> assignment = relationTypeComponentRepository.findRelationTypeComponentByRelationTypeComponentAssetTypeAssignmentId(relationTypeComponentAssetTypeAssignmentId);

    if (assignment.isEmpty()) {
      throw new RelationTypeComponentAssetTypeAssignmentNotFound();
    }

    return assignment.get();
  }

  public RelationTypeComponentWithRelationType findRelationTypeComponentByRelationTypeComponentAttributeTypeAssignmentId (UUID relationTypeComponentAttributeTypeAssignmentId) throws RelationTypeComponentAttributeTypeAssignmentNotFound {
    Optional<RelationTypeComponentWithRelationType> assignment = relationTypeComponentRepository.findRelationTypeComponentByRelationTypeComponentAttributeTypeAssignmentId(relationTypeComponentAttributeTypeAssignmentId);

    if (assignment.isEmpty()) {
      throw new RelationTypeComponentAttributeTypeAssignmentNotFound();
    }

    return assignment.get();
  }

  public List<RelationTypeComponent> findAllByRelationTypeComponentIds (List<UUID> relationTypeComponentIds) {
    return relationTypeComponentRepository.findAllByRelationTypeComponentIds(relationTypeComponentIds);
  }
}
