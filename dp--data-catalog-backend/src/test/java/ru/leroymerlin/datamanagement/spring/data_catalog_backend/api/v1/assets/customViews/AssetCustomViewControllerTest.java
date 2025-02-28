package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.exceptions.CustomViewHeaderQueryIsEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.exceptions.CustomViewTableQueryIsEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.models.get.GetAssetCustomViewHeaderRows;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.models.get.GetAssetCustomViewTableRows;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(AssetCustomViewController.class)
@Import(AssetCustomViewController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AssetCustomViewControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AssetCustomViewService assetCustomViewService;

  @Test
  @WithMockCustomUser
  public void getAssetCustomViewHeaderRowsInvalidAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/123/customViews/123/headerRows")
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
  public void getAssetCustomViewHeaderRowsInvalidCustomViewIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() +"/customViews/123/headerRows")
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
  public void getAssetCustomViewHeaderRowsAssetNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() +"/customViews/" + UUID.randomUUID() + "/headerRows")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetCustomViewService.getAssetCustomViewHeaderRows(any(UUID.class), any(UUID.class)))
        .thenThrow(new AssetNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetCustomViewHeaderRowsCustomViewNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() +"/customViews/" + UUID.randomUUID() + "/headerRows")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetCustomViewService.getAssetCustomViewHeaderRows(any(UUID.class), any(UUID.class)))
        .thenThrow(new CustomViewNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"custom view not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetCustomViewHeaderRowsCustomViewHeaderQueryIsEmptyTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() +"/customViews/" + UUID.randomUUID() + "/headerRows")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetCustomViewService.getAssetCustomViewHeaderRows(any(UUID.class), any(UUID.class)))
        .thenThrow(new CustomViewHeaderQueryIsEmptyException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Custom view header query is empty\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetCustomViewHeaderRowsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() +"/customViews/" + UUID.randomUUID() + "/headerRows")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetCustomViewService.getAssetCustomViewHeaderRows(any(UUID.class), any(UUID.class)))
        .thenReturn(new GetAssetCustomViewHeaderRows());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetCustomViewTableRowsInvalidAssetIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/123/customViews/123/tableRows?page_size=10&page_number=0")
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
  public void getAssetCustomViewTableRowsInvalidCustomViewIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() +"/customViews/123/tableRows?page_size=10&page_number=0")
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
  public void getAssetCustomViewTableRowsAssetNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() +"/customViews/" + UUID.randomUUID() + "/tableRows?page_size=10&page_number=0")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetCustomViewService.getAssetCustomViewTableRows(any(UUID.class), any(UUID.class), any(Integer.class), any(Integer.class)))
        .thenThrow(new AssetNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"asset not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetCustomViewTableRowsCustomViewNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() +"/customViews/" + UUID.randomUUID() + "/tableRows?page_size=10&page_number=0")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetCustomViewService.getAssetCustomViewTableRows(any(UUID.class), any(UUID.class), any(Integer.class), any(Integer.class)))
        .thenThrow(new CustomViewNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"custom view not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetCustomViewTableRowsCustomViewHeaderQueryIsEmptyTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() +"/customViews/" + UUID.randomUUID() + "/tableRows?page_size=10&page_number=0")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetCustomViewService.getAssetCustomViewTableRows(any(UUID.class), any(UUID.class), any(Integer.class), any(Integer.class)))
        .thenThrow(new CustomViewTableQueryIsEmptyException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Custom view table query is empty\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAssetCustomViewTableRowsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assets/" + UUID.randomUUID() +"/customViews/" + UUID.randomUUID() + "/tableRows?page_size=10&page_number=0")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(assetCustomViewService.getAssetCustomViewTableRows(any(UUID.class), any(UUID.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetAssetCustomViewTableRows());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
