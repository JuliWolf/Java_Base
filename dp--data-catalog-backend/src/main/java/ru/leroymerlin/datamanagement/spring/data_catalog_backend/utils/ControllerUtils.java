package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.net.HttpURLConnection;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;

/**
 * @author JuliWolf
 */
public class ControllerUtils {
  public static ResponseEntity<Object> responseNoToken() {
    return ResponseEntity
        .status(HttpURLConnection.HTTP_UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Login or token doesn't exist"));
  }

  public static ResponseEntity<Object> responseTokenIsEmpty() {
    return ResponseEntity
        .status(HttpURLConnection.HTTP_UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request token is empty"));
  }

  public static ResponseEntity<Object> responseBadToken() {
    return ResponseEntity
        .status(HttpURLConnection.HTTP_UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorResponse("Request token has wrong format"));
  }

  public static ResponseEntity<Object> responseJwtTokenExpired() {
    return ResponseEntity
      .status(HttpURLConnection.HTTP_UNAUTHORIZED)
      .contentType(MediaType.APPLICATION_JSON)
      .body(new ErrorResponse("Jwt token expired"));
  }

  public static ResponseEntity<Object> insufficientPrivileges() {
    return ResponseEntity
      .status(HttpURLConnection.HTTP_FORBIDDEN)
      .contentType(MediaType.APPLICATION_JSON)
      .body(new ErrorResponse("User's privileges are insufficient to use this method"));
  }
}
