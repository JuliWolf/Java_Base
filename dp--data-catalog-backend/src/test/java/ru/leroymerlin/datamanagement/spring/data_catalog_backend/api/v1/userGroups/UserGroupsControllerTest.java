package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups;

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
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.get.GetUserGroupsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.userGroups.models.post.PostUserGroupRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(UserGroupsController.class)
@Import(UserGroupsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class UserGroupsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserGroupsService userGroupsService;

  @Test
  @WithMockCustomUser
  public void createUserGroupWithEmptyUserIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/userGroups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUserGroupWithEmptyGroupIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/userGroups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \"" + new UUID(123, 123) + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUserGroupWithInvalidUserIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/userGroups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \"123\", \"group_id\": \"" + new UUID(123, 123) + "\" }");

      when(userGroupsService.createUserGroup(any(PostUserGroupRequest.class), any(User.class)))
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
  public void createUserGroupRoleOrGroupNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/userGroups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \"" + new UUID(123, 123) + "\", \"group_id\": \"" + new UUID(123, 123) + "\" }");

      when(userGroupsService.createUserGroup(any(PostUserGroupRequest.class), any(User.class)))
        .thenThrow(UserNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createUserGroupDataIntegrityViolationExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/userGroups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"user_id\": \"" + new UUID(123, 123) + "\", \"group_id\": \"" + new UUID(123, 123) + "\" }");

      when(userGroupsService.createUserGroup(any(PostUserGroupRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_CONFLICT))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getUserGroupsByUserIdAndRoleIdIllegalArgumentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/userGroups?user_id=123")
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
  public void getUserGroupsByUserIdAndRoleIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/userGroups?user_id=" + new UUID(123,123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(userGroupsService.getUserGroupsByUserIdAndRoleId(any(UUID.class), any(UUID.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetUserGroupsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteUserGroupByIdInvalidUserGroupIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/userGroups/123")
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
  public void deleteUserGroupByIdRequestedUserGroupNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/userGroups/" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(UserGroupNotFoundException.class).doNothing().when(userGroupsService).deleteUserGroupById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteUserGroupByIdIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/userGroups/" + UUID.randomUUID())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(userGroupsService).deleteUserGroupById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
