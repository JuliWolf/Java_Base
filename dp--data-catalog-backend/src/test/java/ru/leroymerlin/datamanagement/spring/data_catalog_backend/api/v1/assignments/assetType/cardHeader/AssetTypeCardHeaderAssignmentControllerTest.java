package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.exceptions.AttributeTypeNotAssignedForAssetTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post.PostAssetTypeCardHeaderAssignmentRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post.PostAssetTypeCardHeaderAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(AssetTypeCardHeaderAssignmentController.class)
@Import(AssetTypeCardHeaderAssignmentController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AssetTypeCardHeaderAssignmentControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AssetTypeCardHeaderAssignmentService assetTypeCardHeaderAssignmentService;

  @Test
  @WithMockCustomUser
  public void createAssetTypeCardHeaderAssignmentEmptyRequiredFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + new UUID(123, 123) + "/assetCardHeader")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"description_field_attribute_type_id\": null }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Required fields are empty.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeCardHeaderAssignmentInvalidAssetTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + "123" + "/assetCardHeader")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"description_field_attribute_type_id\": \"" + UUID.randomUUID() + "\", \"owner_field_role_id\": \"" + UUID.randomUUID() + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Request error\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeCardHeaderAssignmentAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + UUID.randomUUID() + "/assetCardHeader")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"description_field_attribute_type_id\": \"" + UUID.randomUUID() + "\", \"owner_field_role_id\": \"" + UUID.randomUUID() + "\" }");

      when(assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(any(UUID.class), any(PostAssetTypeCardHeaderAssignmentRequest.class), any(User.class)))
        .thenThrow(new AssetTypeNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset type not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeCardHeaderAssignmentRoleNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + UUID.randomUUID() + "/assetCardHeader")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"description_field_attribute_type_id\": \"" + UUID.randomUUID() + "\", \"owner_field_role_id\": \"" + UUID.randomUUID() + "\" }");

      when(assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(any(UUID.class), any(PostAssetTypeCardHeaderAssignmentRequest.class), any(User.class)))
        .thenThrow(new RoleNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Role not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeCardHeaderAssignmentAttributeTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + UUID.randomUUID() + "/assetCardHeader")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"description_field_attribute_type_id\": \"" + UUID.randomUUID() + "\", \"owner_field_role_id\": \"" + UUID.randomUUID() + "\" }");

      when(assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(any(UUID.class), any(PostAssetTypeCardHeaderAssignmentRequest.class), any(User.class)))
        .thenThrow(new AttributeTypeNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Attribute type not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeCardHeaderAssignmentAttributeTypeNotAssignedForAssetTypeTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + UUID.randomUUID() + "/assetCardHeader")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"description_field_attribute_type_id\": \"" + UUID.randomUUID() + "\", \"owner_field_role_id\": \"" + UUID.randomUUID() + "\" }");

      when(assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(any(UUID.class), any(PostAssetTypeCardHeaderAssignmentRequest.class), any(User.class)))
        .thenThrow(new AttributeTypeNotAssignedForAssetTypeException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeCardHeaderAssignmentAssignmentAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + UUID.randomUUID() + "/assetCardHeader")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"description_field_attribute_type_id\": \"" + UUID.randomUUID() + "\", \"owner_field_role_id\": \"" + UUID.randomUUID() + "\" }");

      when(assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(any(UUID.class), any(PostAssetTypeCardHeaderAssignmentRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Card header assignment already exists for this asset type\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeCardHeaderAssignmentSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/assetType/" + new UUID(123, 123) + "/assetCardHeader")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"description_field_attribute_type_id\": \"" + UUID.randomUUID() + "\", \"owner_field_role_id\": \"" + UUID.randomUUID() + "\" }");

      when(assetTypeCardHeaderAssignmentService.createAssetTypeCardHeaderAssignment(any(UUID.class), any(PostAssetTypeCardHeaderAssignmentRequest.class), any(User.class)))
        .thenReturn(new PostAssetTypeCardHeaderAssignmentResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeCardHeaderAssignmentInvalidAssignmentIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/assetCardHeader/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset type card header assignment not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeCardHeaderAssignmentAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/assetCardHeader/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(new AssetTypeNotFoundException()).doNothing()
        .when(assetTypeCardHeaderAssignmentService)
        .deleteAssetTypeCardHeaderAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset type card header assignment not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeCardHeaderAssignmentSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/assetType/assetCardHeader/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(assetTypeCardHeaderAssignmentService)
        .deleteAssetTypeCardHeaderAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("{\"result\":\"Asset type card header assignment was successfully deleted.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
