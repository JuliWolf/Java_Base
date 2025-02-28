package ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.AuthConstants;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InsufficientPrivilegesException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ControllerUtils;

import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.AuthConstants.JWT_EXPIRED;

/**
 * @author JuliWolf
 */
@ControllerAdvice
public class RestResponseAccessDeniedHandler extends ResponseEntityExceptionHandler {

  // 401
  @ExceptionHandler({ AccessDeniedException.class })
  public ResponseEntity<Object> handleAccessDeniedException(final Exception ex, final HttpServletRequest request) {
    String token = request.getHeader(AuthConstants.HEADER_TOKEN);

    String expiredAttribute = (String) request.getAttribute(JWT_EXPIRED);

    if (StringUtils.isNotEmpty(expiredAttribute)) {
      return ControllerUtils.responseJwtTokenExpired();
    }

    if (token == null) {
      return ControllerUtils.responseNoToken();
    }

    if (token.isEmpty()) {
      return ControllerUtils.responseTokenIsEmpty();
    }

    return ControllerUtils.responseBadToken();
  }

  // 403
  @ExceptionHandler({ InsufficientPrivilegesException.class })
  public ResponseEntity<Object> handleInsufficientPrivilegesException(final Exception ex, final HttpServletRequest request) {
    return ControllerUtils.insufficientPrivileges();
  }
}
