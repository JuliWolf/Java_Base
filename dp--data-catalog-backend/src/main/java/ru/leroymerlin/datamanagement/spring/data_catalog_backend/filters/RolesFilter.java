package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.models.UserRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesDAO;

/**
 * @author juliwolf
 */

@Order(4)
@Component
public class RolesFilter extends OncePerRequestFilter {
  private final RolesDAO rolesDAO;

  public RolesFilter (RolesDAO rolesDAO) {
    this.rolesDAO = rolesDAO;
  }

  @Override
  protected void doFilterInternal (
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws IOException, ServletException {
    LoggerWrapper.info(
      "Start roles filter",
      RolesFilter.class.getName()
    );

    Authentication authentication = SecurityContextHolder
      .getContext()
      .getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      filterChain.doFilter(request, response);

      return;
    }

    if (!(authentication.getPrincipal() instanceof AuthUserDetails)) {
      filterChain.doFilter(request, response);

      return;
    }

    AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

    loadRoles(userDetails);

    filterChain.doFilter(request, response);
  }

  private void loadRoles (AuthUserDetails userDetails) {
    UUID userId = userDetails.getUser().getUserId();

    List<UserRole> userRoles = rolesDAO.findAllByUserId(userId, false);

    userDetails.setUserRoles(userRoles);

    if (!userRoles.isEmpty()) return;

    List<UserRole> groupUserRoles = rolesDAO.findAllByUserId(userId, true);

    userDetails.setGroupUserRoles(groupUserRoles);
  }

  @Override
  protected boolean shouldNotFilter (HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/v1");
  }
}
