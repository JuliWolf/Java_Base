package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.RoleActionsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.RoleActionsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.PermissionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.exceptions.RoleActionNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.get.GetRoleActionsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionScopeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostPrivilegeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.models.post.PostRoleActionsRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(RoleActionsController.class)
@Import(RoleActionsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class RoleActionsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RoleActionsService roleActionsService;

  private List<PostPrivilegeRequest> allowedPrivilegesRequest = Arrays.asList(
    new PostPrivilegeRequest(new UUID(1, 1).toString(), new UUID(1, 2).toString(), ActionScopeType.ALL, null, false),
    new PostPrivilegeRequest(new UUID(2, 1).toString(), new UUID(2, 2).toString(), ActionScopeType.ALL, null, false),
    new PostPrivilegeRequest(new UUID(3, 1).toString(), new UUID(3, 2).toString(), ActionScopeType.ALL, null, false)
  );

  private PostRoleActionsRequest request = new PostRoleActionsRequest(new UUID(123, 123).toString(), allowedPrivilegesRequest, new ArrayList<>());

  @Test
  @WithMockCustomUser
  public void createRoleActionsEmptyRoleIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/roleActions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRoleActionsEmptyPrivilegesTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/roleActions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \""+ request.getRole_id() +"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRoleActionsInvalidRoleIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/roleActions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \"123\", \"privilege_allowed\": \""+ request.getPrivilege_allowed() +"\"}");

      when(roleActionsService.createRoleActions(any(PostRoleActionsRequest.class), any(User.class)))
        .thenThrow(IllegalArgumentException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRoleActionsDataIntegrityErrorTest () {
    try {
      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String privilegeAllowed = objectWriter.writeValueAsString(request.getPrivilege_allowed());

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/roleActions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \""+ request.getRole_id() +"\",\"privilege_allowed\":" + privilegeAllowed + "}");

      when(roleActionsService.createRoleActions(any(PostRoleActionsRequest.class), any(User.class)))
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
  public void createRoleActionsObjectNotFoundExceptionTest () {
    try {
      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String privilegeAllowed = objectWriter.writeValueAsString(request.getPrivilege_allowed());

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/roleActions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \""+ request.getRole_id() +"\",\"privilege_allowed\":" + privilegeAllowed + "}");

      when(roleActionsService.createRoleActions(any(PostRoleActionsRequest.class), any(User.class)))
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
  public void getRoleActionsByParamsWrongParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/roleActions?role_id=123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRoleActionsByParamsEmptyParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/roleActions")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(roleActionsService.getRoleActionsByParams(
        any(UUID.class), any(UUID.class), any(UUID.class), any(ActionScopeType.class), any(PermissionType.class), any(UUID.class), any(Integer.class), any(Integer.class))
      ).thenReturn(new GetRoleActionsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRoleActionWrongRoleActionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/roleActions/123")
        .with(csrf())
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
  public void deleteRoleActionRoleActionNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/roleActions/607af026-5f55-42ef-b41c-558feb3926a2")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RoleActionNotFoundException.class).doNothing().when(roleActionsService).deleteRoleActionById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRoleActionSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/roleActions/607af026-5f55-42ef-b41c-558feb3926a2")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(roleActionsService).deleteRoleActionById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
