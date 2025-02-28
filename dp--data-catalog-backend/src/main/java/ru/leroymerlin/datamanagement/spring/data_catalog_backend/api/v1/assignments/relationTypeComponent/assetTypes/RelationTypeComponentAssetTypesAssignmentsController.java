package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.AssetsTypeIsUsedInRelationsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentIsInherited;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.get.GetRelationTypeComponentAssetTypeAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;

/**
 * @author JuliWolf
 */

@RestController
@RequestMapping("/v1")
public class RelationTypeComponentAssetTypesAssignmentsController {
  private final RelationTypeComponentAssetTypesAssignmentsService relationTypeComponentAssetTypesAssignmentsService;

  public RelationTypeComponentAssetTypesAssignmentsController (RelationTypeComponentAssetTypesAssignmentsService relationTypeComponentAssetTypesAssignmentsService) {
    this.relationTypeComponentAssetTypesAssignmentsService = relationTypeComponentAssetTypesAssignmentsService;
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRelationTypeComponentAssetTypesResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/assignments/relationTypeComponent/{relationTypeComponentId}/asset_type/batch")
  public ResponseEntity<Object> createRelationTypeComponentAssetTypesAssignments (
    Authentication userData,
    @PathVariable(value = "relationTypeComponentId") String relationTypeComponentId,
    @RequestBody PostRelationTypeComponentAssetTypesRequest assignmentsRequest
  ) {
    try {
      if (
        assignmentsRequest.getAllowed_asset_type() == null ||
        assignmentsRequest.getAllowed_asset_type().isEmpty()
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("No assignments given."));
      }

      UUID uuid = UUID.fromString(relationTypeComponentId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostRelationTypeComponentAssetTypesResponse assetTypesAssignments = relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(uuid, assignmentsRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assetTypesAssignments);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Illegal arguments in request. error in POST /v1/assignments/relationTypeComponent/{relationTypeComponentId}/asset_type/batch",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid request params"));
    } catch (RelationTypeComponentNotFoundException notFoundException) {
      LoggerWrapper.error("Relation type component " + relationTypeComponentId + " not found. error in POST /v1/assignments/relationTypeComponent/{relationTypeComponentId}/asset_type/batch: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type component not found."));
    } catch (AssetTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Asset type not found. error in POST /v1/assignments/relationTypeComponent/{relationTypeComponentId}/asset_type/batch: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type not found."));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Assignments already exists. error in POST /v1/assignments/relationTypeComponent/{relationTypeComponentId}/asset_type/batch: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Such assignment already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/assignments/relationTypeComponent/{relationTypeComponentId}/asset_type/batch: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationTypeComponentAssetTypeAssignmentsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/assignments/relationTypeComponent/{relationTypeComponent}/assetType")
  public ResponseEntity<Object> getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentId (
    @PathVariable(value = "relationTypeComponent") String relationTypeComponent
  ) {
    try {
      UUID uuid = UUID.fromString(relationTypeComponent);
      GetRelationTypeComponentAssetTypeAssignmentsResponse relationTypeComponentAssetTypeAssignments = relationTypeComponentAssetTypesAssignmentsService.getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentId(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationTypeComponentAssetTypeAssignments);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Illegal arguments in request. error in GET /v1/assignments/relationTypeComponent/{relationTypeComponent}/assetType",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid relation type id"));
    } catch (RelationTypeComponentNotFoundException relationTypeNotFoundException) {
      LoggerWrapper.error("Relation type component '" + relationTypeComponent + "' not found. error in GET /v1/assignments/relationTypeComponent/{relationTypeComponent}/assetType",
        relationTypeNotFoundException.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type component not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assignments/relationTypeComponent/{relationTypeComponent}/assetType: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
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
  @DeleteMapping("/assignments/relationTypeComponent/assetType/{relationTypeComponentAssetTypeAssignment}")
  public ResponseEntity<Object> deleteRelationTypeComponentAssetTypeAssignment (
    Authentication userData,
    @PathVariable(value = "relationTypeComponentAssetTypeAssignment") String relationTypeComponentAssetTypeAssignment
  ) {
    try {
      UUID uuid = UUID.fromString(relationTypeComponentAssetTypeAssignment);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      relationTypeComponentAssetTypesAssignmentsService.deleteRelationTypeComponentAssetTypeAssignment(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Asset type - relation type component assignment was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Illegal relation type component assignment id '" + relationTypeComponentAssetTypeAssignment + "'. error in DELETE /v1/assignments/relationTypeComponent/assetType/{relationTypeComponentAssetTypeAssignment}",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type - relation type component not found."));
    } catch (RelationTypeComponentAssetTypeAssignmentNotFound notFoundException) {
      LoggerWrapper.error("Requested relation type component asset type '" + relationTypeComponentAssetTypeAssignment + "' not found. error in DELETE /v1/assignments/relationTypeComponent/assetType/{relationTypeComponentAssetTypeAssignment}",
        notFoundException.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested relation type attribute type assignment not found"));
    } catch (RelationTypeComponentAssetTypeAssignmentIsInherited isInherited) {
      LoggerWrapper.error("Relation type component asset type is inherited. error in DELETE /v1/assignments/relationTypeComponent/assetType/{relationTypeComponentAssetTypeAssignment}",
        isInherited.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("It is forbidden to deleted inherited assignments."));
    } catch (AssetsTypeIsUsedInRelationsException isUsedInRelationsException) {
      LoggerWrapper.error("This asset type is still used in some relations. error in DELETE /v1/assignments/relationTypeComponent/assetType/{relationTypeComponentAssetTypeAssignment}",
        isUsedInRelationsException.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("This asset type is still used in some relations."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/assignments/relationTypeComponent/assetType/{relationTypeComponentAssetTypeAssignment}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypeComponentAssetTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
