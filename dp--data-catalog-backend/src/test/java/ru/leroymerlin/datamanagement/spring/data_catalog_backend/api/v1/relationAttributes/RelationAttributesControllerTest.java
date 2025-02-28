package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes;

import java.net.HttpURLConnection;
import java.util.List;
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
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.RelationAttributesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.RelationAttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.AttributeTypeNotAllowedForRelationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.RelationAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get.GetRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get.GetRelationAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PatchRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.post.PostRelationAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(RelationAttributesController.class)
@Import(RelationAttributesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class RelationAttributesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RelationAttributesService relationAttributesService;

  @Test
  @WithMockCustomUser
  public void createRelationAttributeEmptyRequiredFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"some\": \"" + UUID.randomUUID() + "\"}");

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
  public void createRelationAttributeInvalidRelationIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_id\": \"123\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid request values.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationAttributeRelationNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationAttributesService.createRelationAttribute(any(PostRelationAttributeRequest.class), any(User.class)))
        .thenThrow(new RelationNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationAttributeAttributeTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationAttributesService.createRelationAttribute(any(PostRelationAttributeRequest.class), any(User.class)))
        .thenThrow(new AttributeTypeNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested attribute type not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationAttributeAttributeTypeNotAllowedForRelationFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationAttributesService.createRelationAttribute(any(PostRelationAttributeRequest.class), any(User.class)))
        .thenThrow(new AttributeTypeNotAllowedForRelationException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"This attribute type is not allowed to be used for this relation.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationAttributeValueValidationExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationAttributesService.createRelationAttribute(any(PostRelationAttributeRequest.class), any(User.class)))
        .thenThrow(new AttributeInvalidDataTypeException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid data type for the attribute\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationAttributeRelationAttributeAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationAttributesService.createRelationAttribute(any(PostRelationAttributeRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"This relation already has this attribute.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationAttributeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationAttributesService.createRelationAttribute(any(PostRelationAttributeRequest.class), any(User.class)))
        .thenReturn(new PostRelationAttributeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationAttributeEmptyValueTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationAttributes/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"\"}");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Null value given.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationAttributeInvalidRelationAttributeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationAttributes/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\"}");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation attribute '123' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationAttributeRelationAttributeNotFoundTest () {
    try {
      UUID uuid = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationAttributes/" + uuid)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\"}");

      when(relationAttributesService.updateRelationAttribute(any(UUID.class), any(PatchRelationAttributeRequest.class), any(User.class)))
        .thenThrow(new RelationAttributeNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation attribute '" + uuid + "' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationAttributeValueValidationErrorTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationAttributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\"}");

      when(relationAttributesService.updateRelationAttribute(any(UUID.class), any(PatchRelationAttributeRequest.class), any(User.class)))
        .thenThrow(new AttributeValueNotAllowedException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Attribute value not allowed\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationAttributeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationAttributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\"}");

      when(relationAttributesService.updateRelationAttribute(any(UUID.class), any(PatchRelationAttributeRequest.class), any(User.class)))
        .thenReturn(new PatchRelationAttributeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationAttributeByIdInvalidRelationAttributeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationAttributes/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation attribute '123' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationAttributeByIdRelationAttributeNotFoundTest () {
    try {
      UUID uuid = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationAttributes/" + uuid)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationAttributesService.getRelationAttributeById(any(UUID.class)))
        .thenThrow(new RelationAttributeNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation attribute '" + uuid + "' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationAttributeByIdSuccessTest () {
    try {
      UUID uuid = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationAttributes/" + uuid)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationAttributesService.getRelationAttributeById(any(UUID.class)))
        .thenReturn(new GetRelationAttributeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationAttributesByParamsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationAttributesService.getRelationAttributesByParams(any(UUID.class), any(List.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetRelationAttributesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationAttributeByIdInvalidRelationAttributeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relationAttributes/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation attribute '123' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationAttributeByIdRelationAttributeNotFoundTest () {
    try {
      UUID uuid = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relationAttributes/" + uuid)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RelationAttributeNotFoundException.class).doNothing().when(relationAttributesService)
          .deleteRelationAttributeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation attribute '" + uuid + "' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationAttributeByIdSuccessTest () {
    try {
      UUID uuid = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relationAttributes/" + uuid)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(relationAttributesService)
        .deleteRelationAttributeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
