package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.AssetTypeAttributeTypesAssignmentsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.AssetTypeAttributeTypesAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AttributeTypeIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.GetAssetTypeAttributeTypeAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.GetAssetTypeAttributeTypesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributeTypesAssignmentsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(AssetTypeAttributeTypesAssignmentsController.class)
@Import(AssetTypeAttributeTypesAssignmentsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AssetTypeAttributeTypesAssignmentsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AssetTypeAttributeTypesAssignmentsService assetTypeAttributeTypesAssignmentsService;

  @Test
  @WithMockCustomUser
  public void createAssetTypeAttributeTypesAssignmentsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + new UUID(123, 123) + "/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [ {\"attribute_id\": \"" + new UUID(123, 123)+ "\"} ] }");

      when(assetTypeAttributeTypesAssignmentsService.createAssetTypeAttributeTypesAssignments(any(UUID.class), any(PostAssetTypeAttributeTypesAssignmentsRequest.class), any(User.class)))
        .thenReturn(new PostAssetTypeAttributesAssignmentsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeAttributeTypesAssignmentsEmptyStatusAssignmentTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + new UUID(123, 123) + "/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeAttributeTypesAssignmentsInvalidAssetTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/123/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [ {\"attribute_id\": \"" + new UUID(123, 123)+ "\"} ] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeAttributeTypesAssignmentsAttributeTypeNotFoundExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + new UUID(123, 123) + "/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [ {\"attribute_id\": \"" + new UUID(123, 123)+ "\"} ] }");

      when(assetTypeAttributeTypesAssignmentsService.createAssetTypeAttributeTypesAssignments(any(UUID.class), any(PostAssetTypeAttributeTypesAssignmentsRequest.class), any(User.class)))
        .thenThrow(AttributeTypeNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeAttributeTypesAssignmentsAssignmentAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + new UUID(123, 123) + "/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [ {\"attribute_id\": \"" + new UUID(123, 123)+ "\"} ] }");

      when(assetTypeAttributeTypesAssignmentsService.createAssetTypeAttributeTypesAssignments(any(UUID.class), any(PostAssetTypeAttributeTypesAssignmentsRequest.class), any(User.class)))
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
  public void getAssetTypeAttributeTypesAssignmentsByAssetTypeIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetType/" + new UUID(123, 123) + "/attribute_type")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByAssetTypeId(any(UUID.class)))
        .thenReturn(new GetAssetTypeAttributeTypesAssignmentsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetTypeAttributeTypesAssignmentsResponseInvalidAssetTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetType/123/attribute_type")
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
  public void getAssetTypeAttributeTypesAssignmentsByAssetTypeIdAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetType/" + new UUID(123, 123) + "/attribute_type")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByAssetTypeId(any(UUID.class)))
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
  public void getAssetTypeAttributeTypesAssignmentsByParams_successTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetTypes/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(nullable(String.class), nullable(String.class), nullable(SortField.class), nullable(SortOrder.class), nullable(Integer.class), nullable(Integer.class)))
        .thenReturn(new GetAssetTypeAttributeTypeAssignmentsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetTypeAttributeTypesAssignmentsByParams_illegalParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetTypes/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(nullable(String.class), nullable(String.class), nullable(SortField.class), nullable(SortOrder.class), nullable(Integer.class), nullable(Integer.class)))
        .thenThrow(IllegalArgumentException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid params\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetTypeAttributeTypesAssignmentsByParams_assetTypeNotFoundExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetTypes/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypeAttributeTypesAssignmentsService.getAssetTypeAttributeTypesAssignmentsByParams(nullable(String.class), nullable(String.class), nullable(SortField.class), nullable(SortOrder.class), nullable(Integer.class), nullable(Integer.class)))
        .thenThrow(new AssetTypeNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested asset type not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeAttributeTypeAssignmentByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/attributeType/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(assetTypeAttributeTypesAssignmentsService).deleteAssetTypeAttributeTypeAssignmentById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeAttributeTypeAssignmentByIdInvalidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/attributeType/123")
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
  public void deleteAssetTypeAttributeTypeAssignmentByIdAssetTypeAttributeTypeAssignmentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/attributeType/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AssetTypeAttributeTypeAssignmentNotFoundException.class).doNothing().when(assetTypeAttributeTypesAssignmentsService).deleteAssetTypeAttributeTypeAssignmentById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeAttributeTypeAssignmentByIdAssignmentIsInheritedTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/attributeType/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AssetTypeAttributeTypeAssignmentIsInheritedException.class).doNothing().when(assetTypeAttributeTypesAssignmentsService).deleteAssetTypeAttributeTypeAssignmentById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeAttributeTypeAssignmentByIdAttributeTypeIsUsedForAssetTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/attributeType/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AttributeTypeIsUsedForAssetException.class).doNothing().when(assetTypeAttributeTypesAssignmentsService).deleteAssetTypeAttributeTypeAssignmentById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeAttributeTypeAssignmentByIdAttributeTypeIsUsedForAssetFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/attributeType/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AttributeTypeIsUsedForAssetException.class).doNothing().when(assetTypeAttributeTypesAssignmentsService).deleteAssetTypeAttributeTypeAssignmentById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
