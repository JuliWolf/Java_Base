package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.RoleResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.get.GetRolesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PatchRoleRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.models.post.PostRoleResponse;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(RolesController.class)
@Import(RolesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class RolesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RolesService rolesService;

  @Test
  @WithMockCustomUser
  public void createRoleEmptyRoleNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/roles")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"role_name\": \"\" }");

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRoleLongRoleNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/roles")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_name\": \"" + StringUtils.repeat('1', 256) + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"role_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRoleLongRoleDescriptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/roles")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_name\": \"role name\", \"role_description\": \"" + StringUtils.repeat('a', 513)+ "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"role_description contains too much symbols. Allowed limit is 512\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRoleSuccessNameTest () {
    try {
      PostRoleRequest postRoleRequest = new PostRoleRequest("some role name", "some description");
      User user = new User();
      user.setUserId(new UUID(1, 2));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/roles")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"role_name\": \""+ postRoleRequest.getRole_name() +"\" }");

      when(rolesService.createRole(any(PostRoleRequest.class), any(User.class)))
        .thenReturn(new PostRoleResponse());

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRolesByNameAndDescriptionWithNoParamsTest () {
    try {
      when(
          rolesService.getRoleByParams(any(String.class), any(String.class), any(Integer.class), any(Integer.class))
      ).thenReturn(new GetRolesResponse());

      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/roles")
          .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRoleByIdWithInvalidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/roles/123")
          .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRoleByIdWithValidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/roles/aba87f7e-0e5c-427f-9e71-2150e31466d9")
          .contentType(MediaType.APPLICATION_JSON);

      when(rolesService.getRoleById(any(UUID.class)))
          .thenReturn(new RoleResponse());

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRoleWithEmptyBodyTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/roles/aba87f7e-0e5c-427f-9e71-2150e31466d9")
          .contentType(MediaType.APPLICATION_JSON)
          .content("");

      when(rolesService.updateRole(any(UUID.class), any(PatchRoleRequest.class), any(User.class)))
          .thenReturn(new PostRoleResponse());

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRoleWithInvalidRoleIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/roles/123")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"role_name\":\"something\" }");

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRoleNotExistingRoleTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/roles/aba87f7e-0e5c-427f-9e71-2150e31466d9")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"role_name\":\"something\" }");

      when(rolesService.updateRole(any(UUID.class), any(PatchRoleRequest.class), any(User.class)))
          .thenThrow(RoleNotFoundException.class);

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRoleLongRoleNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/roles/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_name\":\"" + StringUtils.repeat('f', 256)+ "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"role_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRoleEmptyRoleNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/roles/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_name\":null, \"role_description\": \"123\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"'role_name' is not nullable\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRoleWithExistingRoleNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/roles/aba87f7e-0e5c-427f-9e71-2150e31466d9")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"role_name\":\"something\" }");

      when(rolesService.updateRole(any(UUID.class), any(PatchRoleRequest.class), any(User.class)))
          .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRoleByIdRequestedRoleNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/roles/aba87f7e-0e5c-427f-9e71-2150e31466d9")
          .contentType(MediaType.APPLICATION_JSON);

      doThrow(RoleNotFoundException.class).doNothing().when(rolesService).deleteRoleById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRoleByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/roles/aba87f7e-0e5c-427f-9e71-2150e31466d9")
          .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(rolesService).deleteRoleById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
