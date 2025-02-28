package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeValueAlreadyAssignedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.AttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get.GetAttributeTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.get.GetAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PatchAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.models.post.PostAttributeTypeResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author JuliWolf
 */
@WebMvcTest(AttributeTypesController.class)
@Import(AttributeTypesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AttributeTypesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AttributeTypesService attributeTypesService;

  @Test
  @WithMockCustomUser
  public void createAttributeTypeEmptyRequiredFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeAttributeTypeNameAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type\", \"attribute_type_allowed_values\": [\"1\"], \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      when(attributeTypesService.createAttributeType(any(PostAttributeTypeRequest.class), any(User.class)))
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
  public void createAttributeTypeLongAttributeNameValueTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"" + StringUtils.repeat('a', 256) + "\", \"attribute_type_allowed_values\": [\"1\"], \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"attribute_type_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeLongAttributeDescriptionValueTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type name\", \"attribute_type_description\": \"" + StringUtils.repeat('a', 513)+ "\", \"attribute_type_allowed_values\": [\"1\"], \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"attribute_type_description contains too much symbols. Allowed limit is 512\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeLongAttributeValidationMaskValueTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type name\", \"validation_mask\": \"" + StringUtils.repeat('a', 256)+ "\", \"attribute_type_allowed_values\": [\"1\"], \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"validation_mask contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeLongAttributeRdmTableIdValueTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type name\", \"rdm_table_id\": \"" + StringUtils.repeat('a', 256)+ "\", \"attribute_type_allowed_values\": [\"1\"], \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"rdm_table_id contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeIdenticalAttributeValueNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type\", \"attribute_type_allowed_values\": [\"1\"], \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      when(attributeTypesService.createAttributeType(any(PostAttributeTypeRequest.class), any(User.class)))
        .thenThrow(AttributeTypeValueAlreadyAssignedException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeTypeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type\", \"attribute_type_allowed_values\": [\"1\"], \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      when(attributeTypesService.createAttributeType(any(PostAttributeTypeRequest.class), any(User.class)))
        .thenReturn(new PostAttributeTypeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeTypeInvalidAttributeTypeIdRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributeTypes/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type\", \"attribute_type_allowed_values\": [\"1\"], \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeTypeEmptyRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeTypeEmptyAttributeTypeNameRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"rdm_table_id\":  \"123\", \"attribute_type_name\": null}");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"'attribute_type_name' is not nullable\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeTypeLongAttributeTypeNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"" + StringUtils.repeat('a', 256)+ "\", \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"attribute_type_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeTypeAttributeTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type\", \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      when(attributeTypesService.updateAttributeType(any(UUID.class), any(PatchAttributeTypeRequest.class), any(User.class)))
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
  public void updateAttributeTypeIncompatibleKindTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type\", \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      when(attributeTypesService.updateAttributeType(any(UUID.class), any(PatchAttributeTypeRequest.class), any(User.class)))
        .thenThrow(IncompatibleAttributeKindException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeTypeAttributeNameAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type\", \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      when(attributeTypesService.updateAttributeType(any(UUID.class), any(PatchAttributeTypeRequest.class), any(User.class)))
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
  public void updateAttributeTypeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type\", \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      when(attributeTypesService.updateAttributeType(any(UUID.class), any(PatchAttributeTypeRequest.class), any(User.class)))
        .thenReturn(new AttributeTypeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeTypeValidationMaskExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_name\": \"test attribute type\", \"attribute_type_kind\": \"SINGLE_VALUE_LIST\" }");

      when(attributeTypesService.updateAttributeType(any(UUID.class), any(PatchAttributeTypeRequest.class), any(User.class)))
        .thenThrow(AttributeDoesNotMatchTheMaskException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAttributeTypeByIdInvalidAttributeTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/attributeTypes/123")
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
  public void getAttributeTypeByIdAttributeTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(attributeTypesService.getAttributeTypeById(any(UUID.class)))
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
  public void getAttributeTypeByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(attributeTypesService.getAttributeTypeById(any(UUID.class)))
        .thenReturn(new GetAttributeTypeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAttributeTypesByParamsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(attributeTypesService.getAttributeTypeByParams(any(String.class), any(String.class), any(AttributeKindType.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetAttributeTypesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeTypeByIdWithInvalidAttributeTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/123")
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
  public void deleteAttributeTypeByIdAttributeTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AttributeTypeNotFoundException.class).doNothing().when(attributeTypesService).deleteAttributeTypeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeTypeByIdAttributeWithAttributeTypeExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AttributeWithAttributeTypeExistsException.class).doNothing().when(attributeTypesService).deleteAttributeTypeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeTypeByIdRelationAttributeWithAttributeTypeExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RelationAttributeWithAttributeTypeExistsException.class).doNothing().when(attributeTypesService).deleteAttributeTypeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeTypeByIdRelationComponentAttributeWithAttributeTypeExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RelationComponentAttributeWithAttributeTypeExistsException.class).doNothing().when(attributeTypesService).deleteAttributeTypeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeTypeByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributeTypes/" + new UUID(123, 123))
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(attributeTypesService).deleteAttributeTypeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
