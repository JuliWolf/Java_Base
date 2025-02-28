package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post.PostAllowedValueRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post.PostAllowedValueResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;

@RestController
@RequestMapping("/v1")
public class AttributeTypesAllowedValuesController {
  @Autowired
  private AttributeTypesAllowedValuesService attributeTypesAllowedValuesService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAllowedValueResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAttributeTypeAllowed(#allowedValueRequest.attribute_type_id)")
  @PostMapping("/attributeTypes/allowedValues")
  public ResponseEntity<Object> createAttributeTypeAllowedValue (
    Authentication userData,
    @RequestBody PostAllowedValueRequest allowedValueRequest
    ) {
    if (
      StringUtils.isEmpty(allowedValueRequest.getValue()) ||
      StringUtils.isEmpty(allowedValueRequest.getAttribute_type_id())
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    }

    try {
      if (allowedValueRequest.getValue().length() > 255) {
        throw new InvalidFieldLengthException("value", 255);
      }

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostAllowedValueResponse allowedValue = attributeTypesAllowedValuesService.createAttributeTypeAllowedValue(allowedValueRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(allowedValue);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in POST /v1/attributeTypes/allowedValues: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        AttributeTypesAllowedValuesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid attribute type id in POST /v1/attributeTypes/allowedValues with attributeTypeId " + allowedValueRequest.getAttribute_type_id(),
        illegalArgumentException.getStackTrace(),
        null,
        AttributeTypesAllowedValuesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type not found"));
    } catch (AttributeTypeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AttributeTypesAllowedValuesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type not found"));
    } catch (AttributeTypeDoesNotUseValueListException doesNotUseValueListException) {
      LoggerWrapper.error(doesNotUseValueListException.getMessage(),
        doesNotUseValueListException.getStackTrace(),
        null,
        AttributeTypesAllowedValuesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(doesNotUseValueListException.getMessage()));
    } catch (DataIntegrityViolationException integrityViolationException) {
      LoggerWrapper.error("Attribute type allowed value already assigned to attribute type. error in POST /v1/attributeTypes/allowedValues: " + integrityViolationException.getMessage(),
        integrityViolationException.getStackTrace(),
        null,
        AttributeTypesAllowedValuesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("This value is already assigned to this attribute_type."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/attributeTypes/allowedValues: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributeTypesAllowedValuesController.class.getName()
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
  @DeleteMapping("/attributeTypes/allowedValues/{attributeTypeAllowedValueId}")
  public ResponseEntity<Object> deleteAttributeTypeAllowedValueById (
    Authentication userData,
    @PathVariable(value = "attributeTypeAllowedValueId") String attributeTypeAllowedValueId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(attributeTypeAllowedValueId);
      attributeTypesAllowedValuesService.deleteAttributeTypeAllowedValueById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Attribute type value was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in DELETE /v1/attributeTypes/allowedValues/{attributeTypeAllowedValueId} with attributeTypeAllowedValueId " + attributeTypeAllowedValueId,
        illegalArgumentException.getStackTrace(),
        null,
        AttributeTypesAllowedValuesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested attribute type allowed value not found"));
    } catch (AttributeTypeAllowedValueNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AttributeTypesAllowedValuesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested attribute type allowed value not found"));
    } catch (
      AllowedValueIsUsedInAttributeException |
      AllowedValueIsUsedInRelationAttributeException |
      AllowedValueIsUsedInRelationComponentAttributeException isUsedException
    ) {
      LoggerWrapper.error(isUsedException.getMessage(),
        isUsedException.getStackTrace(),
        null,
        AttributeTypesAllowedValuesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(isUsedException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/attributeTypes/allowedValues/{attributeTypeAllowedValueId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributeTypesAllowedValuesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
