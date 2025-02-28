package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PostRelationTypeComponentRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PostRelationTypeRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class RelationTypesPermissionTest extends MockPermissionTest {
  @Autowired
  private RelationTypeRepository relationTypeRepository;

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
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;

  Entity relationTypeEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    relationTypeEntity = entityRepository.findById(UUID.fromString("b1c5eb66-b777-4ee8-8a39-6ad9e43f447f")).get();
  }

  @AfterAll
  public void clearData () {
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_relationTypes_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationTypeComponentRequest> list = new ArrayList<>();
    list.add(new PostRelationTypeComponentRequest("relation type component", "desc", null, null, null));
    list.add(new PostRelationTypeComponentRequest("second type component", "desc", null, null, null));

    PostRelationTypeRequest postRequest = new PostRelationTypeRequest("relation type name", "desc", 2, false, false, true, false, list);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relationTypes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_relationTypes_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<PostRelationTypeComponentRequest> list = new ArrayList<>();
    list.add(new PostRelationTypeComponentRequest("relation type component", "desc", null, null, null));
    list.add(new PostRelationTypeComponentRequest("second type component", "desc", null, null, null));

    PostRelationTypeRequest postRequest = new PostRelationTypeRequest("relation type name", "desc", 2, false, false, true, false, list);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/relationTypes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_relationTypes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      "{\"relation_type_name\": \"new relation type\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_relationTypes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      "{\"relation_type_name\": \"new relation type\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_relationTypes_GlobalResponsibility_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    RoleAction roleAction = new RoleAction(role, editActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      "{\"relation_type_name\": \"new relation type\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_relationTypes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      "{\"relation_type_name\": \"new relation type\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypes_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypes_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relationTypes_by_id_GlobalResponsibility_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    RoleAction roleAction = new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypes_by_id_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypes_by_id_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_relationTypes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_relationTypes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/relationTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypes_GlobalResponsibility_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    RoleAction roleAction = new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_relationTypes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_relationType () {
    roleActionCachingService.clearCache();

    RelationType relationType = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, false, false, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, relationTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setRelationType(relationType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/relationTypes/" + relationType.getRelationTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }
}
