package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes;

import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.UUID;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeValueAlreadyAssignedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.AttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get.GetAttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get.GetAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PatchAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeResponse;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class AttributeTypesController {

  private final AttributeTypesService attributeTypesService;

  public AttributeTypesController (AttributeTypesService attributeTypesService) {
    this.attributeTypesService = attributeTypesService;
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAttributeTypeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/attributeTypes")
  public ResponseEntity<Object> createAttributeType (
    Authentication userData,
    @RequestBody PostAttributeTypeRequest attributeTypeRequest
  ) {
    if (
      attributeTypeRequest.getAttribute_type_kind() == null ||
      StringUtils.isEmpty(attributeTypeRequest.getAttribute_type_name())
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    }

    try {
      validateFieldsLength(
        attributeTypeRequest.getAttribute_type_name(),
        attributeTypeRequest.getAttribute_type_description(),
        attributeTypeRequest.getValidation_mask(),
        attributeTypeRequest.getRdm_table_id()
      );

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostAttributeTypeResponse attributeType = attributeTypesService.createAttributeType(attributeTypeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(attributeType);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in POST /v1/attributeTypes: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Attribute type '"+ attributeTypeRequest.getAttribute_type_name() +"' already exists. error in POST /v1/attributeTypes: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type '"+ attributeTypeRequest.getAttribute_type_name() +"' already exists"));
    } catch (AttributeTypeValueAlreadyAssignedException alreadyAssignedException) {
      LoggerWrapper.error("Identical names in attribute_type_allowed_values list. error in POST /v1/attributeTypes: " + alreadyAssignedException.getMessage(),
        alreadyAssignedException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Identical names in attribute_type_allowed_values list"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/attributeTypes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttributeTypeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAttributeTypeAllowed(#attributeTypeId)")
  @PatchMapping("/attributeTypes/{attributeTypeId}")
  public ResponseEntity<Object> updateAttributeType (
    Authentication userData,
    @RequestBody PatchAttributeTypeRequest attributeTypeRequest,
    @PathVariable(value = "attributeTypeId") String attributeTypeId
  )  {
    try {
      Optional<String> rdmTableId = OptionalUtils.getOptionalFromField(attributeTypeRequest.getRdm_table_id());
      Optional<String> validationMask = OptionalUtils.getOptionalFromField(attributeTypeRequest.getValidation_mask());
      Optional<String> attributeTypeName = OptionalUtils.getOptionalFromField(attributeTypeRequest.getAttribute_type_name());
      Optional<String> attributeTypeDescription = OptionalUtils.getOptionalFromField(attributeTypeRequest.getAttribute_type_description());

      if (
        attributeTypeRequest.getAttribute_kind() == null &&
        rdmTableId.isEmpty() &&
        validationMask.isEmpty() &&
        attributeTypeName.isEmpty() &&
        attributeTypeDescription.isEmpty()
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Empty request body"));
      }

      if (OptionalUtils.isEmpty(attributeTypeRequest.getAttribute_type_name())) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("'attribute_type_name' is not nullable"));
      }

      validateFieldsLength(
        attributeTypeName.orElse(null),
        attributeTypeDescription.orElse(null),
        validationMask.orElse(null),
        rdmTableId.orElse(null)
      );

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(attributeTypeId);
      AttributeTypeResponse updatedAttributeType = attributeTypesService.updateAttributeType(uuid, attributeTypeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updatedAttributeType);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in PATCH /v1/attributeTypes/{attributeTypeId}: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid attribute type id in PATCH /v1/attributeTypes/{attributeTypeId} with attributeTypeId " + attributeTypeId,
        illegalArgumentException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type not found"));
    } catch (AttributeTypeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type not found"));
    } catch (IncompatibleAttributeKindException incompatibleAttributeKindException) {
      LoggerWrapper.error(incompatibleAttributeKindException.getMessage(),
        incompatibleAttributeKindException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Incompatible attribute kind."));
    } catch (
      ValidationMaskCantBeAppliedException |
      AttributeDoesNotMatchTheMaskException validationMaskException
    ) {
      LoggerWrapper.error(validationMaskException.getMessage(),
        validationMaskException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(validationMaskException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Attribute type '"+ attributeTypeRequest.getAttribute_type_name() +"' already exists. error in PATCH /v1/attributeTypes/{attributeTypeId}: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type '"+ attributeTypeRequest.getAttribute_type_name() +"' already exists"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/attributeTypes/{attributeTypeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAttributeTypeResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAttributeTypeAllowed(#attributeTypeId)")
  @GetMapping("/attributeTypes/{attributeTypeId}")
  public ResponseEntity<Object> getAttributeTypeById (
    @PathVariable(value = "attributeTypeId") String attributeTypeId
  ) {
    try {
      UUID uuid = UUID.fromString(attributeTypeId);
      GetAttributeTypeResponse attributeType = attributeTypesService.getAttributeTypeById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(attributeType);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid attribute type id in GET /v1/attributeTypes/{attributeTypeId} with attributeTypeId " + attributeTypeId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type not found"));
    } catch (AttributeTypeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute type not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/attributeTypes/{attributeTypeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAttributeTypesResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/attributeTypes")
  public ResponseEntity<Object> getAttributeTypesByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "attribute_kind", required = false) AttributeKindType attributeKind,
    @RequestParam(value = "attribute_type_name", required = false) String attributeTypeName,
    @RequestParam(value = "attribute_type_description", required = false) String attributeTypeDescription
  ) {
    try {
      GetAttributeTypesResponse attributeTypes = attributeTypesService.getAttributeTypeByParams(attributeTypeName, attributeTypeDescription, attributeKind, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(attributeTypes);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/attributeTypes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
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
  @PreAuthorize("isAuthenticated() and isAttributeTypeAllowed(#attributeTypeId)")
  @DeleteMapping("/attributeTypes/{attributeTypeId}")
  public ResponseEntity<Object> deleteAttributeTypeById (
    Authentication userData,
    @PathVariable(value = "attributeTypeId") String attributeTypeId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(attributeTypeId);
      attributeTypesService.deleteAttributeTypeById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Attribute type was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in DELETE /v1/attributeTypes/{attributeTypeId} with attributeTypeId " + attributeTypeId,
        illegalArgumentException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested attribute type not found"));
    } catch (AttributeTypeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested attribute type not found"));
    } catch (
      AttributeWithAttributeTypeExistsException |
      RelationAttributeWithAttributeTypeExistsException |
      RelationComponentAttributeWithAttributeTypeExistsException attributeTypeExistsException
    ) {
      LoggerWrapper.error(attributeTypeExistsException.getMessage(),
        attributeTypeExistsException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(attributeTypeExistsException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/attributeTypes/{attributeTypeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  private void validateFieldsLength (
    String attributeTypeName,
    String attributeTypeDescription,
    String validationMask,
    String rdmTableId
  ) throws InvalidFieldLengthException {
    if (
      StringUtils.isNotEmpty(attributeTypeName) &&
      attributeTypeName.length() > 255
    ) {
      throw new InvalidFieldLengthException("attribute_type_name", 255);
    }

    if (
      StringUtils.isNotEmpty(attributeTypeDescription) &&
      attributeTypeDescription.length() > 512
    ) {
      throw new InvalidFieldLengthException("attribute_type_description", 512);
    }

    if (
      StringUtils.isNotEmpty(validationMask) &&
      validationMask.length() > 255
    ) {
      throw new InvalidFieldLengthException("validation_mask", 255);
    }

    if (
      StringUtils.isNotEmpty(rdmTableId) &&
      rdmTableId.length() > 255
    ) {
      throw new InvalidFieldLengthException("rdm_table_id", 255);
    }
  }
}
