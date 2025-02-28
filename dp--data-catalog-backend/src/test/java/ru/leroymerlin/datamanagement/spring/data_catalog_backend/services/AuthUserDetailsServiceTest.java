package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.globalResponsibilities.GlobalResponsibilitiesRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.KeycloakService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.UserInfoResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.WireMockServiceTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.TestUtils.MAPPER;

/**
 * @author juliwolf
 */

@Testable
public class AuthUserDetailsServiceTest extends WireMockServiceTest {
  @Value("${keycloak.realm}")
  private String realm;

  @Autowired
  private AuthUserDetailsService authUserDetailsService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  protected GlobalResponsibilitiesRepository globalResponsibilitiesRepository;

  @BeforeAll
  public void clearUsers () {
    userRepository.deleteAll();
  }

  @AfterEach
  public void clearData () {
    userRepository.deleteAll();
    globalResponsibilitiesRepository.deleteAll();
  }

  @Test
  public void loadUserByUsername_UserNotFound_IntegrationTest () throws JsonProcessingException {
    String accessToken = StringUtils.repeat("*", 50);
    UserInfoResponse userInfoResponse = new UserInfoResponse();
    userInfoResponse.setEmail("test@test.com");
    userInfoResponse.setUid("someusername");

    wireMockServer.stubFor(
      get(urlPathEqualTo("/realms/" + realm + KeycloakService.URL_USER_INFO))
        .withHeader("Authorization", equalTo("Bearer " + accessToken))
        .willReturn(okJson(MAPPER.writeValueAsString(userInfoResponse)))
    );

    authUserDetailsService.loadUserByUsername("someusername", accessToken);

    List<User> users = userRepository.findAll();

    assertAll(
      () -> assertEquals(1, users.size()),
      () -> assertEquals(userInfoResponse.getUid(), users.get(0).getUsername())
    );
  }
}
