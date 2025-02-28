package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorWithDetailsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNameAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNameDoesNotMatchPatternException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.AssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.ChildrenSortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.GetAssetHeaderResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostAssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class AssetsController {
  private final AssetsService assetsService;

  public AssetsController (AssetsService assetsService) {
    this.assetsService = assetsService;
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAssetResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetRequest.asset_type_id)")
  @PostMapping("/assets")
  public ResponseEntity<Object> createAsset (
    Authentication userData,
    @RequestBody PostOrPatchAssetRequest assetRequest
  ) {
    if (
      StringUtils.isEmpty(assetRequest.getAsset_name()) ||
      StringUtils.isEmpty(assetRequest.getAsset_type_id()) ||
      StringUtils.isEmpty(assetRequest.getAsset_displayname())
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    }

    try {
      if (assetRequest.getAsset_name().length() > 255) {
        throw new InvalidFieldLengthException("asset_name", 255);
      }

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostAssetResponse asset = assetsService.createAsset(assetRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(asset);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in POST /v1/assets: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (IllegalArgumentException | StatusNotFoundException exception) {
      LoggerWrapper.error("Invalid params in POST /v1/assets/",
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    } catch (AssetTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Asset type '" + assetRequest.getAsset_type_id() + "' not found in POST /v1/assets",
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type '" + assetRequest.getAsset_type_id() + "' not found."));
    } catch (AssetNameDoesNotMatchPatternException assetNameDoesNotMatchPatternException) {
      LoggerWrapper.error("Asset name does not match pattern in POST /v1/assets",
        assetNameDoesNotMatchPatternException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(assetNameDoesNotMatchPatternException.getMessage()));
    } catch (AssetTypeStatusAssignmentNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage() + " in POST /v1/assets",
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(notFoundException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Asset already exists. error in POST /v1/assets: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset '" + assetRequest.getAsset_name() + "' already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/assets: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostAssetResponse.class)))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypesInBulkAllowed(#assetsRequest)")
  @PostMapping("/assets/bulk")
  public ResponseEntity<Object> createAssetsBulk (
    Authentication userData,
    @RequestBody List<PostOrPatchAssetRequest> assetsRequest
  ) {
    if (assetsRequest.isEmpty()) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Empty request list"));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      List<PostAssetResponse> assets = assetsService.createAssetsBulk(assetsRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assets);
    } catch (InvalidFieldLengthException exception) {
      LoggerWrapper.error("Invalid field length in POST /v1/assets/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(exception.getMessage()));
    } catch (SomeRequiredFieldsAreEmptyException exception) {
      LoggerWrapper.error("Some required fields are empty in POST /v1/assets/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(exception.getMessage(), exception.getDetails()));
    } catch (IllegalArgumentException exception) {
      LoggerWrapper.error("Invalid params in POST /v1/assets/bulk",
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Request error"));
    } catch (DuplicateValueInRequestException exception) {
      LoggerWrapper.error("Duplicate value in request in POST /v1/assets/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(exception.getMessage(), exception.getDetails()));
    } catch (AssetTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Asset type not found in POST /v1/assets/bulk",
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(notFoundException.getMessage(), notFoundException.getDetails()));
    } catch (AssetNameDoesNotMatchPatternException assetNameDoesNotMatchPatternException) {
      LoggerWrapper.error("Asset name does not match pattern in POST /v1/assets/bulk",
        assetNameDoesNotMatchPatternException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(assetNameDoesNotMatchPatternException.getMessage(), assetNameDoesNotMatchPatternException.getDetails()));
    } catch (AssetTypeStatusAssignmentNotFoundException assignmentNotFoundException) {
      LoggerWrapper.error(assignmentNotFoundException.getMessage() + " in POST /v1/assets/bulk",
        assignmentNotFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(assignmentNotFoundException.getMessage(), assignmentNotFoundException.getDetails()));
    } catch (AssetNameAlreadyExistsException dataIntegrityViolationException) {
      LoggerWrapper.error("Asset already exists. error in POST /v1/assets/bulk: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(dataIntegrityViolationException.getMessage(), dataIntegrityViolationException.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/assets/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AssetResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @PatchMapping("/assets/{assetId}")
  public ResponseEntity<Object> updateAsset (
    Authentication userData,
    @PathVariable(value = "assetId") String assetId,
    @RequestBody PostOrPatchAssetRequest assetRequest
  ) {
    if (
      StringUtils.isEmpty(assetRequest.getAsset_name()) &&
      StringUtils.isEmpty(assetRequest.getAsset_type_id()) &&
      StringUtils.isEmpty(assetRequest.getLifecycle_status()) &&
      StringUtils.isEmpty(assetRequest.getAsset_displayname()) &&
      StringUtils.isEmpty(assetRequest.getStewardship_status())
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    }

    try {
      if (
        StringUtils.isNotEmpty(assetRequest.getAsset_name()) &&
        assetRequest.getAsset_name().length() > 255
      ) {
        throw new InvalidFieldLengthException("asset_name", 255);
      }

      UUID uuid = UUID.fromString(assetId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      AssetResponse updatedAsset = assetsService.updateAsset(uuid, assetRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updatedAsset);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in PATCH /v1/assets/{assetId}: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset type id in PATCH /v1/assets/{assetId} with assetId " + assetId,
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request asset not found"));
    } catch (AssetNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request asset not found"));
    } catch (AssetTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Asset type '" + assetRequest.getAsset_type_id() + "' not found in PATCH /v1/assets/",
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type '" + assetRequest.getAsset_type_id() + "' not found."));
    } catch (AssetNameDoesNotMatchPatternException assetNameDoesNotMatchPatternException) {
      LoggerWrapper.error("Asset name does not match pattern in PATCH /v1/assets",
        assetNameDoesNotMatchPatternException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(assetNameDoesNotMatchPatternException.getMessage()));
    } catch (AssetTypeStatusAssignmentNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage() + " in PATCH /v1/assets/{assetId}",
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(notFoundException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Asset already exists. error in PATCH /v1/assets/{assetId}: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset '" + assetRequest.getAsset_name() + "' already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/assets/{assetId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AssetResponse.class)))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isUpdateBulkMethodAllowed(#assetsRequest, false) or isAssetsInBulkAllowed(#assetsRequest))")
  @PatchMapping("/assets/bulk")
  public ResponseEntity<Object> updateBulkAsset (
    Authentication userData,
    @RequestBody List<PatchAssetRequest> assetsRequest
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      List<AssetResponse> updatedAssets = assetsService.updateBulkAsset(assetsRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updatedAssets);
    } catch (SomeRequiredFieldsAreEmptyException requiredFieldEmptyException) {
      LoggerWrapper.error("Some of required fields are empty error in PATCH /v1/assets/bulk: " + requiredFieldEmptyException.getMessage(),
        requiredFieldEmptyException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(requiredFieldEmptyException.getMessage(), requiredFieldEmptyException.getDetails()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Some fields has wrong type in PATCH /v1/assets/bulk: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Request error"));
    } catch (InvalidFieldLengthException exception) {
      LoggerWrapper.error("Invalid field length in PATCH /v1/assets/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(exception.getMessage(), exception.getDetails()));
    } catch (DuplicateValueInRequestException duplicateValueInRequestException) {
      LoggerWrapper.error("Duplicate value in request in PATCH /v1/assets/bulk: " + duplicateValueInRequestException.getMessage(),
        duplicateValueInRequestException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(duplicateValueInRequestException.getMessage(), duplicateValueInRequestException.getDetails()));
    } catch (AssetNotFoundException assetNotFoundException) {
      LoggerWrapper.error("Asset not found in PATCH /v1/assets/bulk: " + assetNotFoundException.getMessage(),
        assetNotFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(assetNotFoundException.getMessage(), assetNotFoundException.getDetails()));
    } catch (AssetTypeNotFoundException assetTypeNotFoundException) {
      LoggerWrapper.error("Asset type not found in PATCH /v1/assets/bulk: " + assetTypeNotFoundException.getMessage(),
        assetTypeNotFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(assetTypeNotFoundException.getMessage(), assetTypeNotFoundException.getDetails()));
    } catch (AssetNameDoesNotMatchPatternException assetNameDoesNotMatchPatternException) {
      LoggerWrapper.error("Asset name does not match pattern in PATCH /v1/assets/bulk",
        assetNameDoesNotMatchPatternException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(assetNameDoesNotMatchPatternException.getMessage(), assetNameDoesNotMatchPatternException.getDetails()));
    } catch (StatusNotFoundException statusNotFoundException) {
      LoggerWrapper.error("Status not found in PATCH /v1/assets/bulk: " + statusNotFoundException.getMessage(),
        statusNotFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(statusNotFoundException.getMessage(), statusNotFoundException.getDetails()));
    } catch (AssetTypeStatusAssignmentNotFoundException assignmentNotFoundException) {
      LoggerWrapper.error(assignmentNotFoundException.getMessage() + " in PATCH /v1/assets/bulk",
        assignmentNotFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(assignmentNotFoundException.getMessage(), assignmentNotFoundException.getDetails()));
    } catch (AssetNameAlreadyExistsException assetNameAlreadyExistsException) {
      LoggerWrapper.error(assetNameAlreadyExistsException.getMessage() + " in PATCH /v1/assets/bulk",
        assetNameAlreadyExistsException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(assetNameAlreadyExistsException.getMessage(), assetNameAlreadyExistsException.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/assets/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = AssetResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/assets/{assetId}")
  public ResponseEntity<Object> getAssetById (
    @PathVariable (value = "assetId") String assetId
  ) {
    try {
      UUID uuid = UUID.fromString(assetId);
      AssetResponse asset = assetsService.getAssetById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(asset);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset id in GET /v1/assets/{assetId} with assetId " + assetId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset not found"));
    } catch (AssetNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assets/{assetId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetsChildrenResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/assets/{assetId}/children")
  public ResponseEntity<Object> getAssetsChildren (
    @PathVariable (value = "assetId") String assetId,
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "sort_order", required = false) SortOrder sortOrder,
    @RequestParam(value = "sort_field", required = false) ChildrenSortField sortField,
    @RequestParam(value = "asset_type_ids", required = false) List<UUID> assetTypeIds,
    @RequestParam(value = "asset_displayname", required = false) String assetDisplayname,
    @RequestParam(value = "lifecycle_status_ids", required = false) List<UUID> lifecycleStatusIds,
    @RequestParam(value = "stewardship_status_ids", required = false) List<UUID> stewardshipStatusId

  ) {
    try {
      UUID uuid = UUID.fromString(assetId);
      GetAssetsChildrenResponse children = assetsService.getAssetsChildren(
        uuid,
        assetDisplayname,
        assetTypeIds,
        lifecycleStatusIds,
        stewardshipStatusId,
        sortField,
        sortOrder,
        pageNumber,
        pageSize
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(children);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in GET /v1/assets/{assetId}/children:",
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assets/{assetId}/children: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetAttributeLinksUsageResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/assets/{assetId}/links")
  public ResponseEntity<Object> getAssetAttributeLinkUsage (
    @PathVariable (value = "assetId") String assetId,
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "asset_type_ids", required = false) List<UUID> assetTypeIds,
    @RequestParam(value = "attribute_type_ids", required = false) List<UUID> attributeTypeIds,
    @RequestParam(value = "lifecycle_status_id", required = false) List<UUID> lifecycleStatusIds,
    @RequestParam(value = "stewardship_status_id", required = false) List<UUID> stewardshipStatusIds
  ) {
    try {
      UUID uuid = UUID.fromString(assetId);
      GetAssetAttributeLinksUsageResponse response = assetsService.getAssetAttributeLinkUsage(
        uuid,
        assetTypeIds,
        attributeTypeIds,
        lifecycleStatusIds,
        stewardshipStatusIds,
        pageNumber,
        pageSize
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset id in GET /v1/assets/{assetId}/links with assetId " + assetId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assets/{assetId}/links: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetPathElementsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/assets/{assetId}/path")
  public ResponseEntity<Object> getAssetPathElements (
    @PathVariable (value = "assetId") String assetId
  ) {
    try {
      UUID uuid = UUID.fromString(assetId);
      GetAssetPathElementsResponse response = assetsService.getAssetPath(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset id in GET /v1/assets/{assetId}/path with assetId " + assetId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assets/{assetId}/children: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetHeaderResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/assets/{assetId}/header")
  public ResponseEntity<Object> getAssetHeader (
    Authentication userData,
    @PathVariable (value = "assetId") String assetId
  ) {
    try {
      UUID uuid = UUID.fromString(assetId);
      GetAssetHeaderResponse response = assetsService.getAssetHeader(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
    } catch (IllegalArgumentException | AssetNotFoundException illegalArgumentException) {
      LoggerWrapper.error("Asset not found in GET /v1/assets/{assetId}/path with assetId " + assetId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assets/{assetId}/children: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/assets")
  public ResponseEntity<Object> getAssetByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "asset_name", required = false) String assetName,
    @RequestParam(value = "asset_displayname", required = false) String assetDisplayName,
    @RequestParam(value = "search_mode", required = false) AssetSearchMode assetSearchMode,
    @RequestParam(value = "search_any_flag", required = false) Boolean isSearchAny,
    @RequestParam(value = "asset_type_ids", required = false) List<UUID> assetTypeIds,
    @RequestParam(value = "lifecycle_statuses", required = false) List<UUID> lifecycleStatuses,
    @RequestParam(value = "stewardship_statuses", required = false) List<UUID> stewardshipStatuses,
    @RequestParam(value = "root_flag", required = false) Boolean rootFlag,
    @RequestParam(value = "sort_field", required = false) SortField sortField,
    @RequestParam(value = "sort_order", required = false) SortOrder sortOrder
  ) {
    try {
      GetAssetsResponse assets = assetsService.getAssetsByParams(
        new GetAssetParams(
          assetName,
          assetDisplayName,
          assetSearchMode,
          isSearchAny,
          assetTypeIds,
          lifecycleStatuses,
          stewardshipStatuses,
          rootFlag,
          sortField,
          sortOrder,
          pageNumber,
          pageSize
        )
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(assets);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assets: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetRelationTypes.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/assets/{assetId}/relationTypes")
  public ResponseEntity<Object> getAssetRelationTypes (
    @PathVariable (value = "assetId") String assetId
  ) {
    try {
      UUID uuid = UUID.fromString(assetId);

      GetAssetRelationTypes relationTypes = assetsService.getAssetRelationTypes(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationTypes);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset id in GET /v1/assets/{assetId}/relationTypes:",
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assets/{assetId}/relationTypes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetChangeHistory.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/assets/{assetId}/changeHistory")
  public ResponseEntity<Object> getAssetChangeHistory (
    @PathVariable (value = "assetId") String assetId,
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "user_ids", required = false) List<UUID> userIds,
    @RequestParam(value = "action_types", required = false) List<AssetHistoryActionType> actionTypes,
    @RequestParam(value = "entity_types", required = false) List<AssetHistoryEntityType> entityTypes,
    @RequestParam(value = "logged_on_min", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date minDate,
    @RequestParam(value = "logged_on_max", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date maxDate
  ) {
    try {
      UUID uuid = UUID.fromString(assetId);

      GetAssetChangeHistory changeHistory = assetsService.getAssetChangeHistory(uuid, new GetChangeHistoryParams(
        userIds,
        actionTypes,
        entityTypes,
        minDate,
        maxDate,
        pageNumber,
        pageSize
      ));

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(changeHistory);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset id in GET /v1/assets/{assetId}/changeHistory:",
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset not found"));
    } catch (AssetNotFoundException assetNotFoundException) {
      LoggerWrapper.error("Asset not found in GET /v1/assets/{assetId}/changeHistory:",
        assetNotFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assets/{assetId}/changeHistory: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
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
  @DeleteMapping("/assets/{assetId}")
  public ResponseEntity<Object> deleteAssetById (
    Authentication userData,
    @PathVariable (value = "assetId") String assetId
  ) {
    try {
      UUID uuid = UUID.fromString(assetId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      assetsService.deleteAssetById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Asset was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid asset id in DELETE /v1/assets/{assetId} with assetId " + assetId + ":",
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset not found"));
    } catch (AssetNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested asset not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/assets/{assetId} with assetId: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
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
  @PreAuthorize("isAuthenticated() and (isBulkMethodAllowed(#assetsRequest, false) or isAssetsIdsInBulkAllowed(#assetsRequest))")
  @DeleteMapping("/assets/bulk")
  public ResponseEntity<Object> deleteAssetsBulk (
    Authentication userData,
    @RequestBody List<UUID> assetsRequest
  ) {
    if (assetsRequest.isEmpty()) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Empty request list"));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      assetsService.deleteAssetsBulk(assetsRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Assets were successfully deleted."));
    } catch (DuplicateValueInRequestException duplicateValueInRequestException) {
      LoggerWrapper.error(duplicateValueInRequestException.getMessage(),
        duplicateValueInRequestException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(duplicateValueInRequestException.getMessage(), duplicateValueInRequestException.getDetails()));
    } catch (AssetNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Requested asset not found", notFoundException.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/assets/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
