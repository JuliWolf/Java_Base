package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.get.GetGroupResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.get.GetGroupsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupResponse;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class GroupsController {
  @Autowired
  private GroupsService groupsService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostGroupResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/groups")
  public ResponseEntity<Object> createGroup (
    Authentication userData,
    @RequestBody PostGroupRequest groupRequest
  ) {
    if (StringUtils.isEmpty(groupRequest.getGroup_name())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Group name is empty"));
    }

    try {
      validateFieldsLength(groupRequest);

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostGroupResponse group = groupsService.createGroup(groupRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(group);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in POST /v1/groups: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        GroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Group " + groupRequest.getGroup_name() + " already exists. error in POST /v1/groups: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        GroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Group " + groupRequest.getGroup_name() + " already exists"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/groups: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GroupsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetGroupsResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/groups")
  public ResponseEntity<Object> getGroupsByNameAndDescription (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "name", required = false) String name,
    @RequestParam(value = "description", required = false) String description
  ) {
    try {
      GetGroupsResponse groups = groupsService.getGroupsByParams(name, description, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(groups);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/groups: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GroupsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetGroupResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/groups/{groupId}")
  public ResponseEntity<Object> getGroupById (
    @PathVariable(value = "groupId") String groupId
  ) {
    try {
      UUID uuid = UUID.fromString(groupId);
      GetGroupResponse group = groupsService.getGroupById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(group);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in GET /v1/groups/{groupId} with groupId " + groupId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        GroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested group not found"));
    } catch (GroupNotFoundException groupNotFoundException) {
      LoggerWrapper.error(groupNotFoundException.getMessage(),
        groupNotFoundException.getStackTrace(),
        null,
        GroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested group not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/groups/{groupId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GroupsController.class.getName()
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
  @DeleteMapping("/groups/{groupId}")
  public ResponseEntity<Object> deleteGroupById (
    Authentication userData,
    @PathVariable(value = "groupId") String groupId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(groupId);
      groupsService.deleteGroupById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Group was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in DELETE /v1/group/{groupId} with groupId " + groupId,
        illegalArgumentException.getStackTrace(),
        null,
        GroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested group not found"));
    } catch (GroupNotFoundException groupNotFoundException) {
      LoggerWrapper.error(groupNotFoundException.getMessage(),
        groupNotFoundException.getStackTrace(),
        null,
        GroupsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested group not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error DELETE /v1/group/{groupId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GroupsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  private void validateFieldsLength (PostGroupRequest groupRequest) throws InvalidFieldLengthException {
    if (
      StringUtils.isNotEmpty(groupRequest.getGroup_name()) &&
      groupRequest.getGroup_name().length() > 255
    ) {
      throw new InvalidFieldLengthException("group_name", 255);
    }

    if (
      StringUtils.isNotEmpty(groupRequest.getGroup_description()) &&
      groupRequest.getGroup_description().length() > 255
    ) {
      throw new InvalidFieldLengthException("group_description", 255);
    }

    if (
      StringUtils.isNotEmpty(groupRequest.getGroup_messenger()) &&
      groupRequest.getGroup_messenger().length() > 255
    ) {
      throw new InvalidFieldLengthException("group_messenger", 255);
    }

    if (
      StringUtils.isNotEmpty(groupRequest.getGroup_email()) &&
      groupRequest.getGroup_email().length() > 255
    ) {
      throw new InvalidFieldLengthException("group_email", 255);
    }
  }
}
