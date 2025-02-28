package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.exceptions.BusinessFunctionNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.exceptions.BusinessTermNotFoundException;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class DevPortalController {
  private final DevPortalService devPortalService;

  public DevPortalController (DevPortalService devPortalService) {
    this.devPortalService = devPortalService;
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
      GetBusinessTermsResponse businessTerms = devPortalService.getBusinessTerms(businessTermName, businessTermTechnicalName, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(businessTerms);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/business-terms: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalController.class.getName()
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
      GetBusinessTermsResponse.GetBusinessTermResponse businessTerm = devPortalService.getBusinessTermById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(businessTerm);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid business term id '" + businessTermId + "' in GET /v1/business-terms/{businessTermId}",
        illegalArgumentException.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (BusinessTermNotFoundException businessTermNotFoundException) {
      LoggerWrapper.error("Business term with id '" + businessTermId + "' not found in GET /v1/business-terms/{businessTermId}",
        businessTermNotFoundException.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/business-terms/{businessTermId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalController.class.getName()
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
      BusinessTermAttributesResponse businessTermAttributes = devPortalService.getBusinessTermAttributes(new BusinessTermAttributeRequest(
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
      LoggerWrapper.error("Invalid business term id '" + businessTermId + "' in GET /v1/business-terms/{businessTermId}/business-attributes",
        illegalArgumentException.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (BusinessTermNotFoundException businessTermNotFoundException) {
      LoggerWrapper.error("Business term with id '" + businessTermId + "' not found in GET /v1/business-terms/{businessTermId}/business-attributes",
        businessTermNotFoundException.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/business-terms/{businessTermId}/business-attributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalController.class.getName()
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
      BusinessTermRelationshipsResponse businessTermRelationships = devPortalService.getBusinessTermRelationships(new BusinessTermRelationshipsRequest(
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
      LoggerWrapper.error("Invalid business term id '" + businessTermId + "' in GET /v1/business-terms/{businessTermId}/business-term-relationships",
        illegalArgumentException.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (BusinessTermNotFoundException businessTermNotFoundException) {
      LoggerWrapper.error("Business term with id '" + businessTermId + "' not found in GET /v1/business-terms/{businessTermId}/business-term-relationships",
        businessTermNotFoundException.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business term not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/business-terms/{businessTermId}/business-term-relationships: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetBusinessFunctionBusinessTermsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/business-functions/{businessFunctionId}/business-terms")
  public ResponseEntity<Object> getBusinessFunctionBusinessTerms (
    @RequestParam(value = "pageSize", required = false) Integer pageSize,
    @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
    @PathVariable(value = "businessFunctionId") String businessFunctionId
  ) {
    try {
      UUID uuid = UUID.fromString(businessFunctionId);
      GetBusinessFunctionBusinessTermsResponse businessTerms = devPortalService.getBusinessFunctionBusinessTerms(uuid, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(businessTerms);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid business function id '" + businessFunctionId + "' in GET /v1/business-functions/{businessFunctionId}/business-terms",
        illegalArgumentException.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business function not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/business-functions/{businessFunctionId}/business-terms: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetBusinessFunctionsResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/business-functions")
  public ResponseEntity<Object> getBusinessFunctions (
    @RequestParam(value = "pageSize", required = false) Integer pageSize,
    @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
    @RequestParam(value = "businessFunctionName", required = false) String businessFunctionName,
    @RequestParam(value = "businessFunctionDomainId", required = false) String businessFunctionDomainId,
    @RequestParam(value = "businessFunctionDescription", required = false) String businessFunctionDescription,
    @RequestParam(value = "businessFunctionOwner.digitalIdentities.ldap", required = false) String businessOwnerLdap,
    @RequestParam(value = "businessFunctionExecutor.structuralUnitNumber", required = false) String businessStructuralUnitNumber
  ) {
    try {
      GetBusinessFunctionsResponse businessFunctions = devPortalService.getBusinessFunctions(
        businessFunctionName,
        businessFunctionDescription,
        businessFunctionDomainId,
        businessOwnerLdap,
        businessStructuralUnitNumber,
        pageNumber, pageSize
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(businessFunctions);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/business-functions/business-functions: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetBusinessFunctionsResponse.GetBusinessFunctionResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping("/business-functions/{businessFunctionId}")
  public ResponseEntity<Object> getBusinessFunctionById (
    @PathVariable(value = "businessFunctionId") String businessFunctionId
  ) {
    try {
      UUID businessFunctionUUID = UUID.fromString(businessFunctionId);
      GetBusinessFunctionsResponse.GetBusinessFunctionResponse businessFunction = devPortalService.getBusinessFunctionById(businessFunctionUUID);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(businessFunction);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid business function id '" + businessFunctionId + "' in GET /v1/business-functions/{businessFunctionId}",
        illegalArgumentException.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business function not found"));
    } catch (BusinessFunctionNotFoundException businessFunctionNotFoundException) {
      LoggerWrapper.error("Business function with id '" + businessFunctionId + "' not found in GET /v1/business-functions/{businessFunctionId}",
        businessFunctionNotFoundException.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested business function not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/business-functions/business-functions: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        DevPortalController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
