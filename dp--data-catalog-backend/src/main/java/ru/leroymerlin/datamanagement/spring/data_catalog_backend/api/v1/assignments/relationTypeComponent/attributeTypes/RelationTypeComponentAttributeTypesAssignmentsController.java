package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.amazonaws.util.StringUtils;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.UUIDUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.RelationTypeAttributeTypesAssignmentsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.GetRelationTypeComponentAssetTypesUsageCountParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.GetRelationTypeComponentsAttributeTypesUsageCountResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.AttributeTypeIsUsedForRelationComponentAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.RelationTypeComponentAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.GetRelationTypeComponentAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class RelationTypeComponentAttributeTypesAssignmentsController {
  private final RelationTypeComponentAttributeTypesAssignmentsService relationTypeComponentAttributeTypesAssignmentsService;

  public RelationTypeComponentAttributeTypesAssignmentsController (
    RelationTypeComponentAttributeTypesAssignmentsService relationTypeComponentAttributeTypesAssignmentsService
  ) {
    this.relationTypeComponentAttributeTypesAssignmentsService = relationTypeComponentAttributeTypesAssignmentsService;
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRelationTypeComponentAttributeTypesResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes/batch")
  public ResponseEntity<Object> createRelationTypeComponentAttributeTypesAssignments (
    Authentication userData,
    @PathVariable(value = "relationTypeComponentId") String relationTypeComponentId,
    @RequestBody PostRelationTypeComponentAttributeTypesRequest assignmentsRequest
  ) {
    try {
      if (!UUIDUtils.isValidUUID(relationTypeComponentId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Relation type component not found."));
      }

      if (
        assignmentsRequest.getRelation_type_component_attribute_type_assignment() == null ||
        assignmentsRequest.getRelation_type_component_attribute_type_assignment().isEmpty()
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("No assignments given."));
      }

      UUID uuid = UUID.fromString(relationTypeComponentId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostRelationTypeComponentAttributeTypesResponse attributeTypesAssignments = relationTypeComponentAttributeTypesAssignmentsService.createRelationTypeComponentAttributeTypesAssignments(uuid, assignmentsRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(attributeTypesAssignments);
    } catch (RelationTypeComponentNotFoundException notFoundException) {
      LoggerWrapper.error("Relation type component " + relationTypeComponentId + " not found. error in POST /v1/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes/batch: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationTypeComponentAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type component not found."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Illegal arguments in request. error in POST /v1/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes/batch",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypeComponentAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid request params"));
    } catch (AttributeTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Attribute type not found. error in POST /v1/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes/batch: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationTypeComponentAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type not found."));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Assignments already exists. error in POST /v1/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes/batch: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        RelationTypeComponentAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Such assignment already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes/batch: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypeComponentAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationTypeComponentAttributeTypesResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes")
  public ResponseEntity<Object> getRelationTypeComponentAttributeTypesAssignments (
    @PathVariable(value = "relationTypeComponentId") String relationTypeComponentId
  ) {
    try {
      if (!UUIDUtils.isValidUUID(relationTypeComponentId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Relation type component not found."));
      }

      UUID uuid = UUID.fromString(relationTypeComponentId);
      GetRelationTypeComponentAttributeTypesResponse attributeTypesAssignments = relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesAssignments(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(attributeTypesAssignments);
    } catch (RelationTypeComponentNotFoundException notFoundException) {
      LoggerWrapper.error("Relation type component " + relationTypeComponentId + " not found. error in GET /v1/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationTypeComponentAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type component not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assignments/relationTypeComponent/{relationTypeComponentId}/attributeTypes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypeComponentAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationTypeComponentsAttributeTypesUsageCountResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/assignments/relationTypeComponents/attributeTypes")
  public ResponseEntity<Object> getRelationTypeComponentAttributeTypesWithUsageCount (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "attribute_type_id", required = false) String attributeTypeId,
    @RequestParam(value = "relation_type_component_id", required = false) String relationTypeComponentId,
    @RequestParam(value = "sort_field", required = false) SortField sortField,
    @RequestParam(value = "sort_order", required = false) SortOrder sortOrder
  ) {
    try {
      GetRelationTypeComponentsAttributeTypesUsageCountResponse relationTypeComponentAttributeTypesWithUsageCount = relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(
        new GetRelationTypeComponentAssetTypesUsageCountParams(
          StringUtils.isNullOrEmpty(relationTypeComponentId) ? null : UUID.fromString(relationTypeComponentId),
          StringUtils.isNullOrEmpty(attributeTypeId) ? null : UUID.fromString(attributeTypeId),
          pageSize,
          pageNumber,
          sortField,
          sortOrder
        )
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationTypeComponentAttributeTypesWithUsageCount);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Illegal arguments in request. error in GET /v1/assignments/relationTypeComponents/attributeTypes",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid params in request"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assignments/relationTypeComponents/attributeTypes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
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
  @DeleteMapping("/assignments/relationTypeComponent/attributeType/{relationTypeComponentAttributeTypeAssignment}")
  public ResponseEntity<Object> deleteRelationTypeComponentAttributeTypeAssignment (
    Authentication userData,
    @PathVariable(value = "relationTypeComponentAttributeTypeAssignment") String relationTypeComponentAttributeTypeAssignment
  ) {
    try {
      if (!UUIDUtils.isValidUUID(relationTypeComponentAttributeTypeAssignment)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Relation type component attribute type assignment not found."));
      }

      UUID uuid = UUID.fromString(relationTypeComponentAttributeTypeAssignment);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      relationTypeComponentAttributeTypesAssignmentsService.deleteRelationTypeComponentAttributeTypeAssignment(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Relation type component - attribute type assignment was successfully deleted."));
    } catch (RelationTypeComponentAttributeTypeAssignmentNotFound notFoundException) {
      LoggerWrapper.error("Requested relation type component attribute type assignment '" + relationTypeComponentAttributeTypeAssignment + "' not found. error in DELETE /v1/assignments/relationTypeComponent/attributeType/{relationTypeComponentAttributeTypeAssignment}",
        notFoundException.getStackTrace(),
        null,
        RelationTypeComponentAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested relation type attribute type assignment not found."));
    } catch (AttributeTypeIsUsedForRelationComponentAttributeException isUsedForRelationComponentAttributeException) {
      LoggerWrapper.error("Relation type component attribute type assignment '" + relationTypeComponentAttributeTypeAssignment + "' is used for relation component attribute. error in DELETE /v1/assignments/relationTypeComponent/attributeType/{relationTypeComponentAttributeTypeAssignment}",
        isUsedForRelationComponentAttributeException.getStackTrace(),
        null,
        RelationTypeComponentAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type is used for relation component attribute"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/assignments/relationTypeComponent/assetType/{relationTypeComponentAssetTypeAssignment}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypeComponentAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
