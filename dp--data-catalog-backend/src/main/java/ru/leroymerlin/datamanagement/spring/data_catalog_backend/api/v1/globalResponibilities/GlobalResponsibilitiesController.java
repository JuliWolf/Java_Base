package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.GlobalResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.GlobalResponsibilityResponsibleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get.GetGlobalResponsibilitiesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get.GetGlobalResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.CreateGlobalResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.PostGlobalResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class GlobalResponsibilitiesController {

  @Autowired
  private GlobalResponsibilitiesService globalResponsibilitiesService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostGlobalResponsibilityResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isRoleAllowed(#request.role_id)")
  @PostMapping("/globalResponsibilities")
  public ResponseEntity<Object> createGlobalResponsibility (
    Authentication userData,
    @RequestBody CreateGlobalResponsibilityRequest request
  ) {
    if (StringUtils.isEmpty(request.getRole_id())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Empty role_id"));
    }

    if (StringUtils.isEmpty(request.getResponsible_id())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Empty responsible_id"));
    }

    if (request.getResponsible_type() == null) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Empty responsible_type"));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostGlobalResponsibilityResponse globalResponsibility = globalResponsibilitiesService.createGlobalResponsibility(request, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(globalResponsibility);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in POST /v1/globalResponsibilities",
        illegalArgumentException.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request"));
    } catch (
      UserNotFoundException |
      RoleNotFoundException |
      GroupNotFoundException |
      GlobalResponsibilityResponsibleNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + ". error in  POST /v1/globalResponsibilities: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Global responsibility with such parameters already exists.: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Global responsibility with such parameters already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/globalResponsibilities: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetGlobalResponsibilitiesResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/globalResponsibilities")
  public ResponseEntity<Object> getGlobalResponsibilitiesByRoleAndTypeAndResponsible (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "role_id", required = false) String roleId,
    @RequestParam(value = "responsible_id", required = false) String responsibleId,
    @RequestParam(value = "responsible_type", required = false) String responsibleType,
    @RequestParam(value = "sort_field", required = false) SortField sortField,
    @RequestParam(value = "sort_order", required = false) SortOrder sortType
  ) {
    try {
      UUID roleUUID = null;
      UUID responsibleUUID = null;

      if (StringUtils.isNotEmpty(roleId)) {
        roleUUID = UUID.fromString(roleId);
      }

      if (StringUtils.isNotEmpty(responsibleId)) {
        responsibleUUID = UUID.fromString(responsibleId);
      }

      GetGlobalResponsibilitiesResponse globalResponsibilities = globalResponsibilitiesService.getGlobalResponsibilitiesByParams(
        roleUUID,
        responsibleUUID,
        responsibleType,
        sortField,
        sortType,
        pageNumber,
        pageSize
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(globalResponsibilities);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in GET /v1/globalResponsibilities",
        illegalArgumentException.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/globalResponsibilities: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetGlobalResponsibilityResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/globalResponsibilities/{globalResponsibilityId}")
  public ResponseEntity<Object> getGlobalResponsibilityById (
    @PathVariable(value = "globalResponsibilityId") String globalResponsibilityId
  ) {
    try {
      UUID uuid = UUID.fromString(globalResponsibilityId);
      GetGlobalResponsibilityResponse globalResponsibility = globalResponsibilitiesService.getGlobalResponsibilityById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(globalResponsibility);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid global responsibility id in GET /v1/globalResponsibilities/{globalResponsibility} with globalResponsibilityId " + globalResponsibilityId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested global responsibility not found"));
    } catch (GlobalResponsibilityNotFoundException responsibilityNotFoundException) {
      LoggerWrapper.error(responsibilityNotFoundException.getMessage(),
        responsibilityNotFoundException.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested global responsibility not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/globalResponsibilities{globalResponsibility}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
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
  @DeleteMapping("/globalResponsibilities/{globalResponsibilityId}")
  public ResponseEntity<Object> deleteGlobalResponsibilityById (
    Authentication userData,
    @PathVariable(value = "globalResponsibilityId") String globalResponsibilityId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(globalResponsibilityId);
      globalResponsibilitiesService.deleteGlobalResponsibilityById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Global responsibility was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid global responsibility id in DELETE /v1/globalResponsibilities/{globalResponsibilityId} with globalResponsibilityId " + globalResponsibilityId,
        illegalArgumentException.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested global responsibility not found"));
    } catch (GlobalResponsibilityNotFoundException globalResponsibilityNotFoundException) {
      LoggerWrapper.error(globalResponsibilityNotFoundException.getMessage(),
        globalResponsibilityNotFoundException.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested global responsibility not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/globalResponsibilities/{globalResponsibilityId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GlobalResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
