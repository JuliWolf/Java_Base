package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.RoleResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.get.GetRolesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PatchRoleRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleResponse;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class RolesController {
  
  @Autowired
  private RolesService rolesService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetRolesResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/roles")
  public ResponseEntity<Object> getRolesByNameAndDescription (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "name", required = false) String name,
    @RequestParam(value = "description", required = false) String description
  ) {
    try {
      GetRolesResponse roles = rolesService.getRoleByParams(name, description, pageNumber, pageSize);

      return ResponseEntity
          .ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(roles);
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/roles: " + exception.getMessage(),
          exception.getStackTrace(),
          null,
          RolesController.class.getName()
      );

      return ResponseEntity
          .internalServerError()
          .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = RoleResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isRoleAllowed(#roleId)")
  @GetMapping("/roles/{roleId}")
  public ResponseEntity<Object> getRoleById (
      @PathVariable(value = "roleId") String roleId
  ) {
    try {
      UUID uuid = UUID.fromString(roleId);
      RoleResponse role = rolesService.getRoleById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(role);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in GET /v1/roles{roleId} with roleId " + roleId + ":",
          illegalArgumentException.getStackTrace(),
          null,
          RolesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested role not found"));
    } catch (RoleNotFoundException roleNotFoundException) {
      LoggerWrapper.error(roleNotFoundException.getMessage(),
          roleNotFoundException.getStackTrace(),
          null,
          RolesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested role not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/roles{roleId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RolesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRoleResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isRoleAllowed(#roleId)")
  @PatchMapping("/roles/{roleId}")
  public ResponseEntity<Object> updateRole (
      Authentication userData,
      @RequestBody PatchRoleRequest roleRequest,
      @PathVariable(value = "roleId") String roleId
  )  {
    try {
      Optional<String> roleName = OptionalUtils.getOptionalFromField(roleRequest.getRole_name());
      Optional<String> roleDescription = OptionalUtils.getOptionalFromField(roleRequest.getRole_description());

      if (roleName.isEmpty() && roleDescription.isEmpty()) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Empty role_name and role_description"));
      }

      if (OptionalUtils.isEmpty(roleRequest.getRole_name())) {
        return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("'role_name' is not nullable"));
      }

      validateFieldsLength(roleName.orElse(null), roleDescription.orElse(null));

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(roleId);
      PostRoleResponse updatedRole = rolesService.updateRole(uuid, roleRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updatedRole);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in PATCH /v1/roles/{roleId}: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in PATCH /v1/roles/{roleId} with roleId " + roleId,
        illegalArgumentException.getStackTrace(),
        null,
        RolesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested role not found"));
    } catch (RoleNotFoundException roleNotFoundException) {
      LoggerWrapper.error(roleNotFoundException.getMessage(),
          roleNotFoundException.getStackTrace(),
          null,
          RolesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested role not found"));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Role '"+ roleRequest.getRole_name() +"' already exists. error in PATCH /v1/roles/{roleId}: " + dataIntegrityViolationException.getMessage(),
          dataIntegrityViolationException.getStackTrace(),
          null,
          RolesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Role '"+ roleRequest.getRole_name() +"' already exists"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in PATCH /v1/roles/{roleId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RolesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostRoleResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/roles")
  public ResponseEntity<Object> createRole (
      Authentication userData,
      @RequestBody PostRoleRequest roleRequest
  ) {
    if (StringUtils.isEmpty(roleRequest.getRole_name())) {
      return ResponseEntity
          .status(HttpURLConnection.HTTP_BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(new ErrorResponse("Empty role_name"));
    }

    try {
      validateFieldsLength(roleRequest.getRole_name(), roleRequest.getRole_description());

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostRoleResponse role = rolesService.createRole(roleRequest, userDetails.getUser());

      return ResponseEntity
          .ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(role);
    } catch(InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in POST /v1/roles: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        AttributeTypesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("Role '"+ roleRequest.getRole_name() +"' already exists. error in POST /v1/roles: " + dataIntegrityViolationException.getMessage(),
          dataIntegrityViolationException.getStackTrace(),
          null,
          RolesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Role '"+ roleRequest.getRole_name() +"' already exists"));
    } catch (Exception exception) {
      LoggerWrapper.error("Unexpected error in POST /v1/roles: " + exception.getMessage(),
          exception.getStackTrace(),
          null,
          RolesController.class.getName()
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
  @PreAuthorize("isAuthenticated() and isRoleAllowed(#roleId)")
  @DeleteMapping("/roles/{roleId}")
  public ResponseEntity<Object> deleteRoleById (
      Authentication userData,
      @PathVariable(value = "roleId") String roleId
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(roleId);
      rolesService.deleteRoleById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("Role was successfully deleted."));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid role id in DELETE /v1/roles/{roleId} with roleId " + roleId,
          illegalArgumentException.getStackTrace(),
          null,
          RolesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested role not found"));
    } catch (RoleNotFoundException roleNotFoundException) {
      LoggerWrapper.error(roleNotFoundException.getMessage(),
          roleNotFoundException.getStackTrace(),
          null,
          RolesController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested role not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in DELETE /v1/roles/{roleId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        RolesController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  private void validateFieldsLength (String roleName, String rolDescription) throws InvalidFieldLengthException {
    if (
      StringUtils.isNotEmpty(roleName) &&
      roleName.length() > 255
    ) {
      throw new InvalidFieldLengthException("role_name", 255);
    }

    if (
      StringUtils.isNotEmpty(rolDescription) &&
      rolDescription.length() > 512
    ) {
      throw new InvalidFieldLengthException("role_description", 512);
    }
  }
}
