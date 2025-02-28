package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services;

import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.AuthService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;

/**
 * @author JuliWolf
 */
@Service
public class AuthUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  private final AuthService authService;

  public AuthUserDetailsService (
    UserRepository userRepository,
    AuthService authService
  ) {
    this.userRepository = userRepository;
    this.authService = authService;
  }

  public UserDetails loadUserByUsername(String username, String apiToken) throws UsernameNotFoundException {
    Optional<User> optionalUser = userRepository.getUserByUsernameAndIsDeletedFalse(username);

    if (optionalUser.isEmpty()) {
      optionalUser = Optional.of(authService.createUserFromKeycloak(apiToken));
    }

    return optionalUser.map(AuthUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("User not found " + username));
  }

  @Override
  public UserDetails loadUserByUsername (String username) throws UsernameNotFoundException {
    return null;
  }
}
