package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.UsernameAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.structUnits.exceptions.StructUnitNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.SearchField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUserRolesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.get.GetUsersResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PatchUserRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PatchUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostOrPatchUserRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostUserResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorWithDetailsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SuccessResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.UUIDUtils;

/**
 * @author JuliWolf
 */
@RestController
@RequestMapping("/v1")
public class UsersController {
  private final UsersService usersService;

  public UsersController (UsersService usersService) {
    this.usersService = usersService;
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostUserResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/users")
  public ResponseEntity<Object> createUser (
    Authentication userData,
    @RequestBody PostOrPatchUserRequest userRequest
  ) {
    if (
      StringUtils.isEmpty(userRequest.getUsername()) ||
      userRequest.getUser_work_status() == null
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Some of required fields are empty."));
    }

    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PostUserResponse user = usersService.createUser(userRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(user);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid struct unit id in POST /v1/users: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Struct unit not found"));
    } catch (StructUnitNotFoundException structUnitNotFoundException) {
      LoggerWrapper.error("Struct unit not found in POST /v1/users: " + structUnitNotFoundException.getMessage(),
        structUnitNotFoundException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Struct unit not found"));
    } catch (InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in POST /v1/users: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("User '"+ userRequest.getUsername() +"' already exists. error in POST /v1/users: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("User with this username already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error POST /v1/users: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostUserResponse.class)))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PostMapping("/users/bulk")
  public ResponseEntity<Object> createUsersBulk (
    Authentication userData,
    @RequestBody List<PostOrPatchUserRequest> userRequests
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      List<PostUserResponse> users = usersService.createUsersBulk(userRequests, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(users);
    } catch (SomeRequiredFieldsAreEmptyException someRequiredFieldsAreEmptyException) {
      LoggerWrapper.error("Some of required field are empty in POST /v1/users/bulk: " + someRequiredFieldsAreEmptyException.getMessage(),
        someRequiredFieldsAreEmptyException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(someRequiredFieldsAreEmptyException.getMessage(), someRequiredFieldsAreEmptyException.getDetails()));
    } catch (InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in POST /v1/users/bulk: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(lengthException.getMessage(), lengthException.getDetails()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid struct unit id in POST /v1/users/bulk: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid params in request"));
    } catch (DuplicateValueInRequestException duplicateValueInRequestException) {
      LoggerWrapper.error("Duplicate username is request in POST /v1/users/bulk: " + duplicateValueInRequestException.getMessage(),
        duplicateValueInRequestException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(duplicateValueInRequestException.getMessage(), duplicateValueInRequestException.getDetails()));
    } catch (StructUnitNotFoundException structUnitNotFoundException) {
      LoggerWrapper.error("Struct unit not found in POST /v1/users/bulk: " + structUnitNotFoundException.getMessage(),
        structUnitNotFoundException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Struct unit not found", structUnitNotFoundException.getDetails()));
    } catch (UsernameAlreadyExistsException usernameAlreadyExistsException) {
      LoggerWrapper.error("Username already exists. error in POST /v1/users/bulk: " + usernameAlreadyExistsException.getMessage(),
        usernameAlreadyExistsException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("User with this username already exists.", usernameAlreadyExistsException.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error POST /v1/users/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PatchUserResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PatchMapping("/users/{userId}")
  public ResponseEntity<Object> updateUser (
    Authentication userData,
    @PathVariable(value = "userId") String userId,
    @RequestBody PostOrPatchUserRequest userRequest
  ) {
    if (
      userRequest.getUser_work_status() == null &&
      StringUtils.isEmpty(userRequest.getEmail()) &&
      StringUtils.isEmpty(userRequest.getUsername()) &&
      StringUtils.isEmpty(userRequest.getFirst_name()) &&
      StringUtils.isEmpty(userRequest.getLast_name()) &&
      StringUtils.isEmpty(userRequest.getBoss_k_pid()) &&
      StringUtils.isEmpty(userRequest.getStruct_unit_id()) &&
      StringUtils.isEmpty(userRequest.getUser_photo_link())
    ) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Empty request body"));
    }

    try {
      UUID uuid = UUID.fromString(userId);
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      PatchUserResponse updatedUser = usersService.updateUser(uuid, userRequest, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updatedUser);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid params in PATCH /v1/users/{userId}: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid params"));
    } catch (UserNotFoundException userNotFoundException) {
      LoggerWrapper.error("User not found in PATCH /v1/users/{userId}: " + userNotFoundException.getMessage(),
        userNotFoundException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("User not found"));
    } catch (StructUnitNotFoundException structUnitNotFoundException) {
      LoggerWrapper.error("Struct unit not found in PATCH /v1/users/{userId}: " + structUnitNotFoundException.getMessage(),
        structUnitNotFoundException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Struct unit not found"));
    } catch (InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in PATCH /v1/users/{userId}: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(lengthException.getMessage()));
    } catch (DataIntegrityViolationException dataIntegrityViolationException) {
      LoggerWrapper.error("User '"+ userRequest.getUsername() +"' already exists. error in PATCH /v1/users/{userId}: " + dataIntegrityViolationException.getMessage(),
        dataIntegrityViolationException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("User with this username already exists."));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error PATCH /v1/users/{userId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = PostUserResponse.class)))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not Found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorWithDetailsResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @PatchMapping("/users/bulk")
  public ResponseEntity<Object> updateUsersBulk (
    Authentication userData,
    @RequestBody List<PatchUserRequest> userRequests
  ) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      List<PatchUserResponse> updatedUsers = usersService.updateUsersBulk(userRequests, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(updatedUsers);
    } catch (SomeRequiredFieldsAreEmptyException someRequiredFieldsAreEmptyException) {
      LoggerWrapper.error("Some of required field are empty in PATCH /v1/users/bulk: " + someRequiredFieldsAreEmptyException.getMessage(),
        someRequiredFieldsAreEmptyException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(someRequiredFieldsAreEmptyException.getMessage(), someRequiredFieldsAreEmptyException.getDetails()));
    } catch (InvalidFieldLengthException lengthException) {
      LoggerWrapper.error("Invalid field length in PATCH /v1/users/bulk: " + lengthException.getMessage(),
        lengthException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(lengthException.getMessage(), lengthException.getDetails()));
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid struct unit id in PATCH /v1/users/bulk: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid params in request"));
    } catch (DuplicateValueInRequestException duplicateValueInRequestException) {
      LoggerWrapper.error("Duplicate username is request in PATCH /v1/users/bulk: " + duplicateValueInRequestException.getMessage(),
        duplicateValueInRequestException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>(duplicateValueInRequestException.getMessage(), duplicateValueInRequestException.getDetails()));
    } catch (StructUnitNotFoundException structUnitNotFoundException) {
      LoggerWrapper.error("Struct unit not found in PATCH /v1/users/bulk: " + structUnitNotFoundException.getMessage(),
        structUnitNotFoundException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("Struct unit not found", structUnitNotFoundException.getDetails()));
    } catch (UsernameAlreadyExistsException usernameAlreadyExistsException) {
      LoggerWrapper.error("Username already exists. error in PATCH /v1/users/bulk: " + usernameAlreadyExistsException.getMessage(),
        usernameAlreadyExistsException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorWithDetailsResponse<>("User with this username already exists.", usernameAlreadyExistsException.getDetails()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error PATCH /v1/users/bulk: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetUserResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/users/{userId}")
  public ResponseEntity<Object> getUserById (
    @PathVariable(value = "userId") String userId
  ) {
    try {
      if (!UUIDUtils.isValidUUID(userId)) {
        throw new UserNotFoundException();
      }

      UUID uuid = UUID.fromString(userId);
      GetUserResponse user = usersService.getUserById(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(user);
    } catch (UserNotFoundException userNotFoundException) {
      LoggerWrapper.error(userNotFoundException.getMessage() + ". error in GET /v1/users/{userId} with userId " + userId,
        userNotFoundException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested user not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error GET /v1/users/{userId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetUserRolesResponse.class))),
    @ApiResponse(responseCode = "404", description = "Not found", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/users/{userId}/roles")
  public ResponseEntity<Object> getUserRoles (
    @PathVariable(value = "userId") String userId
  ) {
    try {
      if (!UUIDUtils.isValidUUID(userId)) {
        throw new UserNotFoundException();
      }

      UUID uuid = UUID.fromString(userId);
      GetUserRolesResponse roles = usersService.getUserRoles(uuid);

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(roles);
    } catch (UserNotFoundException userNotFoundException) {
      LoggerWrapper.error(userNotFoundException.getMessage() + ". error in GET /v1/users/{userId}/roles with userId " + userId,
        userNotFoundException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested user not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error GET /v1/users/{userId}/roles: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = GetUsersResponse.class))),
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @GetMapping("/users")
  public ResponseEntity<Object> getUsersByParams (
    @RequestParam(value = "page_size", required = false) Integer pageSize,
    @RequestParam(value = "page_number", required = false) Integer pageNumber,
    @RequestParam(value = "name", required = false) String name,
    @RequestParam(value = "search_field", required = false) SearchField searchField,
    @RequestParam(value = "sort_field", required = false) SortField sortField,
    @RequestParam(value = "sort_order", required = false) SortOrder sortOrder
  ) {
    try {
      GetUsersResponse users = usersService.getUsersByParams(
        name,
        searchField,
        sortField,
        sortOrder,
        pageNumber,
        pageSize
      );

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(users);
    } catch (IllegalArgumentException illegalArgumentException) {
      LoggerWrapper.error("Invalid search field in GET /v1/users: " + illegalArgumentException.getMessage(),
        illegalArgumentException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Invalid search fields"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error POST /v1/users: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UsersController.class.getName()
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
  @PreAuthorize("isAuthenticated() and isMethodAllowed()")
  @DeleteMapping("/users/{userId}")
  public ResponseEntity<Object> deleteUserById (
    Authentication userData,
    @PathVariable(value = "userId") String userId
  ) {
    try {
      if (!UUIDUtils.isValidUUID(userId)) {
        throw new UserNotFoundException();
      }

      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      UUID uuid = UUID.fromString(userId);
      usersService.deleteUserById(uuid, userDetails.getUser());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new SuccessResponse("User was successfully deleted."));
    } catch (UserNotFoundException userNotFoundException) {
      LoggerWrapper.error(userNotFoundException.getMessage(),
        userNotFoundException.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Requested user not found"));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error DELETE /v1/users/{userId}: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        UsersController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
