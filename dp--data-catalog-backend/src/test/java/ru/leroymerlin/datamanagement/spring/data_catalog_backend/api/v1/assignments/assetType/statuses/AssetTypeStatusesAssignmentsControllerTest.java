package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.AssetTypeStatusesAssignmentsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.AssetTypeStatusesAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.StatusTypeIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(AssetTypeStatusesAssignmentsController.class)
@Import(AssetTypeStatusesAssignmentsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AssetTypeStatusesAssignmentsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AssetTypeStatusesAssignmentsService assetTypeStatusesAssignmentsService;

  @Test
  @WithMockCustomUser
  public void createAssetTypeStatusesAssignmentsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + new UUID(123, 123) + "/status/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_assignment\": [ {\"status_type\": \"STEWARDSHIP\", \"status_id\": \"" + new UUID(123, 123) + "\"} ] }");

      when(assetTypeStatusesAssignmentsService.createAssetTypeStatusesAssignments(any(UUID.class), any(PostAssetTypeStatusesAssignmentsRequest.class), any(User.class)))
        .thenReturn(new PostAssetTypeStatusesAssignmentsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeStatusesAssignmentsEmptyStatusAssignmentTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + new UUID(123, 123) + "/status/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_assignment\": [] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeStatusesAssignmentsInvalidAssetTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/123/status/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_assignment\": [ {\"status_type\": \"STEWARDSHIP\", \"status_id\": \"" + new UUID(123, 123) + "\"} ] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeStatusesAssignmentsStatusNotFoundExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + new UUID(123, 123) + "/status/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_assignment\": [ {\"status_type\": \"STEWARDSHIP\", \"status_id\": \"" + new UUID(123, 123) + "\"} ] }");

      when(assetTypeStatusesAssignmentsService.createAssetTypeStatusesAssignments(any(UUID.class), any(PostAssetTypeStatusesAssignmentsRequest.class), any(User.class)))
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
  public void createAssetTypeStatusesAssignmentsAssignmentAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + new UUID(123, 123) + "/status/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"status_assignment\": [ {\"status_type\": \"STEWARDSHIP\", \"status_id\": \"" + new UUID(123, 123) + "\"} ] }");

      when(assetTypeStatusesAssignmentsService.createAssetTypeStatusesAssignments(any(UUID.class), any(PostAssetTypeStatusesAssignmentsRequest.class), any(User.class)))
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
  public void getAssetTypeStatusesAssignmentsByStatusTypeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetType/" + new UUID(123, 123) + "/status")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypeStatusesAssignmentsService.getAssetTypeStatusesAssignmentsByParams(any(UUID.class), any(AssignmentStatusType.class)))
        .thenReturn(new GetAssetTypeStatusesAssignmentsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetTypeStatusesAssignmentsByStatusTypeInvalidAssetTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetType/123/status")
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
  public void getAssetTypeStatusesAssignmentsByStatusTypeAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetType/" + new UUID(123, 123) + "/status?status_type=STEWARDSHIP")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypeStatusesAssignmentsService.getAssetTypeStatusesAssignmentsByParams(any(UUID.class), any(AssignmentStatusType.class)))
        .thenThrow(AssetTypeNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetTypeStatusesByParamsInvalidRequestParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetTypes/statuses?status_id=123")
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
  public void getAssetTypeStatusesByParamsEmptyRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetTypes/statuses")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypeStatusesAssignmentsService.getAssetTypeStatusesByParams(any(UUID.class), any(AssignmentStatusType.class), any(UUID.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetAssetTypeStatusesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeStatusesAssignmentByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/status/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(assetTypeStatusesAssignmentsService).deleteAssetTypeStatusAssignmentById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeStatusesAssignmentByIdInvalidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/status/123")
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
  public void deleteAssetTypeStatusesAssignmentByIdAssetTypeStatusAssignmentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/status/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AssetTypeStatusAssignmentNotFoundException.class).doNothing().when(assetTypeStatusesAssignmentsService).deleteAssetTypeStatusAssignmentById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeStatusesAssignmentByIdStatusTypeIsUsedForAssetTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/status/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(StatusTypeIsUsedForAssetException.class).doNothing().when(assetTypeStatusesAssignmentsService).deleteAssetTypeStatusAssignmentById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeStatusesAssignmentByIdStatusTypeIsInheritedTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/status/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AssetTypeStatusAssignmentIsInheritedException.class).doNothing().when(assetTypeStatusesAssignmentsService).deleteAssetTypeStatusAssignmentById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
