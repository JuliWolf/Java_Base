package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.AttributeTypeNotAllowedForRelationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.RelationAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get.GetRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get.GetRelationAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationNotFoundException;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class RelationAttributesController {
  @Autowired
  private RelationAttributesService relationAttributesService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRelationAttributeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAttributeTypeAllowed(#postRelationAttributeRequest.attribute_type_id)")
  @PostMapping("/relationAttributes")
  public ResponseEntity<Object> createRelationAttribute (
    Authentication userData,
    @RequestBody PostRelationAttributeRequest postRelationAttributeRequest
    ) {
    try {
      if (
        StringUtils.isEmpty(postRelationAttributeRequest.getValue()) ||
        StringUtils.isEmpty(postRelationAttributeRequest.getRelation_id()) ||
        StringUtils.isEmpty(postRelationAttributeRequest.getAttribute_type_id())
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Some of required fields are empty."));
      }

      if (
        !UUIDUtils.isValidUUID(postRelationAttributeRequest.getRelation_id()) ||
        !UUIDUtils.isValidUUID(postRelationAttributeRequest.getAttribute_type_id())
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Invalid request values."));
      }

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostRelationAttributeResponse relationAttribute = relationAttributesService.createRelationAttribute(postRelationAttributeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationAttribute);
    } catch (
      RelationNotFoundException |
      AttributeTypeNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + ". error in POST /v1/relationAttributes: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (AttributeTypeNotAllowedForRelationException attributeTypeNotAllowedForRelationException) {
      LoggerWrapper.error("This attribute type is not allowed to be used for this relation. error in POST /v1/relationAttributes: " + attributeTypeNotAllowedForRelationException.getMessage(),
        attributeTypeNotAllowedForRelationException.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("This attribute type is not allowed to be used for this relation."));
    } catch (
      AttributeTypeNotAllowedException |
      AttributeInvalidDataTypeException |
      AttributeValueNotAllowedException |
      AttributeValueMaskValidationException exception
    ) {
      LoggerWrapper.error("Error in POST /v1/relationAttributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(exception.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("This relation already has this attribute. error in POST /v1/relationAttributes: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("This relation already has this attribute."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/relationAttributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PatchRelationAttributeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Validation error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PatchMapping("/relationAttributes/{relationAttributeId}")
  public ResponseEntity<Object> updateRelationAttribute (
    Authentication userData,
    @PathVariable("relationAttributeId") String relationAttributeId,
    @RequestBody PatchRelationAttributeRequest patchRelationAttributeRequest
  ) {
    try {
      if (StringUtils.isEmpty(patchRelationAttributeRequest.getValue())) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Null value given."));
      }

      if (!UUIDUtils.isValidUUID(relationAttributeId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Relation attribute '" + relationAttributeId + "' not found."));
      }

      UUID uuid = UUID.fromString(relationAttributeId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PatchRelationAttributeResponse relationAttribute = relationAttributesService.updateRelationAttribute(uuid, patchRelationAttributeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationAttribute);
    } catch (RelationAttributeNotFoundException notFoundException) {
      LoggerWrapper.error("Relation attribute not found. error in PATCH /v1/relationAttributes/{relationAttributeId}: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation attribute '" + relationAttributeId + "' not found."));
    } catch (
      AttributeTypeNotAllowedException |
      AttributeInvalidDataTypeException |
      AttributeValueNotAllowedException |
      AttributeValueMaskValidationException exception
    ) {
      LoggerWrapper.error("Error in PATCH /v1/relationAttributes/{relationAttributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(exception.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error PATCH /v1/relationAttributes/{relationAttributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationAttributeResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/relationAttributes/{relationAttributeId}")
  public ResponseEntity<Object> getRelationAttributeById (
    @PathVariable("relationAttributeId") String relationAttributeId
  ) {
    try {
      if (!UUIDUtils.isValidUUID(relationAttributeId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Relation attribute '" + relationAttributeId + "' not found."));
      }

      UUID uuid = UUID.fromString(relationAttributeId);
      GetRelationAttributeResponse relationAttribute = relationAttributesService.getRelationAttributeById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationAttribute);
    } catch (RelationAttributeNotFoundException notFoundException) {
      LoggerWrapper.error("Relation attribute not found. error in GET /v1/relationAttributes/{relationAttributeId}: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation attribute '" + relationAttributeId + "' not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error GET /v1/relationAttributes/{relationAttributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationAttributesResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/relationAttributes")
  public ResponseEntity<Object> getRelationAttributesByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "relation_id", required = false) String relationId,
    @RequestParam(value = "attribute_type_ids", required = false) List<UUID> attributeTypeIds
  ) {
    try {
      UUID relationUUID = null;

      if (StringUtils.isNotEmpty(relationId) && UUIDUtils.isValidUUID(relationId)) {
        relationUUID = UUID.fromString(relationId);
      }

      GetRelationAttributesResponse relationAttributes = relationAttributesService.getRelationAttributesByParams(relationUUID, attributeTypeIds, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationAttributes);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error GET /v1/relationAttributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
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
  @DeleteMapping("/relationAttributes/{relationAttributeId}")
  public ResponseEntity<Object> deleteRelationAttributeById (
    Authentication userData,
    @PathVariable("relationAttributeId") String relationAttributeId
  ) {
    try {
      if (!UUIDUtils.isValidUUID(relationAttributeId)) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_NOT_FOUND)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Relation attribute '" + relationAttributeId + "' not found."));
      }

      UUID uuid = UUID.fromString(relationAttributeId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      relationAttributesService.deleteRelationAttributeById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Relation attribute was successfully deleted."));
    } catch (RelationAttributeNotFoundException notFoundException) {
      LoggerWrapper.error("Relation attribute not found. error in DELETE /v1/relationAttributes/{relationAttributeId}: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation attribute '" + relationAttributeId + "' not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error DELETE /v1/relationAttributes/{relationAttributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationAttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
