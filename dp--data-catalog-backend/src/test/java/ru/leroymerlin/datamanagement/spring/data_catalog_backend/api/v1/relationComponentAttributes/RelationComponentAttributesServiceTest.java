package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.RelationComponentAttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes.RelationComponentAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributesHistory.RelationComponentAttributesHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.AttributeTypeNotAllowedForRelationComponentException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.RelationComponentAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.get.GetRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationComponentNotFoundException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

public class RelationComponentAttributesServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private RelationComponentAttributeRepository relationComponentAttributeRepository;
  @Autowired
  private RelationComponentAttributesService relationComponentAttributesService;

  @Autowired
  private RelationComponentAttributesHistoryRepository relationComponentAttributesHistoryRepository;

  @Autowired
  private RelationRepository relationRepository;
  @Autowired
  private RelationComponentRepository relationComponentRepository;
  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository;

  AttributeType attributeType;
  RelationComponent relationComponent;
  RelationType relationType;
  RelationTypeComponent relationTypeComponent;

  @BeforeEach
  public void prepareData () {
    attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, null, language, user));
    relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));
    relationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("relation type component name", "desc", null, null, null, language, relationType, user));
    Relation relation = relationRepository.save(new Relation(relationType, null));
    relationComponent = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponent, null, null, null, user));
  }

  @AfterEach
  public void deleteData () {
    relationComponentAttributesHistoryRepository.deleteAll();
    relationTypeComponentAttributeTypeAssignmentRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    relationComponentRepository.deleteAll();
    relationRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
  }

  @Test
  public void createRelationComponentAttributeAttributeTypeNotFoundIntegrationTest () {
    PostRelationComponentAttributeRequest request = new PostRelationComponentAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "value");

    assertThrows(AttributeTypeNotFoundException.class, () -> relationComponentAttributesService.createRelationComponentAttribute(request, user));
  }

  @Test
  public void createRelationComponentAttributeRelationComponentNotFoundIntegrationTest () {
    PostRelationComponentAttributeRequest request = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), UUID.randomUUID().toString(), "value");

    assertThrows(RelationComponentNotFoundException.class, () -> relationComponentAttributesService.createRelationComponentAttribute(request, user));
  }

  @Test
  public void createRelationComponentAttributeAttributeTypeNotAllowedForRelationIntegrationTest () {
    PostRelationComponentAttributeRequest request = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), relationComponent.getRelationComponentId().toString(), "value");

    assertThrows(AttributeTypeNotAllowedForRelationComponentException.class, () -> relationComponentAttributesService.createRelationComponentAttribute(request, user));
  }
  @Test
  public void createRelationComponentAttributeRelationAttributeAlreadyExistsIntegrationTest () {
    relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));
    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    PostRelationComponentAttributeRequest request = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), relationComponent.getRelationComponentId().toString(), "value");

    assertThrows(DataIntegrityViolationException.class, () -> relationComponentAttributesService.createRelationComponentAttribute(request, user));
  }

  @Test
  public void createRelationComponentAttributeSuccessIntegrationTest () {
    relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));

    PostRelationComponentAttributeRequest request = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), relationComponent.getRelationComponentId().toString(), "value");

    PostRelationComponentAttributeResponse response = relationComponentAttributesService.createRelationComponentAttribute(request, user);

    assertAll(
      () -> assertEquals("value", response.getValue()),
      () -> assertNull(response.getValue_datetime()),
      () -> assertNull(response.getInteger_flag()),
      () -> assertEquals(relationComponent.getRelationComponentId(), response.getRelation_component_id()),
      () -> assertEquals(attributeType.getAttributeTypeId(), response.getAttribute_type_id())
    );
  }

  @Test
  public void createRelationComponentAttributeRelationComponentAttributeHistoryIntegrationTest () {
    relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));

    PostRelationComponentAttributeRequest postRequest = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), relationComponent.getRelationComponentId().toString(), "value");

    PostRelationComponentAttributeResponse postResponse = relationComponentAttributesService.createRelationComponentAttribute(postRequest, user);

    List<RelationComponentAttributeHistory> relationComponentAttributeHistories = relationComponentAttributesHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(1, relationComponentAttributeHistories.size(), "relation component attribute history size"),
      () -> assertEquals("3000-01-01 00:00:00.0", relationComponentAttributeHistories.get(0).getValidTo().toString(), "relation component attribute history valid to"),
      () -> assertEquals(postResponse.getCreated_on(), relationComponentAttributeHistories.get(0).getValidFrom(), "relation component attribute history valid from")
    );
  }

  @Test
  public void updateRelationComponentAttributeRelationComponentAttributeNotFoundIntegrationTest () {
    PatchRelationComponentAttributeRequest request = new PatchRelationComponentAttributeRequest("value");

    assertThrows(RelationComponentAttributeNotFoundException.class, () -> relationComponentAttributesService.updateRelationComponentAttribute(UUID.randomUUID(), request, user));
  }

  @Test
  public void updateRelationComponentAttributeSuccessIntegrationTest () {
    RelationComponentAttribute relationComponentAttribute = new RelationComponentAttribute(attributeType, relationComponent, language, user);
    relationComponentAttribute.setValue("123");
    relationComponentAttribute = relationComponentAttributeRepository.save(relationComponentAttribute);

    PatchRelationComponentAttributeRequest request = new PatchRelationComponentAttributeRequest("value");;

    PatchRelationComponentAttributeResponse response = relationComponentAttributesService.updateRelationComponentAttribute(relationComponentAttribute.getRelationComponentAttributeId(), request, user);

    assertAll(
      () -> assertNotNull(response.getLast_modified_by()),
      () -> assertNotNull(response.getLast_modified_on()),
      () -> assertEquals("value", response.getValue()),
      () -> assertNull(response.getValue_datetime()),
      () -> assertNull(response.getValue_bool())
    );
  }

  @Test
  public void updateRelationComponentAttributeRelationComponentAttributeHistoryIntegrationTest () {
    relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));
    PostRelationComponentAttributeRequest postRequest = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), relationComponent.getRelationComponentId().toString(), "123");
    PostRelationComponentAttributeResponse postResponse = relationComponentAttributesService.createRelationComponentAttribute(postRequest, user);

    PatchRelationComponentAttributeRequest patchRequest = new PatchRelationComponentAttributeRequest("value");;
    PatchRelationComponentAttributeResponse patchResponse = relationComponentAttributesService.updateRelationComponentAttribute(postResponse.getRelation_component_attribute_id(), patchRequest, user);

    List<RelationComponentAttributeHistory> relationComponentAttributeHistories = relationComponentAttributesHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, relationComponentAttributeHistories.size(), "relation component attribute history size"),
      () -> assertEquals(postResponse.getCreated_on(), relationComponentAttributeHistories.stream().filter(attribute -> attribute.getValue().equals(postResponse.getValue())).findFirst().get().getValidFrom(), "post relation component attribute history valid from"),
      () -> assertEquals(patchResponse.getLast_modified_on(), relationComponentAttributeHistories.stream().filter(attribute -> attribute.getValue().equals(patchResponse.getValue())).findFirst().get().getValidFrom(), "patch relation component attribute history valid from"),
      () -> assertEquals(patchResponse.getLast_modified_on(), relationComponentAttributeHistories.stream().filter(attribute -> attribute.getValue().equals(postResponse.getValue())).findFirst().get().getValidTo(), "post relation component attribute history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", relationComponentAttributeHistories.stream().filter(attribute -> attribute.getValue().equals(patchResponse.getValue())).findFirst().get().getValidTo().toString(), "patched relation component attribute history valid to")
    );
  }

  @Test
  public void getRelationComponentAttributeByIdRelationAttributeNotFoundIntegrationTest () {
    assertThrows(RelationComponentAttributeNotFoundException.class, () -> relationComponentAttributesService.getRelationComponentAttributeById(UUID.randomUUID()));
  }

  @Test
  public void getRelationComponentAttributeByIdSuccessIntegrationTest () {
    RelationComponentAttribute relationComponentAttribute = new RelationComponentAttribute(attributeType, relationComponent, language, user);
    relationComponentAttribute.setValue("123");
    relationComponentAttribute = relationComponentAttributeRepository.save(relationComponentAttribute);

    GetRelationComponentAttributeResponse response = relationComponentAttributesService.getRelationComponentAttributeById(relationComponentAttribute.getRelationComponentAttributeId());

    assertAll(
      () -> assertNull(response.getLast_modified_by()),
      () -> assertNull(response.getLast_modified_on()),
      () -> assertEquals("123", response.getValue()),
      () -> assertNull(response.getValue_datetime()),
      () -> assertNull(response.getValue_bool()),
      () -> assertEquals(response.getRelation_component_id(), relationComponent.getRelationComponentId()),
      () -> assertEquals(response.getAttribute_type_id(), attributeType.getAttributeTypeId())
    );
  }

  @Test
  public void getRelationAttributesByParamsIntegrationTest () {
    AttributeType firstAttributeType = attributeTypeRepository.save(new AttributeType("boolean attribute type", "attribute with boolean type", AttributeKindType.BOOLEAN, null, null, language, user));
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("first relation type name", "desc", 2, false, false, language, user));
    Relation firstRelation = relationRepository.save(new Relation(firstRelationType, null));
    RelationTypeComponent firstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first relation type component name", "desc", null, null, null, language, relationType, user));
    RelationComponent firstRelationComponent = relationComponentRepository.save(new RelationComponent(firstRelation, firstRelationTypeComponent, null, null, null, user));
    RelationComponentAttribute firstRelationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(firstAttributeType, firstRelationComponent, language, user));

    AttributeType secondAttributeType = attributeTypeRepository.save(new AttributeType("text attribute type", "attribute with text type", AttributeKindType.TEXT, null, null, language, user));
    RelationType secondRelationType = relationTypeRepository.save(new RelationType("second relation type name", "desc", 2, false, false, language, user));
    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, null));
    RelationTypeComponent secondRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second relation type component name", "desc", null, null, null, language, relationType, user));
    RelationComponent secondRelationComponent = relationComponentRepository.save(new RelationComponent(secondRelation, secondRelationTypeComponent, null, null, null, user));
    RelationComponentAttribute secondRelationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(secondAttributeType, secondRelationComponent, language, user));

    AttributeType thirdAttributeType = attributeTypeRepository.save(new AttributeType("date attribute type", "attribute with date type", AttributeKindType.DATE, null, null, language, user));
    RelationType thirdRelationType = relationTypeRepository.save(new RelationType("third relation type name", "desc", 2, false, false, language, user));
    Relation thirdRelation = relationRepository.save(new Relation(thirdRelationType, null));
    RelationTypeComponent thirdRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("third relation type component name", "desc", null, null, null, language, relationType, user));
    RelationComponent thirdRelationComponent = relationComponentRepository.save(new RelationComponent(thirdRelation, thirdRelationTypeComponent, null, null, null, user));
    RelationComponentAttribute thirdRelationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(thirdAttributeType, thirdRelationComponent, language, user));

    AttributeType forthAttributeType = attributeTypeRepository.save(new AttributeType("date time attribute type", "attribute with date time type", AttributeKindType.DATE_TIME, null, null, language, user));
    RelationType forthRelationType = relationTypeRepository.save(new RelationType("forth relation type name", "desc", 2, false, false, language, user));
    Relation forthRelation = relationRepository.save(new Relation(forthRelationType, null));
    RelationTypeComponent forthRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("forth relation type component name", "desc", null, null, null, language, relationType, user));
    RelationComponent forthRelationComponent = relationComponentRepository.save(new RelationComponent(forthRelation, forthRelationTypeComponent, null, null, null, user));
    RelationComponentAttribute forthRelationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(forthAttributeType, forthRelationComponent, language, user));

    RelationComponentAttribute fifthRelationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(thirdAttributeType, firstRelationComponent, language, user));

    assertAll(
      () -> assertEquals(5, relationComponentAttributesService.getRelationComponentAttributesByParams(null, null, 0, 50).getResults().size()),
      () -> assertEquals(2, relationComponentAttributesService.getRelationComponentAttributesByParams(null, List.of(firstRelationComponent.getRelationComponentId()), 0, 50).getResults().size()),
      () -> assertEquals(1, relationComponentAttributesService.getRelationComponentAttributesByParams(null, List.of(secondRelationComponent.getRelationComponentId()), 0, 50).getResults().size()),
      () -> assertEquals(3, relationComponentAttributesService.getRelationComponentAttributesByParams(List.of(firstAttributeType.getAttributeTypeId(), thirdAttributeType.getAttributeTypeId()), null, 0, 50).getResults().size()),
      () -> assertEquals(1, relationComponentAttributesService.getRelationComponentAttributesByParams(List.of(firstAttributeType.getAttributeTypeId(), forthAttributeType.getAttributeTypeId()), List.of(firstRelationComponent.getRelationComponentId()), 0, 50).getResults().size()),
      () -> assertEquals(0, relationComponentAttributesService.getRelationComponentAttributesByParams(List.of(forthAttributeType.getAttributeTypeId(), secondAttributeType.getAttributeTypeId()), List.of(firstRelationComponent.getRelationComponentId()), 0, 50).getResults().size())
    );
  }

  @Test
  public void deleteRelationComponentAttributeByIdRelationComponentAttributeNotFoundIntegrationTest () {
    assertThrows(RelationComponentAttributeNotFoundException.class, () -> relationComponentAttributesService.deleteRelationComponentAttributeById(UUID.randomUUID(), user));
  }

  @Test
  public void deleteRelationComponentAttributeByIdRelationComponentAttributeAlreadyDeletedIntegrationTest () {
    RelationComponentAttribute relationComponentAttribute = new RelationComponentAttribute(attributeType, relationComponent, language, user);
    relationComponentAttribute.setIsDeleted(true);
    RelationComponentAttribute deletedRelationComponentAttribute = relationComponentAttributeRepository.save(relationComponentAttribute);

    assertThrows(RelationComponentAttributeNotFoundException.class, () -> relationComponentAttributesService.deleteRelationComponentAttributeById(deletedRelationComponentAttribute.getRelationComponentAttributeId(), user));
  }

  @Test
  public void deleteRelationComponentAttributeByIdSuccessIntegrationTest () {
    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    relationComponentAttributesService.deleteRelationComponentAttributeById(relationComponentAttribute.getRelationComponentAttributeId(), user);

    Optional<RelationComponentAttribute> deletedRelationComponentAttribute = relationComponentAttributeRepository.findById(relationComponentAttribute.getRelationComponentAttributeId());

    assertAll(
      () -> assertNotNull(deletedRelationComponentAttribute.get().getDeletedOn()),
      () -> assertTrue(deletedRelationComponentAttribute.get().getIsDeleted())
    );
  }

  @Test
  public void deleteRelationComponentAttributeByIdRelationComponentAttributeHistoryIntegrationTest () {
    relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));
    PostRelationComponentAttributeRequest postRequest = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), relationComponent.getRelationComponentId().toString(), "123");
    PostRelationComponentAttributeResponse postResponse = relationComponentAttributesService.createRelationComponentAttribute(postRequest, user);

    relationComponentAttributesService.deleteRelationComponentAttributeById(postResponse.getRelation_component_attribute_id(), user);

    Optional<RelationComponentAttribute> deletedRelationComponentAttribute = relationComponentAttributeRepository.findById(postResponse.getRelation_component_attribute_id());

    List<RelationComponentAttributeHistory> relationComponentAttributeHistories = relationComponentAttributesHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, relationComponentAttributeHistories.size(), "relation component attribute history size"),
      () -> assertEquals(postResponse.getCreated_on(), relationComponentAttributeHistories.stream().filter(asset -> asset.getValue().equals(postResponse.getValue())).findFirst().get().getValidFrom(), "post relation component attribute history valid from"),
      () -> assertEquals(deletedRelationComponentAttribute.get().getDeletedOn(), relationComponentAttributeHistories.stream().filter(RelationComponentAttributeHistory::getIsDeleted).findFirst().get().getValidFrom(), "deleted relation component attribute history valid from"),
      () -> assertEquals(deletedRelationComponentAttribute.get().getDeletedOn(), relationComponentAttributeHistories.stream().filter(asset -> asset.getValue().equals(postResponse.getValue())).findFirst().get().getValidTo(), "post relation component attribute history valid to"),
      () -> assertEquals(deletedRelationComponentAttribute.get().getDeletedOn(), relationComponentAttributeHistories.stream().filter(RelationComponentAttributeHistory::getIsDeleted).findFirst().get().getValidTo(), "deleted relation component attribute history valid to")
    );
  }

  @Test
  public void getRelationAttributesByParamsPaginationIntegrationTest () {
    generateRelationAttributes(130);

    assertAll(
      () -> assertEquals(50, relationComponentAttributesService.getRelationComponentAttributesByParams(null, null, 0, 50).getResults().size()),
      () -> assertEquals(100, relationComponentAttributesService.getRelationComponentAttributesByParams(null, null, 0, 130).getResults().size()),
      () -> assertEquals(2, relationComponentAttributesService.getRelationComponentAttributesByParams(null, null, 0, 2).getResults().size()),
      () -> assertEquals(0, relationComponentAttributesService.getRelationComponentAttributesByParams(null, null, 4, 100).getResults().size()),
      () -> assertEquals(130, relationComponentAttributesService.getRelationComponentAttributesByParams(null, null, 4, 100).getTotal())
    );
  }

  private void generateRelationAttributes (int count) {
    for (int i = 0; i < count; i++) {
      AttributeType attributeType = attributeTypeRepository.save(new AttributeType("boolean attribute type " + i, "attribute with boolean type " + i, AttributeKindType.BOOLEAN, null, null, language, user));
      RelationType relationType = relationTypeRepository.save(new RelationType("relation type name_" + i, "desc", 2, false, false, language, user));
      Relation relation = relationRepository.save(new Relation(relationType, null));
      RelationTypeComponent relationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("relation type component name_" + i, "desc", null, null, null, language, relationType, user));
      RelationComponent relationComponent = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponent, null, null, null, user));
      RelationComponentAttribute firstRelationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));
    }
  }
}
