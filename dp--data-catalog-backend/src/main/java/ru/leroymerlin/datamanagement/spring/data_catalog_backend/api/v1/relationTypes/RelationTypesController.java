package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.RelationTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.get.GetRelationTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PatchRelationTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PostRelationTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PostRelationTypeResponse;

@RestController
@RequestMapping("/v1")
public class RelationTypesController {
  @Autowired
  private RelationTypesService relationTypesService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRelationTypeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/relationTypes")
  public ResponseEntity<Object> createRelationType (
    Authentication userData,
    @RequestBody PostRelationTypeRequest relationTypeRequest
  ) {
    if (
      StringUtils.isEmpty(relationTypeRequest.getRelation_type_name()) ||
      relationTypeRequest.getRelation_type_component_number() == null ||
      relationTypeRequest.getRelation_type_component_number() < 2
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Some of required fields are empty."));
    }

    int componentsSize = relationTypeRequest.getRelation_type_component().size();

    if (
      componentsSize < 2 ||
      relationTypeRequest.getRelation_type_component_number() != componentsSize
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid number of components."));
    }

    boolean hasTwoComponentsAndTwoComponents = componentsSize == 2;

    if (relationTypeRequest.getResponsibility_inheritance_flag() && !hasTwoComponentsAndTwoComponents) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Incorrect roles for responsibility inheritance."));
    }

