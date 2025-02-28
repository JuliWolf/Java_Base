package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters;

import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.AuthConstants;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetailsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.KeycloakService;

import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.AuthConstants.JWT_EXPIRED;

/**
 * @author JuliWolf
 */

@Order(2)
@Component
public class AuthFilter extends OncePerRequestFilter {

  private final AuthUserDetailsService userDetailsService;

  private final KeycloakService keycloakService;

  public AuthFilter (
    AuthUserDetailsService userDetailsService,
    KeycloakService keycloakService
  ) {
    this.userDetailsService = userDetailsService;
    this.keycloakService = keycloakService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    LoggerWrapper.info(
      "Token filter: start",
      AuthFilter.class.getName()
    );

    try {
      // Получаем токен из заголовка
      final String token = request.getHeader(AuthConstants.HEADER_TOKEN);
      final String devPortalToken = request.getHeader(AuthConstants.DEV_PORTAL_HEADER_TOKEN);

      boolean isTokenEmpty = null == token || token.isEmpty();
      boolean isDevPortalTokenEmpty = null == devPortalToken || devPortalToken.isEmpty();

      if (isTokenEmpty && isDevPortalTokenEmpty) {
        LoggerWrapper.info(
          "Token filter: empty",
          AuthFilter.class.getName()
        );

        filterChain.doFilter(request, response);

        return;
      }

      LoggerWrapper.info(
        "Token filter: parse token",
        AuthFilter.class.getName()
      );

      String apiToken = !isTokenEmpty
        ? token
        : devPortalToken;

      // Получаем все данные из токена
      String username = keycloakService.getUsernameFromToken(apiToken);

      if (username == null) {
        LoggerWrapper.info(
          "Token filter: user not found",
          AuthFilter.class.getName()
        );

        filterChain.doFilter(request, response);

        return;
      }

      // Получаем данные пользователя из базы данных
      UserDetails userDetails = userDetailsService.loadUserByUsername(username, apiToken);

      // Создаем экземпляр класса для дальнейшей передачи его в AuthenticationManager
      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
      );

      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder
        .getContext()
        .setAuthentication(authToken);

      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException expiredJwtException) {
      request.setAttribute(JWT_EXPIRED, expiredJwtException.getMessage());

      filterChain.doFilter(request, response);
    } catch (Exception exception) {
      filterChain.doFilter(request, response);
    }
  }

  @Override
  protected boolean shouldNotFilter (HttpServletRequest request) {
    return request.getRequestURI().startsWith("/actuator");
  }
}
