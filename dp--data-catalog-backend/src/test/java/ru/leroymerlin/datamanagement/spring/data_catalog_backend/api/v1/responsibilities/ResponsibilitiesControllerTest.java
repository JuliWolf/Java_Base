package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityAlreadyExistsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.SourceAssetIsNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.get.GetResponsibilitiesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.get.GetResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(ResponsibilitiesController.class)
@Import(ResponsibilitiesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class ResponsibilitiesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ResponsibilitiesService responsibilitiesService;

  @Test
  @WithMockCustomUser
  public void createRelationTypeEmptyRequiredFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeRoleNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \"" + UUID.randomUUID() + "\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"responsible_id\": \"" + UUID.randomUUID() + "\", \"responsible_type\": \"GROUP\" }");

      when(responsibilitiesService.createResponsibility(any(PostResponsibilityRequest.class), any(User.class)))
        .thenThrow(RoleNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeResponsibilityAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \"" + UUID.randomUUID() + "\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"responsible_id\": \"" + UUID.randomUUID() + "\", \"responsible_type\": \"GROUP\" }");

      when(responsibilitiesService.createResponsibility(any(PostResponsibilityRequest.class), any(User.class)))
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
  public void createRelationTypeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \"" + UUID.randomUUID() + "\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"responsible_id\": \"" + UUID.randomUUID() + "\", \"responsible_type\": \"GROUP\" }");

      when(responsibilitiesService.createResponsibility(any(PostResponsibilityRequest.class), any(User.class)))
        .thenReturn(new PostResponsibilityResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createResponsibilitiesBulkSuccessTest () {
    try {
      List<PostResponsibilityRequest> requests = new ArrayList<>();
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.GROUP.name(), UUID.randomUUID()));
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.USER.name(), UUID.randomUUID()));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(responsibilitiesService.createResponsibilitiesBulk(any(List.class), any(User.class)))
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
  public void createResponsibilitiesBulkSomeRequiredFieldsAreEmptyTest () {
    try {
      List<PostResponsibilityRequest> requests = new ArrayList<>();
      requests.add(new PostResponsibilityRequest(null, UUID.randomUUID(), ResponsibleType.GROUP.name(), UUID.randomUUID()));
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.USER.name(), UUID.randomUUID()));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(responsibilitiesService.createResponsibilitiesBulk(any(List.class), any(User.class)))
        .thenThrow(new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(requests.get(0))));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Some of required fields are empty.\",\"details\":{\"asset_id\":" + requests.get(0).getAsset_id() + ",\"role_id\":\"" + requests.get(0).getRole_id() + "\",\"responsible_type\":\"" + requests.get(0).getResponsible_type() + "\",\"responsible_id\":\"" + requests.get(0).getResponsible_id() + "\",\"requestItemId\":\"" + requests.get(0).getRequestItemId() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createResponsibilitiesBulkAssetNotFoundTest () {
    try {
      List<PostResponsibilityRequest> requests = new ArrayList<>();
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.GROUP.name(), UUID.randomUUID()));
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.USER.name(), UUID.randomUUID()));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(responsibilitiesService.createResponsibilitiesBulk(any(List.class), any(User.class)))
        .thenThrow(new AssetNotFoundException(ObjectUtils.convertObjectToMap(requests.get(0))));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested asset not found\",\"details\":{\"asset_id\":\"" + requests.get(0).getAsset_id() + "\",\"role_id\":\"" + requests.get(0).getRole_id() + "\",\"responsible_type\":\"" + requests.get(0).getResponsible_type() + "\",\"responsible_id\":\"" + requests.get(0).getResponsible_id() + "\",\"requestItemId\":\"" + requests.get(0).getRequestItemId() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createResponsibilitiesBulkSourceAssetIsNotAllowedTest () {
    try {
      List<PostResponsibilityRequest> requests = new ArrayList<>();
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.GROUP.name(), UUID.randomUUID()));
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.USER.name(), UUID.randomUUID()));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(responsibilitiesService.createResponsibilitiesBulk(any(List.class), any(User.class)))
        .thenThrow(new SourceAssetIsNotAllowedException(ObjectUtils.convertObjectToMap(requests.get(0))));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Bulk post responsibilities is not allowed for SOURCE-assets.\",\"details\":{\"asset_id\":\"" + requests.get(0).getAsset_id() + "\",\"role_id\":\"" + requests.get(0).getRole_id() + "\",\"responsible_type\":\"" + requests.get(0).getResponsible_type() + "\",\"responsible_id\":\"" + requests.get(0).getResponsible_id() + "\",\"requestItemId\":\"" + requests.get(0).getRequestItemId() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createResponsibilitiesBulkRoleNotFoundTest () {
    try {
      List<PostResponsibilityRequest> requests = new ArrayList<>();
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.GROUP.name(), UUID.randomUUID()));
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.USER.name(), UUID.randomUUID()));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(responsibilitiesService.createResponsibilitiesBulk(any(List.class), any(User.class)))
        .thenThrow(new RoleNotFoundException(ObjectUtils.convertObjectToMap(requests.get(0))));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested role not found\",\"details\":{\"asset_id\":\"" + requests.get(0).getAsset_id() + "\",\"role_id\":\"" + requests.get(0).getRole_id() + "\",\"responsible_type\":\"" + requests.get(0).getResponsible_type() + "\",\"responsible_id\":\"" + requests.get(0).getResponsible_id() + "\",\"requestItemId\":\"" + requests.get(0).getRequestItemId() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createResponsibilitiesBulkUserNotFoundTest () {
    try {
      List<PostResponsibilityRequest> requests = new ArrayList<>();
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.GROUP.name(), UUID.randomUUID()));
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.USER.name(), UUID.randomUUID()));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(responsibilitiesService.createResponsibilitiesBulk(any(List.class), any(User.class)))
        .thenThrow(new UserNotFoundException(ObjectUtils.convertObjectToMap(requests.get(0))));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested user not found\",\"details\":{\"asset_id\":\"" + requests.get(0).getAsset_id() + "\",\"role_id\":\"" + requests.get(0).getRole_id() + "\",\"responsible_type\":\"" + requests.get(0).getResponsible_type() + "\",\"responsible_id\":\"" + requests.get(0).getResponsible_id() + "\",\"requestItemId\":\"" + requests.get(0).getRequestItemId() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createResponsibilitiesBulkGroupNotFoundTest () {
    try {
      List<PostResponsibilityRequest> requests = new ArrayList<>();
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.GROUP.name(), UUID.randomUUID()));
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.USER.name(), UUID.randomUUID()));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(responsibilitiesService.createResponsibilitiesBulk(any(List.class), any(User.class)))
        .thenThrow(new GroupNotFoundException(ObjectUtils.convertObjectToMap(requests.get(0))));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested group not found\",\"details\":{\"asset_id\":\"" + requests.get(0).getAsset_id() + "\",\"role_id\":\"" + requests.get(0).getRole_id() + "\",\"responsible_type\":\"" + requests.get(0).getResponsible_type() + "\",\"responsible_id\":\"" + requests.get(0).getResponsible_id() + "\",\"requestItemId\":\"" + requests.get(0).getRequestItemId() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createResponsibilitiesBulkResponsibilityAlreadyExistsTest () {
    try {
      List<PostResponsibilityRequest> requests = new ArrayList<>();
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.GROUP.name(), UUID.randomUUID()));
      requests.add(new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), ResponsibleType.USER.name(), UUID.randomUUID()));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(responsibilitiesService.createResponsibilitiesBulk(any(List.class), any(User.class)))
        .thenThrow(new ResponsibilityAlreadyExistsException(new DataIntegrityViolationException("Detail: Key (asset_id, role_id, responsible_type, user_id, deleted_flag)=(" + requests.get(0).getAsset_id() + ", " + requests.get(0).getRole_id() + ", " + requests.get(0).getResponsible_type() + ", " + requests.get(0).getResponsible_id() + ", f) already exists.")));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Responsibility with these parameters already exists.\",\"details\":{\"asset_id\":\"" + requests.get(0).getAsset_id() + "\",\"role_id\":\"" + requests.get(0).getRole_id() + "\",\"responsible_type\":\"" + requests.get(0).getResponsible_type() + "\",\"responsible_id\":\"" + requests.get(0).getResponsible_id() + "\",\"requestItemId\":\"" + requests.get(0).getRequestItemId() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getResponsibilityByIdIllegalResponsibilityIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/responsibilities/123")
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
  public void getResponsibilityByIdResponsibilityNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/responsibilities/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(responsibilitiesService.getResponsibilityById(any(UUID.class)))
        .thenThrow(ResponsibilityNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getResponsibilityByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/responsibilities/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(responsibilitiesService.getResponsibilityById(any(UUID.class)))
        .thenReturn(new GetResponsibilityResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getResponsibilitiesByParamsInvalidArgumentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/responsibilities?role_id=123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(responsibilitiesService.getResponsibilitiesByParams(
        nullable(List.class),
        nullable(List.class),
        nullable(List.class),
        nullable(List.class),
        nullable(List.class),
        nullable(List.class),
        nullable(List.class),
        nullable(Boolean.class),
        nullable(SortField.class),
        nullable(SortOrder.class),
        nullable(Integer.class),
        nullable(Integer.class))
      )
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
  public void getResponsibilitiesByParamsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/responsibilities?asset_id=" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(responsibilitiesService.getResponsibilitiesByParams(
        any(List.class),
        any(List.class),
        any(List.class),
        any(List.class),
        any(List.class),
        any(List.class),
        any(List.class),
        any(Boolean.class),
        nullable(SortField.class),
        nullable(SortOrder.class),
        any(Integer.class),
        any(Integer.class))
      )
        .thenReturn(new GetResponsibilitiesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteResponsibilityByIdInvalidResponsibilityIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/responsibilities/123")
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
  public void deleteResponsibilityByIdResponsibilityNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/responsibilities/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(ResponsibilityNotFoundException.class).doNothing().when(responsibilitiesService)
          .deleteResponsibilityById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteResponsibilityByIdResponsibilityIsInheritedTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/responsibilities/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(ResponsibilityIsInheritedException.class).doNothing().when(responsibilitiesService)
        .deleteResponsibilityById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Deleting of inherited responsibilities is not allowed\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteResponsibilityByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/responsibilities/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(responsibilitiesService)
        .deleteResponsibilityById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
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

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      doNothing().when(responsibilitiesService).deleteResponsibilitiesBulk(any(List.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("{\"result\":\"Responsibilities were successfully deleted.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetsBulkDuplicateValuesInRequestTest () {
    try {
      UUID uuid = UUID.randomUUID();
      List<UUID> requests = new ArrayList<>();
      requests.add(uuid);
      requests.add(UUID.randomUUID());

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      doThrow(new DuplicateValueInRequestException("Duplicate responsibility_id in request", Map.of("responsibility_id", uuid)))
        .when(responsibilitiesService).deleteResponsibilitiesBulk(any(List.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Duplicate responsibility_id in request\",\"details\":{\"responsibility_id\":\"" + uuid + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetsBulkResponsibilityIsInheritedExceptionTest () {
    try {
      UUID uuid = UUID.randomUUID();
      List<UUID> requests = new ArrayList<>();
      requests.add(uuid);
      requests.add(UUID.randomUUID());

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      doThrow(new ResponsibilityIsInheritedException(Map.of("responsibility_id", uuid)))
        .when(responsibilitiesService).deleteResponsibilitiesBulk(any(List.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Deleting of inherited responsibilities is not allowed\",\"details\":{\"responsibility_id\":\"" + uuid + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAssetsBulkResponsibilityNotFoundExceptionTest () {
    try {
      UUID uuid = UUID.randomUUID();
      List<UUID> requests = new ArrayList<>();
      requests.add(uuid);
      requests.add(UUID.randomUUID());

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/responsibilities/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      doThrow(new ResponsibilityNotFoundException(Map.of("responsibility_id", uuid)))
        .when(responsibilitiesService).deleteResponsibilitiesBulk(any(List.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested responsibility not found.\",\"details\":{\"responsibility_id\":\"" + uuid + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
