package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.impl.DefaultClaims;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.AuthConstants;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetailsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.JWTUtilsStub;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author JuliWolf
 */
public class AuthFilterMock extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    // Получаем токен из заголовка
    final String token = request.getHeader(AuthConstants.HEADER_TOKEN);

    if (null == token || token.isEmpty()) {
      filterChain.doFilter(request, response);

      return;
    }

    // Получаем все данные из токена
    DefaultClaims userClaims = JWTUtilsStub.decodeJWT(token, AuthConstants.JWT_SECRET_KEY);

    if (userClaims.isEmpty()) {
      filterChain.doFilter(request, response);

      return;
    }

    // Парсим данные токена, получаем имя пользователя и его права
    String username = userClaims.get("username").toString();

    if (StringUtils.isEmpty(username)) {
      filterChain.doFilter(request, response);

      return;
    }

    AuthUserDetailsService mock = Mockito.mock(AuthUserDetailsService.class);
    when(mock.loadUserByUsername(any(String.class))).thenReturn(new AuthUserDetails(new User()));

    // Получаем данные пользователя из базы данных
    UserDetails userDetails = mock.loadUserByUsername(username);

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
  }
}
