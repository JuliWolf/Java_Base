package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes;

import java.net.HttpURLConnection;
import java.util.Optional;
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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetNameValidationMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeHasChildAssetTypesException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypeChildrenResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.get.GetAssetTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PatchAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PostAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PostAssetTypeResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(AssetTypesController.class)
@Import(AssetTypesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AssetTypesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AssetTypesService assetTypesService;

  private final PatchAssetTypeRequest request = new PatchAssetTypeRequest(Optional.of("BI REPORT"), Optional.of("BI REPORT description"), "BI REPORT", "red", null, null);

  @Test
  @WithMockCustomUser
  public void createAssetTypeEmptyRequiredFieldTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assetTypes")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"asset_type_name\": \"" + request.getAsset_type_name() + "\", \"asset_type_acronym\": \"\" }");

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeEmptyNotRequiredFieldTest () {
    try {
      User user = new User();
      user.setUserId(new UUID(1, 2));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assetTypes")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"asset_type_name\": \"" + request.getAsset_type_name() + "\", \"asset_type_acronym\": \"" + request.getAsset_type_acronym() + "\", \"asset_type_description\": \"\", \"asset_type_color\": \"" + request.getAsset_type_color() + "\" }");

      when(assetTypesService.createAssetType(any(PostAssetTypeRequest.class), any(User.class)))
        .thenReturn(new PostAssetTypeResponse());

      mockMvc.perform(requestBuilder)
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeLongAssetTypeNameTest () {
    try {
      User user = new User();
      user.setUserId(new UUID(1, 2));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assetTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_name\": \"" + StringUtils.repeat("*", 256) + "\", \"asset_type_acronym\": \"" + request.getAsset_type_acronym() + "\", \"asset_type_description\": \"\", \"asset_type_color\": \"" + request.getAsset_type_color() + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_type_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeLongAssetTypeAcronymTest () {
    try {
      User user = new User();
      user.setUserId(new UUID(1, 2));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assetTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_name\": \"" +  request.getAsset_type_name() + "\", \"asset_type_acronym\": \"" + StringUtils.repeat('a', 11) + "\", \"asset_type_description\": \"\", \"asset_type_color\": \"" + request.getAsset_type_color() + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_type_acronym contains too much symbols. Allowed limit is 10\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeLongAssetTypeDescriptionTest () {
    try {
      User user = new User();
      user.setUserId(new UUID(1, 2));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assetTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_name\": \"" +  request.getAsset_type_name() + "\", \"asset_type_acronym\": \"" + request.getAsset_type_acronym() + "\", \"asset_type_description\": \""+ StringUtils.repeat('a', 513) + "\", \"asset_type_color\": \"" + request.getAsset_type_color() + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_type_description contains too much symbols. Allowed limit is 512\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeLongAssetTypeColorTest () {
    try {
      User user = new User();
      user.setUserId(new UUID(1, 2));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assetTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_name\": \"" +  request.getAsset_type_name() + "\", \"asset_type_acronym\": \"" + request.getAsset_type_acronym() + "\", \"asset_type_description\": \""+ request.getAsset_type_description() + "\", \"asset_type_color\": \"" + StringUtils.repeat('a', 11) + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_type_color contains too much symbols. Allowed limit is 10\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetTypeWithExistingAssetTypeNameTest () {
    try {
      User user = new User();
      user.setUserId(new UUID(1, 2));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assetTypes")
          .with(csrf())
          .contentType(MediaType.APPLICATION_JSON)
          .content("{ \"asset_type_name\": \"" + request.getAsset_type_name() + "\", \"asset_type_acronym\": \"" + request.getAsset_type_acronym() + "\", \"asset_type_description\": \"\", \"asset_type_color\": \"" + request.getAsset_type_color() + "\" }");

      when(assetTypesService.createAssetType(any(PostAssetTypeRequest.class), any(User.class)))
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
  public void createAssetTypeAssetNameValidationErrorTest () {
    try {
      User user = new User();
      user.setUserId(new UUID(1, 2));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assetTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_name\": \"" + request.getAsset_type_name() + "\", \"asset_type_acronym\": \"" + request.getAsset_type_acronym() + "\", \"asset_type_description\": \"\", \"asset_type_color\": \"" + request.getAsset_type_color() + "\" }");

      when(assetTypesService.createAssetType(any(PostAssetTypeRequest.class), any(User.class)))
        .thenThrow(new AssetNameValidationMaskValidationException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_name_validation_mask_example and asset_name_validation_mask should be filled both\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetTypeByNameAndDescriptionWithNoParamsTest () {
    try {
      when(
        assetTypesService.geAssetTypesByParams(any(Boolean.class), any(String.class), any(String.class), any(Integer.class), any(Integer.class))
      ).thenReturn(new GetAssetTypesResponse());

      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assetTypes")
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
  public void getAssetTypeByIdWithInvalidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assetTypes/123")
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
  public void getAssetTypeByIdWithValidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypesService.getAssetTypeById(any(UUID.class)))
        .thenReturn(new GetAssetTypeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetTypeChildrenInvalidAssetTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assetTypes/123/children")
        .contentType(MediaType.APPLICATION_JSON);

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
  public void getAssetTypeChildrenAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assetTypes/" + UUID.randomUUID() + "/children")
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypesService.getAssetTypeChildren(any(UUID.class), nullable(Integer.class), nullable(Integer.class)))
        .thenThrow(AssetTypeNotFoundException.class);

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
  public void getAssetTypeChildrenSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assetTypes/" + UUID.randomUUID() + "/children")
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypesService.getAssetTypeChildren(any(UUID.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetAssetTypeChildrenResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetTypeByIdAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON);

      when(assetTypesService.getAssetTypeById(any(UUID.class)))
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
  public void updateAssetTypeWithEmptyBodyTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON)
        .content("");

      when(assetTypesService.updateAssetType(any(UUID.class), any(PatchAssetTypeRequest.class), any(User.class)))
        .thenReturn(new PostAssetTypeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetTypeWithInvalidAssetTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assetTypes/123")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_name\":\"something\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetTypeEmptyAssetTypeNameNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_name\":null, \"asset_type_description\": \"123\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"'asset_type_name' is not nullable\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetTypeNotExistingAssetTypeTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_name\":\"something\" }");

      when(assetTypesService.updateAssetType(any(UUID.class), any(PatchAssetTypeRequest.class), any(User.class)))
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
  public void updateAssetTypeLongAssetTypeNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{ \"asset_type_name\":\"" + StringUtils.repeat('a', 256)+ "\" }");

      mockMvc.perform(requestBuilder)
              .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
              .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_type_name contains too much symbols. Allowed limit is 255\"}"))
              .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetTypeWithExistingRoleNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_name\":\"something\" }");

      when(assetTypesService.updateAssetType(any(UUID.class), any(PatchAssetTypeRequest.class), any(User.class)))
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
  public void updateAssetTypeAssetNameValidationMaskTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_name\":\"something\", \"asset_name_validation_mask_example\": \"Hello\" }");

      when(assetTypesService.updateAssetType(any(UUID.class), any(PatchAssetTypeRequest.class), any(User.class)))
        .thenThrow(new AssetNameValidationMaskValidationException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_name_validation_mask_example and asset_name_validation_mask should be filled both\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeByIdRequestedAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AssetTypeNotFoundException.class).doNothing().when(assetTypesService).deleteAssetTypeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(assetTypesService).deleteAssetTypeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetTypeByIdHasChildAssetTypesFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assetTypes/aba87f7e-0e5c-427f-9e71-2150e31466d9")
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AssetTypeHasChildAssetTypesException.class).doNothing().when(assetTypesService).deleteAssetTypeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
