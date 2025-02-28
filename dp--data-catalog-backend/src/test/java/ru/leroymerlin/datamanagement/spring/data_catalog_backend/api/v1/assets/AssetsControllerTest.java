package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNameAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNameDoesNotMatchPatternException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.AssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.ChildrenSortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.GetAssetHeaderResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostAssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(AssetsController.class)
@Import(AssetsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AssetsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AssetsService assetsService;

  PostOrPatchAssetRequest request = new PostOrPatchAssetRequest("test asset name", "test displayName", new UUID(123, 123).toString(), new UUID(1234, 1234).toString(), new UUID(12, 12).toString());
  PostOrPatchAssetRequest secondRequest = new PostOrPatchAssetRequest("test asset name 2", "test displayName 2", new UUID(123, 123).toString(), new UUID(1234, 1234).toString(), new UUID(12, 12).toString());

  @Test
  @WithMockCustomUser
  public void createAssetSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.createAsset(any(PostOrPatchAssetRequest.class), any(User.class)))
        .thenReturn(new PostAssetResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetLongAssetNameValueTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + StringUtils.repeat("*", 256) + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.createAsset(any(PostOrPatchAssetRequest.class), any(User.class)))
        .thenReturn(new PostAssetResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetEmptyRequiredFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetInvalidIdFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"123\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.createAsset(any(PostOrPatchAssetRequest.class), any(User.class)))
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
  public void createAssetAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.createAsset(any(PostOrPatchAssetRequest.class), any(User.class)))
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
  public void createAssetAssetNameMaskValidationErrorTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.createAsset(any(PostOrPatchAssetRequest.class), any(User.class)))
        .thenThrow(new AssetNameDoesNotMatchPatternException("example"));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset name doesn't match 'example' pattern\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetAssignmentsNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.createAsset(any(PostOrPatchAssetRequest.class), any(User.class)))
        .thenThrow(AssetTypeStatusAssignmentNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetAssetAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.createAsset(any(PostOrPatchAssetRequest.class), any(User.class)))
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
  public void createAssetsBulkSuccessTest () {
    try {
      List<PostOrPatchAssetRequest> requests = new ArrayList<>();
      requests.add(new PostOrPatchAssetRequest(request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), request.getLifecycle_status(), null));
      requests.add(new PostOrPatchAssetRequest(secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.createAssetsBulk(any(List.class), any(User.class)))
        .thenReturn(new ArrayList());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetsBulkRequiredFiledAreEmptyTest () {
    try {
      List<PostOrPatchAssetRequest> requests = new ArrayList<>();
      requests.add(new PostOrPatchAssetRequest(request.getAsset_name(), null, request.getAsset_type_id(), request.getLifecycle_status(), null));
      requests.add(new PostOrPatchAssetRequest(null, secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest();
      postRequest.setAsset_name(request.getAsset_name());
      when(assetsService.createAssetsBulk(any(List.class), any(User.class)))
        .thenThrow(new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(postRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Some of required fields are empty.\",\"details\":{\"asset_name\":\"" + request.getAsset_name() + "\",\"asset_displayname\":null,\"asset_type_id\":null,\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetsBulkInvalidFieldLengthTest () {
    try {
      List<PostOrPatchAssetRequest> requests = new ArrayList<>();
      requests.add(new PostOrPatchAssetRequest(StringUtils.repeat("*", 256), request.getAsset_displayname(), request.getAsset_type_id(), request.getLifecycle_status(), null));
      requests.add(new PostOrPatchAssetRequest(secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.createAssetsBulk(any(List.class), any(User.class)))
        .thenThrow(new InvalidFieldLengthException("asset_name", 255));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_name contains too much symbols. Allowed limit is 255\",\"details\":null}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetsBulkEmptyRequestListTest () {
    try {
      List<UUID> requests = new ArrayList<>();

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Empty request list\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetsBulkAssetNameDoesNotMatchPatternTest () {
    try {
      List<PostOrPatchAssetRequest> requests = new ArrayList<>();
      requests.add(new PostOrPatchAssetRequest(request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), request.getLifecycle_status(), null));
      requests.add(new PostOrPatchAssetRequest(secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), "123", null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.createAssetsBulk(any(List.class), any(User.class)))
        .thenThrow(new AssetNameDoesNotMatchPatternException("example", ObjectUtils.convertObjectToMap(request)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset name doesn't match 'example' pattern\",\"details\":{\"asset_name\":\"" + request.getAsset_name() + "\",\"asset_displayname\":\"" + request.getAsset_displayname() + "\",\"asset_type_id\":\"" + request.getAsset_type_id() + "\",\"lifecycle_status\":\"" + request.getLifecycle_status() + "\",\"stewardship_status\":\"" + request.getStewardship_status() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetsBulkIllegalArgumentsTest () {
    try {
      List<PostOrPatchAssetRequest> requests = new ArrayList<>();
      requests.add(new PostOrPatchAssetRequest(request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), request.getLifecycle_status(), null));
      requests.add(new PostOrPatchAssetRequest(secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), "123", null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.createAssetsBulk(any(List.class), any(User.class)))
        .thenThrow(new IllegalArgumentException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Request error\",\"details\":null}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetsBulkDuplicateValueInRequestTest () {
    try {
      List<PostOrPatchAssetRequest> requests = new ArrayList<>();
      requests.add(new PostOrPatchAssetRequest(request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), request.getLifecycle_status(), null));
      requests.add(new PostOrPatchAssetRequest(request.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), "123", null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      PostOrPatchAssetRequest postRequest = new PostOrPatchAssetRequest();
      postRequest.setAsset_name(request.getAsset_name());
      when(assetsService.createAssetsBulk(any(List.class), any(User.class)))
        .thenThrow(new DuplicateValueInRequestException("Duplicate asset_name in request", ObjectUtils.convertObjectToMap(postRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Duplicate asset_name in request\",\"details\":{\"asset_name\":\"" + request.getAsset_name() + "\",\"asset_displayname\":null,\"asset_type_id\":null,\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetsBulkAssetTypeNotFoundTest () {
    try {
      List<PostOrPatchAssetRequest> requests = new ArrayList<>();
      requests.add(new PostOrPatchAssetRequest(request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), request.getLifecycle_status(), null));
      requests.add(new PostOrPatchAssetRequest(secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      PostOrPatchAssetRequest request = new PostOrPatchAssetRequest();
      request.setAsset_type_id(new UUID(123, 123).toString());
      when(assetsService.createAssetsBulk(any(List.class), any(User.class)))
        .thenThrow(new AssetTypeNotFoundException(ObjectUtils.convertObjectToMap(request)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset type not found\",\"details\":{\"asset_name\":null,\"asset_displayname\":null,\"asset_type_id\":\"" + new UUID(123, 123) + "\",\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetsBulkAssetTypeStatusAssignmentNotFoundTest () {
    try {
      List<PostOrPatchAssetRequest> requests = new ArrayList<>();
      requests.add(new PostOrPatchAssetRequest(request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), request.getLifecycle_status(), null));
      requests.add(new PostOrPatchAssetRequest(secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      PostOrPatchAssetRequest request = new PostOrPatchAssetRequest();
      request.setLifecycle_status(new UUID(1234, 1234).toString());
      when(assetsService.createAssetsBulk(any(List.class), any(User.class)))
        .thenThrow(new AssetTypeStatusAssignmentNotFoundException(ObjectUtils.convertObjectToMap(request)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset type status assignment not found\",\"details\":{\"asset_name\":null,\"asset_displayname\":null,\"asset_type_id\":null,\"lifecycle_status\":\""+ new UUID(1234, 1234) +"\",\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAssetsBulkAssetNameAlreadyExistsTest () {
    try {
      List<PostOrPatchAssetRequest> requests = new ArrayList<>();
      requests.add(new PostOrPatchAssetRequest(request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), request.getLifecycle_status(), null));
      requests.add(new PostOrPatchAssetRequest(secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.createAssetsBulk(any(List.class), any(User.class)))
        .thenThrow(new AssetNameAlreadyExistsException(new DataIntegrityViolationException("Detail: Key (asset_name, deleted_flag)=(123, 124, f) already exists.")));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset already exists.\",\"details\":{\"asset_name\":\"123, 124\",\"asset_displayname\":null,\"asset_type_id\":null,\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.updateAsset(any(UUID.class), any(PostOrPatchAssetRequest.class), any(User.class)))
        .thenReturn(new AssetResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetEmptyFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/" + new UUID(123, 123))
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
  public void updateAssetInvalidAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/123" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetLongAssetNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/123" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + StringUtils.repeat('&', 256) + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetAssetTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.updateAsset(any(UUID.class), any(PostOrPatchAssetRequest.class), any(User.class)))
        .thenThrow(AssetTypeNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetAssignmentsNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.updateAsset(any(UUID.class), any(PostOrPatchAssetRequest.class), any(User.class)))
        .thenThrow(AssetTypeStatusAssignmentNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetAssetAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.updateAsset(any(UUID.class), any(PostOrPatchAssetRequest.class), any(User.class)))
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
  public void updateAssetAssetNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
      .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.updateAsset(any(UUID.class), any(PostOrPatchAssetRequest.class), any(User.class)))
        .thenThrow(AssetNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetAssetNameDoesNotMatchPatternTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_name\": \"" + request.getAsset_name() + "\", \"asset_displayname\": \"" + request.getAsset_displayname() + "\", \"asset_type_id\": \"" + request.getAsset_type_id() + "\", \"lifecycle_status\": \"" + request.getLifecycle_status() + "\" }");

      when(assetsService.updateAsset(any(UUID.class), any(PostOrPatchAssetRequest.class), any(User.class)))
        .thenThrow(new AssetNameDoesNotMatchPatternException("example"));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset name doesn't match 'example' pattern\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetBulkSomeRequiredFieldsAreEmptyTest () {
    try {
      PatchAssetRequest invalidRequest = new PatchAssetRequest(null, request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), null, null);
      List<PatchAssetRequest> requests = new ArrayList<>();
      requests.add(invalidRequest);
      requests.add(new PatchAssetRequest(UUID.randomUUID(), secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.updateBulkAsset(any(List.class), any(User.class)))
        .thenThrow(new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(invalidRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Some of required fields are empty.\",\"details\":{\"asset_id\":null,\"asset_name\":\"" + request.getAsset_name() + "\",\"asset_displayname\":\"" + invalidRequest.getAsset_displayname() +"\",\"asset_type_id\":\"" + invalidRequest.getAsset_type_id() + "\",\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetBulkAssetNameDoesNotMatchPatternTest () {
    try {
      PatchAssetRequest invalidRequest = new PatchAssetRequest(null, request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), null, null);
      List<PatchAssetRequest> requests = new ArrayList<>();
      requests.add(invalidRequest);
      requests.add(new PatchAssetRequest(UUID.randomUUID(), secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.updateBulkAsset(any(List.class), any(User.class)))
        .thenThrow(new AssetNameDoesNotMatchPatternException("example", ObjectUtils.convertObjectToMap(invalidRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset name doesn't match 'example' pattern\",\"details\":{\"asset_id\":null,\"asset_name\":\"" + request.getAsset_name() + "\",\"asset_displayname\":\"" + invalidRequest.getAsset_displayname() +"\",\"asset_type_id\":\"" + invalidRequest.getAsset_type_id() + "\",\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetBulkIllegalArgumentsTest () {
    try {
      PatchAssetRequest invalidRequest = new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), null, null);
      List<PatchAssetRequest> requests = new ArrayList<>();
      requests.add(invalidRequest);
      requests.add(new PatchAssetRequest(UUID.randomUUID(), secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.updateBulkAsset(any(List.class), any(User.class)))
        .thenThrow(IllegalArgumentException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Request error\",\"details\":null}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetBulkInvalidFieldsLengthTest () {
    try {
      PatchAssetRequest invalidRequest = new PatchAssetRequest(UUID.randomUUID(), StringUtils.repeat("*", 256), request.getAsset_displayname(), request.getAsset_type_id(), null, null);
      List<PatchAssetRequest> requests = new ArrayList<>();
      requests.add(invalidRequest);
      requests.add(new PatchAssetRequest(UUID.randomUUID(), secondRequest.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.updateBulkAsset(any(List.class), any(User.class)))
        .thenThrow(new InvalidFieldLengthException(ObjectUtils.convertObjectToMap(invalidRequest), "asset_name", 255));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset_name contains too much symbols. Allowed limit is 255\",\"details\":{\"asset_id\":\"" + invalidRequest.getAsset_id() + "\",\"asset_name\":\"" + invalidRequest.getAsset_name() + "\",\"asset_displayname\":\"" + invalidRequest.getAsset_displayname() +"\",\"asset_type_id\":\"" + invalidRequest.getAsset_type_id() + "\",\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetBulkDuplicateValueInRequestTest () {
    try {
      PatchAssetRequest invalidRequest = new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), null, null);
      List<PatchAssetRequest> requests = new ArrayList<>();
      requests.add(invalidRequest);
      requests.add(new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.updateBulkAsset(any(List.class), any(User.class)))
        .thenThrow(new DuplicateValueInRequestException("Asset name already exists.", ObjectUtils.convertObjectToMap(invalidRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset name already exists.\",\"details\":{\"asset_id\":\"" + invalidRequest.getAsset_id() + "\",\"asset_name\":\"" + invalidRequest.getAsset_name() + "\",\"asset_displayname\":\"" + invalidRequest.getAsset_displayname() +"\",\"asset_type_id\":\"" + invalidRequest.getAsset_type_id() + "\",\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetBulkAssetNotFoundTest () {
    try {
      PatchAssetRequest invalidRequest = new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), null, null);
      List<PatchAssetRequest> requests = new ArrayList<>();
      requests.add(invalidRequest);
      requests.add(new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.updateBulkAsset(any(List.class), any(User.class)))
        .thenThrow(new AssetNotFoundException(ObjectUtils.convertObjectToMap(invalidRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset not found\",\"details\":{\"asset_id\":\"" + invalidRequest.getAsset_id() + "\",\"asset_name\":\"" + invalidRequest.getAsset_name() + "\",\"asset_displayname\":\"" + invalidRequest.getAsset_displayname() +"\",\"asset_type_id\":\"" + invalidRequest.getAsset_type_id() + "\",\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetBulkAssetTypeNotFoundTest () {
    try {
      PatchAssetRequest invalidRequest = new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), null, null);
      List<PatchAssetRequest> requests = new ArrayList<>();
      requests.add(invalidRequest);
      requests.add(new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.updateBulkAsset(any(List.class), any(User.class)))
        .thenThrow(new AssetTypeNotFoundException(ObjectUtils.convertObjectToMap(invalidRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset type not found\",\"details\":{\"asset_id\":\"" + invalidRequest.getAsset_id() + "\",\"asset_name\":\"" + invalidRequest.getAsset_name() + "\",\"asset_displayname\":\"" + invalidRequest.getAsset_displayname() +"\",\"asset_type_id\":\"" + invalidRequest.getAsset_type_id() + "\",\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetBulkStatusNotFoundTest () {
    try {
      PatchAssetRequest invalidRequest = new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), null, null);
      List<PatchAssetRequest> requests = new ArrayList<>();
      requests.add(invalidRequest);
      requests.add(new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.updateBulkAsset(any(List.class), any(User.class)))
        .thenThrow(new StatusNotFoundException(ObjectUtils.convertObjectToMap(invalidRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"status not found\",\"details\":{\"asset_id\":\"" + invalidRequest.getAsset_id() + "\",\"asset_name\":\"" + invalidRequest.getAsset_name() + "\",\"asset_displayname\":\"" + invalidRequest.getAsset_displayname() +"\",\"asset_type_id\":\"" + invalidRequest.getAsset_type_id() + "\",\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetBulkAssetTypeStatusAssignmentNotFoundTest () {
    try {
      PatchAssetRequest invalidRequest = new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), null, null);
      List<PatchAssetRequest> requests = new ArrayList<>();
      requests.add(invalidRequest);
      requests.add(new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.updateBulkAsset(any(List.class), any(User.class)))
        .thenThrow(new AssetTypeStatusAssignmentNotFoundException(ObjectUtils.convertObjectToMap(invalidRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset type status assignment not found\",\"details\":{\"asset_id\":\"" + invalidRequest.getAsset_id() + "\",\"asset_name\":\"" + invalidRequest.getAsset_name() + "\",\"asset_displayname\":\"" + invalidRequest.getAsset_displayname() +"\",\"asset_type_id\":\"" + invalidRequest.getAsset_type_id() + "\",\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAssetBulkAssetNameAlreadyExistsTest () {
    try {
      PatchAssetRequest invalidRequest = new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), request.getAsset_displayname(), request.getAsset_type_id(), null, null);
      List<PatchAssetRequest> requests = new ArrayList<>();
      requests.add(invalidRequest);
      requests.add(new PatchAssetRequest(UUID.randomUUID(), request.getAsset_name(), secondRequest.getAsset_displayname(), secondRequest.getAsset_type_id(), null, null));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(assetsService.updateBulkAsset(any(List.class), any(User.class)))
        .thenThrow(new AssetNameAlreadyExistsException(new DataIntegrityViolationException("Detail: Key (asset_name, deleted_flag)=(" + request.getAsset_name() +", f) already exists.")));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset already exists.\",\"details\":{\"asset_name\":\"" + request.getAsset_name()+ "\",\"asset_displayname\":null,\"asset_type_id\":null,\"lifecycle_status\":null,\"stewardship_status\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetById(any(UUID.class)))
        .thenReturn(new AssetResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetByIdInvalidAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/123" + new UUID(123, 123))
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
  public void getAssetByIdAssetNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetById(any(UUID.class)))
        .thenThrow(AssetNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetByParamsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetsByParams(any(GetAssetParams.class)))
        .thenReturn(new GetAssetsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetsChildrenInvalidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/123/children")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid arguments in request\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetsChildrenSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() + "/children")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetsChildren(any(UUID.class), any(String.class), any(List.class), any(List.class), any(List.class), any(ChildrenSortField.class), any(SortOrder.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetAssetsChildrenResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetPathElementsIllegalAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/123/path")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested asset not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetPathElementsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() + "/path")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetPath(any(UUID.class)))
        .thenReturn(new GetAssetPathElementsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetRelationTypesInvalidAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/123" + UUID.randomUUID() + "/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

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
  public void getAssetRelationTypesSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() + "/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetRelationTypes(any(UUID.class)))
        .thenReturn(new GetAssetRelationTypes());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetAttributeLinkUsageIllegalAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/123/links")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested asset not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetAttributeLinkUsageSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() + "/links")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetAttributeLinkUsage(any(UUID.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetAssetAttributeLinksUsageResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetHeaderInvalidAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + "123" + "/header")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested asset not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetHeaderAssetNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() + "/header")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetHeader(any(UUID.class)))
        .thenThrow(new AssetNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested asset not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetHeaderSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() + "/header")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetHeader(any(UUID.class)))
        .thenReturn(new GetAssetHeaderResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetChangeHistory_InvalidAssetId_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + "123" + "/changeHistory?logged_on_min=2023-02-06T12:20:33.471Z&logged_on_max=2025-02-06T12:20:33.471Z")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested asset not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetChangeHistory_AssetNotFound_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() + "/changeHistory?logged_on_min=2023-02-06T12:20:33.471Z&logged_on_max=2025-02-06T12:20:33.471Z")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetChangeHistory(any(UUID.class), any(GetChangeHistoryParams.class)))
        .thenThrow(new AssetNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested asset not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetChangeHistory_Success_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() + "/changeHistory?logged_on_min=2023-02-06T12:20:33.471Z&logged_on_max=2025-02-06T12:20:33.471Z")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetsService.getAssetChangeHistory(any(UUID.class), any(GetChangeHistoryParams.class)))
        .thenReturn(new GetAssetChangeHistory());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assets/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(assetsService).deleteAssetById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetByIdInvalidAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assets/123" + new UUID(123, 123))
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
  public void deleteAssetByIdAssetNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assets/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AssetNotFoundException.class).doNothing().when(assetsService).deleteAssetById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetsBulkSuccessTest () {
    try {
      List<UUID> requests = new ArrayList<>();
      requests.add(UUID.randomUUID());
      requests.add(UUID.randomUUID());

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      doNothing().when(assetsService).deleteAssetsBulk(any(List.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("{\"result\":\"Assets were successfully deleted.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetsBulkEmptyRequestListTest () {
    try {
      List<UUID> requests = new ArrayList<>();

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Empty request list\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetsBulkDuplicateValueInRequestTest () {
    try {
      List<UUID> requests = new ArrayList<>();
      requests.add(new UUID(1234, 1234));
      requests.add(new UUID(1234, 1234) );

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      doThrow(new DuplicateValueInRequestException("Duplicate asset_id in request", Map.of("asset_id", new UUID(1234, 1234))))
        .doNothing().when(assetsService).deleteAssetsBulk(any(List.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Duplicate asset_id in request\",\"details\":{\"asset_id\":\"" + new UUID(1234, 1234) + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetsBulkAssetNotFoundTest () {
    try {
      List<UUID> requests = new ArrayList<>();
      requests.add(UUID.randomUUID());
      requests.add(UUID.randomUUID());

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assets/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      doThrow(new AssetNotFoundException(Map.of("asset_id", new UUID(123, 123))))
        .doNothing().when(assetsService).deleteAssetsBulk(any(List.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested asset not found\",\"details\":{\"asset_id\":\"" + new UUID(123, 123) + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
