package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions;

import java.net.HttpURLConnection;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.actionTypes.ActionTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.entities.EntityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.exceptions.RoleActionNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.exceptions.RoleActionObjectNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.get.GetRoleActionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostRoleActionsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostRoleActionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class RoleActionsController {
  @Autowired
  private RoleActionsService roleActionsService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRoleActionsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isRoleAllowed(#roleActionsRequest.role_id)")
  @PostMapping("/roleActions")
  public ResponseEntity<Object> createRoleActions (
      Authentication userData,
      @RequestBody PostRoleActionsRequest roleActionsRequest
  ) {
    if (StringUtils.isEmpty(roleActionsRequest.getRole_id())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Role id is empty"));
    }

    if (
      roleActionsRequest.getPrivilege_allowed().isEmpty() &&
      roleActionsRequest.getPrivilege_not_allowed().isEmpty()
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Privileges are empty"));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostRoleActionsResponse roleActions = roleActionsService.createRoleActions(roleActionsRequest, userDetails.getUser());

      return ResponseEntity
          .ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(roleActions);
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Role action assignment with given parameters already exists. error in POST /v1/roleActions: " + dataIntegrityViolationException.getMessage(),
          dataIntegrityViolationException.getStackTrace(),
          null,
        RoleActionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Role action assignment with given parameters already exists"));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in POST /v1/roleActions:",
        illegalArgumentException.getStackTrace(),
        null,
        RoleActionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request"));
    } catch (
      RoleNotFoundException |
      EntityNotFoundException |
      ActionTypeNotFoundException |
      AssetTypeNotFoundException |
      AttributeTypeNotFoundException |
      RoleActionObjectNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + ". error in POST /v1/roleActions: " + notFoundException.getMessage(),
          notFoundException.getStackTrace(),
          null,
          RoleActionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/roleActions: " + exception.getMessage(),
          exception.getStackTrace(),
          null,
          AssetTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRoleActionsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/roleActions")
  public ResponseEntity<Object> getRoleActionsByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "role_id", required = false) String role_id,
    @RequestParam(value = "action_scope", required = false) String action_scope,
    @RequestParam(value = "action_type_id", required = false) String action_type_id,
    @RequestParam(value = "entity_type_id", required = false) String entity_type_id,
    @RequestParam(value = "object_type_id", required = false) String object_type_id,
    @RequestParam(value = "permission_type", required = false) String permission_type
  ) {
    try {
      UUID roleId = null;
      if (StringUtils.isNotEmpty(role_id)) {
        roleId = UUID.fromString(role_id);
      }

      ActionScopeType actionScope = null;
      if (StringUtils.isNotEmpty(action_scope)) {
        actionScope = ActionScopeType.valueOf(action_scope);
      }

      UUID actionTypeId = null;
      if (StringUtils.isNotEmpty(action_type_id)) {
        actionTypeId = UUID.fromString(action_type_id);
      }

      UUID entityTypeId = null;
      if (StringUtils.isNotEmpty(entity_type_id)) {
        entityTypeId = UUID.fromString(entity_type_id);
      }

      UUID objectTypeId = null;
      if (StringUtils.isNotEmpty(object_type_id)) {
        objectTypeId = UUID.fromString(object_type_id);
      }

      PermissionType permissionType = null;
      if (StringUtils.isNotEmpty(permission_type)) {
        permissionType = PermissionType.valueOf(permission_type);
      }

      GetRoleActionsResponse roleActions = roleActionsService.getRoleActionsByParams(
        roleId, entityTypeId, actionTypeId, actionScope, permissionType, objectTypeId, pageNumber, pageSize
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(roleActions);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in GET /v1/roleActions:",
        illegalArgumentException.getStackTrace(),
        null,
        RoleActionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/roleActions: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypesController.class.getName()
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
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @DeleteMapping("/roleActions/{roleActionId}")
  public ResponseEntity<Object> deleteRoleAction (
    Authentication userData,
    @PathVariable(value = "roleActionId") String roleActionId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(roleActionId);
      roleActionsService.deleteRoleActionById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Role action assignment was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role action id in request " + roleActionId,
        illegalArgumentException.getStackTrace(),
        null,
        RoleActionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested role_action not found"));
    } catch (RoleActionNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RoleActionsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested role_action not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/roleActions: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RoleActionsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
