package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth;

import java.net.HttpURLConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.models.ProfileResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.models.TokenResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

/**
 * @author JuliWolf
 * @date 07.09.2023
 */
@RestController
@RequestMapping("/v1")
public class AuthController {

  @Autowired
  AuthService authService;

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TokenResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/auth")
  public ResponseEntity<Object> auth (@RequestBody AuthCreds authCreds) {
    if (StringUtils.isEmpty(authCreds.getLogin())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Empty login in request"));
    }

    if (StringUtils.isEmpty(authCreds.getPassword())) {
      return ResponseEntity
        .status(HttpURLConnection.HTTP_UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Empty password in request"));
    }

    try {
      String jwtToken = authService.getKeycloakToken(authCreds.getLogin(), authCreds.getPassword());

      return ResponseEntity
          .ok()
          .contentType(MediaType.APPLICATION_JSON)
          .body(new TokenResponse(jwtToken));
    } catch (UserNotFoundException userNotFoundException) {
      LoggerWrapper.error("Error in authorization: " + userNotFoundException.getMessage(),
          userNotFoundException.getStackTrace(),
          null,
          AuthController.class.getName());

      return ResponseEntity
        .status(HttpURLConnection.HTTP_UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse(userNotFoundException.getMessage()));
    } catch (Exception exception) {
      LoggerWrapper.error("Unexpected error in POST /v1/auth/: " + exception.getMessage(),
          exception.getStackTrace(),
          null,
          AuthController.class.getName());

      return ResponseEntity.internalServerError().build();
    }
  }

  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ProfileResponse.class))),
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "500", description = "Server error", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
  })
  @PreAuthorize("isAuthenticated()")
  @GetMapping ("/profile")
  public ResponseEntity<Object> getProfile (Authentication userData) {
    try {
      AuthUserDetails userDetails = (AuthUserDetails) userData.getPrincipal();
      ProfileResponse profile = authService.getUserFromDb(userDetails.getUsername());

      return ResponseEntity
        .ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(profile);
    } catch (UserNotFoundException userNotFoundException) {
      LoggerWrapper.error("Keycloak searching error in GET /v1/profile: " + userNotFoundException.getMessage(),
        userNotFoundException.getStackTrace(), null, AuthController.class.getName());

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Keycloak searching error: " + userNotFoundException.getMessage()));
    } catch (JwtException jwtException) {
      LoggerWrapper.error("JWT parse error in GET /v1/profile: " + jwtException.getMessage(),
        jwtException.getStackTrace(),
        null,
        AuthController.class.getName()
      );

      return ResponseEntity
        .status(HttpURLConnection.HTTP_UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("JWT parse error: " + jwtException.getMessage()));
    } catch (Exception exception) {
      System.out.println(exception.getClass());

      LoggerWrapper.error("Unexpected error in GET /v1/profile: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        AuthController.class.getName()
      );

      return ResponseEntity
        .internalServerError()
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Got unexpected error: " + exception.getMessage()));
    }
  }
}
