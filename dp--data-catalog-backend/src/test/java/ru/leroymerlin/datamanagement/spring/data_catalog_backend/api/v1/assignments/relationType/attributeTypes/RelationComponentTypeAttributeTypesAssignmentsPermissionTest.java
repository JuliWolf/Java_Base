package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.RelationTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class RelationComponentTypeAttributeTypesAssignmentsPermissionTest extends MockPermissionTest {
  @Autowired
  private RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository;

  @Autowired
  private RelationTypeRepository relationTypeRepository;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;

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
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;
  ActionType grantActionType;
  ActionType revokeActionType;

  Entity relationTypeEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    relationType = relationTypeRepository.save(new RelationType("relation type name", "des", 2, false, false, language, user));

    attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", " desc", AttributeKindType.TEXT, null, null, language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();
    grantActionType = actionTypeRepository.findById(UUID.fromString("d3bfacc2-9438-4296-a149-cfc6287e627d")).get();
    revokeActionType = actionTypeRepository.findById(UUID.fromString("5b197bb3-5140-4acf-b576-7870205b4342")).get();

    relationTypeEntity = entityRepository.findById(UUID.fromString("b1c5eb66-b777-4ee8-8a39-6ad9e43f447f")).get();
  }

  @AfterAll
  public void clearData () {
    relationTypeRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
    attributeTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    relationTypeAttributeTypeAssignmentRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_relationTypeAttributeType_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationTypeAttributeTypeRequest> attributeAssignments = new ArrayList<>();
    attributeAssignments.add(new PostRelationTypeAttributeTypeRequest(attributeType.getAttributeTypeId().toString()));

    PostRelationTypeAttributeTypesRequest postRequest = new PostRelationTypeAttributeTypesRequest(attributeAssignments);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/relationType/" + relationType.getRelationTypeId() + "/attribute_type/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relationTypeAttributeType_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationTypeAttributeTypeRequest> attributeAssignments = new ArrayList<>();
    attributeAssignments.add(new PostRelationTypeAttributeTypeRequest(attributeType.getAttributeTypeId().toString()));

    PostRelationTypeAttributeTypesRequest postRequest = new PostRelationTypeAttributeTypesRequest(attributeAssignments);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/relationType/" + relationType.getRelationTypeId() + "/attribute_type/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relationTypeAttributeType_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationTypeAttributeTypeRequest> attributeAssignments = new ArrayList<>();
    attributeAssignments.add(new PostRelationTypeAttributeTypeRequest(attributeType.getAttributeTypeId().toString()));

    PostRelationTypeAttributeTypesRequest postRequest = new PostRelationTypeAttributeTypesRequest(attributeAssignments);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/relationType/" + relationType.getRelationTypeId() + "/attribute_type/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_relationTypeAttributeType_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationTypeAttributeTypeRequest> attributeAssignments = new ArrayList<>();
    attributeAssignments.add(new PostRelationTypeAttributeTypeRequest(attributeType.getAttributeTypeId().toString()));

    PostRelationTypeAttributeTypesRequest postRequest = new PostRelationTypeAttributeTypesRequest(attributeAssignments);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, addActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assignments/relationType/" + relationType.getRelationTypeId() + "/attribute_type/batch",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeAttributeType_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationType/" + relationType.getRelationTypeId() + "/attribute_type",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeAttributeType_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationType/" + relationType.getRelationTypeId() + "/attribute_type",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeAttributeType_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationType/" + relationType.getRelationTypeId() + "/attribute_type",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeAttributeType_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationType/" + relationType.getRelationTypeId() + "/attribute_type",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeAttributeTypesUsageCount_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationTypes/attributeTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypeAttributeTypesUsageCount_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/relationTypes/attributeTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypeAttributeType_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/relationType/attributeType/" + relationTypeAttributeTypeAssignment.getRelationTypeAttributeTypeAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypeAttributeType_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/relationType/attributeType/" + relationTypeAttributeTypeAssignment.getRelationTypeAttributeTypeAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypeAttributeType_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/relationType/attributeType/" + relationTypeAttributeTypeAssignment.getRelationTypeAttributeTypeAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypeAttributeType_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationType, attributeType, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assignments/relationType/attributeType/" + relationTypeAttributeTypeAssignment.getRelationTypeAttributeTypeAssignmentId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
