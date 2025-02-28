package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log.LogRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.SubscriptionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPermissionTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PatchSubscriptionRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PostSubscriptionRequest;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

public class SubscriptionsPermissionsTest extends MockPermissionTest {
  @Autowired
  private SubscriptionRepository subscriptionRepository;

  @Autowired
  private AssetRepository assetRepository;
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
  Asset asset;
  ActionType addActionType;
  ActionType editActionType;
  ActionType deleteActionType;
  ActionType viewActionType;

  Entity subscriptionEntity;

  User newUser;

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("test name", "desc", language, user));

    AssetType assetType = assetTypeRepository.save(new AssetType("asset type name", "description", "atn", "red", language, user));
    asset = assetRepository.save(new Asset("asset name", assetType, "an", language, null, null, user));

    globalResponsibilitiesRepository.save(new GlobalResponsibility(user, null, role, ResponsibleType.USER, user));

    addActionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d")).get();
    editActionType = actionTypeRepository.findById(UUID.fromString("3b086c0e-d19e-4874-9c18-8e9daf952bee")).get();
    deleteActionType = actionTypeRepository.findById(UUID.fromString("b8eb41e4-e43f-4259-a198-81fc19e7e90a")).get();
    viewActionType = actionTypeRepository.findById(UUID.fromString("c7f65d90-b311-4fac-9af3-68289a60e7e3")).get();

    subscriptionEntity = entityRepository.findById(UUID.fromString("dd6be94c-a022-4553-9d2e-d09226cebe58")).get();

    newUser = userRepository.save(new User("another username", "firstname", "secondname", SourceType.KEYCLOAK, "test@mail.com"));
  }

  @AfterAll
  public void clearData () {
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
    roleRepository.deleteAll();
    logRepository.deleteAll();
  }

  @AfterEach
  public void clearAssets () {
    subscriptionRepository.deleteAll();
    roleActionRepository.deleteAll();
  }

  @Test
  public void POST_subscriptions_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostSubscriptionRequest postRequest = new PostSubscriptionRequest(user.getUserId().toString(), asset.getAssetId().toString(), "* */5 * * * *");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/subscriptions",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void POST_subscriptions_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostSubscriptionRequest postRequest = new PostSubscriptionRequest(user.getUserId().toString(), asset.getAssetId().toString(), "* */5 * * * *");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/subscriptions",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void POST_subscriptions_GlobalResponsibility_ALLOW_ALL_wrong_owner () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    PostSubscriptionRequest postRequest = new PostSubscriptionRequest(newUser.getUserId().toString(), asset.getAssetId().toString(), "* */5 * * * *");
    String requestBody = new ObjectMapper().writeValueAsString(postRequest);

    roleActionRepository.save(new RoleAction(role, addActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.POST,
      "http://localhost:" + port + "/v1/subscriptions",
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_subscriptions_GlobalResponsibility_ALLOW_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* * * * * *", user));

    PatchSubscriptionRequest patchRequest = new PatchSubscriptionRequest("* */5 * * * *");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/subscriptions/" + subscription.getSubscriptionId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void PATCH_subscriptions_GlobalResponsibility_DENY_ALL () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* */5 * * * *", user));

    PatchSubscriptionRequest patchRequest = new PatchSubscriptionRequest("* */5 * * * *");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/subscriptions/" + subscription.getSubscriptionId(),
      requestBody,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void PATCH_subscriptions_GlobalResponsibility_ALLOW_ALL_wrong_owner () throws JsonProcessingException {
    roleActionCachingService.clearCache();

    Subscription subscription = subscriptionRepository.save(new Subscription(newUser, asset, "* * * * * *", user));

    PatchSubscriptionRequest patchRequest = new PatchSubscriptionRequest("* */5 * * * *");
    String requestBody = new ObjectMapper().writeValueAsString(patchRequest);

    roleActionRepository.save(new RoleAction(role, editActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.PATCH,
      "http://localhost:" + port + "/v1/subscriptions/" + subscription.getSubscriptionId(),
      requestBody,
      newUser
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_subscriptions_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/subscriptions",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_subscriptions_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    roleActionRepository.save(new RoleAction(role, viewActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/subscriptions",
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void GET_subscriptions_by_id_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* */5 * * * *", user));

    roleActionRepository.save(new RoleAction(role, viewActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/subscriptions/" + subscription.getSubscriptionId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void GET_subscriptions_by_id_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* */5 * * * *", user));

    roleActionRepository.save(new RoleAction(role, viewActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.GET,
      "http://localhost:" + port + "/v1/subscriptions/" + subscription.getSubscriptionId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_subscriptions_GlobalResponsibility_ALLOW_ALL () {
    roleActionCachingService.clearCache();

    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* */5 * * * *", user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/subscriptions/" + subscription.getSubscriptionId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_OK, request.getStatusCode());
  }

  @Test
  public void DELETE_subscriptions_GlobalResponsibility_DENY_ALL () {
    roleActionCachingService.clearCache();

    Subscription subscription = subscriptionRepository.save(new Subscription(user, asset, "* */5 * * * *", user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.DENY, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/subscriptions/" + subscription.getSubscriptionId(),
      null,
      user
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }

  @Test
  public void DELETE_subscriptions_GlobalResponsibility_ALLOW_ALL_wrong_owner () {
    roleActionCachingService.clearCache();

    Subscription subscription = subscriptionRepository.save(new Subscription(newUser, asset, "* */5 * * * *", user));

    roleActionRepository.save(new RoleAction(role, deleteActionType, subscriptionEntity, ActionScopeType.ALL, PermissionType.ALLOW, user));

    HttpRequestResponse request = httpRequestUtilsStub.createRequest(
      RequestType.DELETE,
      "http://localhost:" + port + "/v1/subscriptions/" + subscription.getSubscriptionId(),
      null,
      newUser
    );

    assertEquals(HttpURLConnection.HTTP_FORBIDDEN, request.getStatusCode());
  }
}
