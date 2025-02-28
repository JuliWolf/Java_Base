package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.RelationTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeAttributeTypeAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.RelationTypeAttributeTypeAssignmentNotFound;

/**
 * @author juliwolf
 */

@Service
public class RelationTypeAttributeTypesAssignmentsDAO {
  protected final RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository;

  public RelationTypeAttributeTypesAssignmentsDAO (
    RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository
  ) {
    this.relationTypeAttributeTypeAssignmentRepository = relationTypeAttributeTypeAssignmentRepository;
  }

  public RelationTypeAttributeTypeAssignment findRelationTypeAttributeTypeAssignmentById (
    UUID relationTypeAttributeTypeAssignmentId,
    boolean isJoinFetch
  ) throws RelationTypeAttributeTypeAssignmentNotFound {
    Optional<RelationTypeAttributeTypeAssignment> foundRelationTypeAttributeTypeAssignment;

    if (isJoinFetch) {
      foundRelationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.findByIdWithJoinedTables(relationTypeAttributeTypeAssignmentId);
    } else {
      foundRelationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.findById(relationTypeAttributeTypeAssignmentId);
    }

    if (foundRelationTypeAttributeTypeAssignment.isEmpty()) {
      throw new RelationTypeAttributeTypeAssignmentNotFound();
    }

    if (foundRelationTypeAttributeTypeAssignment.get().getIsDeleted()) {
      throw new RelationTypeAttributeTypeAssignmentNotFound();
    }

    return foundRelationTypeAttributeTypeAssignment.get();
  }

  public Boolean isAssignmentsExistsByRelationTypeAndAttributeType (UUID relationTypeId, UUID attributeTypeId) {
    return relationTypeAttributeTypeAssignmentRepository.isAssignmentsExistsByRelationTypeAndAttributeType(relationTypeId, attributeTypeId);
  }

  public void deleteAllByAttributeTypeId (UUID attributeTypeId, User user) {
    relationTypeAttributeTypeAssignmentRepository.deleteByParams(attributeTypeId, null, user.getUserId());
  }

  public void deleteAllByRelationTypeId (UUID relationTypeId, User user) {
    relationTypeAttributeTypeAssignmentRepository.deleteByParams(null, relationTypeId, user.getUserId());
  }
}
