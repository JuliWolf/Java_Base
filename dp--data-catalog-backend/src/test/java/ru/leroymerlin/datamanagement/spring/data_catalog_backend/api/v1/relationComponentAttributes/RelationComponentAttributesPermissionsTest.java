package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes.RelationComponentAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class RelationComponentAttributesPermissionsTest extends MockPermissionTest {
  @Autowired
  private RelationComponentAttributeRepository relationComponentAttributeRepository;
  @Autowired
  private RelationRepository relationRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository;
  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private RelationComponentRepository relationComponentRepository;

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
  RelationType relationType;
  RelationTypeComponent relationTypeComponent;
  RelationComponent relationComponent;
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;

  Entity relationComponentAttributeEntity;
  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    attributeType = attributeTypeRepository.save(new AttributeType("some name", "some description", AttributeKindType.TEXT, null, null, language, user));
    relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));
    relationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("relation type component name", "desc", null, null, null, language, relationType, user));
    Relation relation = relationRepository.save(new Relation(relationType, null));
    relationComponent = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponent, null, null, null, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    relationComponentAttributeEntity = entityRepository.findById(UUID.fromString("5c06a7c1-5b3f-49f4-bdc2-e9df4f317d73")).get();
  }

  @AfterAll
  public void clearData () {
    relationTypeComponentAttributeTypeAssignmentRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    relationComponentRepository.deleteAll();
    relationRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    roleActionRepository.deleteAll();
    relationComponentAttributeRepository.deleteAll();
  }

  @Test
  public void POST_relation_component_attributes_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostRelationComponentAttributeRequest postRequest = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), relationComponent.getRelationComponentId().toString(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relationComponentAttributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relation_component_attributes_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostRelationComponentAttributeRequest postRequest = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), relationComponent.getRelationComponentId().toString(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relationComponentAttributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relation_component_attributes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostRelationComponentAttributeRequest postRequest = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), relationComponent.getRelationComponentId().toString(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationComponentAttributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relationComponentAttributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relation_component_attributes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostRelationComponentAttributeRequest postRequest = new PostRelationComponentAttributeRequest(attributeType.getAttributeTypeId().toString(), relationComponent.getRelationComponentId().toString(), "some value");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationComponentAttributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relationComponentAttributes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_relation_component_attributes_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    PatchRelationComponentAttributeRequest patchRequest = new PatchRelationComponentAttributeRequest("some value");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" + relationComponentAttribute.getRelationComponentAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_relation_component_attributes_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    PatchRelationComponentAttributeRequest patchRequest = new PatchRelationComponentAttributeRequest("some value");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" + relationComponentAttribute.getRelationComponentAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_relation_component_attributes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    PatchRelationComponentAttributeRequest patchRequest = new PatchRelationComponentAttributeRequest("some value");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, relationComponentAttributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" +relationComponentAttribute.getRelationComponentAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_relation_component_attributes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    PatchRelationComponentAttributeRequest patchRequest = new PatchRelationComponentAttributeRequest("some value");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, relationComponentAttributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" +relationComponentAttribute.getRelationComponentAttributeId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relation_component_attributes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationComponentAttributes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relation_component_attributes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationComponentAttributes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relation_component_attributes_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" + relationComponentAttribute.getRelationComponentAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relation_component_attributes_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" + relationComponentAttribute.getRelationComponentAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relation_component_attributes_by_id_GlobalResponsibility_ALLOW_ALL_DENY_INE_ID_attributeType () {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationComponentAttributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" + relationComponentAttribute.getRelationComponentAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relation_component_attributes_by_id_GlobalResponsibility_DENY_ALL_ALLOW_INE_ID_attributeType () {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationComponentAttributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" + relationComponentAttribute.getRelationComponentAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relation_component_attributes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" + relationComponentAttribute.getRelationComponentAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relation_component_attributes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" + relationComponentAttribute.getRelationComponentAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relation_component_attributes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationComponentAttributeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" + relationComponentAttribute.getRelationComponentAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relation_component_attributes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeType, relationComponent, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationComponentAttributeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationComponentAttributeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationComponentAttributes/" + relationComponentAttribute.getRelationComponentAttributeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
