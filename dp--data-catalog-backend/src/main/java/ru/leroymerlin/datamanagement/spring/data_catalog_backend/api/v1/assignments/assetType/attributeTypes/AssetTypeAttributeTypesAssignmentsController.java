package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AttributeTypeIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.GetAssetTypeAttributeTypeAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.GetAssetTypeAttributeTypesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributeTypesAssignmentsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class AssetTypeAttributeTypesAssignmentsController {

  private final AssetTypeAttributeTypesAssignmentsService assetTypeAttributeTypesAssignmentsService;

  public AssetTypeAttributeTypesAssignmentsController (
    AssetTypeAttributeTypesAssignmentsService assetTypeAttributeTypesAssignmentsService
  ) {
    this.assetTypeAttributeTypesAssignmentsService = assetTypeAttributeTypesAssignmentsService;
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAssetTypeAttributesAssignmentsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetTypeId)")
  @PostMapping("/assignments/assetType/{assetTypeId}/attribute_type/batch")
  public ResponseEntity<Object> createAssetTypeAttributeTypesAssignments (
    Authentication userData,
    @PathVariable(value = "assetTypeId") String assetTypeId,
    @RequestBody PostAssetTypeAttributeTypesAssignmentsRequest assignmentsRequest
  ) {
    try {
      if (
        assignmentsRequest.getAttribute_assignment() == null ||
        assignmentsRequest.getAttribute_assignment().isEmpty()
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("No assignments given."));
      }

      UUID uuid = UUID.fromString(assetTypeId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostAssetTypeAttributesAssignmentsResponse assignmentsAttributeTypes = assetTypeAttributeTypesAssignmentsService.createAssetTypeAttributeTypesAssignments(uuid, assignmentsRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assignmentsAttributeTypes);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset type id in POST /v1/assignments/assetType/{assetTypeId}/attribute_type/batch with assetTypeId " + assetTypeId,
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type not found."));
    } catch (
      AssetTypeNotFoundException |
      AttributeTypeNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + " in POST /v1/assignments/assetType/{assetTypeId}/attribute_type/batch",
        notFoundException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Assignments already exists. error in POST /v1/assignments/assetType/{assetTypeId}/attribute_type/batch: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Such assignment already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/assignments/assetType/{assetTypeId}/attribute_type/batch: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @Deprecated
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetTypeAttributeTypesAssignmentsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetTypeId)")
  @GetMapping("/assignments/assetType/{assetTypeId}/attribute_type")
  public ResponseEntity<Object> getAssetTypeAttributeTypesAssignmentsByAssetTypeId (
    @PathVariable(value = "assetTypeId") String assetTypeId
  ) {
    try {
      UUID uuid = UUID.fromString(assetTypeId);
      GetAssetTypeAttributeTypesAssignmentsResponse assignments = assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByAssetTypeId(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assignments);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset type id in GET /v1/assignments/assetType/{assetTypeId}/attribute_type with assetTypeId " + assetTypeId,
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type not found."));
    } catch (AssetTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + " in GET /v1/assignments/assetType/{assetTypeId}/attribute_type",
        notFoundException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assignments/assetType/{assetTypeId}/attribute_type: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetTypeAttributeTypeAssignmentsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/assignments/assetTypes/attributeTypes")
  public ResponseEntity<Object> getAssetTypeAttributeTypesAssignmentsByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "asset_type_id", required = false) String assetTypeId,
    @RequestParam(value = "attribute_type_id", required = false) String attributeTypeId,
    @RequestParam(value = "sort_field", required = false) SortField sortField,
    @RequestParam(value = "sort_order", required = false) SortOrder sortOrder
  ) {
    try {
      GetAssetTypeAttributeTypeAssignmentsResponse assignments = assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(
        assetTypeId,
        attributeTypeId,
        sortField,
        sortOrder,
        pageNumber,
        pageSize
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assignments);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid params in GET /v1/assignments/assetTypes/attributeTypes" + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid params"));
    } catch (
        AssetTypeNotFoundException |
        AttributeTypeNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + " in GET /v1/assignments/assetTypes/attributeTypes",
        notFoundException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assignments/assetTypes/attributeTypes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
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
  @DeleteMapping("/assignments/assetType/attributeType/{assetTypeAttributeTypeAssignmentId}")
  public ResponseEntity<Object> deleteAssetTypeAttributeTypeAssignmentById (
    Authentication userData,
    @PathVariable(value = "assetTypeAttributeTypeAssignmentId") String assetTypeAttributeTypeAssignmentId
  ) {
    try {
      UUID uuid = UUID.fromString(assetTypeAttributeTypeAssignmentId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      assetTypeAttributeTypesAssignmentsService.deleteAssetTypeAttributeTypeAssignmentById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Asset type - attribute type assignment was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset type id in DELETE /v1/assignments/assetType/attributeType/{assetTypeAttributeTypeAssignmentId} with assetTypeAttributeTypeAssignmentId " + assetTypeAttributeTypeAssignmentId,
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset_type_attribute_type assignment not found"));
    } catch (AssetTypeAttributeTypeAssignmentNotFoundException notFoundException) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + " in DELETE /v1/assignments/assetType/attributeType/{assetTypeAttributeTypeAssignmentId}",
        notFoundException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset_type_attribute_type assignment not found"));
    } catch (AssetTypeAttributeTypeAssignmentIsInheritedException inheritedException) {
      LoggerWrapper.error("Requested asset type attribute type assignment is inherited in DELETE /v1/assignments/assetType/attributeType/{assetTypeAttributeTypeAssignmentId}",
        inheritedException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("It is forbidden to delete inherited assignment."));
    } catch (AttributeTypeIsUsedForAssetException assetException) {
      LoggerWrapper.error("Asset is used in DELETE /v1/assignments/assetType/attributeType/{assetTypeAttributeTypeAssignmentId} " + assetException.getMessage(),
        assetException.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(assetException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/assignments/assetType/attributeType/{assetTypeAttributeTypeAssignmentId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
