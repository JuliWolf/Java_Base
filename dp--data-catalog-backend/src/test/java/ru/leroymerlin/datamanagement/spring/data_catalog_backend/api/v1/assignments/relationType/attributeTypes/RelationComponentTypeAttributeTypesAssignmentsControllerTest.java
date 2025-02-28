package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes;

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
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.RelationTypeAttributeTypesAssignmentsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.RelationTypeAttributeTypesAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.AttributeTypeIsUsedForRelationAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.exceptions.RelationTypeAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypeAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypeAttributeTypesUsageCountParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.get.GetRelationTypesAttributeTypesUsageCountResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.models.post.PostRelationTypeAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(RelationTypeAttributeTypesAssignmentsController.class)
@Import(RelationTypeAttributeTypesAssignmentsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class RelationComponentTypeAttributeTypesAssignmentsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RelationTypeAttributeTypesAssignmentsService relationTypeAttributeTypesAssignmentsService;

  @Test
  @WithMockCustomUser
  public void createRelationTypeAttributeTypesAssignmentsInvalidRelationTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationType/123/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [{\"attribute_id\": \"" + UUID.randomUUID() + "\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeAttributeTypesAssignmentsEmptyAttributeTypeAssignmentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationType/" + UUID.randomUUID() + "/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeAttributeTypesAssignmentsRelationTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationType/" + UUID.randomUUID() + "/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [{\"attribute_id\": \"" + UUID.randomUUID() + "\"}] }");

      when(relationTypeAttributeTypesAssignmentsService.createRelationTypeAttributeTypesAssignments(any(UUID.class), any(PostRelationTypeAttributeTypesRequest.class), any(User.class)))
        .thenThrow(RelationTypeNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeAttributeTypesAssignmentsAttributeTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationType/" + UUID.randomUUID() + "/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [{\"attribute_id\": \"" + UUID.randomUUID() + "\"}] }");

      when(relationTypeAttributeTypesAssignmentsService.createRelationTypeAttributeTypesAssignments(any(UUID.class), any(PostRelationTypeAttributeTypesRequest.class), any(User.class)))
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
  public void createRelationTypeAttributeTypesAssignmentsAssignmentAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationType/" + UUID.randomUUID() + "/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [{\"attribute_id\": \"" + UUID.randomUUID() + "\"}] }");

      when(relationTypeAttributeTypesAssignmentsService.createRelationTypeAttributeTypesAssignments(any(UUID.class), any(PostRelationTypeAttributeTypesRequest.class), any(User.class)))
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
  public void createRelationTypeAttributeTypesAssignmentsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationType/" + UUID.randomUUID() + "/attribute_type/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_assignment\": [{\"attribute_id\": \"" + UUID.randomUUID() + "\"}] }");

      when(relationTypeAttributeTypesAssignmentsService.createRelationTypeAttributeTypesAssignments(any(UUID.class), any(PostRelationTypeAttributeTypesRequest.class), any(User.class)))
        .thenReturn(new PostRelationTypeAttributeTypesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypeAttributeTypesAssignmentsByRelationTypeIdInvalidRelationTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationType/123/attribute_type")
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
  public void getRelationTypeAttributeTypesAssignmentsByRelationTypeIdRelationTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationType/" + UUID.randomUUID() + "/attribute_type")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesAssignmentsByRelationTypeId(any(UUID.class)))
        .thenThrow(RelationTypeNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypeAttributeTypesAssignmentsByRelationTypeIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationType/" + UUID.randomUUID() + "/attribute_type")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesAssignmentsByRelationTypeId(any(UUID.class)))
        .thenReturn(new GetRelationTypeAttributeTypesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypeAttributeTypesWithUsageCount_InvalidRequestParams_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationTypes/attributeTypes?relation_type_id=123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid params in request\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypeAttributeTypesWithUsageCount_Success_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationTypes/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypeAttributeTypesAssignmentsService.getRelationTypeAttributeTypesWithUsageCount(any(GetRelationTypeAttributeTypesUsageCountParams.class)))
        .thenReturn(new GetRelationTypesAttributeTypesUsageCountResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeAttributeTypeAssignmentInvalidAssignmentIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationType/attributeType/123")
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
  public void deleteRelationTypeAttributeTypeAssignmentAssignmentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationType/attributeType/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RelationTypeAttributeTypeAssignmentNotFound.class).doNothing()
        .when(relationTypeAttributeTypesAssignmentsService).deleteRelationTypeAttributeTypeAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeAttributeTypeAssignmentAttributeTypeIsUsedForRelationAttributeTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationType/attributeType/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AttributeTypeIsUsedForRelationAttributeException.class).doNothing()
        .when(relationTypeAttributeTypesAssignmentsService).deleteRelationTypeAttributeTypeAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeAttributeTypeAssignmentSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationType/attributeType/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing()
        .when(relationTypeAttributeTypesAssignmentsService).deleteRelationTypeAttributeTypeAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
