package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader;

/**
 * @author juliwolf
 */

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.exceptions.AttributeTypeNotAssignedForAssetTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post.PostAssetTypeCardHeaderAssignmentRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post.PostAssetTypeCardHeaderAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

@RestController
@RequestMapping("/v1")
public class AssetTypeCardHeaderAssignmentController {
  @Autowired
  private AssetTypeCardHeaderAssignmentService assetTypeCardHeaderAssignmentService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAssetTypeCardHeaderAssignmentResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetTypeId)")
  @PostMapping("/assignments/assetType/{assetTypeId}/assetCardHeader")
  public ResponseEntity<Object> createAssetTypeCardHeaderAssignment (
    Authentication userData,
    @PathVariable(value = "assetTypeId") String assetTypeId,
    @RequestBody PostAssetTypeCardHeaderAssignmentRequest assetTypeRequest
  ) {
    if (
      StringUtils.isEmpty(assetTypeRequest.getOwner_field_role_id()) &&
      StringUtils.isEmpty(assetTypeRequest.getDescription_field_attribute_type_id())
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Required fields are empty."));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID assetTypeUUID = UUID.fromString(assetTypeId);
      PostAssetTypeCardHeaderAssignmentResponse assetTypeCardHeaderAssignmentResponse = assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(assetTypeUUID, assetTypeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assetTypeCardHeaderAssignmentResponse);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Request error. error in POST /v1/assignments/assetTypes/{assetTypeId}/assetCardHeader: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypeCardHeaderAssignmentController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    } catch (AssetTypeNotFoundException assetTypeNotFoundException) {
      LoggerWrapper.error("Asset type with id '" + assetTypeRequest.getDescription_field_attribute_type_id() +"' does not exists. error in POST /v1/assignments/assetTypes/{assetTypeId}/assetCardHeader",
        assetTypeNotFoundException.getStackTrace(),
        null,
        AssetTypeCardHeaderAssignmentController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type not found."));
    } catch (RoleNotFoundException roleNotFoundException) {
      LoggerWrapper.error("Role with id '" + assetTypeRequest.getOwner_field_role_id() +"' does not exists. error in POST /v1/assignments/assetTypes/{assetTypeId}/assetCardHeader",
        roleNotFoundException.getStackTrace(),
        null,
        AssetTypeCardHeaderAssignmentController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Role not found."));
    } catch (AttributeTypeNotFoundException attributeTypeNotFoundException) {
      LoggerWrapper.error("Attribute type with id '" + assetTypeId +"' does not exists. error in POST /v1/assignments/assetTypes/{assetTypeId}/assetCardHeader",
        attributeTypeNotFoundException.getStackTrace(),
        null,
        AssetTypeCardHeaderAssignmentController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type not found."));
    } catch (AttributeTypeNotAssignedForAssetTypeException assignedForAssetTypeException) {
      LoggerWrapper.error(assignedForAssetTypeException.getMessage() + " error in POST /v1/assignments/assetTypes/{assetTypeId}/assetCardHeader",
        assignedForAssetTypeException.getStackTrace(),
        null,
        AssetTypeCardHeaderAssignmentController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(assignedForAssetTypeException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Card header assignment already exists for this asset type. error in POST /v1/assignments/assetTypes/{assetTypeId}/assetCardHeader: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AssetTypeCardHeaderAssignmentController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Card header assignment already exists for this asset type"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/assignments/assetTypes/{assetTypeId}/assetCardHeader: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeCardHeaderAssignmentController.class.getName()
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
  @DeleteMapping("/assignments/assetType/assetCardHeader/{assetTypeCardHeaderAssignmentId}")
  public ResponseEntity<Object> deleteAssetTypeCardHeaderAssignment (
    Authentication userData,
    @PathVariable(value = "assetTypeCardHeaderAssignmentId") String assetTypeCardHeaderAssignmentId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID assetTypeCardHeaderAssignmentUUID = UUID.fromString(assetTypeCardHeaderAssignmentId);
      assetTypeCardHeaderAssignmentService.deleteAssetTypeCardHeaderAssignment(assetTypeCardHeaderAssignmentUUID, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Asset type card header assignment was successfully deleted."));
    } catch (
      IllegalArgumentException |
      AssetTypeNotFoundException assetTypeNotFoundException
    ) {
      LoggerWrapper.error("Asset type card header assignment with id '" + assetTypeCardHeaderAssignmentId +"' not found. error in POST /v1/assignments/assetTypes/assetCardHeader/{assetTypeCardHeaderAssignmentId}",
        assetTypeNotFoundException.getStackTrace(),
        null,
        AssetTypeCardHeaderAssignmentController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type card header assignment not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/assignments/assetTypes/assetCardHeader/{assetTypeCardHeaderAssignmentId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeCardHeaderAssignmentController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
