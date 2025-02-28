package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.UserGroupResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.get.GetUserGroupsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.post.PostUserGroupRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class UserGroupsController {

  @Autowired
  private UserGroupsService userGroupsService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserGroupResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/userGroups")
  public ResponseEntity<Object> createUserGroup (
    Authentication userData,
    @RequestBody PostUserGroupRequest userGroupRequest
  ) {
    if (StringUtils.isEmpty(userGroupRequest.getUser_id())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("user_id is empty"));
    }

    if (StringUtils.isEmpty(userGroupRequest.getGroup_id())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("group_id is empty"));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UserGroupResponse userGroup = userGroupsService.createUserGroup(userGroupRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(userGroup);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in POST /v1/userGroups",
        illegalArgumentException.getStackTrace(),
        null,
        UserGroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request"));
    } catch (UserNotFoundException | GroupNotFoundException notFoundException) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + ". error in  POST /v1/userGroups: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        UserGroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("User group assignment with given parameters already exists. error in POST /v1/userGroups: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        UserGroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_CONFLICT)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("User group assignment with given parameters already exists"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/userGroups: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UserGroupsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetUserGroupsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/userGroups")
  public ResponseEntity<Object> getUserGroupsByUserIdAndRoleId (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "user_id", required = false) String userId,
    @RequestParam(value = "role_id", required = false) String roleId
  ) {
    try {
      UUID userUUID = null;
      UUID roleUUID = null;

      if (StringUtils.isNotEmpty(userId)) {
        userUUID = UUID.fromString(userId);
      }

      if (StringUtils.isNotEmpty(roleId)) {
        roleUUID = UUID.fromString(roleId);
      }

      GetUserGroupsResponse groups = userGroupsService.getUserGroupsByUserIdAndRoleId(userUUID, roleUUID, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(groups);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in GET /v1/userGroups",
        illegalArgumentException.getStackTrace(),
        null,
        UserGroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/userGroups: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UserGroupsController.class.getName()
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
  @DeleteMapping("/userGroups/{userGroupId}")
  public ResponseEntity<Object> deleteUserGroupById (
    Authentication userData,
    @PathVariable(value = "userGroupId") String userGroupId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(userGroupId);
      userGroupsService.deleteUserGroupById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("User group was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid user group id in DELETE /v1/userGroups/{userGroupId} with userGroupId " + userGroupId,
        illegalArgumentException.getStackTrace(),
        null,
        UserGroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested user group not found"));
    } catch (UserGroupNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        UserGroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested user group not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/userGroups/{userGroupId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UserGroupsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
