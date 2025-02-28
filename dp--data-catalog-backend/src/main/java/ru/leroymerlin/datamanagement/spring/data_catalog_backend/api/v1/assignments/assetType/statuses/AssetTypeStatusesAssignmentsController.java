package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.StatusTypeIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class AssetTypeStatusesAssignmentsController {

  @Autowired
  private AssetTypeStatusesAssignmentsService assetTypeStatusesAssignmentsService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAssetTypeStatusesAssignmentsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetTypeId)")
  @PostMapping("/assignments/assetType/{assetTypeId}/status/batch")
  public ResponseEntity<Object> createAssetTypeStatusesAssignments (
    Authentication userData,
    @PathVariable(value = "assetTypeId") String assetTypeId,
    @RequestBody PostAssetTypeStatusesAssignmentsRequest assignmentsRequest
  ) {
    try {
      if (
        assignmentsRequest.getStatus_assignment() == null ||
        assignmentsRequest.getStatus_assignment().isEmpty()
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("No assignments given."));
      }

      UUID uuid = UUID.fromString(assetTypeId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostAssetTypeStatusesAssignmentsResponse assignmentStatuses = assetTypeStatusesAssignmentsService.createAssetTypeStatusesAssignments(uuid, assignmentsRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assignmentStatuses);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset type id in POST /v1/assignments/assetType/{assetTypeId}/status/batch with assetTypeId " + assetTypeId,
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type not found."));
    } catch (
      StatusNotFoundException |
      AssetTypeNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + " in POST /v1/assignments/assetType/{assetTypeId}/status/batch",
        notFoundException.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Assignments already exists. error in POST /v1/assignments/assetType/{assetTypeId}/status/batch: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Such assignment already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/assignments/assetType/{assetTypeId}/status/batch: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetTypeStatusesAssignmentsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetTypeId)")
  @GetMapping("/assignments/assetType/{assetTypeId}/status")
  public ResponseEntity<Object> getAssetTypeStatusesAssignmentsByStatusType (
    @PathVariable(value = "assetTypeId") String assetTypeId,
    @RequestParam(value = "status_type", required = false) AssignmentStatusType statusType
  ) {
    try {
      UUID uuid = UUID.fromString(assetTypeId);
      GetAssetTypeStatusesAssignmentsResponse statuses = assetTypeStatusesAssignmentsService.getAssetTypeStatusesAssignmentsByParams(uuid, statusType);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(statuses);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset type id in GET /v1/assignments/assetType/{assetTypeId}/status with assetTypeId " + assetTypeId,
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type not found."));
    } catch (AssetTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + " in GET /v1/assignments/assetType/{assetTypeId}/status",
        notFoundException.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assignments/assetType/{assetTypeId}/status: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetTypeStatusesAssignmentsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/assignments/assetTypes/statuses")
  public ResponseEntity<Object> getAssetTypeStatusesByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "status_id", required = false) String statusId,
    @RequestParam(value = "status_type", required = false) AssignmentStatusType statusType,
    @RequestParam(value = "asset_type_id", required = false) String assetTypeId
  ) {
    try {
      UUID statusUUID = null;
      if (StringUtils.isNotEmpty(statusId)) {
        statusUUID = UUID.fromString(statusId);
      }

      UUID assetTypeUUID = null;
      if (StringUtils.isNotEmpty(assetTypeId)) {
        assetTypeUUID = UUID.fromString(assetTypeId);
      }

      GetAssetTypeStatusesResponse statuses = assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(statusUUID, statusType, assetTypeUUID, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(statuses);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid params in GET /v1/assignments/assetTypes/statuses",
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assignments/assetTypes/statuses: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SuccessResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @DeleteMapping("/assignments/assetType/status/{assetTypeStatusAssignmentId}")
  public ResponseEntity<Object> deleteAssetTypeStatusAssignmentById (
    Authentication userData,
    @PathVariable(value = "assetTypeStatusAssignmentId") String assetTypeStatusAssignmentId
  ) {
    try {
      UUID uuid = UUID.fromString(assetTypeStatusAssignmentId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      assetTypeStatusesAssignmentsService.deleteAssetTypeStatusAssignmentById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Asset type - status assignment was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset type id in DELETE /v1/assignments/assetType/status/{assetTypeStatusAssignmentId} with assetTypeStatusAssignmentId " + assetTypeStatusAssignmentId,
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset_type_status assignment not found"));
    } catch (AssetTypeStatusAssignmentNotFoundException notFoundException) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + " in DELETE /v1/assignments/assetType/status/{assetTypeStatusAssignmentId}",
        notFoundException.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset_type_status assignment not found"));
    } catch (AssetTypeStatusAssignmentIsInheritedException assetTypeStatusAssignmentIsInheritedException) {
      LoggerWrapper.error("Requested assignment is inherited in DELETE /v1/assignments/assetType/status/{assetTypeStatusAssignmentId}",
        assetTypeStatusAssignmentIsInheritedException.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("It is forbidden to delete inherited assignments."));
    } catch (StatusTypeIsUsedForAssetException assetException) {
      LoggerWrapper.error("Asset is used in DELETE /v1/assignments/assetType/status/{assetTypeStatusAssignmentId} " + assetException.getMessage(),
        assetException.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(assetException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/assignments/assetType/status/{assetTypeStatusAssignmentId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeStatusesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
