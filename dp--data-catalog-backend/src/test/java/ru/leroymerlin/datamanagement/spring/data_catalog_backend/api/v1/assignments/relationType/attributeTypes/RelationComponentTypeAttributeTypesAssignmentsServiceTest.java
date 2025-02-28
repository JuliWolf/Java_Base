package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.RelationTypeAttributeTypesAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.RelationTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.RelationAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.AttributeTypeIsUsedForRelationAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.RelationTypeAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypeAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypeAttributeTypesUsageCountParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

public class RelationComponentTypeAttributeTypesAssignmentsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private RelationTypeAttributeTypesAssignmentsService relationTypeAttributeTypesAssignmentsService;

  @Autowired
  private RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository;

  @Autowired
  private RelationAttributeRepository relationAttributeRepository;
  @Autowired
  private RelationRepository relationRepository;

  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;

  private Relation relation;
  private RelationType relationType;
  private AttributeType booleanAttributeType;
  private AttributeType decimalAttributeType;

  @BeforeAll
  public void prepareData () {
    relationType = relationTypeRepository.save(new RelationType("test name", "test desc", 2, false, false, language, user));
    booleanAttributeType = attributeTypeRepository.save(new AttributeType("attribute name", "attribute desc", AttributeKindType.BOOLEAN, null, null, language, user));
    decimalAttributeType = attributeTypeRepository.save(new AttributeType("decimal attribute", "attribute desc", AttributeKindType.DECIMAL, null, null, language, user));

    relation = relationRepository.save(new Relation(relationType, user));
  }

  @AfterAll
  public void clearData () {
    relationRepository.deleteAll();
    relationTypeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearAssignments () {
    relationAttributeRepository.deleteAll();
    relationTypeAttributeTypeAssignmentRepository.deleteAll();
  }

  @Test
  public void createRelationTypeAttributeTypesAssignments_RelationTypeNotFoundException_IntegrationTest () {
    assertThrows(RelationTypeNotFoundException.class, () -> relationTypeAttributeTypesAssignmentsService.createRelationTypeAttributeTypesAssignments(UUID.randomUUID(), new PostRelationTypeAttributeTypesRequest(), user));
  }

  @Test
  public void createRelationTypeAttributeTypesAssignments_InvalidAttributeTypeId_IntegrationTest () {
    PostRelationTypeAttributeTypeRequest postRelationTypeAttributeTypeRequest = new PostRelationTypeAttributeTypeRequest("123");
    List<PostRelationTypeAttributeTypeRequest> requestList = new ArrayList<>();
    requestList.add(postRelationTypeAttributeTypeRequest);

    assertThrows(IllegalArgumentException.class, () -> relationTypeAttributeTypesAssignmentsService.createRelationTypeAttributeTypesAssignments(relationType.getRelationTypeId(), new PostRelationTypeAttributeTypesRequest(requestList), user));
  }

  @Test
  public void createRelationTypeAttributeTypesAssignments_AttributeTypeNotFoundException_IntegrationTest () {
    PostRelationTypeAttributeTypeRequest postRelationTypeAttributeTypeRequest = new PostRelationTypeAttributeTypeRequest(UUID.randomUUID().toString());
    List<PostRelationTypeAttributeTypeRequest> requestList = new ArrayList<>();
    requestList.add(postRelationTypeAttributeTypeRequest);

    assertThrows(AttributeTypeNotFoundException.class, () -> relationTypeAttributeTypesAssignmentsService.createRelationTypeAttributeTypesAssignments(relationType.getRelationTypeId(), new PostRelationTypeAttributeTypesRequest(requestList), user));
  }

  @Test
  public void createRelationTypeAttributeTypesAssignments_AssignmentAlreadyExists_IntegrationTest () {
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, booleanAttributeType, user));
    PostRelationTypeAttributeTypeRequest booleanAttributeTypeRequest = new PostRelationTypeAttributeTypeRequest(booleanAttributeType.getAttributeTypeId().toString());
    List<PostRelationTypeAttributeTypeRequest> requestList = new ArrayList<>();
    requestList.add(booleanAttributeTypeRequest);

    assertThrows(DataIntegrityViolationException.class, () -> relationTypeAttributeTypesAssignmentsService.createRelationTypeAttributeTypesAssignments(relationType.getRelationTypeId(), new PostRelationTypeAttributeTypesRequest(requestList), user));
  }

  @Test
  public void createRelationTypeAttributeTypesAssignments_Success_IntegrationTest () {
    PostRelationTypeAttributeTypeRequest booleanAttributeTypeRequest = new PostRelationTypeAttributeTypeRequest(booleanAttributeType.getAttributeTypeId().toString());
    PostRelationTypeAttributeTypeRequest decimalAttributeTypeRequest = new PostRelationTypeAttributeTypeRequest(decimalAttributeType.getAttributeTypeId().toString());
    List<PostRelationTypeAttributeTypeRequest> requestList = new ArrayList<>();
    requestList.add(booleanAttributeTypeRequest);
    requestList.add(decimalAttributeTypeRequest);

    PostRelationTypeAttributeTypesResponse response = relationTypeAttributeTypesAssignmentsService.createRelationTypeAttributeTypesAssignments(relationType.getRelationTypeId(), new PostRelationTypeAttributeTypesRequest(requestList), user);

    assertAll(
      () -> assertEquals(2, response.getRelation_attribute_assignment().size()),
      () -> assertEquals(relationType.getRelationTypeId(), response.getRelation_attribute_assignment().get(0).getRelation_type_id())
    );
  }

  @Test
  public void getRelationTypeAttributeTypesAssignmentsByRelationTypeId_RelationTypeNotFoundException_IntegrationTest () {
    assertThrows(RelationTypeNotFoundException.class, () -> relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesAssignmentsByRelationTypeId(UUID.randomUUID()));
  }

  @Test
  public void getRelationTypeAttributeTypesAssignmentsByRelationTypeId_Success_IntegrationTest () {
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, booleanAttributeType, user));
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, decimalAttributeType, user));

    GetRelationTypeAttributeTypesResponse response = relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesAssignmentsByRelationTypeId(relationType.getRelationTypeId());

    assertAll(
      () -> assertEquals(2, response.getRelation_attribute_assignment().size()),
      () -> assertEquals(relationType.getRelationTypeName(), response.getRelation_type_name()),
      () -> assertEquals(booleanAttributeType.getAttributeTypeId(), response.getRelation_attribute_assignment().stream().filter(a -> a.getAttribute_type_id().equals(booleanAttributeType.getAttributeTypeId())).findFirst().get().getAttribute_type_id()),
      () -> assertEquals(decimalAttributeType.getAttributeTypeId(), response.getRelation_attribute_assignment().stream().filter(a -> a.getAttribute_type_id().equals(decimalAttributeType.getAttributeTypeId())).findFirst().get().getAttribute_type_id())
    );
  }

  @Test
  public void getRelationTypeAttributeTypesWithUsageCount_Success_IntegrationTest () {
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, booleanAttributeType, user));
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, decimalAttributeType, user));

    relationAttributeRepository.save(new RelationAttribute(booleanAttributeType, relation, language,user));

    assertAll(
      () -> assertEquals(2, relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(new GetRelationTypeAttributeTypesUsageCountParams()).getTotal(), "total without filters"),
      () -> assertEquals(2, relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(new GetRelationTypeAttributeTypesUsageCountParams(relationType.getRelationTypeId(), null, 50, 0, null, null)).getTotal(), "total by relation type id"),
      () -> assertEquals(1, relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(new GetRelationTypeAttributeTypesUsageCountParams(null, booleanAttributeType.getAttributeTypeId(), 50, 0, null, null)).getTotal(), "total by boolean attribute type"),
      () -> assertEquals(1, relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(new GetRelationTypeAttributeTypesUsageCountParams(null, booleanAttributeType.getAttributeTypeId(), 50, 0, null, null)).getResults().get(0).getRelation_type_attribute_type_usage_count(), "usage count by boolean attribute type"),
      () -> assertEquals(1, relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(new GetRelationTypeAttributeTypesUsageCountParams(null, decimalAttributeType.getAttributeTypeId(), 50, 0, null, null)).getTotal(), "total by decimal attribute type"),
      () -> assertEquals(0, relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(new GetRelationTypeAttributeTypesUsageCountParams(null, decimalAttributeType.getAttributeTypeId(), 50, 0, null, null)).getResults().get(0).getRelation_type_attribute_type_usage_count(), "usage count by decimal attribute type"),
      () -> assertEquals(booleanAttributeType.getAttributeTypeName(), relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(new GetRelationTypeAttributeTypesUsageCountParams(null, null, 50, 0, SortField.ATTRIBUTE_TYPE_NAME, null)).getResults().get(0).getAttribute_type_name(), "sorted by attribute type name ASC"),
      () -> assertEquals(decimalAttributeType.getAttributeTypeName(), relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(new GetRelationTypeAttributeTypesUsageCountParams(null, null, 50, 0, SortField.ATTRIBUTE_TYPE_NAME, SortOrder.DESC)).getResults().get(0).getAttribute_type_name(), "sorted by attribute type name DESC"),
      () -> assertEquals(relationType.getRelationTypeName(), relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(new GetRelationTypeAttributeTypesUsageCountParams(null, null, 50, 0, SortField.RELATION_TYPE_NAME, null)).getResults().get(0).getRelation_type_name(), "sorted by relation type name ASC")
    );
  }

  @Test
  public void deleteRelationTypeAttributeTypeAssignmentAssignmentNotFoundIntegrationTest () {
    assertThrows(RelationTypeAttributeTypeAssignmentNotFound.class, () -> relationTypeAttributeTypesAssignmentsService.deleteRelationTypeAttributeTypeAssignment(UUID.randomUUID(), user));
  }

  @Test
  public void deleteRelationTypeAttributeTypeAssignmentAttributeTypeIsUsedForRelationAttributeIntegrationTest () {
    RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, booleanAttributeType, user));

    relationAttributeRepository.save(new RelationAttribute(booleanAttributeType, relation, language,user));

    assertThrows(AttributeTypeIsUsedForRelationAttributeException.class, () -> relationTypeAttributeTypesAssignmentsService.deleteRelationTypeAttributeTypeAssignment(relationTypeAttributeTypeAssignment.getRelationTypeAttributeTypeAssignmentId(), user));
  }

  @Test
  public void deleteRelationTypeAttributeTypeAssignmentAssignmentAlreadyDeletedIntegrationTest () {
    RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = new RelationTypeAttributeTypeAssignment(relationType, booleanAttributeType, user);
    relationTypeAttributeTypeAssignment.setIsDeleted(true);
    relationTypeAttributeTypeAssignmentRepository.save(relationTypeAttributeTypeAssignment);

    assertThrows(RelationTypeAttributeTypeAssignmentNotFound.class, () -> relationTypeAttributeTypesAssignmentsService.deleteRelationTypeAttributeTypeAssignment(relationTypeAttributeTypeAssignment.getRelationTypeAttributeTypeAssignmentId(), user));
  }

  @Test
  public void deleteRelationTypeAttributeTypeAssignmentSuccessIntegrationTest () {
    RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, booleanAttributeType, user));

    relationTypeAttributeTypesAssignmentsService.deleteRelationTypeAttributeTypeAssignment(relationTypeAttributeTypeAssignment.getRelationTypeAttributeTypeAssignmentId(), user);

    Optional<RelationTypeAttributeTypeAssignment> deletedAssignment = relationTypeAttributeTypeAssignmentRepository.findById(relationTypeAttributeTypeAssignment.getRelationTypeAttributeTypeAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn()),
      () -> assertEquals(deletedAssignment.get().getDeletedBy().getUserId(), user.getUserId())
    );
  }
}
