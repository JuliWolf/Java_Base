package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.UUIDUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions.InvalidCronExpressionException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.exceptions.SubscriptionNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.get.GetSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.get.GetSubscriptionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PatchSubscriptionRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PatchSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PostSubscriptionRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.subscriptions.models.post.PostSubscriptionResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class SubscriptionsController {
  @Autowired
  private SubscriptionsService subscriptionsService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostSubscriptionResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed() and isOwner(#subscriptionRequest.user_id)")
  @PostMapping("/subscriptions")
  public ResponseEntity<Object> createSubscription (
    Authentication userData,
    @RequestBody PostSubscriptionRequest subscriptionRequest
  ) {
    if (StringUtils.isEmpty(subscriptionRequest.getUser_id())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("user_id is empty"));
    }

    if (StringUtils.isEmpty(subscriptionRequest.getAsset_id())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("asset_id is empty"));
    }

    if (
      !UUIDUtils.isValidUUID(subscriptionRequest.getUser_id()) ||
      !UUIDUtils.isValidUUID(subscriptionRequest.getAsset_id())
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid request params"));
    }

    if (StringUtils.isEmpty(subscriptionRequest.getNotification_schedule())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("notification_schedule is empty"));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostSubscriptionResponse subscription = subscriptionsService.createSubscription(subscriptionRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(subscription);
    } catch (
      UserNotFoundException |
      AssetNotFoundException notFoundException
    ) {
      LoggerWrapper.error(notFoundException.getMessage() + ". error in POST /v1/subscriptions",
        notFoundException.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(notFoundException.getMessage()));
    } catch (InvalidCronExpressionException invalidCronExpressionException) {
      LoggerWrapper.error("Invalid cron expression " + subscriptionRequest.getNotification_schedule() + ". error in POST /v1/subscriptions:" + invalidCronExpressionException.getMessage(),
        invalidCronExpressionException.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid cron expression"));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Subscription with asset_id and user_id already exists. error in POST /v1/subscriptions: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Subscription with these parameters already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/subscriptions: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PatchSubscriptionResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed() and isOwner()")
  @PatchMapping("/subscriptions/{subscriptionId}")
  public ResponseEntity<Object> updateSubscription (
    Authentication userData,
    @PathVariable("subscriptionId") String subscriptionId,
    @RequestBody PatchSubscriptionRequest subscriptionRequest
  ) {
    try {
      if (!UUIDUtils.isValidUUID(subscriptionId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("invalid subscriptionId"));
      }

      if (StringUtils.isEmpty(subscriptionRequest.getNotification_schedule())) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("notification_schedule is empty"));
      }

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(subscriptionId);
      PatchSubscriptionResponse subscription = subscriptionsService.updateSubscription(uuid, subscriptionRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(subscription);
    } catch (SubscriptionNotFoundException notFoundException) {
      LoggerWrapper.error("Subscription not found. error in PATCH /v1/subscriptions/{subscriptionId} with subscriptionId " + subscriptionId+ ": " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Subscription not found"));
    } catch (InvalidCronExpressionException invalidCronExpressionException) {
      LoggerWrapper.error("Invalid cron expression " + subscriptionRequest.getNotification_schedule() + ". error in PATCH /v1/subscriptions/{subscriptionId} with subscriptionId " + subscriptionId+ ": " + invalidCronExpressionException.getMessage(),
        invalidCronExpressionException.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid request params"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/subscriptions/{subscriptionId} with subscriptionId " + subscriptionId+ ": " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetSubscriptionResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/subscriptions/{subscriptionId}")
  public ResponseEntity<Object> getSubscriptionById (
    @PathVariable("subscriptionId") String subscriptionId
  ) {
    try {
      if (!UUIDUtils.isValidUUID(subscriptionId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("invalid subscriptionId"));
      }

      UUID uuid = UUID.fromString(subscriptionId);
      GetSubscriptionResponse subscription = subscriptionsService.getSubscriptionById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(subscription);
    } catch (SubscriptionNotFoundException notFoundException) {
      LoggerWrapper.error("Subscription not found. error in GET /v1/subscriptions/{subscriptionId} with subscriptionId " + subscriptionId+ ": " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Subscription not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/subscriptions/{subscriptionId} with subscriptionId " + subscriptionId+ ": " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetSubscriptionsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/subscriptions")
  public ResponseEntity<Object> getSubscriptionsByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "asset_id", required = false) String assetId,
    @RequestParam(value = "user_id", required = false) String userId,
    @RequestParam(value = "asset_type_ids", required = false) List<UUID> assetTypeIds,
    @RequestParam(value = "lifecycle_status_ids", required = false) List<UUID> lifecycleStatusIds,
    @RequestParam(value = "stewardship_status_ids", required = false) List<UUID> stewardshipStatusIds,
    @RequestParam(value = "sort_field", required = false) SortField sortField,
    @RequestParam(value = "sort_order", required = false) SortOrder sortOrder
  ) {
    try {
      UUID assetUUID = null;
      if (StringUtils.isNotEmpty(assetId) && UUIDUtils.isValidUUID(assetId)) {
        assetUUID = UUID.fromString(assetId);
      }

      UUID userUUID = null;
      if (StringUtils.isNotEmpty(userId) && UUIDUtils.isValidUUID(userId)) {
        userUUID = UUID.fromString(userId);
      }

      GetSubscriptionsResponse subscriptions = subscriptionsService.getSubscriptionsByParams(
        assetUUID,
        userUUID,
        assetTypeIds,
        lifecycleStatusIds,
        stewardshipStatusIds,
        sortField,
        sortOrder,
        pageNumber,
        pageSize
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(subscriptions);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/subscriptions: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SuccessResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed() and isOwner()")
  @DeleteMapping("/subscriptions/{subscriptionId}")
  public ResponseEntity<Object> deleteSubscriptionById (
    Authentication userData,
    @PathVariable("subscriptionId") String subscriptionId
  ) {
    try {
      if (!UUIDUtils.isValidUUID(subscriptionId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("invalid subscriptionId"));
      }

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(subscriptionId);
      subscriptionsService.deleteSubscriptionById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Subscription was successfully deleted."));
    } catch (SubscriptionNotFoundException notFoundException) {
      LoggerWrapper.error("Subscription not found. error in DELETE /v1/subscriptions/{subscriptionId} with subscriptionId " + subscriptionId+ ": " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Subscription not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/subscriptions/{subscriptionId} with subscriptionId " + subscriptionId+ ": " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        SubscriptionsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
