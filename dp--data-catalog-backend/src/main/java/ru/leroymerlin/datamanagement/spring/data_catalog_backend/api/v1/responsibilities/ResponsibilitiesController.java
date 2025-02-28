package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorWithDetailsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.SourceAssetIsNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.get.GetResponsibilitiesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.get.GetResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class ResponsibilitiesController {
  private final ResponsibilitiesService responsibilitiesService;

  @Autowired
  public ResponsibilitiesController (ResponsibilitiesService responsibilitiesService) {
    this.responsibilitiesService = responsibilitiesService;
  }


  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostResponsibilityResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isRoleAllowed(#responsibilityRequest.role_id, false) or isAssetAllowed())")
  @PostMapping("/responsibilities")
  public ResponseEntity<Object> createResponsibility (
    Authentication userData,
    @RequestBody PostResponsibilityRequest responsibilityRequest
  ) {
    if (
      responsibilityRequest.getRole_id() == null ||
      responsibilityRequest.getAsset_id() == null ||
      responsibilityRequest.getResponsible_id() == null ||
      StringUtils.isEmpty(responsibilityRequest.getResponsible_type())
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Some of required fields are empty."));
    }
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostResponsibilityResponse responsibility = responsibilitiesService.createResponsibility(responsibilityRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(responsibility);
    } catch (
      RoleNotFoundException |
      UserNotFoundException |
      GroupNotFoundException |
      AssetNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + ". error in POST /v1/responsibilities",
        notFoundException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Responsibility with these parameters already exists. error in POST /v1/responsibilities: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Responsibility with these parameters already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/responsibilities: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostResponsibilityRequest.class)))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isRolesInBulkAllowed(#responsibilitiesRequest, false) or isAssetsInBulkAllowed(#responsibilitiesRequest))")
  @PostMapping("/responsibilities/bulk")
  public ResponseEntity<Object> createResponsibilitiesBulk (
    Authentication userData,
    @RequestBody List<PostResponsibilityRequest> responsibilitiesRequest
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      List<PostResponsibilityResponse> responsibilities = responsibilitiesService.createResponsibilitiesBulk(responsibilitiesRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(responsibilities);
    } catch (SomeRequiredFieldsAreEmptyException someRequiredFieldsAreEmptyException) {
      LoggerWrapper.error("Some required fields are empty. error in POST /v1/responsibilities/bulk",
        someRequiredFieldsAreEmptyException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(someRequiredFieldsAreEmptyException.getMessage(), someRequiredFieldsAreEmptyException.getDetails()));
    } catch (SourceAssetIsNotAllowedException sourceAssetIsNotAllowedException) {
      LoggerWrapper.error("Source asset is not allowed. error in POST /v1/responsibilities/bulk",
        sourceAssetIsNotAllowedException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(sourceAssetIsNotAllowedException.getMessage(), sourceAssetIsNotAllowedException.getDetails()));
    } catch (
      RoleNotFoundException |
      UserNotFoundException |
      GroupNotFoundException |
      AssetNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + ". error in POST /v1/responsibilities/bulk",
        notFoundException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Requested " + notFoundException.getMessage(), notFoundException.getDetails()));
    } catch (ResponsibilityAlreadyExistsException dataIntegrityViolationException) {
      LoggerWrapper.error("Responsibility with these parameters already exists. error in POST /v1/responsibilities/bulk: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Responsibility with these parameters already exists.", dataIntegrityViolationException.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/responsibilities/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetResponsibilityResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/responsibilities/{responsibilityId}")
  public ResponseEntity<Object> getResponsibilityById (
    @PathVariable("responsibilityId") String responsibilityId
  ) {
    try {
      UUID uuid = UUID.fromString(responsibilityId);
      GetResponsibilityResponse responsibility = responsibilitiesService.getResponsibilityById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(responsibility);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid responsibility id '" + responsibilityId + "' in request. error in GET /v1/responsibilities/{responsibilityId}: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested responsibility not found."));
    } catch (ResponsibilityNotFoundException notFoundException) {
      LoggerWrapper.error("Requested responsibility '" + responsibilityId + "' not found. error in GET /v1/responsibilities/{responsibilityId}",
        notFoundException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested responsibility not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/responsibilities/{responsibilityId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetResponsibilitiesResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/responsibilities")
  public ResponseEntity<Object> getResponsibilitiesByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "asset_ids", required = false) List<UUID> assetIds,
    @RequestParam(value = "role_ids", required = false) List<UUID> roleIds,
    @RequestParam(value = "user_ids", required = false) List<UUID> userIds,
    @RequestParam(value = "group_ids", required = false) List<UUID> groupIds,
    @RequestParam(value = "asset_type_ids", required = false) List<UUID> assetTypeIds,
    @RequestParam(value = "lifecycle_status_ids", required = false) List<UUID> lifecycleStatusIds,
    @RequestParam(value = "stewardship_status_ids", required = false) List<UUID> stewardshipStatusIds,
    @RequestParam(value = "inherited_flag", required = false) Boolean inheritedFlag,
    @RequestParam(value = "sort_field", required = false) SortField sortField,
    @RequestParam(value = "sortOrder", required = false) SortOrder sortOrder
  ) {
    try {
      GetResponsibilitiesResponse responsibilities = responsibilitiesService.getResponsibilitiesByParams(
        assetIds,
        roleIds,
        userIds,
        groupIds,
        assetTypeIds,
        lifecycleStatusIds,
        stewardshipStatusIds,
        inheritedFlag,
        sortField,
        sortOrder,
        pageNumber,
        pageSize
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(responsibilities);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in request. error in GET /v1/responsibilities: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/responsibilities: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
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
  @DeleteMapping("/responsibilities/{responsibilityId}")
  public ResponseEntity<Object> deleteResponsibilityById  (
    Authentication userData,
    @PathVariable("responsibilityId") String responsibilityId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(responsibilityId);
      responsibilitiesService.deleteResponsibilityById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Responsibility was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid responsibility id '" + responsibilityId + "' in request. error in DELETE /v1/responsibilities/{responsibilityId}: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested responsibility not found."));
    } catch (ResponsibilityNotFoundException notFoundException) {
      LoggerWrapper.error("Requested responsibility '" + responsibilityId + "' not found. error in DELETE /v1/responsibilities/{responsibilityId}",
        notFoundException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested responsibility not found."));
    } catch (ResponsibilityIsInheritedException isInheritedException) {
      LoggerWrapper.error("Requested responsibility '" + responsibilityId + "' is inherited. error in DELETE /v1/responsibilities/{responsibilityId}",
        isInheritedException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Deleting of inherited responsibilities is not allowed"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/responsibilities/{responsibilityId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
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
  @PreAuthorize("isAuthenticated() and (isBulkMethodAllowed(#responsibilitiesRequest, false) or isAssetsIdsInBulkAllowed(#responsibilitiesRequest))")
  @DeleteMapping("/responsibilities/bulk")
  public ResponseEntity<Object> deleteResponsibilityById  (
    Authentication userData,
    @RequestBody List<UUID> responsibilitiesRequest
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      responsibilitiesService.deleteResponsibilitiesBulk(responsibilitiesRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Responsibilities were successfully deleted."));
    } catch (DuplicateValueInRequestException duplicateValueInRequestException) {
      LoggerWrapper.error(duplicateValueInRequestException.getMessage() + " in DELETE /v1/responsibilities/bulk",
        duplicateValueInRequestException.getStackTrace(),
        null,
        AttributesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(duplicateValueInRequestException.getMessage(), duplicateValueInRequestException.getDetails()));
    } catch (ResponsibilityNotFoundException notFoundException) {
      LoggerWrapper.error("Requested responsibility not found. error in DELETE /v1/responsibilities/bulk",
        notFoundException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Requested responsibility not found.", notFoundException.getDetails()));
    } catch (ResponsibilityIsInheritedException isInheritedException) {
      LoggerWrapper.error("Requested responsibility is inherited. error in DELETE /v1/responsibilities/bulk",
        isInheritedException.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(isInheritedException.getMessage(), isInheritedException.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/responsibilities/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        ResponsibilitiesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
