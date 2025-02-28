package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth;

import java.net.HttpURLConnection;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.AuthController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.AuthService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.AuthConstants;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.auth.models.ProfileResponse;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */

@WebMvcTest(value = AuthController.class)
@Import({AuthController.class})
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AuthControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AuthService authService;

  @Test
  public void authEmptyLoginTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/auth")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"password\":123 }");

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_UNAUTHORIZED))
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void authEmptyPasswordTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/auth")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"login\":\"leroy\" }");

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_UNAUTHORIZED))
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void authSuccessPasswordTest () {
    try (MockedStatic<AuthService> authServiceMockedStatic = mockStatic(AuthService.class)) {
      String jwt = "jwt-token";
      when(authService.getKeycloakToken("leroy", "123")).thenReturn(jwt);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/auth")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"login\":\"leroy\", \"password\":123 }");

      MvcResult mvcResult = mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andReturn();

      Assertions.assertEquals("{\"token\":\"" + jwt +"\"}", mvcResult.getResponse().getContentAsString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser(isAuthorized = false)
  public void getProfileEmptyTokenTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/profile")
          .accept(MediaType.APPLICATION_JSON)
          .header(AuthConstants.HEADER_TOKEN, "");

      MvcResult response = mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_UNAUTHORIZED))
          .andReturn();

      Assertions.assertTrue(response.getResponse().getContentAsString().contains("Request token is empty"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser(isAuthorized = false)
  public void getProfileNoTokenTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/profile")
          .accept(MediaType.APPLICATION_JSON);

      MvcResult response = mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_UNAUTHORIZED))
          .andReturn();

      Assertions.assertTrue(response.getResponse().getContentAsString().contains("Login or token doesn't exist"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getProfileAuthorizedTest () {
    try {
      String username = "test_username";
      String firstName = "some_firstName";
      String secondName = "some_firstName";
      String email = "test@mail.com";

      when(authService.getUserFromDb(any(String.class))).thenReturn(
          new ProfileResponse(new UUID(123, 123), username, firstName, secondName, email, SourceType.KEYCLOAK, "ru", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()))
      );

      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/profile")
          .accept(MediaType.APPLICATION_JSON);

      MvcResult response = mockMvc.perform(requestBuilder)
          .andReturn();

      Assertions.assertTrue(response.getResponse().getContentAsString().contains(username));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
