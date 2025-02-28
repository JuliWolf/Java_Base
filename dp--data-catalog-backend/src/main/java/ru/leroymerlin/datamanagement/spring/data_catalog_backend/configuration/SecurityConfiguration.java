package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.AuthService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.AuthFilter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.BulkRequestFilter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.RoleActionsFilter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.AuthUserDetailsService;

/**
 * @author JuliWolf
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

  private final AuthFilter authFilter;

  private final RoleActionsFilter roleActionsFilter;

  private final BulkRequestFilter bulkRequestFilter;

  private final UserRepository userRepository;

  private final AuthService authService;

  public SecurityConfiguration (
    AuthFilter authFilter,
    RoleActionsFilter roleActionsFilter,
    BulkRequestFilter bulkRequestFilter,
    UserRepository userRepository,
    AuthService authService
  ) {
    this.authFilter = authFilter;
    this.roleActionsFilter = roleActionsFilter;
    this.bulkRequestFilter = bulkRequestFilter;
    this.userRepository = userRepository;
    this.authService = authService;
  }

  @Bean
  public SecurityFilterChain filterChain (HttpSecurity http) throws Exception {
    HttpSecurity httpSecurity = http
        .csrf(AbstractHttpConfigurer::disable);

    httpSecurity
      .authenticationProvider(authenticationProvider())
      .addFilterBefore(bulkRequestFilter, BasicAuthenticationFilter.class)
      .addFilterBefore(authFilter, BasicAuthenticationFilter.class)
      .addFilterAfter(roleActionsFilter, BasicAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return new AuthUserDetailsService(userRepository, authService);
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService());
    return authenticationProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
