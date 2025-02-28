package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.cachedRequest.CachedBodyHttpServletRequest;

/**
 * @author juliwolf
 */

@Order(1)
@Component
public class BulkRequestFilter extends OncePerRequestFilter {
  private final RequestMatcher bulkUriMatcher = new AntPathRequestMatcher("/v1/*/bulk");

  public static Map<String, Integer> MAX_REQUEST_COUNT_BY_METHOD;

  @Autowired
  public BulkRequestFilter (
    @Value("${bulk.max-request.post}") Integer postCount,
    @Value("${bulk.max-request.update}") Integer updateCount,
    @Value("${bulk.max-request.delete}") Integer deleteCount
  ) {
    BulkRequestFilter.MAX_REQUEST_COUNT_BY_METHOD = Stream.of(new Object[][] {
      { "POST", postCount },
      { "PATCH", updateCount },
      { "DELETE", deleteCount }
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));
  }

  @Override
  protected void doFilterInternal (
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws IOException, ServletException {
    LoggerWrapper.info(
      "Start filter bulk request",
      BulkRequestFilter.class.getName()
    );

    CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

    try {
      List jsonRequest = new ObjectMapper().readValue(wrappedRequest.getCachedBody(), List.class);

      final int MAX_BULK_SIZE = MAX_REQUEST_COUNT_BY_METHOD.get(request.getMethod());

      if (jsonRequest.size() > MAX_BULK_SIZE) {
        createErrorResponse(response, HttpURLConnection.HTTP_ENTITY_TOO_LARGE, "Request array too large");
        return;
      }
    } catch (IOException ioException) {
      LoggerWrapper.error(
        "Error when parsing bulk request body: method - " + wrappedRequest.getMethod() + " url - " + wrappedRequest.getServletPath(),
        BulkRequestFilter.class.getName()
      );

      createErrorResponse(response, HttpURLConnection.HTTP_BAD_REQUEST, "Request error");
      return;
    }

    filterChain.doFilter(wrappedRequest, response);
  }

  @Override
  protected boolean shouldNotFilter (HttpServletRequest request) {
    RequestMatcher matcher = new NegatedRequestMatcher(bulkUriMatcher);
    return matcher.matches(request);
  }

  private void createErrorResponse (
    HttpServletResponse response,
    int statusCode,
    String errorMessage
  ) throws IOException {
    response.setStatus(statusCode);
    response.setContentType(MediaType.APPLICATION_JSON.toString());

    Map<String, String> errorResponse = new HashMap<>();
    errorResponse.put("error", errorMessage);

    ObjectMapper objectMapper = new ObjectMapper();
    String jsonResponse = objectMapper.writeValueAsString(errorResponse);

    response.getWriter().write(jsonResponse);
  }
}
