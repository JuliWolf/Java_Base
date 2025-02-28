package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.RelationAttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.RelationTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.RelationAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributesHistory.RelationAttributesHistoryRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.AttributeTypeNotAllowedForRelationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.RelationAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get.GetRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationNotFoundException;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author juliwolf
 */

public class RelationAttributesServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private RelationAttributeRepository relationAttributeRepository;
  @Autowired
  private RelationAttributesService relationAttributesService;

  @Autowired
  private RelationRepository relationRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository;
  @Autowired
  private RelationAttributesHistoryRepository relationAttributesHistoryRepository;

  AttributeType attributeType;
  Relation relation;
  RelationType relationType;

  @BeforeEach
  public void prepareData () {
    attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, null, language, user));
    relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));
    relation = relationRepository.save(new Relation(relationType, null));
  }

  @AfterEach
  public void deleteData () {
    relationAttributesHistoryRepository.deleteAll();
    relationTypeAttributeTypeAssignmentRepository.deleteAll();
    relationAttributeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    relationRepository.deleteAll();
    relationTypeRepository.deleteAll();
  }

  @Test
  public void createRelationAttributeRelationNotFoundIntegrationTest () {
    PostRelationAttributeRequest request = new PostRelationAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "value");

    assertThrows(RelationNotFoundException.class, () -> relationAttributesService.createRelationAttribute(request, user));
  }

  @Test
  public void createRelationAttributeAttributeTypeNotFoundIntegrationTest () {
    PostRelationAttributeRequest request = new PostRelationAttributeRequest(UUID.randomUUID().toString(), relation.getRelationId().toString(), "value");

    assertThrows(AttributeTypeNotFoundException.class, () -> relationAttributesService.createRelationAttribute(request, user));
  }

  @Test
  public void createRelationAttributeAttributeTypeNotAllowedForRelationIntegrationTest () {
    PostRelationAttributeRequest request = new PostRelationAttributeRequest(attributeType.getAttributeTypeId().toString(), relation.getRelationId().toString(), "value");

    assertThrows(AttributeTypeNotAllowedForRelationException.class, () -> relationAttributesService.createRelationAttribute(request, user));
  }
  @Test
  public void createRelationAttributeRelationAttributeAlreadyExistsIntegrationTest () {
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType, user));
    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    PostRelationAttributeRequest request = new PostRelationAttributeRequest(attributeType.getAttributeTypeId().toString(), relation.getRelationId().toString(), "value");

    assertThrows(DataIntegrityViolationException.class, () -> relationAttributesService.createRelationAttribute(request, user));
  }

  @Test
  public void createRelationAttributeSuccessIntegrationTest () {
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType, user));

    PostRelationAttributeRequest request = new PostRelationAttributeRequest(attributeType.getAttributeTypeId().toString(), relation.getRelationId().toString(), "value");

    PostRelationAttributeResponse response = relationAttributesService.createRelationAttribute(request, user);

    assertAll(
      () -> assertEquals("value", response.getValue()),
      () -> assertNull(response.getValue_datetime()),
      () -> assertNull(response.getInteger_flag()),
      () -> assertEquals(relation.getRelationId(), response.getRelation_id()),
      () -> assertEquals(attributeType.getAttributeTypeId(), response.getAttribute_type_id())
    );
  }

  @Test
  public void createRelationAttributeRelationAttributeHistoryIntegrationTest () {
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType, user));
    PostRelationAttributeRequest request = new PostRelationAttributeRequest(attributeType.getAttributeTypeId().toString(), relation.getRelationId().toString(), "value");

    PostRelationAttributeResponse response = relationAttributesService.createRelationAttribute(request, user);

    List<RelationAttributeHistory> relationAttributeHistories = relationAttributesHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(1, relationAttributeHistories.size(), "relation attribute history size"),
      () -> assertEquals("3000-01-01 00:00:00.0", relationAttributeHistories.get(0).getValidTo().toString(), "relation attribute history valid to"),
      () -> assertEquals(response.getCreated_on(), relationAttributeHistories.get(0).getValidFrom(), "relation attribute history valid from")
    );
  }

  @Test
  public void updateRelationAttributeRelationAttributeNotFoundIntegrationTest () {
    PatchRelationAttributeRequest request = new PatchRelationAttributeRequest("value");

    assertThrows(RelationAttributeNotFoundException.class, () -> relationAttributesService.updateRelationAttribute(UUID.randomUUID(), request, user));
  }

  @Test
  public void updateRelationAttributeRelationAttributeHistoryIntegrationTest () {
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType, user));
    PostRelationAttributeRequest postRequest = new PostRelationAttributeRequest(attributeType.getAttributeTypeId().toString(), relation.getRelationId().toString(), "123");

    PostRelationAttributeResponse postResponse = relationAttributesService.createRelationAttribute(postRequest, user);

    PatchRelationAttributeRequest request = new PatchRelationAttributeRequest("value");

    PatchRelationAttributeResponse patchResponse = relationAttributesService.updateRelationAttribute(postResponse.getRelation_attribute_id(), request, user);

    List<RelationAttributeHistory> relationAttributeHistories = relationAttributesHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, relationAttributeHistories.size(), "relation attribute history size"),
      () -> assertEquals(postResponse.getCreated_on(), relationAttributeHistories.stream().filter(attribute -> attribute.getValue().equals(postResponse.getValue())).findFirst().get().getValidFrom(), "post relation attribute history valid from"),
      () -> assertEquals(patchResponse.getLast_modified_on(), relationAttributeHistories.stream().filter(attribute -> attribute.getValue().equals(patchResponse.getValue())).findFirst().get().getValidFrom(), "patch relation attribute history valid from"),
      () -> assertEquals(patchResponse.getLast_modified_on(), relationAttributeHistories.stream().filter(attribute -> attribute.getValue().equals(postResponse.getValue())).findFirst().get().getValidTo(), "post relation attribute history valid to"),
      () -> assertEquals("3000-01-01 00:00:00.0", relationAttributeHistories.stream().filter(attribute -> attribute.getValue().equals(patchResponse.getValue())).findFirst().get().getValidTo().toString(), "patched relation attribute history valid to")
    );
  }

  @Test
  public void updateRelationAttributeSuccessIntegrationTest () {
    RelationAttribute relationAttribute = new RelationAttribute(attributeType, relation, language, user);
    relationAttribute.setValue("123");
    relationAttribute = relationAttributeRepository.save(relationAttribute);

    PatchRelationAttributeRequest request = new PatchRelationAttributeRequest("value");

    PatchRelationAttributeResponse response = relationAttributesService.updateRelationAttribute(relationAttribute.getRelationAttributeId(), request, user);

    assertAll(
      () -> assertNotNull(response.getLast_modified_by()),
      () -> assertNotNull(response.getLast_modified_on()),
      () -> assertEquals("value", response.getValue()),
      () -> assertNull(response.getValue_datetime()),
      () -> assertNull(response.getValue_bool())
    );
  }

  @Test
  public void getRelationAttributeByIdRelationAttributeNotFoundIntegrationTest () {
    assertThrows(RelationAttributeNotFoundException.class, () -> relationAttributesService.getRelationAttributeById(UUID.randomUUID()));
  }

  @Test
  public void getRelationAttributeByIdSuccessIntegrationTest () {
    RelationAttribute relationAttribute = new RelationAttribute(attributeType, relation, language, user);
    relationAttribute.setValue("123");
    relationAttribute = relationAttributeRepository.save(relationAttribute);

    GetRelationAttributeResponse response = relationAttributesService.getRelationAttributeById(relationAttribute.getRelationAttributeId());

    assertAll(
      () -> assertNull(response.getLast_modified_by()),
      () -> assertNull(response.getLast_modified_on()),
      () -> assertEquals("123", response.getValue()),
      () -> assertNull(response.getValue_datetime()),
      () -> assertNull(response.getValue_bool()),
      () -> assertEquals(response.getRelation_id(), relation.getRelationId()),
      () -> assertEquals(response.getAttribute_type_id(), attributeType.getAttributeTypeId())
    );
  }

  @Test
  public void getRelationAttributesByParamsIntegrationTest () {
    AttributeType firstAttributeType = attributeTypeRepository.save(new AttributeType("boolean attribute type", "attribute with boolean type", AttributeKindType.BOOLEAN, null, null, language, user));
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("first relation type name", "desc", 2, false, false, language, user));
    Relation firstRelation = relationRepository.save(new Relation(firstRelationType, null));
    RelationAttribute firstRelationAttribute = relationAttributeRepository.save(new RelationAttribute(firstAttributeType, firstRelation, language, user));

    AttributeType secondAttributeType = attributeTypeRepository.save(new AttributeType("text attribute type", "attribute with text type", AttributeKindType.TEXT, null, null, language, user));
    RelationType secondRelationType = relationTypeRepository.save(new RelationType("second relation type name", "desc", 2, false, false, language, user));
    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, null));
    RelationAttribute secondRelationAttribute = relationAttributeRepository.save(new RelationAttribute(secondAttributeType, secondRelation, language, user));

    AttributeType thirdAttributeType = attributeTypeRepository.save(new AttributeType("date attribute type", "attribute with date type", AttributeKindType.DATE, null, null, language, user));
    RelationType thirdRelationType = relationTypeRepository.save(new RelationType("third relation type name", "desc", 2, false, false, language, user));
    Relation thirdRelation = relationRepository.save(new Relation(thirdRelationType, null));
    RelationAttribute thirdRelationAttribute = relationAttributeRepository.save(new RelationAttribute(thirdAttributeType, thirdRelation, language, user));

    AttributeType forthAttributeType = attributeTypeRepository.save(new AttributeType("date time attribute type", "attribute with date time type", AttributeKindType.DATE_TIME, null, null, language, user));
    RelationType forthRelationType = relationTypeRepository.save(new RelationType("forth relation type name", "desc", 2, false, false, language, user));
    Relation forthRelation = relationRepository.save(new Relation(forthRelationType, null));
    RelationAttribute forthRelationAttribute = relationAttributeRepository.save(new RelationAttribute(forthAttributeType, forthRelation, language, user));

    RelationAttribute fifthRelationAttribute = relationAttributeRepository.save(new RelationAttribute(thirdAttributeType, firstRelation, language, user));

    assertAll(
      () -> assertEquals(5, relationAttributesService.getRelationAttributesByParams(null, null, 0, 50).getResults().size()),
      () -> assertEquals(2, relationAttributesService.getRelationAttributesByParams(firstRelation.getRelationId(), null, 0, 50).getResults().size()),
      () -> assertEquals(1, relationAttributesService.getRelationAttributesByParams(secondRelation.getRelationId(), null, 0, 50).getResults().size()),
      () -> assertEquals(3, relationAttributesService.getRelationAttributesByParams(null, List.of(firstAttributeType.getAttributeTypeId(), thirdAttributeType.getAttributeTypeId()), 0, 50).getResults().size()),
      () -> assertEquals(1, relationAttributesService.getRelationAttributesByParams(firstRelation.getRelationId(), List.of(firstAttributeType.getAttributeTypeId(), forthAttributeType.getAttributeTypeId()), 0, 50).getResults().size()),
      () -> assertEquals(0, relationAttributesService.getRelationAttributesByParams(firstRelation.getRelationId(), List.of(forthAttributeType.getAttributeTypeId(), secondAttributeType.getAttributeTypeId()), 0, 50).getResults().size())
    );
  }

  @Test
  public void deleteRelationAttributeByIdRelationAttributeNotFoundIntegrationTest () {
    assertThrows(RelationAttributeNotFoundException.class, () -> relationAttributesService.deleteRelationAttributeById(UUID.randomUUID(), user));
  }

  @Test
  public void deleteRelationAttributeByIdRelationAttributeAlreadyDeletedIntegrationTest () {
    RelationAttribute relationAttribute = new RelationAttribute(attributeType, relation, language, user);
    relationAttribute.setIsDeleted(true);
    RelationAttribute deletedRelationAttribute = relationAttributeRepository.save(relationAttribute);

    assertThrows(RelationAttributeNotFoundException.class, () -> relationAttributesService.deleteRelationAttributeById(deletedRelationAttribute.getRelationAttributeId(), user));
  }

  @Test
  public void deleteRelationAttributeByIdSuccessIntegrationTest () {
    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    relationAttributesService.deleteRelationAttributeById(relationAttribute.getRelationAttributeId(), user);

    Optional<RelationAttribute> deletedRelationAttribute = relationAttributeRepository.findById(relationAttribute.getRelationAttributeId());

    assertAll(
      () -> assertNotNull(deletedRelationAttribute.get().getDeletedOn()),
      () -> assertTrue(deletedRelationAttribute.get().getIsDeleted())
    );
  }

  @Test
  public void deleteRelationAttributeByIdRelationAttributeHistoryIntegrationTest () {
    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType, user));
    PostRelationAttributeRequest postRequest = new PostRelationAttributeRequest(attributeType.getAttributeTypeId().toString(), relation.getRelationId().toString(), "123");

    PostRelationAttributeResponse postResponse = relationAttributesService.createRelationAttribute(postRequest, user);

    relationAttributesService.deleteRelationAttributeById(postResponse.getRelation_attribute_id(), user);

    Optional<RelationAttribute> deletedRelationAttribute = relationAttributeRepository.findById(postResponse.getRelation_attribute_id());

    List<RelationAttributeHistory> relationAttributeHistories = relationAttributesHistoryRepository.findAll();

    assertAll(
      () -> assertEquals(2, relationAttributeHistories.size(), "relation attribute history size"),
      () -> assertEquals(postResponse.getCreated_on(), relationAttributeHistories.stream().filter(asset -> asset.getValue().equals(postResponse.getValue())).findFirst().get().getValidFrom(), "post relation attribute history valid from"),
      () -> assertEquals(deletedRelationAttribute.get().getDeletedOn(), relationAttributeHistories.stream().filter(RelationAttributeHistory::getIsDeleted).findFirst().get().getValidFrom(), "deleted relation attribute history valid from"),
      () -> assertEquals(deletedRelationAttribute.get().getDeletedOn(), relationAttributeHistories.stream().filter(asset -> asset.getValue().equals(postResponse.getValue())).findFirst().get().getValidTo(), "post relation attribute history valid to"),
      () -> assertEquals(deletedRelationAttribute.get().getDeletedOn(), relationAttributeHistories.stream().filter(RelationAttributeHistory::getIsDeleted).findFirst().get().getValidTo(), "deleted relation attribute history valid to")
    );
  }

  @Test
  public void getRelationAttributesByParamsPaginationIntegrationTest () {
    generateRelationAttributes(130);

    assertAll(
      () -> assertEquals(50, relationAttributesService.getRelationAttributesByParams(null, null, 0, 50).getResults().size()),
      () -> assertEquals(100, relationAttributesService.getRelationAttributesByParams(null, null, 0, 130).getResults().size()),
      () -> assertEquals(2, relationAttributesService.getRelationAttributesByParams(null, null, 0, 2).getResults().size()),
      () -> assertEquals(0, relationAttributesService.getRelationAttributesByParams(null, null, 4, 100).getResults().size()),
      () -> assertEquals(130, relationAttributesService.getRelationAttributesByParams(null, null, 4, 100).getTotal())
    );
  }

  private void generateRelationAttributes (int count) {
    for (int i = 0; i < count; i++) {
      AttributeType attributeType = attributeTypeRepository.save(new AttributeType("boolean attribute type " + i, "attribute with boolean type " + i, AttributeKindType.BOOLEAN, null, null, language, user));
      RelationType relationType = relationTypeRepository.save(new RelationType("relation type name_" + i, "desc", 2, false, false, language, user));
      Relation relation = relationRepository.save(new Relation(relationType, null));

      relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));
    }
  }
}
