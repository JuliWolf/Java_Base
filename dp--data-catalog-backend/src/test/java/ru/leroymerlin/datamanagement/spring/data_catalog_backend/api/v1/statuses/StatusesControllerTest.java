package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.StatusesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.StatusesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.StatusResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.get.GetStatusesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PatchStatusRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PostStatusRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PostStatusResponse;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(StatusesController.class)
@Import(StatusesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class StatusesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private StatusesService statusesService;

  @Test
  @WithMockCustomUser
  public void createStatusEmptyStatusNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/statuses")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createStatusLongStatusNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/statuses")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"" + StringUtils.repeat('2', 256) + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"status_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createStatusLongStatusDescriptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/statuses")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"some name\", \"status_description\": \"" + StringUtils.repeat('2', 513)+ "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"status_description contains too much symbols. Allowed limit is 512\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createStatusSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/statuses")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"some status\" }");

      when(statusesService.createStatus(any(PostStatusRequest.class), any(User.class)))
        .thenReturn(new PostStatusResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createStatusWithExistingStatusNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/statuses")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"existing status\" }");

      when(statusesService.createStatus(any(PostStatusRequest.class), any(User.class)))
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
  public void updateStatusWithEmptyParamsNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/statuses/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateStatusEmptyStatusNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/statuses/" + new UUID(123, 123))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\":null, \"status_description\": \"123\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"'status_name' is not nullable\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateStatusWithInvalidStatusIdNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/statuses/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"new status\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateStatusNotExistingStatusNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/statuses/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"new status\" }");

      when(statusesService.updateStatus(any(UUID.class), any(PatchStatusRequest.class), any(User.class)))
        .thenThrow(StatusNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateStatusSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/statuses/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"new status\" }");

      when(statusesService.updateStatus(any(UUID.class), any(PatchStatusRequest.class), any(User.class)))
        .thenReturn(new StatusResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateStatusWithExistingStatusNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/statuses/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"new status\" }");

      when(statusesService.updateStatus(any(UUID.class), any(PatchStatusRequest.class), any(User.class)))
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
  public void getStatusByIdWithInvalidStatusIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/statuses/123")
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
  public void getStatusByIdStatusNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/statuses/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"new status\" }");

      when(statusesService.getStatusById(any(UUID.class)))
        .thenThrow(StatusNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getStatusByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/statuses/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_name\": \"new status\" }");

      when(statusesService.getStatusById(any(UUID.class)))
        .thenReturn(new StatusResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getStatusesByNameAndDescriptionEmptyParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/statuses")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(statusesService.getStatusesByParams(any(String.class), any(String.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetStatusesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteStatusByIdInvalidStatusIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/statuses/123")
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
  public void deleteStatusByIdStatusNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/statuses/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(StatusNotFoundException.class).doNothing().when(statusesService).deleteStatusById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteStatusByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/statuses/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(statusesService).deleteStatusById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteStatusByIdStatusIsUsedForAssetTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/statuses/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(StatusIsUsedForAssetException.class).doNothing().when(statusesService).deleteStatusById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
