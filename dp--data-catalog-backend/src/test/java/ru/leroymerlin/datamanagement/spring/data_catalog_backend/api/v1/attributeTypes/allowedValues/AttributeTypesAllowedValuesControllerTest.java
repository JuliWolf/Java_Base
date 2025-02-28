package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues;

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
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.AttributeTypesAllowedValuesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.AttributeTypesAllowedValuesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post.PostAllowedValueRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post.PostAllowedValueResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AttributeTypesAllowedValuesController.class)
@Import(AttributeTypesAllowedValuesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AttributeTypesAllowedValuesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AttributeTypesAllowedValuesService attributeTypesAllowedValuesService;

  @Test
  @WithMockCustomUser
  public void createAttributeTypeAllowedValueEmptyRequiredValuesTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes/allowedValues")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeAllowedValueWrongAttributeTypeTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes/allowedValues")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"123\", \"value\": \"some value\" }");

      when(attributeTypesAllowedValuesService.createAttributeTypeAllowedValue(any(PostAllowedValueRequest.class), any(User.class)))
        .thenThrow(IllegalArgumentException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeAllowedValueAttributeTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes/allowedValues")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"" + UUID.randomUUID() + "\", \"value\": \"some value\" }");

      when(attributeTypesAllowedValuesService.createAttributeTypeAllowedValue(any(PostAllowedValueRequest.class), any(User.class)))
        .thenThrow(AttributeTypeNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeAllowedValueAttributeTypeDoesNotUseValueListTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes/allowedValues")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"" + UUID.randomUUID() + "\", \"value\": \"some value\" }");

      when(attributeTypesAllowedValuesService.createAttributeTypeAllowedValue(any(PostAllowedValueRequest.class), any(User.class)))
        .thenThrow(AttributeTypeDoesNotUseValueListException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeAllowedValueAllowedValueAlreadyExistsListTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes/allowedValues")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"" + UUID.randomUUID() + "\", \"value\": \"some value\" }");

      when(attributeTypesAllowedValuesService.createAttributeTypeAllowedValue(any(PostAllowedValueRequest.class), any(User.class)))
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
  public void createAttributeTypeAllowedValueLongValueTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes/allowedValues")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"" + UUID.randomUUID() + "\", \"value\": \"" + StringUtils.repeat('1', 256) + "\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"value contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeAllowedValueSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes/allowedValues")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"" + UUID.randomUUID() + "\", \"value\": \"some value\" }");

      when(attributeTypesAllowedValuesService.createAttributeTypeAllowedValue(any(PostAllowedValueRequest.class), any(User.class)))
        .thenReturn(new PostAllowedValueResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeTypeAllowedValueByIdWrongAllowedValueIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/allowedValues/123")
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
  public void deleteAttributeTypeAllowedValueByIdAttributeTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/allowedValues/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AttributeTypeAllowedValueNotFoundException.class)
        .doNothing()
        .when(attributeTypesAllowedValuesService).deleteAttributeTypeAllowedValueById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeTypeAllowedValueByIdValueIsUsedInAttributeTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/allowedValues/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AllowedValueIsUsedInAttributeException.class)
        .doNothing()
        .when(attributeTypesAllowedValuesService).deleteAttributeTypeAllowedValueById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeTypeAllowedValueByIdValueIsUsedInRelationAttributeTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/allowedValues/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AllowedValueIsUsedInRelationAttributeException.class)
        .doNothing()
        .when(attributeTypesAllowedValuesService).deleteAttributeTypeAllowedValueById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeTypeAllowedValueByIdValueIsUsedInRelationComponentAttributeTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/allowedValues/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AllowedValueIsUsedInRelationComponentAttributeException.class)
        .doNothing()
        .when(attributeTypesAllowedValuesService).deleteAttributeTypeAllowedValueById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeTypeAllowedValueByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/allowedValues/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

        doNothing().when(attributeTypesAllowedValuesService)
          .deleteAttributeTypeAllowedValueById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
