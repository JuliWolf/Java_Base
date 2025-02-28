package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal.exceptions.BusinessTermNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v2.devPortal.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v2")
public class DevPortalControllerV2 {
  private final DevPortalServiceV2 devPortalServiceV2;

  public DevPortalControllerV2 (DevPortalServiceV2 devPortalServiceV2) {
    this.devPortalServiceV2 = devPortalServiceV2;
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetBusinessTermsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/business-terms")
  public ResponseEntity<Object> getBusinessTerms (
    @RequestParam(value = "pageSize", required = false) Integer pageSize,
    @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
    @RequestParam(value = "businessTermName", required = false) String businessTermName,
    @RequestParam(value = "businessTermTechnicalName", required = false) String businessTermTechnicalName
  ) {
    try {
      GetBusinessTermsResponse businessTerms = devPortalServiceV2.getBusinessTerms(businessTermName, businessTermTechnicalName, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(businessTerms);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v2/business-terms: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalControllerV2.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetBusinessTermsResponse.GetBusinessTermResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/business-terms/{businessTermId}")
  public ResponseEntity<Object> getBusinessTermById (
    @PathVariable(value = "businessTermId") String businessTermId
  ) {
    try {
      UUID uuid = UUID.fromString(businessTermId);
      GetBusinessTermsResponse.GetBusinessTermResponse businessTerm = devPortalServiceV2.getBusinessTermById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(businessTerm);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid business term id '" + businessTermId + "' in GET /v2/business-terms/{businessTermId}",
        illegalArgumentException.getStackTrace(),
        null,
        DevPortalControllerV2.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (BusinessTermNotFoundException businessTermNotFoundException) {
      LoggerWrapper.error("Business term with id '" + businessTermId + "' not found in GET /v2/business-terms/{businessTermId}",
        businessTermNotFoundException.getStackTrace(),
        null,
        DevPortalControllerV2.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v2/business-terms/{businessTermId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalControllerV2.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BusinessTermAttributesResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/business-terms/{businessTermId}/business-attributes")
  public ResponseEntity<Object> getBusinessTermAttributes (
    @PathVariable(value = "businessTermId") String businessTermId,
    @RequestParam(value = "pageSize", required = false) Integer pageSize,
    @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
    @RequestParam(value = "businessAttributeName", required = false) String businessAttributeName,
    @RequestParam(value = "businessAttributeTechnicalName", required = false) String businessAttributeTechnicalName,
    @RequestParam(value = "businessAttributeDataType", required = false) BusinessAttributeDataType businessAttributeDataType,
    @RequestParam(value = "businessAttributeConfidentiality", required = false) BusinessAttributeConfidentiality businessAttributeConfidentiality
  ) {
    try {
      UUID uuid = UUID.fromString(businessTermId);
      BusinessTermAttributesResponse businessTermAttributes = devPortalServiceV2.getBusinessTermAttributes(new BusinessTermAttributeRequest(
        uuid,
        businessAttributeName,
        businessAttributeTechnicalName,
        businessAttributeDataType,
        businessAttributeConfidentiality,
        pageSize,
        pageNumber
      ));

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(businessTermAttributes);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid business term id '" + businessTermId + "' in GET /v2/business-terms/{businessTermId}/business-attributes",
        illegalArgumentException.getStackTrace(),
        null,
        DevPortalControllerV2.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (BusinessTermNotFoundException businessTermNotFoundException) {
      LoggerWrapper.error("Business term with id '" + businessTermId + "' not found in GET /v2/business-terms/{businessTermId}/business-attributes",
        businessTermNotFoundException.getStackTrace(),
        null,
        DevPortalControllerV2.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v2/business-terms/{businessTermId}/business-attributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalControllerV2.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BusinessTermRelationshipsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/business-terms/{businessTermId}/business-term-relationships")
  public ResponseEntity<Object> getBusinessTermRelationships (
    @PathVariable(value = "businessTermId") String businessTermId,
    @RequestParam(value = "pageSize", required = false) Integer pageSize,
    @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
    @RequestParam(value = "businessTermRelationshipName", required = false) String businessTermRelationshipName,
    @RequestParam(value = "businessTermRelationCardinality", required = false) BusinessRelationCardinality businessTermRelationCardinality,
    @RequestParam(value = "businessTermRelationshipTechnicalName", required = false) String businessAttributeDataType
  ) {
    try {
      UUID uuid = UUID.fromString(businessTermId);
      BusinessTermRelationshipsResponse businessTermRelationships = devPortalServiceV2.getBusinessTermRelationships(new BusinessTermRelationshipsRequest(
        uuid,
        businessTermRelationshipName,
        businessAttributeDataType,
        businessTermRelationCardinality,
        pageSize,
        pageNumber
      ));

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(businessTermRelationships);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid business term id '" + businessTermId + "' in GET /v2/business-terms/{businessTermId}/business-term-relationships",
        illegalArgumentException.getStackTrace(),
        null,
        DevPortalControllerV2.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (BusinessTermNotFoundException businessTermNotFoundException) {
      LoggerWrapper.error("Business term with id '" + businessTermId + "' not found in GET /v2/business-terms/{businessTermId}/business-term-relationships",
        businessTermNotFoundException.getStackTrace(),
        null,
        DevPortalControllerV2.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v2/business-terms/{businessTermId}/business-term-relationships: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalControllerV2.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
