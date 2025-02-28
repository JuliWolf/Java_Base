package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypesRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class RelationTypeComponentAttributeTypesAssignmentsPermissionTest extends MockPermissionTest {
  @Autowired
  private RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository;

  @Autowired
  private RelationTypeRepository relationTypeRepository;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;

  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;

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
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;
  ActionType grantActionType;
  ActionType revokeActionType;

  Entity relationTypeComponentEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    relationType = relationTypeRepository.save(new RelationType("relation type name", "des", 2, false, false, language, user));
    relationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

    attributeType = attributeTypeRepository.save(new AttributeType("attribute type", "desc", AttributeKindType.TEXT, null, null, language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();
    grantActionType = actionTypeRepository.findById(UUID.fromString("d3bfacc2-9438-4296-a149-cfc6287e627d")).get();
    revokeActionType = actionTypeRepository.findById(UUID.fromString("5b197bb3-5140-4acf-b576-7870205b4342")).get();

    relationTypeComponentEntity = entityRepository.findById(UUID.fromString("b1c5eb66-b777-4ee8-8a39-6ad9e43f447f")).get();
  }

  @AfterAll
  public void clearData () {
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    relationTypeComponentAttributeTypeAssignmentRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_relationTypeComponentAttributeType_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationTypeComponentAttributeTypeRequest> allowedAttributeTypes = new ArrayList<>();
    allowedAttributeTypes.add(new PostRelationTypeComponentAttributeTypeRequest(attributeType.getAttributeTypeId().toString()));

    PostRelationTypeComponentAttributeTypesRequest postRequest = new PostRelationTypeComponentAttributeTypesRequest(allowedAttributeTypes);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/" + relationTypeComponent.getRelationTypeComponentId() + "/attributeTypes/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relationTypeComponentAttributeType_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationTypeComponentAttributeTypeRequest> allowedAttributeTypes = new ArrayList<>();
    allowedAttributeTypes.add(new PostRelationTypeComponentAttributeTypeRequest(attributeType.getAttributeTypeId().toString()));

    PostRelationTypeComponentAttributeTypesRequest postRequest = new PostRelationTypeComponentAttributeTypesRequest(allowedAttributeTypes);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/" + relationTypeComponent.getRelationTypeComponentId() + "/attributeTypes/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relationTypeComponentAttributeType_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationTypeComponentAttributeTypeRequest> allowedAttributeTypes = new ArrayList<>();
    allowedAttributeTypes.add(new PostRelationTypeComponentAttributeTypeRequest(attributeType.getAttributeTypeId().toString()));

    PostRelationTypeComponentAttributeTypesRequest postRequest = new PostRelationTypeComponentAttributeTypesRequest(allowedAttributeTypes);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationTypeComponentEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/" + relationTypeComponent.getRelationTypeComponentId() + "/attributeTypes/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relationTypeComponentAttributeType_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationTypeComponentAttributeTypeRequest> allowedAttributeTypes = new ArrayList<>();
    allowedAttributeTypes.add(new PostRelationTypeComponentAttributeTypeRequest(attributeType.getAttributeTypeId().toString()));

    PostRelationTypeComponentAttributeTypesRequest postRequest = new PostRelationTypeComponentAttributeTypesRequest(allowedAttributeTypes);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationTypeComponentEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/" + relationTypeComponent.getRelationTypeComponentId() + "/attributeTypes/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeComponentAttributeType_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/" + relationTypeComponent.getRelationTypeComponentId() + "/attributeTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeComponentAttributeType_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/" + relationTypeComponent.getRelationTypeComponentId() + "/attributeTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeComponentAttributeType_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationTypeComponentEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/" + relationTypeComponent.getRelationTypeComponentId() + "/attributeTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeComponentAttributeType_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationTypeComponentEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/" + relationTypeComponent.getRelationTypeComponentId() + "/attributeTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeComponentAttributeTypesUsageCount_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponents/attributeTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeComponentAttributeTypesUsageCount_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponents/attributeTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypeComponentAttributeType_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    RelationTypeComponentAttributeTypeAssignment relationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/attributeType/" + relationTypeComponentAttributeTypeAssignment.getRelationTypeComponentAttributeTypeAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypeComponentAttributeType_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    RelationTypeComponentAttributeTypeAssignment relationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/attributeType/" + relationTypeComponentAttributeTypeAssignment.getRelationTypeComponentAttributeTypeAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypeComponentAttributeType_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationTypeComponentAttributeTypeAssignment relationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationTypeComponentEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/attributeType/" + relationTypeComponentAttributeTypeAssignment.getRelationTypeComponentAttributeTypeAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypeComponentAttributeType_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationTypeComponentAttributeTypeAssignment relationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(relationTypeComponent, attributeType, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeComponentEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationTypeComponentEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/relationTypeComponent/attributeType/" + relationTypeComponentAttributeTypeAssignment.getRelationTypeComponentAttributeTypeAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
