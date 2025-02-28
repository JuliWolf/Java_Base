package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class AssetTypeRelationTypeComponentAssignmentsPermissionTest extends MockPermissionTest {
  @Autowired
  private RelationTypeRepository relationTypeRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

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
  RelationType relationType;
  RelationTypeComponent relationTypeComponent;
  AssetType assetType;
  ActionType viewActionType;

  Entity assetTypeEntity;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("asset type relation component role name", "desc", language, user));

    relationType = relationTypeRepository.save(new RelationType("relation type name", "des", 2, false, false, language, user));
    relationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

    assetType = assetTypeRepository.save(new AssetType("asset type", "desc", "acr", "color", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    assetTypeEntity = entityRepository.findById(UUID.fromString("b8abfa30-b1ad-4e07-8f8a-c2cf40a723bf")).get();
  }

  @AfterAll
  public void clearData () {
    assetTypeRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAfterEach () {
    roleActionRepository.deleteAll();
  }

  @Test
  public void GET_assetTypeRelationTypeComponent_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/relationTypeComponents",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_assetTypeRelationTypeComponent_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/relationTypeComponents",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assetTypeRelationTypeComponent_GlobalResponsibility_ALLOW_ALL_DENY_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ONE_ID, PermissionType.DENY, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/relationTypeComponents",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_assetTypeRelationTypeComponent_GlobalResponsibility_DENY_ALL_ALLOW_ONE_ID_assetType () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ALL, PermissionType.DENY, user));
    RoleAction roleAction = new RoleAction(role, viewActionType, assetTypeEntity, ActionScopeType.ONE_ID, PermissionType.ALLOW, user);
    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/assignments/assetType/" + assetType.getAssetTypeId() + "/relationTypeComponents",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }
}
