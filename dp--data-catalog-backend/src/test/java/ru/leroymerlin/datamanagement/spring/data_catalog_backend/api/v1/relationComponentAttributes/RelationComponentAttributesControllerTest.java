package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.RelationComponentAttributesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.RelationComponentAttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.AttributeTypeNotAllowedForRelationComponentException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.RelationComponentAttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.get.GetRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.get.GetRelationComponentAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PatchRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post.PostRelationComponentAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationComponentNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(RelationComponentAttributesController.class)
@Import(RelationComponentAttributesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class RelationComponentAttributesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RelationComponentAttributesService relationComponentAttributesService;

  @Test
  @WithMockCustomUser
  public void createRelationComponentAttributeEmptyRequiredFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationComponentAttributes")
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
  public void createRelationComponentAttributeInvalidRelationComponentIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationComponentAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_component_id\": \"123\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

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
  public void createRelationComponentAttributeRelationComponentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationComponentAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_component_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationComponentAttributesService.createRelationComponentAttribute(any(PostRelationComponentAttributeRequest.class), any(User.class)))
        .thenThrow(new RelationComponentNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation component not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationAttributeAttributeTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationComponentAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_component_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationComponentAttributesService.createRelationComponentAttribute(any(PostRelationComponentAttributeRequest.class), any(User.class)))
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
  public void createRelationAttributeAttributeTypeNotAllowedForRelationComponentTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationComponentAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_component_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationComponentAttributesService.createRelationComponentAttribute(any(PostRelationComponentAttributeRequest.class), any(User.class)))
        .thenThrow(new AttributeTypeNotAllowedForRelationComponentException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"This attribute type is not allowed to be used for this relation component.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationAttributeValueValidationExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationComponentAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_component_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationComponentAttributesService.createRelationComponentAttribute(any(PostRelationComponentAttributeRequest.class), any(User.class)))
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
  public void createRelationAttributeRelationComponentAttributeAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationComponentAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_component_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationComponentAttributesService.createRelationComponentAttribute(any(PostRelationComponentAttributeRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"This relation component already has this attribute.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationAttributeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationComponentAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\", \"relation_component_id\": \"" + UUID.randomUUID() + "\", \"attribute_type_id\": \"" + UUID.randomUUID() + "\"}");

      when(relationComponentAttributesService.createRelationComponentAttribute(any(PostRelationComponentAttributeRequest.class), any(User.class)))
        .thenReturn(new PostRelationComponentAttributeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationComponentAttributeEmptyValueTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationComponentAttributes/123")
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
  public void updateRelationComponentAttributeInvalidRelationComponentAttributeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationComponentAttributes/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\"}");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation component attribute '123' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationComponentAttributeRelationComponentAttributeNotFoundTest () {
    try {
      UUID uuid = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationComponentAttributes/" + uuid)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\"}");

      when(relationComponentAttributesService.updateRelationComponentAttribute(any(UUID.class), any(PatchRelationComponentAttributeRequest.class), any(User.class)))
        .thenThrow(new RelationComponentAttributeNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation component attribute '" + uuid + "' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationComponentAttributeValueValidationErrorTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationComponentAttributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\"}");

      when(relationComponentAttributesService.updateRelationComponentAttribute(any(UUID.class), any(PatchRelationComponentAttributeRequest.class), any(User.class)))
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
  public void updateRelationComponentAttributeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationComponentAttributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"123\"}");

      when(relationComponentAttributesService.updateRelationComponentAttribute(any(UUID.class), any(PatchRelationComponentAttributeRequest.class), any(User.class)))
        .thenReturn(new PatchRelationComponentAttributeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationComponentAttributeByIdInvalidRelationAttributeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationComponentAttributes/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation component attribute '123' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationComponentAttributeByIdRelationAttributeNotFoundTest () {
    try {
      UUID uuid = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationComponentAttributes/" + uuid)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationComponentAttributesService.getRelationComponentAttributeById(any(UUID.class)))
        .thenThrow(new RelationComponentAttributeNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation component attribute '" + uuid + "' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationComponentAttributeByIdSuccessTest () {
    try {
      UUID uuid = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationComponentAttributes/" + uuid)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationComponentAttributesService.getRelationComponentAttributeById(any(UUID.class)))
        .thenReturn(new GetRelationComponentAttributeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationComponentAttributesByParamsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationComponentAttributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationComponentAttributesService.getRelationComponentAttributesByParams(any(List.class), any(List.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetRelationComponentAttributesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationComponentAttributeByIdInvalidRelationAttributeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relationComponentAttributes/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation component attribute '123' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationComponentAttributeByIdRelationAttributeNotFoundTest () {
    try {
      UUID uuid = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relationComponentAttributes/" + uuid)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RelationComponentAttributeNotFoundException.class).doNothing().when(relationComponentAttributesService)
          .deleteRelationComponentAttributeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation component attribute '" + uuid + "' not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationComponentAttributeByIdSuccessTest () {
    try {
      UUID uuid = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relationComponentAttributes/" + uuid)
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(relationComponentAttributesService)
        .deleteRelationComponentAttributeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
