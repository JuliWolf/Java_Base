package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.AuthFilterMock;

/**
 * @author JuliWolf
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@ComponentScan({
  "ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.security",
})
public class AuthFilterConfigurationMock {

  private AuthFilterMock authFilter = new AuthFilterMock();

  @Bean
  public SecurityFilterChain filterChain (HttpSecurity http) throws Exception {
    HttpSecurity httpSecurity = http
        .csrf(AbstractHttpConfigurer::disable);

    httpSecurity
        .addFilterBefore(
            authFilter, BasicAuthenticationFilter.class
        );

    return http.build();
  }
}
