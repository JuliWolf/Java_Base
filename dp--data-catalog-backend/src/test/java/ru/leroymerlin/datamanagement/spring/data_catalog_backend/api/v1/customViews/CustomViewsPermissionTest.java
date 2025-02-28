package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.CustomViewRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewHeaderRowName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class CustomViewsPermissionTest extends MockPermissionTest {
  @Autowired
  private CustomViewRepository customViewRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

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

  Entity customViewEntity;

  AssetType assetType;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    customViewEntity = entityRepository.findById(UUID.fromString("6e1ae00a-2b61-47b0-8f73-d6b0fce1f97a")).get();

    assetType = assetTypeRepository.save(new AssetType("asset type name", "desc", "acr", "color", language, user));
  }

  @AfterAll
  public void clearData () {
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    roleActionRepository.deleteAll();
    customViewRepository.deleteAll();
  }

  @Test
  public void POST_customViews_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("secondName", AttributeKindType.BOOLEAN));
    PostCustomViewRequest postRequest = new PostCustomViewRequest(assetType.getAssetTypeId().toString(), "some name", role.getRoleId().toString(), headerRowNames, null, "Select value from asset_type ORDER BY value limit 1", null, null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, customViewEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/customViews",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_customViews_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("secondName", AttributeKindType.BOOLEAN));
    PostCustomViewRequest postRequest = new PostCustomViewRequest(assetType.getAssetTypeId().toString(), "some name", role.getRoleId().toString(), headerRowNames, null, "Select value from asset_type ORDER BY value", null, null, null, null, null);
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, customViewEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/customViews",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_customViews_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", "[]", null, "query", null, "[{\"column_kind\": \"RTF\", \"column_name\": \"name_1\"}, {\"column_kind\": \"TEXT\", \"column_name\": \"name_2\"}, {\"column_kind\": \"SINGLE_VALUE_LIST\", \"column_name\": \"name_3\"}]", null, "table query", null,  role, user));

    roleActionRepository.save(new RoleAction(role, editActionType, customViewEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/customViews/" + customView.getCustomViewId(),
      "{\"custom_view_name\": \"another name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_customViews_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", "[]", null, "query", null, "[{\"column_kind\": \"RTF\", \"column_name\": \"name_1\"}, {\"column_kind\": \"TEXT\", \"column_name\": \"name_2\"}, {\"column_kind\": \"SINGLE_VALUE_LIST\", \"column_name\": \"name_3\"}]", null, "table query", null,  role, user));

    roleActionRepository.save(new RoleAction(role, editActionType, customViewEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/customViews/" + customView.getCustomViewId(),
      "{\"custom_view_name\": \"another name\"}",
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_customViews_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", "[]", null, "query", null, "[{\"column_kind\": \"RTF\", \"column_name\": \"name_1\"}, {\"column_kind\": \"TEXT\", \"column_name\": \"name_2\"}, {\"column_kind\": \"SINGLE_VALUE_LIST\", \"column_name\": \"name_3\"}]", null, "table query", null,  role, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, customViewEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/customViews",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_customViews_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", "[]", null, "query", null, "[{\"column_kind\": \"RTF\", \"column_name\": \"name_1\"}, {\"column_kind\": \"TEXT\", \"column_name\": \"name_2\"}, {\"column_kind\": \"SINGLE_VALUE_LIST\", \"column_name\": \"name_3\"}]", null, "table query", null,  role, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, customViewEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/customViews",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_customViews_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", "[]", null, "query", null, "[{\"column_kind\": \"RTF\", \"column_name\": \"name_1\"}, {\"column_kind\": \"TEXT\", \"column_name\": \"name_2\"}, {\"column_kind\": \"SINGLE_VALUE_LIST\", \"column_name\": \"name_3\"}]", null, "table query", null,  role, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, customViewEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/customViews/" + customView.getCustomViewId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_customViews_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", "[]", null, "query", null, "[{\"column_kind\": \"RTF\", \"column_name\": \"name_1\"}, {\"column_kind\": \"TEXT\", \"column_name\": \"name_2\"}, {\"column_kind\": \"SINGLE_VALUE_LIST\", \"column_name\": \"name_3\"}]", null, "table query", null,  role, user));

    roleActionRepository.save(new RoleAction(role, viewActionType, customViewEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/customViews/" + customView.getCustomViewId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_customViews_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", "[]", null, "query", null, "[{\"column_kind\": \"RTF\", \"column_name\": \"name_1\"}, {\"column_kind\": \"TEXT\", \"column_name\": \"name_2\"}, {\"column_kind\": \"SINGLE_VALUE_LIST\", \"column_name\": \"name_3\"}]", null, "table query", null,  role, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, customViewEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/customViews/" + customView.getCustomViewId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_customViews_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", "[]", null, "query", null, "[{\"column_kind\": \"RTF\", \"column_name\": \"name_1\"}, {\"column_kind\": \"TEXT\", \"column_name\": \"name_2\"}, {\"column_kind\": \"SINGLE_VALUE_LIST\", \"column_name\": \"name_3\"}]", null, "table query", null,  role, user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, customViewEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/customViews/" + customView.getCustomViewId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }
}
