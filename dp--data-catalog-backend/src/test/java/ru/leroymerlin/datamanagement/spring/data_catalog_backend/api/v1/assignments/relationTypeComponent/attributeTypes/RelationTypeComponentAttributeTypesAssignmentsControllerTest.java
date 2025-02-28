package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypesAssignmentsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypesAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.AttributeTypeIsUsedForRelationComponentAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.exceptions.RelationTypeComponentAttributeTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.GetRelationTypeComponentAssetTypesUsageCountParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.GetRelationTypeComponentAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.get.GetRelationTypeComponentsAttributeTypesUsageCountResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.models.post.PostRelationTypeComponentAttributeTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(RelationTypeComponentAttributeTypesAssignmentsController.class)
@Import(RelationTypeComponentAttributeTypesAssignmentsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class RelationTypeComponentAttributeTypesAssignmentsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RelationTypeComponentAttributeTypesAssignmentsService relationTypeComponentAttributeTypesAssignmentsService;

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAttributeTypesAssignmentsInvalidRelationTypeComponentIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/123/attributeTypes/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_component_attribute_type_assignment\": [{\"attribute_type_id\": \"" + UUID.randomUUID() + "\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation type component not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAttributeTypesAssignmentsAssignmentsIsEmptyInRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/attributeTypes/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_component_attribute_type_assignment\": [] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"No assignments given.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAttributeTypesAssignmentsRelationTypeComponentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/attributeTypes/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_component_attribute_type_assignment\": [{\"attribute_type_id\": \"" + UUID.randomUUID() + "\"}] }");

      when(relationTypeComponentAttributeTypesAssignmentsService.createRelationTypeComponentAttributeTypesAssignments(any(UUID.class), any(PostRelationTypeComponentAttributeTypesRequest.class), any(User.class)))
        .thenThrow(RelationTypeComponentNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation type component not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAttributeTypesAssignmentsInvalidAttributeTypeInRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/attributeTypes/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_component_attribute_type_assignment\": [{\"attribute_type_id\": \"123\"}] }");

      when(relationTypeComponentAttributeTypesAssignmentsService.createRelationTypeComponentAttributeTypesAssignments(any(UUID.class), any(PostRelationTypeComponentAttributeTypesRequest.class), any(User.class)))
        .thenThrow(IllegalArgumentException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid request params\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAttributeTypesAssignmentsAttributeTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/attributeTypes/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_component_attribute_type_assignment\": [{\"attribute_type_id\": \"" + UUID.randomUUID() + "\"}] }");

      when(relationTypeComponentAttributeTypesAssignmentsService.createRelationTypeComponentAttributeTypesAssignments(any(UUID.class), any(PostRelationTypeComponentAttributeTypesRequest.class), any(User.class)))
        .thenThrow(AttributeTypeNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Attribute type not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAttributeTypesAssignmentsAssignmentAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/attributeTypes/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_component_attribute_type_assignment\": [{\"attribute_type_id\": \"" + UUID.randomUUID() + "\"}] }");

      when(relationTypeComponentAttributeTypesAssignmentsService.createRelationTypeComponentAttributeTypesAssignments(any(UUID.class), any(PostRelationTypeComponentAttributeTypesRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Such assignment already exists.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeComponentAttributeTypesAssignmentsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/attributeTypes/batch")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_component_attribute_type_assignment\": [{\"attribute_type_id\": \"" + UUID.randomUUID() + "\"}] }");

      when(relationTypeComponentAttributeTypesAssignmentsService.createRelationTypeComponentAttributeTypesAssignments(any(UUID.class), any(PostRelationTypeComponentAttributeTypesRequest.class), any(User.class)))
        .thenReturn(new PostRelationTypeComponentAttributeTypesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypeComponentAttributeTypesAssignmentsInvalidRelationTypeComponentTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationTypeComponent/123/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation type component not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypeComponentAttributeTypesAssignmentsRelationTypeComponentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesAssignments(any(UUID.class)))
        .thenThrow(RelationTypeComponentNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation type component not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypeComponentAttributeTypesAssignmentsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationTypeComponent/" + UUID.randomUUID() + "/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesAssignments(any(UUID.class)))
        .thenReturn(new GetRelationTypeComponentAttributeTypesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypeComponentAttributeTypesWithUsageCount_InvalidRequestParams_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationTypeComponents/attributeTypes?relation_type_component_id=123")
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
  public void getRelationTypeComponentAttributeTypesWithUsageCount_Success_Test () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/assignments/relationTypeComponents/attributeTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypeComponentAttributeTypesAssignmentsService.getRelationTypeComponentAttributeTypesWithUsageCount(any(GetRelationTypeComponentAssetTypesUsageCountParams.class)))
        .thenReturn(new GetRelationTypeComponentsAttributeTypesUsageCountResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeComponentAttributeTypeAssignmentInvalidAssignmentIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationTypeComponent/attributeType/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation type component attribute type assignment not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeComponentAttributeTypeAssignmentRelationTypeComponentAttributeTypeAssignmentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationTypeComponent/attributeType/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RelationTypeComponentAttributeTypeAssignmentNotFound.class).doNothing().when(relationTypeComponentAttributeTypesAssignmentsService)
          .deleteRelationTypeComponentAttributeTypeAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation type attribute type assignment not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeComponentAttributeTypeAssignmentRelationTypeComponentAttributeTypeAssignmentIsUsedForRelationComponentAttributeTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationTypeComponent/attributeType/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AttributeTypeIsUsedForRelationComponentAttributeException.class).doNothing().when(relationTypeComponentAttributeTypesAssignmentsService)
        .deleteRelationTypeComponentAttributeTypeAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Attribute type is used for relation component attribute\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeComponentAttributeTypeAssignmentSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/assignments/relationTypeComponent/attributeType/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(relationTypeComponentAttributeTypesAssignmentsService)
        .deleteRelationTypeComponentAttributeTypeAssignment(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
