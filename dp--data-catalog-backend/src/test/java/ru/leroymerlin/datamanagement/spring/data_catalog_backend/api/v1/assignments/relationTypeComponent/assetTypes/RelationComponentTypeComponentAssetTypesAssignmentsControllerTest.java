package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypesAssignmentsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypesAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.AssetsTypeIsUsedInRelationsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentIsInherited;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.get.GetRelationTypeComponentAssetTypeAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(RelationTypeComponentAssetTypesAssignmentsController.class)
@Import(RelationTypeComponentAssetTypesAssignmentsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class RelationComponentTypeComponentAssetTypesAssignmentsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RelationTypeComponentAssetTypesAssignmentsService relationTypeComponentAssetTypesAssignmentsService;

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAssetTypesAssignmentsInvalidRelationTypeComponentIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/123/asset_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"allowed_asset_type\": [{\"asset_type_id\": \"" + UUID.randomUUID() + "\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAssetTypesAssignmentsEmptyAssetTypesInRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/asset_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"allowed_asset_type\": [] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAssetTypesAssignmentsRelationTypeComponentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/asset_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"allowed_asset_type\": [{\"asset_type_id\": \"" + UUID.randomUUID() + "\"}] }");

      when(relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(any(UUID.class), any(PostRelationTypeComponentAssetTypesRequest.class), any(User.class)))
        .thenThrow(RelationTypeComponentNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAssetTypesAssignmentsInvalidAssetTypeInRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/asset_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"allowed_asset_type\": [{\"asset_type_id\": \"" + UUID.randomUUID() + "\"}] }");

      when(relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(any(UUID.class), any(PostRelationTypeComponentAssetTypesRequest.class), any(User.class)))
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
  public void createRelationTypeComponentAssetTypesAssignmentsAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/asset_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"allowed_asset_type\": [{\"asset_type_id\": \"123\"}] }");

      when(relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(any(UUID.class), any(PostRelationTypeComponentAssetTypesRequest.class), any(User.class)))
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
  public void createRelationTypeComponentAssetTypesAssignmentsAssignmentsAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/asset_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"allowed_asset_type\": [{\"asset_type_id\": \"123\"}] }");

      when(relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(any(UUID.class), any(PostRelationTypeComponentAssetTypesRequest.class), any(User.class)))
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
  public void createRelationTypeComponentAssetTypesAssignmentsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/asset_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"allowed_asset_type\": [{\"asset_type_id\": \"123\"}] }");

      when(relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(any(UUID.class), any(PostRelationTypeComponentAssetTypesRequest.class), any(User.class)))
        .thenReturn(new PostRelationTypeComponentAssetTypesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentIdInvalidRelationTypeComponentIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationTypeComponent/123/assetType")
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
  public void getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentIdRelationTypeComponentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationTypeComponent/" + UUID.randomUUID()+ "/assetType")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypeComponentAssetTypesAssignmentsService.getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentId(any(UUID.class)))
        .thenThrow(RelationTypeComponentNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationTypeComponent/" + UUID.randomUUID()+ "/assetType")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypeComponentAssetTypesAssignmentsService.getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentId(any(UUID.class)))
        .thenReturn(new GetRelationTypeComponentAssetTypeAssignmentsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeComponentAssetTypeAssignmentInvalidAssignmentIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationTypeComponent/assetType/123")
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
  public void deleteRelationTypeComponentAssetTypeAssignmentAssignmentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationTypeComponent/assetType/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RelationTypeComponentAssetTypeAssignmentNotFound.class)
          .doNothing().when(relationTypeComponentAssetTypesAssignmentsService).deleteRelationTypeComponentAssetTypeAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeComponentAssetTypeAssignmentAssetIsInheritedTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationTypeComponent/assetType/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RelationTypeComponentAssetTypeAssignmentIsInherited.class)
        .doNothing().when(relationTypeComponentAssetTypesAssignmentsService).deleteRelationTypeComponentAssetTypeAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeComponentAssetTypeAssignmentAssetIsUsedInRelationsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationTypeComponent/assetType/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AssetsTypeIsUsedInRelationsException.class)
        .doNothing().when(relationTypeComponentAssetTypesAssignmentsService).deleteRelationTypeComponentAssetTypeAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"This asset type is still used in some relations.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeComponentAssetTypeAssignmentSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationTypeComponent/assetType/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing()
        .when(relationTypeComponentAssetTypesAssignmentsService).deleteRelationTypeComponentAssetTypeAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
