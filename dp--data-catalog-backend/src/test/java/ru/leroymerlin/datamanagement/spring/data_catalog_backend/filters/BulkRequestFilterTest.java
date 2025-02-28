package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class BulkRequestFilterTest extends MockPermissionTest {

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  @Autowired
  private RoleActionRepository roleActionRepository;
  @Autowired
  private ActionTypeRepository actionTypeRepository;
  @Autowired
  private EntityRepository entityRepository;
  @Autowired
  private LogRepository logRepository;

  Role role;

  AssetType assetType;

  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  Entity assetEntity;

  @AfterAll
  public void clearData () {
    roleActionRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    assetTypeRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    assetRepository.deleteAll();
  }

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    assetType = assetTypeRepository.save(new AssetType("asset type name", "desc", "atn", "red", language, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();

    assetEntity = entityRepository.findById(UUID.fromString("f2d482f5-fe31-45c8-856d-512a9aa56fde")).get();

    roleActionRepository.save(new RoleAction(role, addActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
    roleActionRepository.save(new RoleAction(role, deleteActionType, assetEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));
  }

  @Test
  public void POST_maximumRequestsExceededException () throws JsonProcessingException {
    String requestBody = buildRequestString(4);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, request.getStatusCode());
  }

  @Test
  public void POST_successTest () throws JsonProcessingException {
    String requestBody = buildRequestString(3);

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_maximumRequestsExceededException () throws JsonProcessingException {
    String requestBody = buildRequestString(3);

    httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    List<UUID> assets = assetRepository.findAll().stream().map(Asset::getAssetId).toList();

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/bulk",
      new ObjectMapper().writeValueAsString(assets),
      user
    );

    assertEquals(HttpURLConnection.HTTP_ENTITY_TOO_LARGE, request.getStatusCode());
  }

  @Test
  public void DELETE_successTest () throws JsonProcessingException {
    String requestBody = buildRequestString(2);

    httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/assets/bulk",
      requestBody,
      user
    );

    List<UUID> assets = assetRepository.findAll().stream().map(Asset::getAssetId).toList();

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/assets/bulk",
      new ObjectMapper().writeValueAsString(assets),
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  private String buildRequestString (Integer requestsCount) throws JsonProcessingException {
    List<PostOrPatchAssetRequest> requests = new ArrayList<>();
    for (int i = 0; i < requestsCount; i++) {
      requests.add(new PostOrPatchAssetRequest("test asset number " + i, "test asset displayname " + i, assetType.getAssetTypeId().toString(), null, null));
    }

    return new ObjectMapper().writeValueAsString(requests);
  }
}
