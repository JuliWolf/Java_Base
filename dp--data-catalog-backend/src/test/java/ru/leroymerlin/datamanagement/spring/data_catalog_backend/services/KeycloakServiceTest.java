package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.KeycloakService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.AccessTokenResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.TokenInfoResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.UserInfoResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.WireMockServiceTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.TestUtils.MAPPER;

/**
 * @author juliwolf
 */
@Testable
public class KeycloakServiceTest extends WireMockServiceTest {
  @Value("${keycloak.realm}")
  private String realm;

  @Autowired
  private KeycloakService keycloakService;

  @Test
  public void getUserInfoSuccessTest () throws Exception {
    String accessToken = StringUtils.repeat("*", 50);
    UserInfoResponse userInfoResponse = new UserInfoResponse();
    userInfoResponse.setEmail("test@test.com");

    wireMockServer.stubFor(
      get(urlPathEqualTo("/realms/" + realm + KeycloakService.URL_USER_INFO))
        .withHeader("Authorization", equalTo("Bearer " + accessToken))
        .willReturn(okJson(MAPPER.writeValueAsString(userInfoResponse)))
    );

    User userInfo = keycloakService.getUserInfo(accessToken);

    assertEquals("test@test.com", userInfo.getEmail());
  }

  @Test
  public void getUserInfoUserNotFoundTest () {
    String accessToken = StringUtils.repeat("*", 50);

    assertThrows(UserNotFoundException.class, () -> keycloakService.getUserInfo(accessToken));
  }

  @Test
  public void getUsernameFromTokenSuccessTest () throws Exception {
    String accessToken = StringUtils.repeat("*", 50);
    TokenInfoResponse tokenInfoResponse = new TokenInfoResponse();
    tokenInfoResponse.setUid("test");
    tokenInfoResponse.setActive(true);

    wireMockServer.stubFor(
      post(urlPathEqualTo("/realms/" + realm + KeycloakService.URL_TOKEN_INFO))
        .willReturn(okJson(MAPPER.writeValueAsString(tokenInfoResponse)))
    );

    String username = keycloakService.getUsernameFromToken(accessToken);

    assertEquals("test", username);
  }

  @Test
  public void getUsernameFromTokenTokenNotActiveTest () throws JsonProcessingException {
    String accessToken = StringUtils.repeat("*", 50);

    TokenInfoResponse tokenInfoResponse = new TokenInfoResponse(false, "test");

    wireMockServer.stubFor(
      post(urlPathEqualTo("/realms/" + realm + KeycloakService.URL_TOKEN_INFO))
        .willReturn(okJson(MAPPER.writeValueAsString(tokenInfoResponse)))
    );

    String username = keycloakService.getUsernameFromToken(accessToken);

    assertNull(username);
  }

  @Test
  public void checkUserAndGetTokenSuccessTest () throws Exception {
    String username = "test";
    String password = "hello";
    String accessToken = StringUtils.repeat("*", 50);
    AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
    accessTokenResponse.setAccess_token(accessToken);

    wireMockServer.stubFor(
      post(urlPathEqualTo("/realms/" + realm + KeycloakService.URL_TOKEN))
        .willReturn(okJson(MAPPER.writeValueAsString(accessTokenResponse)))
    );

    AccessTokenResponse tokenResponse = keycloakService.checkUserAndGetToken(username, password);

    assertEquals(accessToken, tokenResponse.getAccess_token());
  }

  @Test
  public void checkUserAndGetTokenUserNotFoundTest () {
    String username = "test";
    String password = "hello";

    assertThrows(UserNotFoundException.class, () -> keycloakService.checkUserAndGetToken(username, password));
  }
}
