package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews;

import java.net.HttpURLConnection;
import java.util.List;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RequestError;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewQueryDoesNotMatchPatternException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.DroppingTableException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewHeaderRowName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewTableColumnName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get.GetCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get.GetCustomViewsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PatchCustomViewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PatchCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class CustomViewsController {
  
  @Autowired
  private CustomViewsService customViewsService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostCustomViewResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/customViews")
  public ResponseEntity<Object> createCustomView (
    Authentication userData,
    @RequestBody PostCustomViewRequest customViewRequest
  ) {
    try {
      if (
        StringUtils.isEmpty(customViewRequest.getAsset_type_id()) ||
        StringUtils.isEmpty(customViewRequest.getCustom_view_name())
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Request error"));
      }

      if (
        customViewRequest.getHeader_row_names().isEmpty() &&
        customViewRequest.getTable_column_names().isEmpty()
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Request error"));
      }

      validatePrepareAndClearQuery(customViewRequest.getHeader_prepare_query(), customViewRequest.getHeader_clear_query());
      validatePrepareAndClearQuery(customViewRequest.getTable_prepare_query(), customViewRequest.getTable_clear_query());

      validateJsonValues(customViewRequest.getHeader_select_query(), customViewRequest.getHeader_row_names());
      validateJsonValues(customViewRequest.getTable_select_query(), customViewRequest.getTable_column_names());

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostCustomViewResponse customView = customViewsService.createCustomView(customViewRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(customView);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid assetTypeId or roleId in POST /v1/customViews: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    } catch (RequestError requestError) {
      LoggerWrapper.error("Request error in request in POST /v1/customViews: " + requestError.getMessage(),
        requestError.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(requestError.getMessage()));
    } catch (
      RoleNotFoundException |
      AssetTypeNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + " in POST /v1/customViews: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (SomeRequiredFieldsAreEmptyException requiredFieldsAreEmptyException) {
      LoggerWrapper.error("Required fields are empty in POST /v1/customViews: " + requiredFieldsAreEmptyException.getMessage(),
        requiredFieldsAreEmptyException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(requiredFieldsAreEmptyException.getMessage()));
    } catch (CustomViewQueryDoesNotMatchPatternException validationError) {
      LoggerWrapper.error("Query does not match pattern in POST /v1/customViews: " + validationError.getMessage(),
        validationError.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(validationError.getMessage()));
    } catch (DroppingTableException droppingTableException) {
      LoggerWrapper.error("Query trying to drop backand query in POST /v1/customViews: " + droppingTableException.getMessage(),
        droppingTableException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(droppingTableException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Custom view with this name already exists for this asset type in POST /v1/customViews: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Custom view with this name already exists for this asset type"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/customViews: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
          .internalServerError()
          .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PatchCustomViewResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PatchMapping("/customViews/{customViewId}")
  public ResponseEntity<Object> updateCustomView (
    Authentication userData,
    @RequestBody PatchCustomViewRequest customViewRequest,
    @PathVariable(value = "customViewId") String customViewId
  ) {
    try {
      Optional<String> roleId = OptionalUtils.getOptionalFromField(customViewRequest.getRole_id());
      Optional<String> tableClearQuery = OptionalUtils.getOptionalFromField(customViewRequest.getTable_clear_query());
      Optional<String> tableSelectQuery = OptionalUtils.getOptionalFromField(customViewRequest.getTable_select_query());
      Optional<String> tablePrepareQuery = OptionalUtils.getOptionalFromField(customViewRequest.getTable_prepare_query());
      Optional<String> headerClearQuery = OptionalUtils.getOptionalFromField(customViewRequest.getHeader_clear_query());
      Optional<String> headerSelectQuery = OptionalUtils.getOptionalFromField(customViewRequest.getHeader_select_query());
      Optional<String> headerPrepareQuery = OptionalUtils.getOptionalFromField(customViewRequest.getHeader_prepare_query());
      Optional<List<CustomViewHeaderRowName>> headerRowNames = OptionalUtils.getOptionalFromField(customViewRequest.getHeader_row_names());
      Optional<List<CustomViewTableColumnName>> tableColumnNames = OptionalUtils.getOptionalFromField(customViewRequest.getTable_column_names());

      if (
        roleId.isEmpty() &&
        tableSelectQuery.isEmpty() &&
        tableClearQuery.isEmpty() &&
        tablePrepareQuery.isEmpty() &&
        headerClearQuery.isEmpty() &&
        headerSelectQuery.isEmpty() &&
        headerPrepareQuery.isEmpty() &&
        headerRowNames.isEmpty() &&
        tableColumnNames.isEmpty() &&
        StringUtils.isEmpty(customViewRequest.getCustom_view_name())
      ) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("All field are empty"));
      }

      validatePrepareAndClearQuery(tablePrepareQuery.orElse(null), tableClearQuery.orElse(null));
      validatePrepareAndClearQuery(headerPrepareQuery.orElse(null), headerClearQuery.orElse(null));

      validateJsonValues(headerSelectQuery.orElse(null), headerRowNames.orElse(null));
      validateJsonValues(tableSelectQuery.orElse(null), tableColumnNames.orElse(null));

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(customViewId);
      PatchCustomViewResponse customView = customViewsService.updateCustomView(uuid, customViewRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(customView);
    } catch (RequestError requestError) {
      LoggerWrapper.error("Request error in request in PATCH /v1/customViews/{customView}: " + requestError.getMessage(),
        requestError.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(requestError.getMessage()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Wrong format for customViewId or roleId in PATCH /v1/customViews/{customView}: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    } catch (SomeRequiredFieldsAreEmptyException someRequiredFieldsAreEmptyException) {
      LoggerWrapper.error("Trying to clear header_row_names table_column_names and  in PATCH /v1/customViews/{customView}: " + someRequiredFieldsAreEmptyException.getMessage(),
        someRequiredFieldsAreEmptyException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("header_row_names and table_column_names can not be null. At least on of the field should not be empty."));
    } catch (
      RoleNotFoundException |
      CustomViewNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + " in PATCH /v1/customViews/{customView}: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (CustomViewQueryDoesNotMatchPatternException validationError) {
      LoggerWrapper.error("Query does not match pattern in PATCH /v1/customViews/{customView}: " + validationError.getMessage(),
        validationError.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(validationError.getMessage()));
    } catch (DroppingTableException droppingTableException) {
      LoggerWrapper.error("Query trying to drop backand query in PATCH /v1/customViews/{customView}: " + droppingTableException.getMessage(),
        droppingTableException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(droppingTableException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Custom view with this name already exists for this asset type in PATCH /v1/customViews/{customView}: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Custom view with this name already exists for this asset type"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/customViews/{customView}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetCustomViewsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/customViews")
  public ResponseEntity<Object> getCustomViewsByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "role_id", required = false) String roleId,
    @RequestParam(value = "asset_type_id", required = false) String assetTypeId,
    @RequestParam(value = "custom_view_name", required = false) String customViewName
  ) {
    try {
      UUID roleUUID = null;
      if (StringUtils.isNotEmpty(roleId)) {
        roleUUID = UUID.fromString(roleId);
      }

      UUID assetTypeUUID = null;
      if (StringUtils.isNotEmpty(assetTypeId)) {
        assetTypeUUID = UUID.fromString(assetTypeId);
      }

      GetCustomViewsResponse customViews = customViewsService.getCustomViewsByParams(roleUUID, assetTypeUUID, customViewName, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(customViews);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Requested params error in GET /v1/customViews: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/customViews: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetCustomViewResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/customViews/{customViewId}")
  public ResponseEntity<Object> getCustomViewById (
    @PathVariable(value = "customViewId") String customViewId
  ) {
    try {
      UUID uuid = UUID.fromString(customViewId);
      GetCustomViewResponse customView = customViewsService.getCustomViewById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(customView);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Wrong format for customViewId in GET /v1/customViews/{customView}: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested custom view not found"));
    } catch (CustomViewNotFoundException notFoundException) {
      LoggerWrapper.error("Requested custom view not found in GET /v1/customViews/{customView}: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested custom view not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/customViews/{customView}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetCustomViewResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @DeleteMapping("/customViews/{customViewId}")
  public ResponseEntity<Object> deleteCustomView (
    Authentication userData,
    @PathVariable(value = "customViewId") String customViewId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(customViewId);
      customViewsService.deleteCustomViewById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Custom view was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Wrong format for customViewId in DELETE /v1/customViews/{customView}: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested custom view not found"));
    } catch (CustomViewNotFoundException notFoundException) {
      LoggerWrapper.error("Requested custom view not found in DELETE /v1/customViews/{customView}: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested custom view not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/customViews/{customView}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        CustomViewsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  private void validateJsonValues (String query, List list) {
    boolean isNamesLstEmpty = list == null || list.isEmpty();
    boolean isQueryEmpty = StringUtils.isEmpty(query);

    if (
      (!isNamesLstEmpty && isQueryEmpty) ||
      (isNamesLstEmpty && !isQueryEmpty)
    ) {
      throw new RequestError();
    }
  }

  private void validatePrepareAndClearQuery (String prepareQuery, String clearQuery) {
    boolean isPrepareQueryEmpty = StringUtils.isEmpty(prepareQuery);
    boolean isClearQueryEmpty = StringUtils.isEmpty(clearQuery);

    if (
      isPrepareQueryEmpty && !isClearQueryEmpty ||
      !isPrepareQueryEmpty && isClearQueryEmpty
    ) {
      throw new RequestError();
    }
  }
}
