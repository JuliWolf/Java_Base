package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.DevPortalController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.DevPortalService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.devPortal.exceptions.BusinessTermNotFoundException;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(DevPortalController.class)
@Import(DevPortalController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class DevPortalControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private DevPortalService devPortalService;

  @Test
  @WithMockCustomUser
  public void getBusinessTermsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/business-terms")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(devPortalService.getBusinessTerms(any(String.class), any(String.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetBusinessTermsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getBusinessTermByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/business-terms/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(devPortalService.getBusinessTermById(any(UUID.class)))
        .thenReturn(new GetBusinessTermsResponse.GetBusinessTermResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getBusinessTermByIdIllegalArgumentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/business-terms/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested business term not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getBusinessTermByIdBusinessTermNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/business-terms/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(devPortalService.getBusinessTermById(any(UUID.class)))
        .thenThrow(BusinessTermNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested business term not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getBusinessTermAttributesSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/business-terms/" + UUID.randomUUID() + "/business-attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(devPortalService.getBusinessTermAttributes(any(BusinessTermAttributeRequest.class)))
        .thenReturn(new BusinessTermAttributesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getBusinessTermAttributesIllegalArgumentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/business-terms/123" + UUID.randomUUID() + "/business-attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested business term not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getBusinessTermAttributesBusinessTermNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/business-terms/" + UUID.randomUUID() + "/business-attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(devPortalService.getBusinessTermAttributes(any(BusinessTermAttributeRequest.class)))
        .thenThrow(BusinessTermNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested business term not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getBusinessTermRelationshipsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/business-terms/" + UUID.randomUUID() + "/business-term-relationships")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(devPortalService.getBusinessTermRelationships(any(BusinessTermRelationshipsRequest.class)))
        .thenReturn(new BusinessTermRelationshipsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getBusinessTermRelationshipsIllegalArgumentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/business-terms/123" + UUID.randomUUID() + "/business-term-relationships")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested business term not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getBusinessTermRelationshipsBusinessTermNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/business-terms/" + UUID.randomUUID() + "/business-term-relationships")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(devPortalService.getBusinessTermRelationships(any(BusinessTermRelationshipsRequest.class)))
        .thenThrow(BusinessTermNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested business term not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
