package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponentAttributeTypeAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.RelationTypeComponentAttributeTypeAssignmentNotFound;

/**
 * @author juliwolf
 */

@Service
public class RelationTypeComponentAttributeTypesAssignmentsDAO {
  protected final RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository;

  public RelationTypeComponentAttributeTypesAssignmentsDAO (
    RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository
  ) {
    this.relationTypeComponentAttributeTypeAssignmentRepository = relationTypeComponentAttributeTypeAssignmentRepository;
  }

  public RelationTypeComponentAttributeTypeAssignment findRelationTypeComponentAttributeTypeAssignmentById (
    UUID relationTypeComponentAttributeTypeAssignmentId
  ) throws RelationTypeComponentAttributeTypeAssignmentNotFound {
    Optional<RelationTypeComponentAttributeTypeAssignment> foundRelationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.findById(relationTypeComponentAttributeTypeAssignmentId);;

    if (foundRelationTypeComponentAttributeTypeAssignment.isEmpty()) {
      throw new RelationTypeComponentAttributeTypeAssignmentNotFound();
    }

    if (foundRelationTypeComponentAttributeTypeAssignment.get().getIsDeleted()) {
      throw new RelationTypeComponentAttributeTypeAssignmentNotFound();
    }

    return foundRelationTypeComponentAttributeTypeAssignment.get();
  }

  public Boolean isAssignmentsExistsByRelationTypeComponentAndAttributeType (UUID relationTypeComponentId, UUID attributeTypeId) {
    return relationTypeComponentAttributeTypeAssignmentRepository.isAssignmentsExistsByRelationTypeComponentAndAttributeType(relationTypeComponentId, attributeTypeId);
  }

  public void deleteAllByAttributeTypeId (UUID attributeTypeId, User user) {
    List<RelationTypeComponentAttributeTypeAssignment> assignments = relationTypeComponentAttributeTypeAssignmentRepository.findAllByAttributeTypeId(attributeTypeId);
    assignments.forEach(relationTypeComponentAttributeTypeAssignment -> {
      relationTypeComponentAttributeTypeAssignment.setIsDeleted(true);
      relationTypeComponentAttributeTypeAssignment.setDeletedBy(user);
      relationTypeComponentAttributeTypeAssignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));
    });

    relationTypeComponentAttributeTypeAssignmentRepository.saveAll(assignments);
  }

  public void deleteAllByRelationTypeComponentIds (List<UUID> relationTypeComponentIds, User user) {
    List<RelationTypeComponentAttributeTypeAssignment> assignments = relationTypeComponentAttributeTypeAssignmentRepository.findAllByRelationTypeComponentIds(relationTypeComponentIds);
    assignments.forEach(relationTypeComponentAttributeTypeAssignment -> {
      relationTypeComponentAttributeTypeAssignment.setIsDeleted(true);
      relationTypeComponentAttributeTypeAssignment.setDeletedBy(user);
      relationTypeComponentAttributeTypeAssignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));
    });

    relationTypeComponentAttributeTypeAssignmentRepository.saveAll(assignments);
  }
}
