package ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers;

import java.net.HttpURLConnection;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import jakarta.servlet.http.HttpServletRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.ErrorResponse;

/**
 * @author juliwolf
 */

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler({ MaxUploadSizeExceededException.class })
  public ResponseEntity<Object> handleInsufficientPrivilegesException(final Exception ex, final HttpServletRequest request) {
    return ResponseEntity
      .status(HttpURLConnection.HTTP_BAD_REQUEST)
      .contentType(MediaType.APPLICATION_JSON)
      .body(new ErrorResponse("Size exceeds restriction"));
  }
}
