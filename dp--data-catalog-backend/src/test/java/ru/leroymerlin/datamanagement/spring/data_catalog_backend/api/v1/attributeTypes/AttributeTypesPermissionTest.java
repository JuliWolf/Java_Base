package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class AttributeTypesPermissionTest extends MockPermissionTest {

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
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;

  Entity attributeTypeEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    attributeTypeEntity = entityRepository.findById(UUID.fromString("c4dbe5b6-98dc-431e-807f-a5109a19ee9d")).get();
  }

  @AfterAll
  public void clearData () {
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    attributeTypeRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_attributeTypes_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostAttributeTypeRequest postRequest = new PostAttributeTypeRequest("some name", "desc", AttributeKindType.TEXT, null, new ArrayList<>(), null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributeTypes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_attributeTypes_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostAttributeTypeRequest postRequest = new PostAttributeTypeRequest("some name", "desc", AttributeKindType.TEXT, null, new ArrayList<>(), null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/attributeTypes",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_attributeTypes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributeTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_attributeTypes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributeTypes",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_attributeTypes_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_attributeTypes_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_attributeTypes_by_id_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, attributeTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_attributeTypes_by_id_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, attributeTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_attributeTypes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      "{\"attribute_type_name\": \"some new name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_attributeTypes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      "{\"attribute_type_name\": \"some new name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_attributeTypes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      "{\"attribute_type_name\": \"some new name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_attributeTypes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, editActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      "{\"attribute_type_name\": \"some new name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributeTypes_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_attributeTypes_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_attributeTypes_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, deleteActionType, attributeTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_attributeTypes_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_attributeType () {
    roleActionCachingService.clearCache();

    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type name", "desc", AttributeKindType.TEXT, null, null, language, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, attributeTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, editActionType, attributeTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAttributeType(attributeType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/attributeTypes/" + attributeType.getAttributeTypeId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }
}
