package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.CustomViewRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewHeaderRowName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewTableColumnName;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class AssetCustomViewPermissionTest extends MockPermissionTest {
  @Autowired
  private CustomViewRepository customViewRepository;

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  @Autowired
  private ResponsibilityRepository responsibilityRepository;

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
  Role assetRole;
  Asset asset;
  Asset secondAsset;
  CustomView customViewWithHeaderRows;
  CustomView customViewWithTableColumns;
  AssetType assetType;
  AssetType secondAssetType;
  ActionType viewActionType;

  Entity assetEntity;
  Entity assetTypeEntity;

  ObjectMapper objectMapper = new ObjectMapper();

  @SneakyThrows
  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));
    assetRole = roleRepository.save(new Role("test asset name", "desc", language, user));

    assetType = assetTypeRepository.save(new AssetType("asset type name", "desc", "atn", "red", language, user));
    secondAssetType = assetTypeRepository.save(new AssetType("second asset type name", "desc", "atn", "red", language, user));

    asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));
    secondAsset = assetRepository.save(new Asset("second asset name", secondAssetType, "disp", language, null, null, user));

    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("tech_name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("synonyms", AttributeKindType.TEXT));

    customViewWithHeaderRows = customViewRepository.save(new CustomView(assetType, "some name", objectMapper.writeValueAsString(headerRowNames), null, "select a.value as tech_name, a2.value as synonyms from asset ast left join attribute a on ast.asset_id = a.asset_id and not a.deleted_flag and a.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' left join attribute a2 on ast.asset_id = a2.asset_id and not a2.deleted_flag and a2.attribute_type_id = '29942fad-13e0-4770-9eec-55e78896f4ac' where ast.asset_id = :assetId", null, null, null, null, null, role, user));

    List<CustomViewTableColumnName> tableColumnNames = new ArrayList<>();
    tableColumnNames.add(new CustomViewTableColumnName("tech_name", AttributeKindType.TEXT));
    tableColumnNames.add(new CustomViewTableColumnName("asset_displayname", AttributeKindType.TEXT));
    customViewWithTableColumns = customViewRepository.save(new CustomView(secondAssetType, "second name", null, null, null, null, objectMapper.writeValueAsString(tableColumnNames), null, "select a.value as tech_name, ast.asset_displayname as asset_displayname from asset ast left join attribute a on ast.asset_id = a.asset_id and not a.deleted_flag where ast.asset_id = :assetId ORDER BY ast.asset_id", null, role, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));
    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, assetRole, ResponsibleType.USER, user));

    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    assetEntity = entityRepository.findById(UUID.fromString("f2d482f5-fe31-45c8-856d-512a9aa56fde")).get();
    assetTypeEntity = entityRepository.findById(UUID.fromString("b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf")).get();
  }

  @AfterAll
  public void clearData () {
    customViewRepository.deleteAll();
    assetRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    assetTypeRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    responsibilityRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void GET_asset_custom_view_header_rows_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/customViews/" + customViewWithHeaderRows.getCustomViewId() + "/headerRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_header_rows_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/customViews/" + customViewWithHeaderRows.getCustomViewId() + "/headerRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_header_rows_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/customViews/" + customViewWithHeaderRows.getCustomViewId() + "/headerRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_header_rows_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/customViews/" + customViewWithHeaderRows.getCustomViewId() + "/headerRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_header_rows_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/customViews/" + customViewWithHeaderRows.getCustomViewId() + "/headerRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_header_rows_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    responsibilityRepository.save(new Responsibility(user, null, asset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + asset.getAssetId() + "/customViews/" + customViewWithHeaderRows.getCustomViewId() + "/headerRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_table_columns_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + secondAsset.getAssetId() + "/customViews/" + customViewWithTableColumns.getCustomViewId() + "/tableRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_table_columns_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + secondAsset.getAssetId() + "/customViews/" + customViewWithTableColumns.getCustomViewId() + "/tableRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_table_columns_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(secondAssetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + secondAsset.getAssetId() + "/customViews/" + customViewWithTableColumns.getCustomViewId() + "/tableRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_table_columns_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(secondAssetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + secondAsset.getAssetId() + "/customViews/" + customViewWithTableColumns.getCustomViewId() + "/tableRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_table_columns_GlobalResponsibility_DENY_ONE_ID_assetType_responsibility_ALLOW_ALL_assetId () {
    roleActionCachingService.clearCache();

    responsibilityRepository.save(new Responsibility(user, null, secondAsset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(secondAssetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + secondAsset.getAssetId() + "/customViews/" + customViewWithTableColumns.getCustomViewId() + "/tableRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_asset_custom_view_table_columns_GlobalResponsibility_ALLOW_ONE_ID_assetType_responsibility_DENY_ALL_assetId () {
    roleActionCachingService.clearCache();

    responsibilityRepository.save(new Responsibility(user, null, secondAsset, assetRole, ResponsibleType.USER, user));

    roleActionRepository.save(new RoleAction(assetRole, viewActionType, assetEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(secondAssetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assets/" + secondAsset.getAssetId() + "/customViews/" + customViewWithTableColumns.getCustomViewId() + "/tableRows",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
