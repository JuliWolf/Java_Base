package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.exceptions.CustomViewHeaderQueryIsEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.exceptions.CustomViewTableQueryIsEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.models.get.GetAssetCustomViewHeaderRows;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.models.get.GetAssetCustomViewTableRows;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class AssetCustomViewController {

  @Autowired
  private AssetCustomViewService assetCustomViewService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetCustomViewHeaderRows.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/assets/{assetId}/customViews/{customViewId}/headerRows")
  public ResponseEntity<Object> getAssetCustomViewHeaderRows (
    @PathVariable(value = "assetId") String assetId,
    @PathVariable(value = "customViewId") String customViewId
  ) {
    try {
      UUID assetUUID = UUID.fromString(assetId);
      UUID customViewUUID = UUID.fromString(customViewId);
      GetAssetCustomViewHeaderRows response = assetCustomViewService.getAssetCustomViewHeaderRows(assetUUID, customViewUUID);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid id in GET /v1/assets/{assetId}/customViews/{customViewId}/headerRows:" + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    } catch (
      AssetNotFoundException |
      CustomViewNotFoundException notFoundException
    ) {
      LoggerWrapper.error(notFoundException.getMessage() + " in GET /v1/assets/{assetId}/customViews/{customViewId}/headerRows",
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(notFoundException.getMessage()));
    } catch (CustomViewHeaderQueryIsEmptyException customViewHeaderQueryIsEmptyException) {
      LoggerWrapper.error("Custom view header query is empty in GET /v1/assets/{assetId}/customViews/{customViewId}/headerRows:" + customViewHeaderQueryIsEmptyException.getMessage(),
        customViewHeaderQueryIsEmptyException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(customViewHeaderQueryIsEmptyException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assets/{assetId}/customViews/{customViewId}/headerRows: " + exception.getMessage(),
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
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetCustomViewHeaderRows.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed())")
  @GetMapping("/assets/{assetId}/customViews/{customViewId}/tableRows")
  public ResponseEntity<Object> getAssetCustomViewTableRows (
    @PathVariable(value = "assetId") String assetId,
    @PathVariable(value = "customViewId") String customViewId,
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber
  ) {
    try {
      UUID assetUUID = UUID.fromString(assetId);
      UUID customViewUUID = UUID.fromString(customViewId);
      GetAssetCustomViewTableRows response = assetCustomViewService.getAssetCustomViewTableRows(assetUUID, customViewUUID, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid id in GET /v1/assets/{assetId}/customViews/{customViewId}/tableRows:" + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request error"));
    } catch (
      AssetNotFoundException |
      CustomViewNotFoundException notFoundException
    ) {
      LoggerWrapper.error(notFoundException.getMessage() + " in GET /v1/assets/{assetId}/customViews/{customViewId}/tableRows",
        notFoundException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(notFoundException.getMessage()));
    } catch (CustomViewTableQueryIsEmptyException customViewTableQueryIsEmptyException) {
      LoggerWrapper.error("Custom view table query is empty in GET /v1/assets/{assetId}/customViews/{customViewId}/tableRows:" + customViewTableQueryIsEmptyException.getMessage(),
        customViewTableQueryIsEmptyException.getStackTrace(),
        null,
        AssetsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(customViewTableQueryIsEmptyException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assets/{assetId}/customViews/{customViewId}/tableRows: " + exception.getMessage(),
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
