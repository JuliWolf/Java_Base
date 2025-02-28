package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.CustomViewsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.CustomViewsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewQueryDoesNotMatchPatternException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.DroppingTableException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewHeaderRowName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.QueryType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get.GetCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get.GetCustomViewsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PatchCustomViewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PatchCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(CustomViewsController.class)
@Import(CustomViewsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class CustomViewsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CustomViewsService customViewsService;

  private ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

  @Test
  @WithMockCustomUser
  public void createCustomViewEmptyRequiredFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"asset_type_id\": \"\" }");

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
  public void createCustomViewEmptyHeaderAndColumnFieldsTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id(UUID.randomUUID().toString());

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

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
  public void createCustomViewEmptyHeaderQueryAndHeaderRowNamesTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id(UUID.randomUUID().toString());
      postCustomViewRequest.getHeader_row_names().add(new CustomViewHeaderRowName("row name", AttributeKindType.TEXT));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

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
  public void createCustomViewEmptyClearQueryTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id(UUID.randomUUID().toString());
      postCustomViewRequest.setRole_id(UUID.randomUUID().toString());
      postCustomViewRequest.setHeader_select_query("Select value from asset_type");
      postCustomViewRequest.setHeader_clear_query("drop table bt_inheritance;");
      postCustomViewRequest.getHeader_row_names().add(new CustomViewHeaderRowName("row name", AttributeKindType.TEXT));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

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
  public void createCustomViewInvalidAssetTypeIdTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id("123");
      postCustomViewRequest.setHeader_select_query("Select value from asset_type");
      postCustomViewRequest.getHeader_row_names().add(new CustomViewHeaderRowName("row name", AttributeKindType.TEXT));

      when(customViewsService.createCustomView(any(PostCustomViewRequest.class), any(User.class)))
        .thenThrow(IllegalArgumentException.class);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

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
  public void createCustomViewSomeRequiredFieldsAreEmptyTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id(UUID.randomUUID().toString());
      postCustomViewRequest.setHeader_select_query("Select value from asset_type");
      postCustomViewRequest.getHeader_row_names().add(new CustomViewHeaderRowName("row name", AttributeKindType.TEXT));

      when(customViewsService.createCustomView(any(PostCustomViewRequest.class), any(User.class)))
        .thenThrow(new SomeRequiredFieldsAreEmptyException());

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Some of required fields are empty.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createCustomViewRoleNotFoundTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id(UUID.randomUUID().toString());
      postCustomViewRequest.setRole_id(UUID.randomUUID().toString());
      postCustomViewRequest.setHeader_select_query("Select value from asset_type");
      postCustomViewRequest.getHeader_row_names().add(new CustomViewHeaderRowName("row name", AttributeKindType.TEXT));

      when(customViewsService.createCustomView(any(PostCustomViewRequest.class), any(User.class)))
        .thenThrow(new RoleNotFoundException());

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested role not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createCustomViewAssetTypeNotFoundTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id(UUID.randomUUID().toString());
      postCustomViewRequest.setRole_id(UUID.randomUUID().toString());
      postCustomViewRequest.setHeader_select_query("Select value from asset_type");
      postCustomViewRequest.getHeader_row_names().add(new CustomViewHeaderRowName("row name", AttributeKindType.TEXT));

      when(customViewsService.createCustomView(any(PostCustomViewRequest.class), any(User.class)))
        .thenThrow(new AssetTypeNotFoundException());

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

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
  public void createCustomViewQueryDesNotMatchPatternTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id(UUID.randomUUID().toString());
      postCustomViewRequest.setRole_id(UUID.randomUUID().toString());
      postCustomViewRequest.setHeader_select_query("Select value from asset_type");
      postCustomViewRequest.getHeader_row_names().add(new CustomViewHeaderRowName("row name", AttributeKindType.TEXT));

      when(customViewsService.createCustomView(any(PostCustomViewRequest.class), any(User.class)))
        .thenThrow(new CustomViewQueryDoesNotMatchPatternException(QueryType.TABLE, ".*select.*from.*order by.*"));

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Table query doesn't match '.*select.*from.*order by.*' pattern\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createCustomViewDroppingTableTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id(UUID.randomUUID().toString());
      postCustomViewRequest.setRole_id(UUID.randomUUID().toString());
      postCustomViewRequest.setHeader_select_query("DROP table asset_type");
      postCustomViewRequest.getHeader_row_names().add(new CustomViewHeaderRowName("row name", AttributeKindType.TEXT));

      when(customViewsService.createCustomView(any(PostCustomViewRequest.class), any(User.class)))
        .thenThrow(new DroppingTableException());

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Dropping backend tables is strictly FORBIDDEN.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createCustomViewCustomViewAlreadyExistsTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id(UUID.randomUUID().toString());
      postCustomViewRequest.setRole_id(UUID.randomUUID().toString());
      postCustomViewRequest.setHeader_select_query("Select value from asset_type");
      postCustomViewRequest.getHeader_row_names().add(new CustomViewHeaderRowName("row name", AttributeKindType.TEXT));

      when(customViewsService.createCustomView(any(PostCustomViewRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Custom view with this name already exists for this asset type\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createCustomViewSuccessTest () {
    try {
      PostCustomViewRequest postCustomViewRequest = new PostCustomViewRequest();
      postCustomViewRequest.setCustom_view_name("some name");
      postCustomViewRequest.setAsset_type_id(UUID.randomUUID().toString());
      postCustomViewRequest.setRole_id(UUID.randomUUID().toString());
      postCustomViewRequest.setHeader_select_query("Select value from asset_type");
      postCustomViewRequest.getHeader_row_names().add(new CustomViewHeaderRowName("row name", AttributeKindType.TEXT));

      when(customViewsService.createCustomView(any(PostCustomViewRequest.class), any(User.class)))
        .thenReturn(new PostCustomViewResponse());

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/customViews")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectWriter.writeValueAsString(postCustomViewRequest));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateCustomViewEmptyFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"\"}");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"All field are empty\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateCustomViewEmptyHeaderQueryWithHeaderRowNamesFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"some name\", \"header_select_query\":  \"Select something from\"}");

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
  public void updateCustomViewInvalidCustomViewIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/123" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"some name\"}");

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
  public void updateCustomViewCustomViewNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"some name\"}");

      when(customViewsService.updateCustomView(any(UUID.class), any(PatchCustomViewRequest.class), any(User.class)))
        .thenThrow(new CustomViewNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested custom view not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateCustomViewRoleNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"some name\"}");

      when(customViewsService.updateCustomView(any(UUID.class), any(PatchCustomViewRequest.class), any(User.class)))
        .thenThrow(new RoleNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested role not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateCustomViewSomeRequiredFieldsAreEmptyTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"some name\"}");

      when(customViewsService.updateCustomView(any(UUID.class), any(PatchCustomViewRequest.class), any(User.class)))
        .thenThrow(new SomeRequiredFieldsAreEmptyException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"header_row_names and table_column_names can not be null. At least on of the field should not be empty.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateCustomViewIllegalRoleIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"some name\"}");

      when(customViewsService.updateCustomView(any(UUID.class), any(PatchCustomViewRequest.class), any(User.class)))
        .thenThrow(IllegalArgumentException.class);

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
  public void updateCustomViewDroppingTableTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"some name\"}");

      when(customViewsService.updateCustomView(any(UUID.class), any(PatchCustomViewRequest.class), any(User.class)))
        .thenThrow(new DroppingTableException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Dropping backend tables is strictly FORBIDDEN.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateCustomViewQueryDesNotMatchPatternTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"some name\"}");

      when(customViewsService.updateCustomView(any(UUID.class), any(PatchCustomViewRequest.class), any(User.class)))
        .thenThrow(new CustomViewQueryDoesNotMatchPatternException(QueryType.TABLE, ".*select.*from.*order by.*"));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Table query doesn't match '.*select.*from.*order by.*' pattern\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateCustomViewCustomViewAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"some name\"}");

      when(customViewsService.updateCustomView(any(UUID.class), any(PatchCustomViewRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Custom view with this name already exists for this asset type\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateCustomViewSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"custom_view_name\":  \"some name\"}");

      when(customViewsService.updateCustomView(any(UUID.class), any(PatchCustomViewRequest.class), any(User.class)))
        .thenReturn(new PatchCustomViewResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getCustomViewsByParamsIllegalArgumentTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/customViews?role_id=123")
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
  public void getCustomViewsByParamsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/customViews?role_id=" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(customViewsService.getCustomViewsByParams(any(UUID.class), any(UUID.class), any(String.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetCustomViewsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getCustomViewByIdIllegalCustomViewIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/customViews/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested custom view not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getCustomViewByIdCustomViewNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(customViewsService.getCustomViewById(any(UUID.class)))
        .thenThrow(CustomViewNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested custom view not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getCustomViewByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(customViewsService.getCustomViewById(any(UUID.class)))
        .thenReturn(new GetCustomViewResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteCustomViewIllegalCustomViewIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/customViews/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested custom view not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteCustomViewCustomViewNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(CustomViewNotFoundException.class).when(customViewsService)
        .deleteCustomViewById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested custom view not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteCustomViewSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/customViews/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(customViewsService)
        .deleteCustomViewById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
