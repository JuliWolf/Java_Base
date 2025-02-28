package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes;

import java.net.HttpURLConnection;
import java.util.Optional;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetNameValidationMaskDoesNotMatchExampleException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetNameValidationMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeHasChildAssetTypesException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypeChildrenResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PatchAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PostAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PostAssetTypeResponse;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class AssetTypesController {

  @Autowired
  private AssetTypesService assetTypesService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAssetTypeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/assetTypes")
  public ResponseEntity<Object> createAssetType (
      Authentication userData,
      @RequestBody PostAssetTypeRequest assetTypeRequest
  ) {
    if (
        StringUtils.isEmpty(assetTypeRequest.getAsset_type_name()) ||
        StringUtils.isEmpty(assetTypeRequest.getAsset_type_color()) ||
        StringUtils.isEmpty(assetTypeRequest.getAsset_type_acronym())
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    }

    try {
      validateFieldsLength(
        assetTypeRequest.getAsset_type_name(),
        assetTypeRequest.getAsset_type_description(),
        assetTypeRequest.getAsset_type_acronym(),
        assetTypeRequest.getAsset_type_color()
      );

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostAssetTypeResponse assetType = assetTypesService.createAssetType(assetTypeRequest, userDetails.getUser());

      return ResponseEntity
          .ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(assetType);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in POST /v1/assetTypes: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Request error. error in POST /v1/assetTypes: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    } catch (AssetTypeNotFoundException assetTypeNotFoundException) {
      LoggerWrapper.error("Asset type with id '"+ assetTypeRequest.getParent_asset_type_id() +"' does not exists. error in POST /v1/assetTypes",
        assetTypeNotFoundException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Parent asset type not found."));
    } catch (
      AssetNameValidationMaskValidationException |
      AssetNameValidationMaskDoesNotMatchExampleException validationException
    ) {
      LoggerWrapper.error("Asset type validation exception. error in POST /v1/assetTypes",
        validationException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(validationException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Asset type '"+ assetTypeRequest.getAsset_type_name() +"' already exists. error in POST /v1/assetTypes: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type '"+ assetTypeRequest.getAsset_type_name() +"' already exists"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/assetTypes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAssetTypeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetTypeId)")
  @PatchMapping("/assetTypes/{assetTypeId}")
  public ResponseEntity<Object> updateAssetType (
    Authentication userData,
    @RequestBody PatchAssetTypeRequest assetTypeRequest,
    @PathVariable(value = "assetTypeId") String assetTypeId
  )  {
    try {
      Optional<String> assetTypeName = OptionalUtils.getOptionalFromField(assetTypeRequest.getAsset_type_name());
      Optional<String> assetTypeDescription = OptionalUtils.getOptionalFromField(assetTypeRequest.getAsset_type_description());
      Optional<String> assetNameValidationMask = OptionalUtils.getOptionalFromField(assetTypeRequest.getAsset_name_validation_mask());
      Optional<String> assetNameValidationMaskExample = OptionalUtils.getOptionalFromField(assetTypeRequest.getAsset_name_validation_mask_example());

      if (
        assetTypeName.isEmpty() &&
        StringUtils.isEmpty(assetTypeRequest.getAsset_type_color()) &&
        StringUtils.isEmpty(assetTypeRequest.getAsset_type_acronym()) &&
        assetTypeDescription.isEmpty() &&
        assetNameValidationMask.isEmpty() &&
        assetNameValidationMaskExample.isEmpty()
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Empty request body"));
      }

      if (OptionalUtils.isEmpty(assetTypeRequest.getAsset_type_name())) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("'asset_type_name' is not nullable"));
      }

      validateFieldsLength(
        assetTypeName.orElse(null),
        assetTypeDescription.orElse(null),
        assetTypeRequest.getAsset_type_acronym(),
        assetTypeRequest.getAsset_type_color()
      );

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(assetTypeId);
      PostAssetTypeResponse updatedAssetType = assetTypesService.updateAssetType(uuid, assetTypeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updatedAssetType);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in PATCH /v1/assetTypes/{assetTypeId}: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in PATCH /v1/assetTypes/{assetTypeId} with assetTypeId " + assetTypeId,
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested role not found"));
    } catch (AssetTypeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested role not found"));
    } catch (
      AssetNameValidationMaskValidationException |
      AssetNameValidationMaskDoesNotMatchExampleException validationException
    ) {
      LoggerWrapper.error("Asset type validation exception. error in PATCH /v1/assetTypes/{assetTypeId}",
        validationException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(validationException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Asset type '"+ assetTypeRequest.getAsset_type_name() +"' already exists. error in PATCH /v1/assetTypes/{assetTypeId}: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type '"+ assetTypeRequest.getAsset_type_name() +"' already exists"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/assetTypes/{assetTypeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetTypesResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/assetTypes")
  public ResponseEntity<Object> getAssetTypeByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "root_flag", required = false) Boolean rootFlag,
    @RequestParam(value = "asset_type_name", required = false) String assetTypeName,
    @RequestParam(value = "asset_type_description", required = false) String assetTypeDescription
  ) {
    try {
      GetAssetTypesResponse assetTypes = assetTypesService.geAssetTypesByParams(rootFlag, assetTypeName, assetTypeDescription, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assetTypes);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assetTypes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetTypeResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetTypeId)")
  @GetMapping("/assetTypes/{assetTypeId}")
  public ResponseEntity<Object> getAssetTypeById (
    @PathVariable(value = "assetTypeId") String assetTypeId
  ) {
    try {
      UUID uuid = UUID.fromString(assetTypeId);
      GetAssetTypeResponse assetType = assetTypesService.getAssetTypeById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assetType);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in GET /v1/assetTypes/{assetTypeId} with assetTypeId " + assetTypeId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset type not found"));
    } catch (AssetTypeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset type not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assetTypes/{assetTypeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetTypeChildrenResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetTypeId)")
  @GetMapping("/assetTypes/{assetTypeId}/children")
  public ResponseEntity<Object> getAssetTypeChildren (
    @PathVariable(value = "assetTypeId") String assetTypeId,
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber
  ) {
    try {
      UUID uuid = UUID.fromString(assetTypeId);
      GetAssetTypeChildrenResponse assetTypeChildren = assetTypesService.getAssetTypeChildren(uuid, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assetTypeChildren);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in GET /v1/assetTypes/{assetTypeId}/children with assetTypeId " + assetTypeId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset type not found"));
    } catch (AssetTypeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset type not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assetTypes/{assetTypeId}/children: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypesController.class.getName()
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
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetTypeId)")
  @DeleteMapping("/assetTypes/{assetTypeId}")
  public ResponseEntity<Object> deleteAssetTypeById (
    Authentication userData,
    @PathVariable(value = "assetTypeId") String assetTypeId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(assetTypeId);
      assetTypesService.deleteAssetTypeById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Asset type was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in DELETE /v1/assetTypes/{assetTypeId} with assetTypeId " + assetTypeId,
        illegalArgumentException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset type not found"));
    } catch (AssetTypeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset type not found"));
    } catch (AssetTypeHasChildAssetTypesException assetTypeHasChildAssetTypesException) {
      LoggerWrapper.error(assetTypeHasChildAssetTypesException.getMessage(),
        assetTypeHasChildAssetTypesException.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(assetTypeHasChildAssetTypesException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/assetTypes/{assetTypeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  private void validateFieldsLength (
    String assetTypeName,
    String assetTypeDescription,
    String assetTypeAcronym,
    String assetTypeColor
  ) throws InvalidFieldLengthException {
    if (
      StringUtils.isNotEmpty(assetTypeName) &&
      assetTypeName.length() > 255
    ) {
      throw new InvalidFieldLengthException("asset_type_name", 255);
    }

    if (
      StringUtils.isNotEmpty(assetTypeDescription) &&
      assetTypeDescription.length() > 512
    ) {
      throw new InvalidFieldLengthException("asset_type_description", 512);
    }

    if (
      StringUtils.isNotEmpty(assetTypeAcronym) &&
      assetTypeAcronym.length() > 10
    ) {
      throw new InvalidFieldLengthException("asset_type_acronym", 10);
    }

    if (
      StringUtils.isNotEmpty(assetTypeColor) &&
      assetTypeColor.length() > 10
    ) {
      throw new InvalidFieldLengthException("asset_type_color", 10);
    }
  }
}
