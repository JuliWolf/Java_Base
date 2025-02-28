package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypesAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes.RelationComponentAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.AttributeTypeIsUsedForRelationComponentAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.RelationTypeComponentAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.GetRelationTypeComponentAssetTypesUsageCountParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.GetRelationTypeComponentAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author juliwolf
 */

public class RelationTypeComponentAttributeTypesAssignmentsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private RelationTypeComponentAttributeTypesAssignmentsService relationTypeComponentAttributeTypesAssignmentsService;

  @Autowired
  private RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private RelationComponentAttributeRepository relationComponentAttributeRepository;
  @Autowired
  private RelationComponentRepository relationComponentRepository;

  RelationType relationType;
  RelationType secondRelationType;
  RelationTypeComponent relationTypeComponent;
  RelationTypeComponent seconRelationTypeComponent;
  AttributeType attributeType;
  AttributeType secondAttributeType;
  RelationComponent relationComponent;

  @BeforeAll
  public void createData () {
    relationType  = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, null, null, language, user));
    secondRelationType  = relationTypeRepository.save(new RelationType("second relation type name", "desc", 2, null, null, language, user));
    relationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("test name", "test desc", null, null, null, language, relationType, user));
    seconRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second test name", "test desc", null, null, null, language, relationType, user));

    attributeType = attributeTypeRepository.save(new AttributeType("attribute type", "desc", AttributeKindType.TEXT, null ,null, language, user));
    secondAttributeType = attributeTypeRepository.save(new AttributeType("second attribute type", "desc", AttributeKindType.TEXT, null ,null, language, user));

    relationComponent = relationComponentRepository.save(new RelationComponent(null, relationTypeComponent, null, null, null, user));
  }

  @AfterAll
  public void clearData () {
    relationComponentRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearAssignments () {
    relationComponentAttributeRepository.deleteAll();
    relationTypeComponentAttributeTypeAssignmentRepository.deleteAll();
  }

  @Test
  public void createRelationTypeComponentAttributeTypesAssignments_RelationTypeComponentNotFoundException_IntegrationTest () {
    ArrayList<PostRelationTypeComponentAttributeTypeRequest> list = new ArrayList<>();
    list.add(new PostRelationTypeComponentAttributeTypeRequest(UUID.randomUUID().toString()));
    PostRelationTypeComponentAttributeTypesRequest request = new PostRelationTypeComponentAttributeTypesRequest(list);

    assertThrows(RelationTypeComponentNotFoundException.class, () -> relationTypeComponentAttributeTypesAssignmentsService.createRelationTypeComponentAttributeTypesAssignments(UUID.randomUUID(), request, user));
  }

  @Test
  public void createRelationTypeComponentAttributeTypesAssignments_IllegalAttributeTypeId_IntegrationTest () {
    ArrayList<PostRelationTypeComponentAttributeTypeRequest> list = new ArrayList<>();
    list.add(new PostRelationTypeComponentAttributeTypeRequest("123"));
    PostRelationTypeComponentAttributeTypesRequest request = new PostRelationTypeComponentAttributeTypesRequest(list);

    assertThrows(IllegalArgumentException.class, () -> relationTypeComponentAttributeTypesAssignmentsService.createRelationTypeComponentAttributeTypesAssignments(relationTypeComponent.getRelationTypeComponentId(), request, user));
  }

  @Test
  public void createRelationTypeComponentAttributeTypesAssignments_AttributeTypeNotFoundException_IntegrationTest () {
    ArrayList<PostRelationTypeComponentAttributeTypeRequest> list = new ArrayList<>();
    list.add(new PostRelationTypeComponentAttributeTypeRequest(UUID.randomUUID().toString()));
    PostRelationTypeComponentAttributeTypesRequest request = new PostRelationTypeComponentAttributeTypesRequest(list);

    assertThrows(AttributeTypeNotFoundException.class, () -> relationTypeComponentAttributeTypesAssignmentsService.createRelationTypeComponentAttributeTypesAssignments(relationTypeComponent.getRelationTypeComponentId(), request, user));
  }

  @Test
  public void createRelationTypeComponentAttributeTypesAssignments_Success_IntegrationTest () {
    ArrayList<PostRelationTypeComponentAttributeTypeRequest> list = new ArrayList<>();
    list.add(new PostRelationTypeComponentAttributeTypeRequest(attributeType.getAttributeTypeId().toString()));
    PostRelationTypeComponentAttributeTypesRequest request = new PostRelationTypeComponentAttributeTypesRequest(list);

    PostRelationTypeComponentAttributeTypesResponse response = relationTypeComponentAttributeTypesAssignmentsService.createRelationTypeComponentAttributeTypesAssignments(relationTypeComponent.getRelationTypeComponentId(), request, user);

    assertAll(
      () -> assertEquals(1, response.getRelation_type_component_attribute_type_assignment().size()),
      () -> assertEquals(relationTypeComponent.getRelationTypeComponentId(), response.getRelation_type_component_attribute_type_assignment().get(0).getRelation_type_component_id()),
      () -> assertEquals(attributeType.getAttributeTypeId(), response.getRelation_type_component_attribute_type_assignment().get(0).getAttribute_type_id())
    );
  }

  @Test
  public void getRelationTypeComponentAttributeTypesAssignments_RelationTypeComponentNotFoundException_IntegrationTest () {
    assertThrows(RelationTypeComponentNotFoundException.class, () -> relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesAssignments(UUID.randomUUID()));
  }

  @Test
  public void getRelationTypeComponentAttributeTypesAssignments_Success_IntegrationTest () {
    RelationTypeComponentAttributeTypeAssignment relationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));

    GetRelationTypeComponentAttributeTypesResponse response = relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesAssignments(relationTypeComponent.getRelationTypeComponentId());

    assertAll(
      () -> assertEquals(1, response.getRelation_type_component_attribute_type_assignment().size()),
      () -> assertEquals(relationTypeComponentAttributeTypeAssignment.getRelationTypeComponentAttributeTypeAssignmentId(), response.getRelation_type_component_attribute_type_assignment().get(0).getRelation_type_component_attribute_type_id()),
      () -> assertEquals(relationTypeComponent.getRelationTypeComponentId(), response.getRelation_type_component_id()),
      () -> assertEquals(relationTypeComponent.getRelationTypeComponentName(), response.getRelation_type_component_name()),
      () -> assertEquals(attributeType.getAttributeTypeId(), response.getRelation_type_component_attribute_type_assignment().get(0).getAttribute_type_id())
    );
  }

  @Test
  public void getRelationTypeAttributeTypesWithUsageCount_Success_IntegrationTest () {
    RelationTypeComponentAttributeTypeAssignment firstAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));
    RelationTypeComponentAttributeTypeAssignment secondAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(seconRelationTypeComponent, secondAttributeType, user));
    RelationTypeComponentAttributeTypeAssignment thirdAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(seconRelationTypeComponent, attributeType, user));
    RelationTypeComponentAttributeTypeAssignment forthAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, secondAttributeType, user));

    relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    assertAll(
      () -> assertEquals(4, relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams()).getTotal(), "total without filters"),
      () -> assertEquals(2, relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(relationTypeComponent.getRelationTypeComponentId(), null, 50, 0, null, null)).getTotal(), "total by relation type component"),
      () -> assertEquals(2, relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(seconRelationTypeComponent.getRelationTypeComponentId(), null, 50, 0, null, null)).getTotal(), "total by second relation type component"),
      () -> assertEquals(2, relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(null, attributeType.getAttributeTypeId(), 50, 0, null, null)).getTotal(), "total by attribute type"),
      () -> assertEquals(2, relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(null, secondAttributeType.getAttributeTypeId(), 50, 0, null, null)).getTotal(), "total by second attribute type"),
      () -> assertEquals(1, relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(relationTypeComponent.getRelationTypeComponentId(), secondAttributeType.getAttributeTypeId(), 50, 0, null, null)).getTotal(), "total by relation type component and second attribute type"),
      () -> assertEquals(0, relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(relationTypeComponent.getRelationTypeComponentId(), secondAttributeType.getAttributeTypeId(), 50, 0, null, null)).getResults().get(0).getRelation_type_component_attribute_type_usage_count(), "usage count by relation type component and second attribute type"),
      () -> assertEquals(1, relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(relationTypeComponent.getRelationTypeComponentId(), attributeType.getAttributeTypeId(), 50, 0, null, null)).getResults().get(0).getRelation_type_component_attribute_type_usage_count(), "usage count by relation type component and attribute type"),
      () -> assertEquals(attributeType.getAttributeTypeName(), relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(null, null, 50, 0, SortField.ATTRIBUTE_TYPE_NAME, null)).getResults().get(0).getAttribute_type_name(), "attribute type name with sort field by asset type name ASC"),
      () -> assertEquals(secondAttributeType.getAttributeTypeName(), relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(null, null, 50, 0, SortField.ATTRIBUTE_TYPE_NAME, SortOrder.DESC)).getResults().get(0).getAttribute_type_name(), "attribute type name with sort field by asset type name DESC"),
      () -> assertEquals(seconRelationTypeComponent.getRelationTypeComponentName(), relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(null, null, 50, 0, SortField.RELATION_TYPE_COMPONENT_NAME, null)).getResults().get(0).getRelation_type_component_name(), "relation type component name with sort field by relation type component name ASC"),
      () -> assertEquals(relationTypeComponent.getRelationTypeComponentName(), relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(new GetRelationTypeComponentAssetTypesUsageCountParams(null, null, 50, 0, SortField.RELATION_TYPE_COMPONENT_NAME, SortOrder.DESC)).getResults().get(0).getRelation_type_component_name(), "relation type component name with sort field by relation type component name DESC")
    );
  }

  @Test
  public void deleteRelationTypeComponentAttributeTypeAssignment_RelationTypeComponentAttributeTypeAssignmentNotFound_IntegrationTest () {
    assertThrows(RelationTypeComponentAttributeTypeAssignmentNotFound.class, () -> relationTypeComponentAttributeTypesAssignmentsService.deleteRelationTypeComponentAttributeTypeAssignment(UUID.randomUUID(), user));
  }

  @Test
  public void deleteRelationTypeComponentAttributeTypeAssignment_AttributeTypeIsUsedForRelationComponentAttributeException_IntegrationTest () {
    RelationTypeComponentAttributeTypeAssignment relationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));

    relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    assertThrows(AttributeTypeIsUsedForRelationComponentAttributeException.class, () -> relationTypeComponentAttributeTypesAssignmentsService.deleteRelationTypeComponentAttributeTypeAssignment(relationTypeComponentAttributeTypeAssignment.getRelationTypeComponentAttributeTypeAssignmentId(), user));
  }

  @Test
  public void deleteRelationTypeComponentAttributeTypeAssignment_Success_IntegrationTest () {
    RelationTypeComponentAttributeTypeAssignment relationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));

    relationTypeComponentAttributeTypesAssignmentsService.deleteRelationTypeComponentAttributeTypeAssignment(relationTypeComponentAttributeTypeAssignment.getRelationTypeComponentAttributeTypeAssignmentId(), user);

    Optional<RelationTypeComponentAttributeTypeAssignment> deletedAssignment = relationTypeComponentAttributeTypeAssignmentRepository.findById(relationTypeComponentAttributeTypeAssignment.getRelationTypeComponentAttributeTypeAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedBy()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn())
    );
  }
}
