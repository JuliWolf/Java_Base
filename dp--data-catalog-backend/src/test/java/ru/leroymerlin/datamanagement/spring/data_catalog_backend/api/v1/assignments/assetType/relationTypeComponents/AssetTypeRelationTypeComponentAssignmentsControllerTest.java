package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents;

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
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents.AssetTypeRelationTypeComponentAssignmentsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents.AssetTypeRelationTypeComponentAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents.models.get.GetAssetTypeRelationTypeComponentAssignments;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(AssetTypeRelationTypeComponentAssignmentsController.class)
@Import(AssetTypeRelationTypeComponentAssignmentsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AssetTypeRelationTypeComponentAssignmentsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AssetTypeRelationTypeComponentAssignmentsService assetTypeRelationTypeComponentAssignmentsService;

  @Test
  @WithMockCustomUser
  public void getAssetTypeRelationTypeComponentAssignmentsInvalidAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetType/123/relationTypeComponents")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset type not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetTypeRelationTypeComponentAssignmentsAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetType/" + UUID.randomUUID() + "/relationTypeComponents")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(
        nullable(UUID.class),
        nullable(HierarchyRole.class),
        nullable(ResponsibilityInheritanceRole.class),
        nullable(String.class),
        nullable(Integer.class),
        nullable(Integer.class)
      )).thenThrow(AssetTypeNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset type not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetTypeRelationTypeComponentAssignmentsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/assetType/" + UUID.randomUUID() + "/relationTypeComponents")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(
        nullable(UUID.class),
        nullable(HierarchyRole.class),
        nullable(ResponsibilityInheritanceRole.class),
        nullable(String.class),
        nullable(Integer.class),
        nullable(Integer.class)
      )).thenReturn(new GetAssetTypeRelationTypeComponentAssignments());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
