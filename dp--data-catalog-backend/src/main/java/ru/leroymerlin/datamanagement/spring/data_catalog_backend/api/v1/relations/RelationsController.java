package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorWithDetailsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationsAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsResponse;

/**
 * @author juliwolf
 */

@RestController
@RequestMapping("/v1")
public class RelationsController {
  @Autowired
  private RelationsService relationsService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRelationsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isRelationTypeAllowed(#relationRequest.relation_type_id, false) or isAssetRelationTypeAllowed(#relationRequest.relation_type_id))")
  @PostMapping("/relations")
  public ResponseEntity<Object> createRelations (
    Authentication userData,
    @RequestBody PostRelationsRequest relationRequest
  ) {
    if (StringUtils.isEmpty(relationRequest.getRelation_type_id())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type is empty"));
    }

    if (
      relationRequest.getComponent() == null ||
      relationRequest.getComponent().size() < 2
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid number of components."));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostRelationsResponse relation = relationsService.createRelations(relationRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relation);
    } catch(IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid request params error in POST /v1/relations: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request"));
    } catch(
      AssetNotFoundException |
      RelationTypeNotFoundException |
      RelationTypeComponentNotFoundException notfoundException
    ) {
      LoggerWrapper.error("Requested " + notfoundException.getMessage() + ". error in POST /v1/relations",
        notfoundException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " +  notfoundException.getMessage()));
    } catch(InvalidComponentForRelationTypeException invalidComponentForRelationTypeException) {
      LoggerWrapper.error("Invalid component for this relation type. error in POST /v1/relations: " + invalidComponentForRelationTypeException.getMessage(),
        invalidComponentForRelationTypeException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid component for this relation type."));
    } catch (
      InvalidAssetTypeForComponentException |
      InvalidNumberOfComponentsException |
      InvalidHierarchyBetweenAssetsException |
      RelationTypeDoesNotAllowedRelatedAssetException validationError
    ) {
      LoggerWrapper.error(validationError.getMessage() + ". error in POST /v1/relations",
        validationError.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(validationError.getMessage()));
    } catch (
      RelationAlreadyExistsException |
      DataIntegrityViolationException exception
    ) {
      LoggerWrapper.error("Relation with these parameters already exists. error in POST /v1/relations: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation with these parameters already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/relations: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRelationsResponse.class)))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and (isRelationTypesInBulkAllowed(#relationsRequest, false) or isBulkAssetRelationTypeAllowed(#relationsRequest))")
  @PostMapping("/relations/bulk")
  public ResponseEntity<Object> createRelationsBulk (
    Authentication userData,
    @RequestBody List<PostRelationsRequest> relationsRequest
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      List<PostRelationsResponse> relations = relationsService.createRelationsBulk(relationsRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relations);
    } catch (SomeRequiredFieldsAreEmptyException someRequiredFieldsAreEmptyException) {
      LoggerWrapper.error("Invalid field length in POST /v1/relations/bulk: " + someRequiredFieldsAreEmptyException.getMessage(),
        someRequiredFieldsAreEmptyException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(someRequiredFieldsAreEmptyException.getMessage(), someRequiredFieldsAreEmptyException.getDetails()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in request. error in POST /v1/relations/bulk: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Invalid arguments in request"));
    } catch (DuplicateValueInRequestException duplicateValueInRequestException) {
      LoggerWrapper.error("Duplicates value in request. error in POST /v1/relations/bulk: " + duplicateValueInRequestException.getMessage(),
        duplicateValueInRequestException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(duplicateValueInRequestException.getMessage(), duplicateValueInRequestException.getDetails()));
    } catch (RelationAlreadyExistsException exception) {
      LoggerWrapper.error("Relation with these parameters already exists. error in POST /v1/relations/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(exception.getMessage(), exception.getDetails()));
    } catch (RelationTypeDoesNotAllowedRelatedAssetException relationTypeDoesNotAllowedRelatedAssetException) {
      LoggerWrapper.error(relationTypeDoesNotAllowedRelatedAssetException.getMessage() + ". error in POST /v1/relations/bulk",
        relationTypeDoesNotAllowedRelatedAssetException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(relationTypeDoesNotAllowedRelatedAssetException.getMessage(), relationTypeDoesNotAllowedRelatedAssetException.getDetails()));
    } catch (RelationTypeNotFoundException notFoundException) {
      LoggerWrapper.error("Requested relation type not found. error in POST /v1/relations/bulk: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Requested relation type not found.", notFoundException.getDetails()));
    } catch (InvalidNumberOfComponentsException invalidNumberOfComponentsException) {
      LoggerWrapper.error(invalidNumberOfComponentsException.getMessage() + ". error in POST /v1/relations/bulk",
        invalidNumberOfComponentsException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(invalidNumberOfComponentsException.getMessage(), invalidNumberOfComponentsException.getDetails()));
    } catch (InvalidAssetTypeForComponentException invalidAssetTypeForComponent) {
      LoggerWrapper.error("Invalid asset type for the component. error in POST /v1/relations/bulk: " + invalidAssetTypeForComponent.getMessage(),
        invalidAssetTypeForComponent.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(invalidAssetTypeForComponent.getMessage(), invalidAssetTypeForComponent.getDetails()));
    } catch (AssetNotFoundException assetNotFoundException) {
      LoggerWrapper.error("Asset not found. error in POST /v1/relations/bulk: " + assetNotFoundException.getMessage(),
        assetNotFoundException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Asset not found.", assetNotFoundException.getDetails()));
    } catch (RelationTypeComponentNotFoundException relationTypeComponentNotFoundException) {
      LoggerWrapper.error("Relation type component not found. error in POST /v1/relations/bulk: " + relationTypeComponentNotFoundException.getMessage(),
        relationTypeComponentNotFoundException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Relation type component not found.", relationTypeComponentNotFoundException.getDetails()));
    } catch (InvalidComponentForRelationTypeException invalidComponentForRelationTypeException) {
      LoggerWrapper.error("Invalid component for this relation type. error in POST /v1/relations/bulk: " + invalidComponentForRelationTypeException.getMessage(),
        invalidComponentForRelationTypeException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(invalidComponentForRelationTypeException.getMessage(), invalidComponentForRelationTypeException.getDetails()));
    } catch (InvalidHierarchyBetweenAssetsException invalidHierarchyBetweenAssetsException) {
      LoggerWrapper.error("Invalid hierarchy between assets. error in POST /v1/relations/bulk: " + invalidHierarchyBetweenAssetsException.getMessage(),
        invalidHierarchyBetweenAssetsException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(invalidHierarchyBetweenAssetsException.getMessage(), invalidHierarchyBetweenAssetsException.getDetails()));
    } catch (Exception exception) {
      LoggerWrapper.error("Unexpected error in POST /v1/relations/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/relations/{relationId}")
  public ResponseEntity<Object> getRelationsById (
    @PathVariable("relationId") String relationId
  ) {
    try {
      UUID uuid = UUID.fromString(relationId);
      GetRelationResponse relation = relationsService.getRelationById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relation);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid relation id '" + relationId + "' in request. error in GET /v1/relations/{relationId}: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested relation not found."));
    } catch (RelationNotFoundException notFoundException) {
      LoggerWrapper.error("Requested relation '" + relationId + "' not found. error in GET /v1/relations/{relationId}",
        notFoundException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested relation not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/relations/{relationId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationsResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/relations")
  public ResponseEntity<Object> getRelationsByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "asset_id", required = false) String asset_id,
    @RequestParam(value = "relation_type_id", required = false) String relation_type_id,
    @RequestParam(value = "relation_type_component_id", required = false) String relation_type_component_id,
    @RequestParam(value = "hierarchy_flag", required = false) Boolean hierarchy_flag,
    @RequestParam(value = "responsibility_inheritance_flag", required = false) Boolean responsibility_inheritance_flag
  ) {
    try {
      UUID assetId = null;
      if (StringUtils.isNotEmpty(asset_id)) {
        assetId = UUID.fromString(asset_id);
      }

      UUID relationTypeId = null;
      if (StringUtils.isNotEmpty(relation_type_id)) {
        relationTypeId = UUID.fromString(relation_type_id);
      }

      UUID relationTypeComponentId = null;
      if (StringUtils.isNotEmpty(relation_type_component_id)) {
        relationTypeComponentId = UUID.fromString(relation_type_component_id);
      }

      GetRelationsResponse relations = relationsService.getRelationsByParams(assetId, relationTypeId, relationTypeComponentId, hierarchy_flag, responsibility_inheritance_flag, pageNumber, pageSize);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relations);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in request. error in GET /v1/relations: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/relations: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationsAttributesResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/relations/{relationId}/attributes")
  public ResponseEntity<Object> getRelationAttributes (
    @PathVariable("relationId") String relationId
  ) {
    try {
      UUID uuid = UUID.fromString(relationId);
      GetRelationsAttributesResponse relationAttributes = relationsService.getRelationAttributes(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationAttributes);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid relation id '" + relationId + "' in request. error in GET /v1/relations/{relationId}/attributes: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested relation not found."));
    } catch (RelationNotFoundException notFoundException) {
      LoggerWrapper.error("Requested relation '" + relationId + "' not found. error in GET /v1/relations/{relationId}/attributes",
        notFoundException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested relation not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/relations/{relationId}/attributes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationsController.class.getName()
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
  @PreAuthorize("isAuthenticated() and (isMethodAllowed(false) or isAssetAllowed(true))")
  @DeleteMapping("/relations/{relationId}")
  public ResponseEntity<Object> deleteRelation (
    Authentication userData,
    @PathVariable("relationId") String relationId
  ) {
    try {
      UUID uuid = UUID.fromString(relationId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      relationsService.deleteRelation(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Relation was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Relation '" + relationId + "' not found. error in DELETE /v1/relations/{relationId}: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested relation not found."));
    } catch (RelationNotFoundException notFoundException) {
      LoggerWrapper.error("Relation '" + relationId + "' not found. error in DELETE /v1/relations/{relationId}",
        notFoundException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested relation not found."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/relations/{relationId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationsController.class.getName()
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
  @PreAuthorize("isAuthenticated() and (isBulkMethodAllowed(#relationIds, false) or isAssetsIdsInBulkAllowed(#relationIds, true))")
  @DeleteMapping("/relations/bulk")
  public ResponseEntity<Object> deleteRelationsBulk (
    Authentication userData,
    @RequestBody List<UUID> relationIds
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      relationsService.deleteRelationsBulk(relationIds, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Relation was successfully deleted."));
    } catch (RelationNotFoundException notFoundException) {
      LoggerWrapper.error("Relation not found. error in DELETE /v1/relations/bulk",
        notFoundException.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Requested relation not found.", notFoundException.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/relations/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationsController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
