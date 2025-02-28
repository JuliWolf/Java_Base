package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations;

import java.net.HttpURLConnection;
import java.util.ArrayList;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationsAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(RelationsController.class)
@Import(RelationsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class RelationsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RelationsService relationsService;

  private List<PostRelationsRequest> requests = new ArrayList<>();
  private PostRelationsRequest firstRequest = new PostRelationsRequest(null, List.of(new PostRelationRequest(UUID.randomUUID().toString(), UUID.randomUUID().toString())));
  private PostRelationsRequest secondRequest = new PostRelationsRequest(UUID.randomUUID().toString(), List.of(new PostRelationRequest(UUID.randomUUID().toString(), UUID.randomUUID().toString())));

  private ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
  private String validAssetRequest = objectWriter.writeValueAsString(requests);

  {
    requests.add(firstRequest);
    requests.add(secondRequest);
  }

  public RelationsControllerTest () throws JsonProcessingException {
  }

  @Test
  @WithMockCustomUser
  public void createRelationsEmptyRequiredFieldsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation type is empty\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsEmptyComponentTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"" + UUID.randomUUID() + "\", \"component\": [{}] }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid number of components.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsIllegalArgumentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"" + UUID.randomUUID() + "\", \"component\": [{ \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }, { \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }] }");

      when(relationsService.createRelations(any(PostRelationsRequest.class), any(User.class)))
        .thenThrow(IllegalArgumentException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid arguments in request\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsAssetNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"" + UUID.randomUUID() + "\", \"component\": [{ \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }, { \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }] }");

      when(relationsService.createRelations(any(PostRelationsRequest.class), any(User.class)))
        .thenThrow(new AssetNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested asset not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsRelationTypeComponentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"" + UUID.randomUUID() + "\", \"component\": [{ \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }, { \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }] }");

      when(relationsService.createRelations(any(PostRelationsRequest.class), any(User.class)))
        .thenThrow(new RelationTypeNotFoundException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation type not found\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsInvalidComponentForRelationTypeTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"" + UUID.randomUUID() + "\", \"component\": [{ \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }, { \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }] }");

      when(relationsService.createRelations(any(PostRelationsRequest.class), any(User.class)))
        .thenThrow(new InvalidComponentForRelationTypeException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid component for this relation type.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsInvalidAssetForComponentTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"" + UUID.randomUUID() + "\", \"component\": [{ \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }, { \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }] }");

      UUID componentId = UUID.randomUUID();
      when(relationsService.createRelations(any(PostRelationsRequest.class), any(User.class)))
        .thenThrow(new InvalidAssetTypeForComponentException(componentId));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid asset type for the component '" + componentId + "'\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsInvalidHierarchyBetweenAssetsExceptionTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"" + UUID.randomUUID() + "\", \"component\": [{ \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }, { \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }] }");

      when(relationsService.createRelations(any(PostRelationsRequest.class), any(User.class)))
        .thenThrow(new InvalidHierarchyBetweenAssetsException());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid hierarchy between assets.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsRelationWithSuchParamsAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"" + UUID.randomUUID() + "\", \"component\": [{ \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }, { \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }] }");

      when(relationsService.createRelations(any(PostRelationsRequest.class), any(User.class)))
        .thenThrow(DataIntegrityViolationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation with these parameters already exists.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsRelationParamsAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"" + UUID.randomUUID() + "\", \"component\": [{ \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }, { \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }] }");

      when(relationsService.createRelations(any(PostRelationsRequest.class), any(User.class)))
        .thenThrow(RelationAlreadyExistsException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation with these parameters already exists.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"relation_type_id\": \"" + UUID.randomUUID() + "\", \"component\": [{ \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }, { \"asset_id\": \"" + UUID.randomUUID() + "\", \"component_id\": \"" + UUID.randomUUID() + "\" }] }");

      when(relationsService.createRelations(any(PostRelationsRequest.class), any(User.class)))
        .thenReturn(new PostRelationsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkSomeRequiredFieldsAreEmptyTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(firstRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Some of required fields are empty.\",\"details\":{\"relation_type_id\":null,\"component\":[{\"asset_id\":\"" + firstRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + firstRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkIllegalArgumentsExceptionTest () {
    try {
      firstRequest.setRelation_type_id("123");

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(IllegalArgumentException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid arguments in request\",\"details\":null}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkDuplicateValueInRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new DuplicateValueInRequestException("Duplicate in request.", ObjectUtils.convertObjectToMap(secondRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Duplicate in request.\",\"details\":{\"relation_type_id\":\"" + secondRequest.getRelation_type_id() + "\",\"component\":[{\"asset_id\":\"" + secondRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + secondRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkRelationsAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new RelationAlreadyExistsException(ObjectUtils.convertObjectToMap(secondRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation with these parameters already exists.\",\"details\":{\"relation_type_id\":\"" + secondRequest.getRelation_type_id() + "\",\"component\":[{\"asset_id\":\"" + secondRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + secondRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkInvalidAssetInRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new RelationTypeDoesNotAllowedRelatedAssetException(ObjectUtils.convertObjectToMap(secondRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"This relation type doesn't allow self related assets\",\"details\":{\"relation_type_id\":\"" + secondRequest.getRelation_type_id() + "\",\"component\":[{\"asset_id\":\"" + secondRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + secondRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkRelationTypeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new RelationTypeNotFoundException(ObjectUtils.convertObjectToMap(secondRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation type not found.\",\"details\":{\"relation_type_id\":\"" + secondRequest.getRelation_type_id() + "\",\"component\":[{\"asset_id\":\"" + secondRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + secondRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkInvalidNumberOfComponentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new InvalidNumberOfComponentsException(ObjectUtils.convertObjectToMap(secondRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid number of components\",\"details\":{\"relation_type_id\":\"" + secondRequest.getRelation_type_id() + "\",\"component\":[{\"asset_id\":\"" + secondRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + secondRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkInvalidAssetTypeForComponentTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new InvalidAssetTypeForComponentException(ObjectUtils.convertObjectToMap(secondRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid asset type for the component\",\"details\":{\"relation_type_id\":\"" + secondRequest.getRelation_type_id() + "\",\"component\":[{\"asset_id\":\"" + secondRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + secondRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkAssetNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new AssetNotFoundException(ObjectUtils.convertObjectToMap(secondRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Asset not found.\",\"details\":{\"relation_type_id\":\"" + secondRequest.getRelation_type_id() + "\",\"component\":[{\"asset_id\":\"" + secondRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + secondRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkRelationTypeComponentNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new RelationTypeComponentNotFoundException(ObjectUtils.convertObjectToMap(secondRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Relation type component not found.\",\"details\":{\"relation_type_id\":\"" + secondRequest.getRelation_type_id() + "\",\"component\":[{\"asset_id\":\"" + secondRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + secondRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkInvalidComponentForRelationTypeTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new InvalidComponentForRelationTypeException(ObjectUtils.convertObjectToMap(secondRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid component for this relation type.\",\"details\":{\"relation_type_id\":\"" + secondRequest.getRelation_type_id() + "\",\"component\":[{\"asset_id\":\"" + secondRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + secondRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkInvalidHierarchyBetweenAssetsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenThrow(new InvalidHierarchyBetweenAssetsException(ObjectUtils.convertObjectToMap(secondRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid hierarchy between assets.\",\"details\":{\"relation_type_id\":\"" + secondRequest.getRelation_type_id() + "\",\"component\":[{\"asset_id\":\"" + secondRequest.getComponent().get(0).getAsset_id() + "\",\"component_id\":\"" + secondRequest.getComponent().get(0).getComponent_id() + "\"}]}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createRelationsBulkSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/relations/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(relationsService.createRelationsBulk(any(List.class), any(User.class)))
        .thenReturn(new ArrayList());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationsByIdInvalidRelationIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relations/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationsByIdRelationNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relations/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationsService.getRelationById(any(UUID.class)))
        .thenThrow(RelationNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationsByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relations/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationsService.getRelationById(any(UUID.class)))
        .thenReturn(new GetRelationResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationsByParamsIllegalArgumentsInRequestTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relations?asset_id=123" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationsService.getRelationsByParams(any(UUID.class), any(UUID.class), any(UUID.class), any(Boolean.class), any(Boolean.class), any(Integer.class), any(Integer.class)))
        .thenThrow(IllegalArgumentException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Invalid arguments in request.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationsByParamsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relations?asset_id=" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationsService.getRelationsByParams(any(UUID.class), any(UUID.class), any(UUID.class), any(Boolean.class), any(Boolean.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetRelationsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationAttributesInvalidRelationIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relations/123/attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationAttributesRelationNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relations/" + UUID.randomUUID() + "/attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationsService.getRelationAttributes(any(UUID.class)))
        .thenThrow(RelationNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getRelationAttributesSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/relations/" + UUID.randomUUID() + "/attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(relationsService.getRelationAttributes(any(UUID.class)))
        .thenReturn(new GetRelationsAttributesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  @Test
  @WithMockCustomUser
  public void deleteRelationInvalidRelationIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relations/123" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationRelationNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relations/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(RelationNotFoundException.class).doNothing()
        .when(relationsService).deleteRelation(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested relation not found.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteRelationSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/relations/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing()
        .when(relationsService).deleteRelation(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
