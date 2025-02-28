package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes;

import java.net.HttpURLConnection;
import java.util.List;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.UUIDUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeTypeNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.AttributeTypeNotAllowedForRelationComponentException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.RelationComponentAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.get.GetRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.get.GetRelationComponentAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationComponentNotFoundException;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class RelationComponentAttributesController {
  @Autowired
  private RelationComponentAttributesService relationComponentAttributesService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRelationComponentAttributeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAttributeTypeAllowed(#postRelationComponentAttributeRequest.attribute_type_id)")
  @PostMapping("/relationComponentAttributes")
  public ResponseEntity<Object> createRelationComponentAttribute (
    Authentication userData,
    @RequestBody PostRelationComponentAttributeRequest postRelationComponentAttributeRequest
  ) {
    try {
      if (
        StringUtils.isEmpty(postRelationComponentAttributeRequest.getValue()) ||
        StringUtils.isEmpty(postRelationComponentAttributeRequest.getAttribute_type_id()) ||
        StringUtils.isEmpty(postRelationComponentAttributeRequest.getRelation_component_id())
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Some of required fields are empty."));
      }

      if (
        !UUIDUtils.isValidUUID(postRelationComponentAttributeRequest.getAttribute_type_id()) ||
        !UUIDUtils.isValidUUID(postRelationComponentAttributeRequest.getRelation_component_id())
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Invalid request values."));
      }

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostRelationComponentAttributeResponse relationComponentAttribute = relationComponentAttributesService.createRelationComponentAttribute(postRelationComponentAttributeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationComponentAttribute);
    } catch (
      AttributeTypeNotFoundException |
      RelationComponentNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + ". error in POST /v1/relationComponentAttributes: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (AttributeTypeNotAllowedForRelationComponentException attributeTypeNotAllowedForRelationComponentException) {
      LoggerWrapper.error("This attribute type is not allowed to be used for this relation. error in POST /v1/relationComponentAttributes: " + attributeTypeNotAllowedForRelationComponentException.getMessage(),
        attributeTypeNotAllowedForRelationComponentException.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("This attribute type is not allowed to be used for this relation component."));
    } catch (
      AttributeTypeNotAllowedException |
      AttributeInvalidDataTypeException |
      AttributeValueNotAllowedException |
      AttributeValueMaskValidationException exception
    ) {
      LoggerWrapper.error("Error in POST /v1/relationComponentAttributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(exception.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("This relation already has this attribute. error in POST /v1/relationComponentAttributes: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("This relation component already has this attribute."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/relationComponentAttributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PatchRelationComponentAttributeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PatchMapping("/relationComponentAttributes/{relationComponentAttributeId}")
  public ResponseEntity<Object> updateRelationComponentAttribute (
    Authentication userData,
    @PathVariable("relationComponentAttributeId") String relationComponentAttributeId,
    @RequestBody PatchRelationComponentAttributeRequest patchRelationAttributeRequest
  ) {
    try {
      if (StringUtils.isEmpty(patchRelationAttributeRequest.getValue())) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Null value given."));
      }

      if (!UUIDUtils.isValidUUID(relationComponentAttributeId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Relation component attribute '" + relationComponentAttributeId + "' not found."));
      }

      UUID uuid = UUID.fromString(relationComponentAttributeId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PatchRelationComponentAttributeResponse relationComponentAttribute = relationComponentAttributesService.updateRelationComponentAttribute(uuid, patchRelationAttributeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationComponentAttribute);
    } catch (RelationComponentAttributeNotFoundException notFoundException) {
      LoggerWrapper.error("Relation component attribute not found. error in PATCH /v1/relationComponentAttributes/{relationComponentAttributeId}: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation component attribute '" + relationComponentAttributeId + "' not found."));
    } catch (
      AttributeTypeNotAllowedException |
      AttributeInvalidDataTypeException |
      AttributeValueNotAllowedException |
      AttributeValueMaskValidationException exception
    ) {
      LoggerWrapper.error("Error in PATCH /v1/relationComponentAttributes/{relationComponentAttributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(exception.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error PATCH /v1/relationComponentAttributes/{relationComponentAttributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationComponentAttributeResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/relationComponentAttributes/{relationComponentAttributeId}")
  public ResponseEntity<Object> getRelationComponentAttributeById (
    @PathVariable("relationComponentAttributeId") String relationComponentAttributeId
  ) {
    try {
      if (!UUIDUtils.isValidUUID(relationComponentAttributeId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Relation component attribute '" + relationComponentAttributeId + "' not found."));
      }

      UUID uuid = UUID.fromString(relationComponentAttributeId);
      GetRelationComponentAttributeResponse relationComponentAttribute = relationComponentAttributesService.getRelationComponentAttributeById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationComponentAttribute);
    } catch (RelationComponentAttributeNotFoundException notFoundException) {
      LoggerWrapper.error("Relation component attribute not found. error in GET /v1/relationAttributes/{relationComponentAttributeId}: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation component attribute '" + relationComponentAttributeId + "' not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error GET /v1/relationAttributes/{relationComponentAttributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationComponentAttributesResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/relationComponentAttributes")
  public ResponseEntity<Object> getRelationComponentAttributesByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "attribute_type_ids", required = false) List<UUID> attributeTypeIds,
    @RequestParam(value = "relation_component_ids", required = false) List<UUID> relationComponentIds
    ) {
    try {
      GetRelationComponentAttributesResponse relationComponentAttributes = relationComponentAttributesService.getRelationComponentAttributesByParams(attributeTypeIds, relationComponentIds, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationComponentAttributes);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error GET /v1/relationComponentAttributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
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
  @DeleteMapping("/relationComponentAttributes/{relationComponentAttributeId}")
  public ResponseEntity<Object> deleteRelationComponentAttributeById (
    Authentication userData,
    @PathVariable("relationComponentAttributeId") String relationComponentAttributeId
  ) {
    try {
      if (!UUIDUtils.isValidUUID(relationComponentAttributeId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Relation component attribute '" + relationComponentAttributeId + "' not found."));
      }

      UUID uuid = UUID.fromString(relationComponentAttributeId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      relationComponentAttributesService.deleteRelationComponentAttributeById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Relation component attribute was successfully deleted."));
    } catch (RelationComponentAttributeNotFoundException notFoundException) {
      LoggerWrapper.error("Relation attribute not found. error in DELETE /v1/relationComponentAttributes/{relationComponentAttributeId}: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation component attribute '" + relationComponentAttributeId + "' not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error DELETE /v1/relationComponentAttributes/{relationComponentAttributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationComponentAttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
