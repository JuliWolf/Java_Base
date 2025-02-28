package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataExtractStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataSourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.MetadataSourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models.GetMetadataExtractRequestParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models.GetMetadataExtractsResponse;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class MetadataExtractsController {
  private final MetadataExtractsService metadataExtractsService;

  public MetadataExtractsController (MetadataExtractsService metadataExtractsService) {
    this.metadataExtractsService = metadataExtractsService;
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetMetadataExtractsResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/metadataExtracts")
  public ResponseEntity<Object> getMetadataExtracts (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "extract_source_kind", required = false) MetadataSourceKind metadataSourceKind,
    @RequestParam(value = "extract_source_type", required = false) MetadataSourceType metadataSourceType,
    @RequestParam(value = "extract_status", required = false) MetadataExtractStatus extractStatus,
    @RequestParam(value = "full_meta_flag", required = false) Boolean fullMetaFlag
  ) {
    try {
      GetMetadataExtractsResponse response = metadataExtractsService.getMetadataExtracts(new GetMetadataExtractRequestParams(
        pageSize,
        pageNumber,
        metadataSourceKind,
        metadataSourceType,
        extractStatus,
        fullMetaFlag
      ));

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/images: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        MetadataExtractsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
