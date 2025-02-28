package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes;

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
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.RelationTypesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.RelationTypesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.RelationTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.get.GetRelationTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PatchRelationTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PostRelationTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.PostRelationTypeResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(RelationTypesController.class)
@Import(RelationTypesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class RelationTypesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RelationTypesService relationTypesService;

  @Test
  @WithMockCustomUser
  public void createRelationTypeEmptyRequiredFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeInvalidComponentsNumberTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_component_number\": 1, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeInvalidComponentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_component_number\": 2, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeIncorrectRolesForResponsibilityInParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"responsibility_inheritance_flag\": true, \"relation_type_component_number\": 2, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeIncorrectRolesInHierarchyInParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"hierarchy_flag\": true, \"relation_type_component_number\": 2, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeIncorrectRolesInHierarchyTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_component_number\": 2, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.createRelationType(any(PostRelationTypeRequest.class), any(User.class)))
        .thenThrow(IncorrectRoleInHierarchyException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeIncorrectRolesForResponsibilityTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_component_number\": 2, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.createRelationType(any(PostRelationTypeRequest.class), any(User.class)))
        .thenThrow(IncorrectRoleForResponsibilityInheritanceException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeRelationTypeAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_component_number\": 2, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.createRelationType(any(PostRelationTypeRequest.class), any(User.class)))
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
  public void createRelationTypeLongRelationTypeNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"" + StringUtils.repeat('g', 256) + "\", \"relation_type_component_number\": 2, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"relation_type_name contains too much symbols. Allowed limit is 255\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeLongRelationTypeDescriptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_description\": \"" + StringUtils.repeat('g', 513) + "\",\"relation_type_component_number\": 2, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"relation_type_description contains too much symbols. Allowed limit is 512\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeLongRelationTypeComponentDescriptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\",\"relation_type_component_number\": 2, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\", \"relation_type_component_description\": \"" + StringUtils.repeat('f', 513)+ "\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.createRelationType(any(PostRelationTypeRequest.class), any(User.class)))
        .thenThrow(new InvalidFieldLengthException("relation_type_component_description", 512));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"relation_type_component_description contains too much symbols. Allowed limit is 512\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationTypeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_component_number\": 2, \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.createRelationType(any(PostRelationTypeRequest.class), any(User.class)))
        .thenReturn(new PostRelationTypeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationTypeInvalidRelationTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationTypes/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_description\": \"some new desc\", \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationTypeRelationTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_description\": \"some new desc\", \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.updateRelationType(any(UUID.class), any(PatchRelationTypeRequest.class), any(User.class)))
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
  public void updateRelationTypeNullRelationTypeNameTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": null, \"relation_type_description\": \"some new desc\", \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"'relation_type_name' is not nullable\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationTypeLongRelationTypeDescriptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_description\": \"" + StringUtils.repeat('a', 513)+ "\", \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"relation_type_description contains too much symbols. Allowed limit is 512\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationTypeRelationTypeComponentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_description\": \"some new desc\", \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.updateRelationType(any(UUID.class), any(PatchRelationTypeRequest.class), any(User.class)))
        .thenThrow(RelationTypeComponentNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationTypeMultipleRelationHasAssetTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_description\": \"some new desc\", \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.updateRelationType(any(UUID.class), any(PatchRelationTypeRequest.class), any(User.class)))
        .thenThrow(MultipleRelationExistsWithAssetException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationTypeMultipleRelationComponentHasSameAssetTest () {
    try {
      UUID relationTypeComponentId = UUID.randomUUID();
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_description\": \"some new desc\", \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.updateRelationType(any(UUID.class), any(PatchRelationTypeRequest.class), any(User.class)))
        .thenThrow(new MultipleRelationExistsWithSameAssetException(relationTypeComponentId));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Multiple relations with same asset for relation type component with id " + relationTypeComponentId + " already exist.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationTypeRequestValidationExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_description\": \"some new desc\", \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.updateRelationType(any(UUID.class), any(PatchRelationTypeRequest.class), any(User.class)))
        .thenThrow(IncorrectRoleInHierarchyException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateRelationTypeRelationTypeWithSuchNameAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_name\": \"some name\", \"relation_type_description\": \"some new desc\", \"relation_type_component\": [{\"relation_type_component_name\":  \"some name\"}, {\"relation_type_component_name\":  \"some name\"}] }");

      when(relationTypesService.updateRelationType(any(UUID.class), any(PatchRelationTypeRequest.class), any(User.class)))
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
  public void getRelationTypeByIdInvalidRelationTypeIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationTypes/123")
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
  public void getRelationTypeByIdRelationTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypesService.getRelationTypeById(any(UUID.class)))
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
  public void getRelationTypeByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypesService.getRelationTypeById(any(UUID.class)))
        .thenReturn(new RelationTypeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypesByParamsInvalidAllowedAssetTypeParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationTypes?allowed_asset_type=123")
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
  public void getRelationTypesByParamsEmptyParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationTypes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypesService.getRelationTypesByParams(
        any(String.class),
        any(Integer.class),
        any(Boolean.class),
        any(Boolean.class),
        any(UUID.class),
        any(Boolean.class),
        any(Boolean.class),
        any(Integer.class),
        any(Integer.class)
      ))
        .thenReturn(new GetRelationTypesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationTypesByParamsWithParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relationTypes?component_number=2")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationTypesService.getRelationTypesByParams(
        any(String.class),
        any(Integer.class),
        any(Boolean.class),
        any(Boolean.class),
        any(UUID.class),
        any(Boolean.class),
        any(Boolean.class),
        any(Integer.class),
        any(Integer.class)
      ))
        .thenReturn(new GetRelationTypesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeByIdInvalidRelationIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relationTypes/123")
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
  public void deleteRelationTypeByIdRelationTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RelationTypeNotFoundException.class).doNothing()
        .when(relationTypesService)
        .deleteRelationTypeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationTypeByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relationTypes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing()
        .when(relationTypesService)
        .deleteRelationTypeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
