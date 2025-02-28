package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorWithDetailsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.ErrorWithDetail;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AssetAlreadyHasAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeTypeNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.AttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.get.GetAttributesResponse;

@RestController
@RequestMapping("/v1")
public class AttributesController {
  @Autowired
  private AttributesService attributesService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAttributeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isAttributeTypeAllowed(#attributeRequest.attribute_type_id, false) or isAssetAllowed())")
  @PostMapping("/attributes")
  public ResponseEntity<Object> createAttribute (
    Authentication userData,
    @RequestBody PostAttributeRequest attributeRequest
  ) {
    if (
      attributeRequest.getAsset_id() == null ||
      StringUtils.isEmpty(attributeRequest.getValue()) ||
      StringUtils.isEmpty(attributeRequest.getAttribute_type_id())
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostAttributeResponse attribute = attributesService.createAttribute(attributeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(attribute);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in request. error in POST /v1/attributes: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request"));
    } catch (AssetNotFoundException assetNotFoundException) {
      LoggerWrapper.error("Asset '" + attributeRequest.getAsset_id() + "' not found in POST /v1/attributes",
        assetNotFoundException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset '" + attributeRequest.getAsset_id() + "' not found"));
    } catch (
      AttributeTypeNotAllowedException |
      AttributeInvalidDataTypeException |
      AttributeValueNotAllowedException |
      AttributeValueMaskValidationException exception) {
      LoggerWrapper.error("Error in POST /v1/attributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(exception.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("This asset already has this attribute. error in POST /v1/attributes: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("This asset already has this attribute."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/attributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAttributeResponse.class)))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isAttributeTypesInBulkAllowed(#attributeRequests, false) or isAssetsInBulkAllowed(#attributeRequests))")
  @PostMapping("/attributes/bulk")
  public ResponseEntity<Object> createAttributesBulk (
    Authentication userData,
    @RequestBody List<PostAttributeRequest> attributeRequests
  ) {
    if (attributeRequests.isEmpty()) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Empty request list"));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      List<PostAttributeResponse> attributes = attributesService.createAttributesBulk(attributeRequests, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(attributes);
    } catch (SomeRequiredFieldsAreEmptyException someRequiredFieldsAreEmptyException) {
      LoggerWrapper.error("Invalid field length in POST /v1/attributes/bulk: " + someRequiredFieldsAreEmptyException.getMessage(),
        someRequiredFieldsAreEmptyException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(someRequiredFieldsAreEmptyException.getMessage()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in request. error in POST /v1/attributes/bulk: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Invalid arguments in request"));
    } catch (DuplicateValueInRequestException duplicateValueInRequestException) {
      LoggerWrapper.error("Duplicates value in request. error in POST /v1/attributes/bulk: " + duplicateValueInRequestException.getMessage(),
        duplicateValueInRequestException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(duplicateValueInRequestException.getMessage(), duplicateValueInRequestException.getDetails()));
    } catch (AssetNotFoundException notFoundException) {
      LoggerWrapper.error("Asset not found in POST /v1/attributes/bulk",
        notFoundException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(notFoundException.getMessage(), notFoundException.getDetails()));
    } catch (AttributeTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Attribute type not found in POST /v1/attributes/bulk",
        notFoundException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(notFoundException.getMessage(), notFoundException.getDetails()));
    } catch (
      AttributeTypeNotAllowedException |
      AttributeInvalidDataTypeException |
      AttributeValueNotAllowedException |
      AttributeValueMaskValidationException exception) {
      LoggerWrapper.error("Error in POST /v1/attributes/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      ErrorWithDetail errorWithDetail = exception;

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(errorWithDetail.getMessage(), errorWithDetail.getDetails()));
    } catch (AssetAlreadyHasAttributeException dataIntegrityViolationException) {
      LoggerWrapper.error("This asset already has this attribute. error in POST /v1/attributes/bulk: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("This asset already has this attribute.", dataIntegrityViolationException.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/attributes/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PatchAttributeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @PatchMapping("/attributes/{attributeId}")
  public ResponseEntity<Object> updateAttribute (
    Authentication userData,
    @PathVariable(value = "attributeId") String attributeId,
    @RequestBody PatchAttributeRequest attributeRequest
  ) {
    if (StringUtils.isEmpty(attributeRequest.getValue())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Null value given."));
    }

    try {
      UUID uuid = UUID.fromString(attributeId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PatchAttributeResponse updatedAttribute = attributesService.updateAttribute(uuid, attributeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updatedAttribute);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Attribute '" + attributeId + "'not found. error in PATCH /v1/attributes/{attributeId}: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute '" + attributeId + "'not found"));
    } catch (AttributeNotFoundException attributeNotFoundException) {
      LoggerWrapper.error("Attribute '" + attributeId + "' not found in PATCH /v1/attributes",
        attributeNotFoundException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute '" + attributeId + "' not found"));
    } catch (
      AttributeTypeNotAllowedException |
      AttributeInvalidDataTypeException |
      AttributeValueNotAllowedException |
      AttributeValueMaskValidationException exception
    ) {
      LoggerWrapper.error("Error in PATCH /v1/attributes/{attributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(exception.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/attributes/{attributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PatchAttributeResponse.class)))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isUpdateBulkMethodAllowed(#attributesRequest, false) or isAssetsInBulkAllowed(#attributesRequest))")
  @PatchMapping("/attributes/bulk")
  public ResponseEntity<Object> updateAttributesBulk (
    Authentication userData,
    @RequestBody List<PatchBulkAttributeRequest> attributesRequest
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      List<PatchAttributeResponse> updatedAttributes = attributesService.updateAttributesBulk(attributesRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updatedAttributes);
    } catch (SomeRequiredFieldsAreEmptyException requiredFieldEmptyException) {
      LoggerWrapper.error("Some of required fields are empty error in PATCH /v1/attributes/bulk: " + requiredFieldEmptyException.getMessage(),
        requiredFieldEmptyException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(requiredFieldEmptyException.getMessage(), requiredFieldEmptyException.getDetails()));
    } catch (AttributeNotFoundException attributeNotFoundException) {
      LoggerWrapper.error("Attribute not found in PATCH /v1/attributes/bulk: " + attributeNotFoundException.getMessage(),
        attributeNotFoundException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(attributeNotFoundException.getMessage(), attributeNotFoundException.getDetails()));
    } catch (DuplicateValueInRequestException duplicateValueInRequestException) {
      LoggerWrapper.error(duplicateValueInRequestException.getMessage() + " in PATCH /v1/attributes/bulk",
        duplicateValueInRequestException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(duplicateValueInRequestException.getMessage(), duplicateValueInRequestException.getDetails()));
    } catch (
      AttributeTypeNotAllowedException |
      AttributeInvalidDataTypeException |
      AttributeValueNotAllowedException |
      AttributeValueMaskValidationException exception
    ) {
      LoggerWrapper.error("Error in PATCH /v1/attributes/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(exception.getMessage(), exception.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/attributes/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AttributeResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/attributes/{attributeId}")
  public ResponseEntity<Object> getAttributeById (
    @PathVariable(value = "attributeId") String attributeId
  ) {
    try {
      UUID uuid = UUID.fromString(attributeId);
      AttributeResponse attribute = attributesService.getAttributeById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(attribute);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid attribute type id in GET /v1/attributes/{attributeId} with attributeId " + attributeId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute not found"));
    } catch (AttributeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/attributes/{attributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAttributesResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/attributes")
  public ResponseEntity<Object> getAttributesByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "asset_id", required = false) String assetId,
    @RequestParam(value = "attribute_type_id", required = false) String attributeTypeId
  ) {
    try {
      UUID assetUUID = null;
      UUID attributeTypeUUID = null;

      if (StringUtils.isNotEmpty(assetId)) {
        assetUUID = UUID.fromString(assetId);
      }

      if (StringUtils.isNotEmpty(attributeTypeId)) {
        attributeTypeUUID = UUID.fromString(attributeTypeId);
      }

      GetAttributesResponse attributes = attributesService.getAttributesByParams(assetUUID, attributeTypeUUID, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(attributes);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid request params id in GET /v1/attributes:",
        illegalArgumentException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid request params"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/attributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
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
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @DeleteMapping("/attributes/{attributeId}")
  public ResponseEntity<Object> deleteAttributeById (
    Authentication userData,
    @PathVariable(value = "attributeId") String attributeId
  ) {
    try {
      UUID uuid = UUID.fromString(attributeId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      attributesService.deleteAttributeById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Attribute was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid attribute type id in DELETE /v1/attributes/{attributeId} with attributeId " + attributeId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute not found"));
    } catch (AttributeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Attribute not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/attributes/{attributeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SuccessResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isBulkMethodAllowed(#attributeRequests, false) or isAssetsIdsInBulkAllowed(#attributeRequests))")
  @DeleteMapping("/attributes/bulk")
  public ResponseEntity<Object> deleteAttributesBulk (
    Authentication userData,
    @RequestBody List<UUID> attributeRequests
  ) {
    if (attributeRequests.isEmpty()) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Empty request list"));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      attributesService.deleteAttributesBulk(attributeRequests, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Attributes were successfully deleted."));
    } catch (DuplicateValueInRequestException duplicateValueInRequestException) {
      LoggerWrapper.error(duplicateValueInRequestException.getMessage() + " in DELETE /v1/attributes/bulk",
        duplicateValueInRequestException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(duplicateValueInRequestException.getMessage(), duplicateValueInRequestException.getDetails()));
    } catch (AttributeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Requested attribute not found", notFoundException.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/attributes/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
