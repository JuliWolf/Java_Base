package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.RelationTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.RelationAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class RelationAttributesPermissionsTest extends MockPermissionTest {
  @Autowired
  private RelationAttributeRepository relationAttributeRepository;
  @Autowired
  private RelationRepository relationRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository;

  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private RoleActionRepository roleActionRepository;
  @Autowired
  private ActionTypeRepository actionTypeRepository;
  @Autowired
  private EntityRepository entityRepository;
  @Autowired
  private LogRepository logRepository;

  @Autowired
  private RoleActionCachingService roleActionCachingService;

  Role role;
  AttributeType attributeType;
  Relation relation;
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;

  Entity relationAttributeEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, null, language, user));
    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));
    relation = relationRepository.save(new Relation(relationType, null));
    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    relationAttributeEntity = entityRepository.findById(UUID.fromString("85ba86eb-f55d-4635-aae7-7db72a6a29f1")).get();
  }

  @AfterAll
  public void clearData () {
    logRepository.deleteAll();
    relationTypeAttributeTypeAssignmentRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    relationRepository.deleteAll();
    relationTypeRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    roleActionRepository.deleteAll();
    relationAttributeRepository.deleteAll();
  }

  @Test
  public void POST_relation_attributes_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostRelationAttributeRequest postRequest = new PostRelationAttributeRequest(attributeType.getAttributeTypeId().toString(), relation.getRelationId().toString(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relationAttributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relation_attributes_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostRelationAttributeRequest postRequest = new PostRelationAttributeRequest(attributeType.getAttributeTypeId().toString(), relation.getRelationId().toString(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relationAttributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relation_attributes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostRelationAttributeRequest postRequest = new PostRelationAttributeRequest(attributeType.getAttributeTypeId().toString(), relation.getRelationId().toString(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationAttributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relationAttributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relation_attributes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostRelationAttributeRequest postRequest = new PostRelationAttributeRequest(attributeType.getAttributeTypeId().toString(), relation.getRelationId().toString(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationAttributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relationAttributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_relation_attributes_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    PatchRelationAttributeRequest postRequest = new PatchRelationAttributeRequest("some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_relation_attributes_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    PatchRelationAttributeRequest postRequest = new PatchRelationAttributeRequest("some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_relation_attributes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    PatchRelationAttributeRequest postRequest = new PatchRelationAttributeRequest("some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, relationAttributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_relation_attributes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    PatchRelationAttributeRequest postRequest = new PatchRelationAttributeRequest("some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, relationAttributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relation_attributes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationAttributes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relation_attributes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationAttributes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relation_by_id_attributes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relation_by_id_attributes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relation_by_id_attributes_GlobalResponsibility_ALLOW_ALL_DENY_INE_ID_attributeType () {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationAttributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relation_by_id_attributes_GlobalResponsibility_DENY_ALL_ALLOW_INE_ID_attributeType () {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationAttributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relation_attributes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relation_attributes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relation_attributes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationAttributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relation_attributes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeType, relation, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationAttributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationAttributes/" + relationAttribute.getRelationAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