    if (relationTypeRequest.getHierarchy_flag() && !hasTwoComponentsAndTwoComponents) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Incorrect roles in hierarchy"));
    }

    try {
      validateFieldsLength(relationTypeRequest.getRelation_type_name(), relationTypeRequest.getRelation_type_description());

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostRelationTypeResponse relationType = relationTypesService.createRelationType(relationTypeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationType);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in POST /v1/relationTypes: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (SomeRequiredFieldsAreEmptyException requiredFieldsAreEmpty) {
      LoggerWrapper.error("Some of required fields are empty. error in POST /v1/relationTypes: " + requiredFieldsAreEmpty.getMessage(),
        requiredFieldsAreEmpty.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Some of required fields are empty."));
    } catch (
      IncorrectRoleInHierarchyException |
      IncorrectRoleForResponsibilityInheritanceException validationRequestException
    ) {
      LoggerWrapper.error("Validation params error in POST /v1/relationTypes: " + validationRequestException.getMessage(),
        validationRequestException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(validationRequestException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Relation type '" + relationTypeRequest.getRelation_type_name() + "' already exists. error in POST /v1/relationTypes: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type '" + relationTypeRequest.getRelation_type_name() + "' already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in POST /v1/relationTypes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = RelationTypeResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isRelationTypeAllowed(#relationTypeId)")
  @PatchMapping("/relationTypes/{relationTypeId}")
  public ResponseEntity<Object> updateRelationType (
    Authentication userData,
    @PathVariable String relationTypeId,
    @RequestBody PatchRelationTypeRequest relationTypeRequest
  ) {
    try {
      Optional<String> relationTypeName = OptionalUtils.getOptionalFromField(relationTypeRequest.getRelation_type_name());
      Optional<String> relationTypeDescription = OptionalUtils.getOptionalFromField(relationTypeRequest.getRelation_type_description());

      if (OptionalUtils.isEmpty(relationTypeRequest.getRelation_type_name())) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("'relation_type_name' is not nullable"));
      }

      validateFieldsLength(relationTypeName.orElse(null), relationTypeDescription.orElse(null));

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(relationTypeId);
      RelationTypeResponse relationType = relationTypesService.updateRelationType(uuid, relationTypeRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationType);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in PATCH /v1/relationTypes/{relationTypeId}: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid arguments in PATCH /v1/relationTypes/{relationTypeId} with id '" + relationTypeId + "':",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid arguments in request"));
    } catch (
      RelationTypeNotFoundException |
      RelationTypeComponentNotFoundException notFoundException
    ) {
      LoggerWrapper.error("Requested " + notFoundException.getMessage() + ". error in PATCH /v1/relationTypes/{relationTypeId}: " + notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested " + notFoundException.getMessage()));
    } catch (MultipleRelationExistsWithAssetException multipleRelationExistsWithAssetException) {
      LoggerWrapper.error("Same assets relations still exist for this relation_type in PATCH /v1/relationTypes/{relationTypeId}: " + multipleRelationExistsWithAssetException.getMessage(),
        multipleRelationExistsWithAssetException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(multipleRelationExistsWithAssetException.getMessage()));
    } catch (MultipleRelationExistsWithSameAssetException multipleRelationExistsWithSameAssetException) {
      LoggerWrapper.error("Multiple relations with same asset for relation type component in PATCH /v1/relationTypes/{relationTypeId}: " + multipleRelationExistsWithSameAssetException.getMessage(),
        multipleRelationExistsWithSameAssetException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(multipleRelationExistsWithSameAssetException.getMessage()));
    } catch (
      InvalidRolesException |
      SelfRelatedAssetExistsException |
      IncorrectRoleInHierarchyException |
      InvalidNumberOfComponentsException |
      IncorrectRoleForResponsibilityInheritanceException validationRequestException
    ) {
      LoggerWrapper.error("Validation params error in PATCH /v1/relationTypes/{relationTypeId}: " + validationRequestException.getMessage(),
        validationRequestException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(validationRequestException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Relation type '" + relationTypeRequest.getRelation_type_name() + "' already exists. error in PATCH /v1/relationTypes/{relationTypeId}: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type '" + relationTypeRequest.getRelation_type_name() + "' already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/relationTypes/{relationTypeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = RelationTypeResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isRelationTypeAllowed(#relationTypeId)")
  @GetMapping("/relationTypes/{relationTypeId}")
  public ResponseEntity<Object> getRelationTypeById (
    @PathVariable String relationTypeId
  ) {
    try {
      UUID uuid = UUID.fromString(relationTypeId);
      RelationTypeResponse relationType = relationTypesService.getRelationTypeById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationType);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid relation type id '" + relationTypeId + "' in GET /v1/relationTypes/{relationTypeId}",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type not found"));
    } catch (RelationTypeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/relationTypes/{relationTypeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRelationTypesResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/relationTypes")
  public ResponseEntity<Object> getRelationTypesByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "hierarchy_flag", required = false) Boolean hierarchyFlag,
    @RequestParam(value = "uniqueness_flag", required = false) Boolean uniquenessFlag,
    @RequestParam(value = "component_number", required = false) Integer componentNumber,
    @RequestParam(value = "relation_type_name", required = false) String relationTypeName,
    @RequestParam(value = "allowed_asset_type", required = false) String allowedAssetType,
    @RequestParam(value = "self_related_flag", required = false) Boolean selfRelatedFlag,
    @RequestParam(value = "responsibility_inheritance_flag", required = false) Boolean responsibilityInheritanceFlag
    ) {
    try {
      UUID allowedAssetTypeId = null;
      if (StringUtils.isNotEmpty(allowedAssetType)) {
        allowedAssetTypeId = UUID.fromString(allowedAssetType);
      }
      GetRelationTypesResponse relationTypes = relationTypesService.getRelationTypesByParams(
        relationTypeName,
        componentNumber,
        hierarchyFlag,
        responsibilityInheritanceFlag,
        allowedAssetTypeId,
        selfRelatedFlag,
        uniquenessFlag,
        pageNumber,
        pageSize
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(relationTypes);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid allowed asset type id '" + allowedAssetType + "' in GET /v1/relationTypes",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid request params"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/relationTypes: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypesController.class.getName()
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
  @PreAuthorize("isAuthenticated() and isRelationTypeAllowed(#relationTypeId)")
  @DeleteMapping("/relationTypes/{relationTypeId}")
  public ResponseEntity<Object> deleteRelationTypeById (
    Authentication userData,
    @PathVariable String relationTypeId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(relationTypeId);
      relationTypesService.deleteRelationTypeById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Relation type was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid relation type id '" + relationTypeId+ "' in DELETE /v1/relationTypes/{relationTypeId}",
        illegalArgumentException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type not found"));
    } catch (RelationTypeNotFoundException notFoundException) {
      LoggerWrapper.error(notFoundException.getMessage(),
        notFoundException.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Relation type not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/relationTypes/{relationTypeId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RelationTypesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  private void validateFieldsLength (String relationTypeName, String relationTypeDescription) throws InvalidFieldLengthException {
    if (
      StringUtils.isNotEmpty(relationTypeName) &&
      relationTypeName.length() > 255
    ) {
      throw new InvalidFieldLengthException("relation_type_name", 255);
    }

    if (
      StringUtils.isNotEmpty(relationTypeDescription) &&
      relationTypeDescription.length() > 512
    ) {
      throw new InvalidFieldLengthException("relation_type_description", 512);
    }
  }
}
