package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.RelationTypeAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypeAttributeTypesUsageCountParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypeAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypesAttributeTypesUsageCountResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;

/**
 * @author juliwolf
 */

public interface RelationTypeAttributeTypesAssignmentsService {
  PostRelationTypeAttributeTypesResponse createRelationTypeAttributeTypesAssignments (
    UUID uuid,
    PostRelationTypeAttributeTypesRequest assignmentsRequest,
    User user
  ) throws RelationTypeNotFoundException, AttributeTypeNotFoundException;

  GetRelationTypeAttributeTypesResponse getRelationTypeAttributeTypesAssignmentsByRelationTypeId (UUID uuid);

  void deleteRelationTypeAttributeTypeAssignment (UUID relationTypeAttributeTypeAssignmentId, User user) throws RelationTypeAttributeTypeAssignmentNotFound;

  GetRelationTypesAttributeTypesUsageCountResponse getRelationTypeAttributeTypesWithUsageCount (GetRelationTypeAttributeTypesUsageCountParams params) throws RelationTypeNotFoundException, AttributeTypeNotFoundException;
}
