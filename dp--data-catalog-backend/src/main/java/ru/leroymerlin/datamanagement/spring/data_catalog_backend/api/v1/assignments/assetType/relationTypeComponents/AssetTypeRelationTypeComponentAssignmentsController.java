package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents.models.get.GetAssetTypeRelationTypeComponentAssignments;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class AssetTypeRelationTypeComponentAssignmentsController {

  @Autowired
  private AssetTypeRelationTypeComponentAssignmentsService assetTypeRelationTypeComponentAssignmentsService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetAssetTypeRelationTypeComponentAssignments.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isAssetTypeAllowed(#assetTypeId)")
  @GetMapping("/assignments/assetType/{assetTypeId}/relationTypeComponents")
  public ResponseEntity<Object> getAssetTypeRelationTypeComponentAssignments (
    @PathVariable(value = "assetTypeId") String assetTypeId,
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "hierarchy_role", required = false) HierarchyRole hierarchyRole,
    @RequestParam(value = "relation_type_component_name", required = false) String relationTypeComponentName,
    @RequestParam(value = "responsibility_inheritance_role", required = false) ResponsibilityInheritanceRole responsibilityInheritanceRole
  ) {
    try {
      UUID assetTypeUUID = UUID.fromString(assetTypeId);

      GetAssetTypeRelationTypeComponentAssignments response = assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(assetTypeUUID, hierarchyRole, responsibilityInheritanceRole, relationTypeComponentName, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(response);
    } catch (
      IllegalArgumentException |
      AssetTypeNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Asset type with id '" + assetTypeId +"' not found. error in GET /v1/assignments/assetType/{assetTypeId}/relationTypeComponents",
        notFoundException.getStackTrace(),
        null,
        AssetTypeRelationTypeComponentAssignmentsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Asset type not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/assignments/assetType/{assetTypeId}/relationTypeComponents: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AssetTypeRelationTypeComponentAssignmentsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
