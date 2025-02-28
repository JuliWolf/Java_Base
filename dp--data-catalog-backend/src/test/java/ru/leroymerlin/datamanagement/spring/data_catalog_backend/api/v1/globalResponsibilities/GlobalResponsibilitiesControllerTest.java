package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponsibilities;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.GlobalResponsibilitiesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions.GlobalResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get.GetGlobalResponsibilitiesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.get.GetGlobalResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.models.post.CreateGlobalResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(GlobalResponsibilitiesController.class)
@Import(GlobalResponsibilitiesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class GlobalResponsibilitiesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private GlobalResponsibilitiesService globalResponsibilitiesService;

  private CreateGlobalResponsibilityRequest request = new CreateGlobalResponsibilityRequest(new UUID(1, 1).toString(), ResponsibleType.GROUP, new UUID(2, 2).toString());

  @Test
  @WithMockCustomUser
  public void createGlobalResponsibilityEmptyRoleIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/globalResponsibilities")
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
  public void createGlobalResponsibilityEmptyResponsibleIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/globalResponsibilities")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \"" + request.getRole_id() + "\", \"responsible_id\": \"" + request.getResponsible_id() + "\"}");

      MvcResult result = mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();

      assertEquals("{\"error\":\"Empty responsible_type\"}", result.getResponse().getContentAsString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createGlobalResponsibilityInvalidRoleIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/globalResponsibilities")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \"123\", \"responsible_id\": \"" + request.getResponsible_id() + "\", \"responsible_type\": \"" + request.getResponsible_type() + "\"}");

      when(globalResponsibilitiesService.createGlobalResponsibility(any(CreateGlobalResponsibilityRequest.class), any(User.class)))
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
  public void createGlobalResponsibilityDataIntegrityExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/globalResponsibilities")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \"" + request.getRole_id() + "\", \"responsible_id\": \"" + request.getResponsible_id() + "\", \"responsible_type\": \"" + request.getResponsible_type() + "\"}");

      when(globalResponsibilitiesService.createGlobalResponsibility(any(CreateGlobalResponsibilityRequest.class), any(User.class)))
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
  public void createGlobalResponsibilityNotFoundExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/globalResponsibilities")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"role_id\": \"" + request.getRole_id() + "\", \"responsible_id\": \"" + request.getResponsible_id() + "\", \"responsible_type\": \"" + request.getResponsible_type() + "\"}");

      when(globalResponsibilitiesService.createGlobalResponsibility(any(CreateGlobalResponsibilityRequest.class), any(User.class)))
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
  public void getGlobalResponsibilitiesByRoleAndTypeAndResponsibleIllegalArgumentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/globalResponsibilities?role_id=123")
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
  public void getGlobalResponsibilitiesByRoleAndTypeAndResponsibleSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/globalResponsibilities?role_id="+ new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(globalResponsibilitiesService.getGlobalResponsibilitiesByParams(
        any(UUID.class),
        any(UUID.class),
        any(String.class),
        any(SortField.class),
        any(SortOrder.class),
        any(Integer.class),
        any(Integer.class)
      ))
        .thenReturn(new GetGlobalResponsibilitiesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getGlobalResponsibilityByIdInvalidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/globalResponsibilities/123")
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
  public void getGlobalResponsibilityByIdWithValidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/globalResponsibilities/" + new UUID(123,123))
        .contentType(MediaType.APPLICATION_JSON);

      when(globalResponsibilitiesService.getGlobalResponsibilityById(any(UUID.class)))
        .thenReturn(new GetGlobalResponsibilityResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getGlobalResponsibilityByIdResponsibilityNotFoundExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/globalResponsibilities/" + new UUID(123,123))
        .contentType(MediaType.APPLICATION_JSON);

      when(globalResponsibilitiesService.getGlobalResponsibilityById(any(UUID.class)))
        .thenThrow(GlobalResponsibilityNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteGlobalResponsibilityByIdRequestedGlobalResponsibilityNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/globalResponsibilities/" + new UUID(123,123))
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(GlobalResponsibilityNotFoundException.class).doNothing().when(globalResponsibilitiesService).deleteGlobalResponsibilityById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteGlobalResponsibilityByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/globalResponsibilities/" + new UUID(123,123))
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(globalResponsibilitiesService).deleteGlobalResponsibilityById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
