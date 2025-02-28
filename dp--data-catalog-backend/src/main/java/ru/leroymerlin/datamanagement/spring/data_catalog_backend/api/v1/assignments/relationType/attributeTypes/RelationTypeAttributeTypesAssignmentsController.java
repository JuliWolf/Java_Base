package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.AttributeTypeIsUsedForRelationAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.RelationTypeAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypeAttributeTypesUsageCountParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypeAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypesAttributeTypesUsageCountResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;

/**
 * @author JuliWolf
 */

@RestController
@RequestMapping("/v1")
public class RelationTypeAttributeTypesAssignmentsController {

  @Autowired
  private RelationTypeAttributeTypesAssignmentsService relationTypeAttributeTypesAssignmentsService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRelationTypeAttributeTypesResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isRelationTypeAllowed(#relationTypeId)")
  @PostMapping("/assignments/relationType/{relationTypeId}/attribute_type/batch")
  public ResponseEntity<Object> createRelationTypeAttributeTypesAssignments (
    Authentication userData,
    @PathVariable(value = "relationTypeId") String relationTypeId,
    @RequestBody PostRelationTypeAttributeTypesRequest assignmentsRequest
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

      UUID uuid = UUID.fromString(relationTypeId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostRelationTypeAttributeTypesResponse assignmentsAttributeTypes = relationTypeAttributeTypesAssignmentsService.createRelationTypeAttributeTypesAssignments(uuid, assignmentsRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assignmentsAttributeTypes);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Illegal arguments in request. error in POST /v1/assignments/relationType/{relationTypeId}/attribute_type/batch",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid request params"));
    } catch (RelationTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Relation type " + relationTypeId + " not found. error in POST /v1/assignments/relationType/{relationTypeId}/attribute_type/batch: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type not found."));
    } catch (AttributeTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Attribute type not found. error in POST /v1/assignments/relationType/{relationTypeId}/attribute_type/batch: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type not found."));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Assignments already exists. error in POST /v1/assignments/relationType/{relationTypeId}/attribute_type/batch: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Such assignment already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/assignments/relationType/{relationTypeId}/attribute_type/batch: " + exception.getMessage(),
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
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationTypeAttributeTypesResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isRelationTypeAllowed(#relationTypeId)")
  @GetMapping("/assignments/relationType/{relationTypeId}/attribute_type")
  public ResponseEntity<Object> getRelationTypeAttributeTypesAssignmentsByRelationTypeId (
    @PathVariable(value = "relationTypeId") String relationTypeId
  ) {
    try {
      UUID uuid = UUID.fromString(relationTypeId);
      GetRelationTypeAttributeTypesResponse relationTypeAttributeTypesAssignments = relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesAssignmentsByRelationTypeId(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationTypeAttributeTypesAssignments);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Illegal arguments in request. error in GET /v1/assignments/relationType/{relationTypeId}/attribute_type",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid relation type id"));
    } catch (RelationTypeNotFoundException relationTypeNotFoundException) {
      LoggerWrapper.error("Relation type '" + relationTypeId + "' not found. error in GET /v1/assignments/relationType/{relationTypeId}/attribute_type",
        relationTypeNotFoundException.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assignments/relationType/{relationTypeId}/attribute_type: " + exception.getMessage(),
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
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationTypesAttributeTypesUsageCountResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/assignments/relationTypes/attributeTypes")
  public ResponseEntity<Object> getRelationTypeAttributeTypesWithUsageCount (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "relation_type_id", required = false) String relationTypeId,
    @RequestParam(value = "attribute_type_id", required = false) String attributeTypeId,
    @RequestParam(value = "sort_field", required = false) SortField sortField,
    @RequestParam(value = "sort_order", required = false) SortOrder sortOrder
  ) {
    try {
      GetRelationTypesAttributeTypesUsageCountResponse relationTypeAttributeTypes = relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(
        new GetRelationTypeAttributeTypesUsageCountParams(
          StringUtils.isNullOrEmpty(relationTypeId) ? null : UUID.fromString(relationTypeId),
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
        .body(relationTypeAttributeTypes);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Illegal arguments in request. error in GET /v1/assignments/relationTypes/attributeTypes",
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

      LoggerWrapper.error("Unexpected error in GET /v1/assignments/relationTypes/attributeTypes: " + exception.getMessage(),
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
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @DeleteMapping("/assignments/relationType/attributeType/{relationTypeAttributeTypeAssignmentId}")
  public ResponseEntity<Object> deleteRelationTypeAttributeTypeAssignment (
    Authentication userData,
    @PathVariable(value = "relationTypeAttributeTypeAssignmentId") String relationTypeAttributeTypeAssignmentId
  ) {
    try {
      UUID uuid = UUID.fromString(relationTypeAttributeTypeAssignmentId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      relationTypeAttributeTypesAssignmentsService.deleteRelationTypeAttributeTypeAssignment(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Relation type - attribute type assignment was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Illegal relation type assignment id '" + relationTypeAttributeTypeAssignmentId + "'. error in DELETE /v1/assignments/relationType/attributeType/{relationTypeAttributeTypeAssignment}",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested relation type attribute type assignment not found"));
    } catch (RelationTypeAttributeTypeAssignmentNotFound notFoundException) {
      LoggerWrapper.error("Requested relation type attribute type '" + relationTypeAttributeTypeAssignmentId + "' not found. error in DELETE /v1/assignments/relationType/attributeType/{relationTypeAttributeTypeAssignment}",
        notFoundException.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested relation type attribute type assignment not found"));
    } catch (AttributeTypeIsUsedForRelationAttributeException isUsedForRelationAttributeException) {
      LoggerWrapper.error("Requested relation type attribute type '" + relationTypeAttributeTypeAssignmentId + "' is used for relation attribute. error in DELETE /v1/assignments/relationType/attributeType/{relationTypeAttributeTypeAssignment}",
        isUsedForRelationAttributeException.getStackTrace(),
        null,
        RelationTypeAttributeTypesAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type is used for relation attribute"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/assignments/relationType/attributeType/{relationTypeAttributeTypeAssignment}: " + exception.getMessage(),
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
}
