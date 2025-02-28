package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups;

import java.net.HttpURLConnection;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.get.GetGroupResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.get.GetGroupsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.models.post.PostGroupResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(GroupsController.class)
@Import(GroupsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class GroupsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private GroupsService groupsService;

  private PostGroupRequest request = new PostGroupRequest("test name", "test description", "test@mail.com", "loop");

  @Test
  @WithMockCustomUser
  public void createGroupWithEmptyGroupNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/groups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"group_name\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createGroupLongGroupNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/groups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"group_name\": \"" + StringUtils.repeat('a', 256) + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"group_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createGroupLongGroupDescriptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/groups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"group_name\": \"" + request.getGroup_name() + "\", \"group_description\": \"" + StringUtils.repeat('z', 256)+ "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"group_description contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createGroupLongGroupMessengerTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/groups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"group_name\": \"" + request.getGroup_name() + "\", \"group_messenger\": \"" + StringUtils.repeat('z', 256)+ "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"group_messenger contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createGroupLongGroupEmailTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/groups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"group_name\": \"" + request.getGroup_name() + "\", \"group_email\": \"" + StringUtils.repeat('z', 256)+ "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"group_email contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createGroupSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/groups")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"group_name\": \"" + request.getGroup_name() + "\" }");

      when(groupsService.createGroup(any(PostGroupRequest.class), any(User.class)))
        .thenReturn(new PostGroupResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getGroupsByNameAndDescriptionWithNoParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/groups")
        .contentType(MediaType.APPLICATION_JSON);

      when(groupsService.getGroupsByParams(any(String.class), any(String.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetGroupsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  @Test
  @WithMockCustomUser
  public void getGroupsByIdWithInvalidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/groups/123")
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
  public void getGroupByIdNotFoundExceptionIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/groups/" + new UUID(123, 123))
        .contentType(MediaType.APPLICATION_JSON);

      when(groupsService.getGroupById(any(UUID.class)))
        .thenThrow(GroupNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getGroupByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/groups/" + new UUID(123, 123))
        .contentType(MediaType.APPLICATION_JSON);

      when(groupsService.getGroupById(any(UUID.class)))
        .thenReturn(new GetGroupResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteGroupByIdRequestedGroupNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/groups/" + new UUID(123,123))
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(GroupNotFoundException.class).doNothing().when(groupsService).deleteGroupById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteGroupByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/groups/" + new UUID(123,123))
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(groupsService).deleteGroupById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
