package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AssetAlreadyHasAttributeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions.AttributeTypeNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.AttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.get.GetAttributesResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AttributesController.class)
@Import(AttributesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class })
public class AttributesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AttributesService attributesService;

  @Test
  @WithMockCustomUser
  public void createAttributeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"" + UUID.randomUUID() + "\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"value\": \"some value\" }");

      when(attributesService.createAttribute(any(PostAttributeRequest.class), any(User.class)))
        .thenReturn(new PostAttributeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeEmptyRequiredParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"value\": \"some value\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeAssetNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"" + UUID.randomUUID() + "\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"value\": \"some value\" }");

      when(attributesService.createAttribute(any(PostAttributeRequest.class), any(User.class)))
        .thenThrow(AssetNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeWrongParamsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"" + UUID.randomUUID() + "\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"value\": \"some value\" }");

      when(attributesService.createAttribute(any(PostAttributeRequest.class), any(User.class)))
        .thenThrow(AttributeValueNotAllowedException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributeAttributeAlreadyExistsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"attribute_type_id\": \"" + UUID.randomUUID() + "\", \"asset_id\": \"" + UUID.randomUUID() + "\", \"value\": \"some value\" }");

      when(attributesService.createAttribute(any(PostAttributeRequest.class), any(User.class)))
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
  public void createAttributesBulkRequiredEmptyRequestListTest () {
    try {
      List<PostAttributeRequest> requests = new ArrayList<>();

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Empty request list\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributesBulkRequiredFieldsAreEmptyTest () {
    try {
      List<PostAttributeRequest> requests = new ArrayList<>();
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), null, "value"));
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "another value"));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.createAttributesBulk(any(List.class), any(User.class)))
        .thenThrow(SomeRequiredFieldsAreEmptyException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributesBulkIllegalArgumentsTest () {
    try {
      List<PostAttributeRequest> requests = new ArrayList<>();
      requests.add(new PostAttributeRequest("123", UUID.randomUUID(), "value"));
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "another value"));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.createAttributesBulk(any(List.class), any(User.class)))
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
  public void createAttributesBulkAssetNotFoundTest () {
    try {
      List<PostAttributeRequest> requests = new ArrayList<>();
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "value"));
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "another value"));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.createAttributesBulk(any(List.class), any(User.class)))
        .thenThrow(AssetNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributesBulkAttributeTypeNotFoundTest () {
    try {
      List<PostAttributeRequest> requests = new ArrayList<>();
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "value"));
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "another value"));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.createAttributesBulk(any(List.class), any(User.class)))
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
  public void createAttributesBulkValidationExceptionTest () {
    try {
      List<PostAttributeRequest> requests = new ArrayList<>();
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "value"));
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "another value"));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.createAttributesBulk(any(List.class), any(User.class)))
        .thenThrow(AttributeInvalidDataTypeException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributesBulkAttributeTypeNotAllowedTest () {
    try {
      List<PostAttributeRequest> requests = new ArrayList<>();
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "value"));
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "another value"));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.createAttributesBulk(any(List.class), any(User.class)))
        .thenThrow(AttributeTypeNotAllowedException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void createAttributesBulkAttributeAlreadyExistsTest () {
    try {
      List<PostAttributeRequest> requests = new ArrayList<>();
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "value"));
      requests.add(new PostAttributeRequest(UUID.randomUUID().toString(), UUID.randomUUID(), "another value"));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.createAttributesBulk(any(List.class), any(User.class)))
        .thenThrow(new AssetAlreadyHasAttributeException(new DataIntegrityViolationException("Detail: Key (attribute_type_id, asset_id, deleted_flag)=(" + new UUID(123, 123)+ ", " + new UUID(1234, 1234) + ", f)")));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"This asset already has this attribute.\",\"details\":{\"attribute_type_id\":\"" + new UUID(123, 123) + "\",\"asset_id\":\"" + new UUID(1234, 1234) + "\",\"value\":null}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"some value\" }");

      when(attributesService.updateAttribute(any(UUID.class), any(PatchAttributeRequest.class), any(User.class)))
        .thenReturn(new PatchAttributeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeInvalidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributes/123")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"some value\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeEmptyValueTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"\" }");

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeAttributeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"some value\" }");

      when(attributesService.updateAttribute(any(UUID.class), any(PatchAttributeRequest.class), any(User.class)))
        .thenThrow(AttributeNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeValueValidationErrorTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"value\": \"some value\" }");

      when(attributesService.updateAttribute(any(UUID.class), any(PatchAttributeRequest.class), any(User.class)))
        .thenThrow(AttributeValueMaskValidationException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeBulkSuccessTest () {
    try {
      List<PatchBulkAttributeRequest> requests = new ArrayList<>();
      requests.add(new PatchBulkAttributeRequest(UUID.randomUUID(), "value"));
      requests.add(new PatchBulkAttributeRequest(UUID.randomUUID(), "another value"));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.updateAttributesBulk(any(List.class), any(User.class)))
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
  public void updateAttributeBulkSomeRequiredFieldsAreEmptyTest () {
    try {
      PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(null, "value");
      PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(UUID.randomUUID(), "another value");
      List<PatchBulkAttributeRequest> requests = List.of(
        firstRequest, secondRequest
      );

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.updateAttributesBulk(any(List.class), any(User.class)))
        .thenThrow(new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(firstRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Some of required fields are empty.\",\"details\":{\"attribute_id\":null,\"value\":\"" + firstRequest.getValue() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeBulkAttributeNotFoundTest () {
    try {
      PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(UUID.randomUUID(), "value");
      PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(UUID.randomUUID(), "another value");
      List<PatchBulkAttributeRequest> requests = List.of(
        firstRequest, secondRequest
      );

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.updateAttributesBulk(any(List.class), any(User.class)))
        .thenThrow(new AttributeNotFoundException(ObjectUtils.convertObjectToMap(firstRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"attribute not found\",\"details\":{\"attribute_id\":\"" + firstRequest.getAttribute_id() + "\",\"value\":\"" + firstRequest.getValue() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeBulkDuplicateValueInRequestTest () {
    try {
      PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(UUID.randomUUID(), "value");
      PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(UUID.randomUUID(), "another value");
      List<PatchBulkAttributeRequest> requests = List.of(
        firstRequest, secondRequest
      );

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.updateAttributesBulk(any(List.class), any(User.class)))
        .thenThrow(new DuplicateValueInRequestException("Several entries for this attribute_id", ObjectUtils.convertObjectToMap(firstRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Several entries for this attribute_id\",\"details\":{\"attribute_id\":\"" + firstRequest.getAttribute_id() + "\",\"value\":\"" + firstRequest.getValue() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void updateAttributeBulkValidationErrorTest () {
    try {
      PatchBulkAttributeRequest firstRequest = new PatchBulkAttributeRequest(UUID.randomUUID(), "value");
      PatchBulkAttributeRequest secondRequest = new PatchBulkAttributeRequest(UUID.randomUUID(), "another value");
      List<PatchBulkAttributeRequest> requests = List.of(
        firstRequest, secondRequest
      );

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      when(attributesService.updateAttributesBulk(any(List.class), any(User.class)))
        .thenThrow(new AttributeTypeNotAllowedException(ObjectUtils.convertObjectToMap(firstRequest)));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"This attribute type is not allowed to be used for this asset. \",\"details\":{\"attribute_id\":\"" + firstRequest.getAttribute_id() + "\",\"value\":\"" + firstRequest.getValue() + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAttributeByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/attributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(attributesService.getAttributeById(any(UUID.class)))
        .thenReturn(new AttributeResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAttributeByIdInvalidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/attributes/123")
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
  public void getAttributeByIdAttributeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/attributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(attributesService.getAttributeById(any(UUID.class)))
        .thenThrow(AttributeNotFoundException.class);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAttributesByParamsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/attributes")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(attributesService.getAttributesByParams(any(UUID.class), any(UUID.class), any(Integer.class), any(Integer.class)))
        .thenReturn(new GetAttributesResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void getAttributesByParamsIllegalArgumentsTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/attributes?asset_id=123")
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
  public void deleteAttributeByIdSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doNothing().when(attributesService).deleteAttributeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributeByIdInvalidIdTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributes/123")
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
  public void deleteAttributeByIdAttributeNotFoundTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributes/" + UUID.randomUUID())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      doThrow(AttributeNotFoundException.class).doNothing().when(attributesService).deleteAttributeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributesBulkSuccessTest () {
    try {
      List<UUID> requests = new ArrayList<>();
      requests.add(UUID.randomUUID());
      requests.add(UUID.randomUUID());

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      doNothing().when(attributesService).deleteAttributeById(any(UUID.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("{\"result\":\"Attributes were successfully deleted.\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributesBulkEmptyRequestListTest () {
    try {
      List<UUID> requests = new ArrayList<>();

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Empty request list\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributesBulkDuplicateValueInRequestTest () {
    try {
      List<UUID> requests = new ArrayList<>();
      requests.add(new UUID(1234, 1234));
      requests.add(new UUID(1234, 1234));

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      doThrow(new DuplicateValueInRequestException("Duplicate attribute_id in request", Map.of("attribute_id", new UUID(1234, 1234))))
        .doNothing().when(attributesService).deleteAttributesBulk(any(List.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Duplicate attribute_id in request\",\"details\":{\"attribute_id\":\"" + new UUID(1234, 1234) + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void deleteAttributesBulkAttributeNotFoundTest () {
    try {
      List<UUID> requests = new ArrayList<>();
      requests.add(UUID.randomUUID());
      requests.add(UUID.randomUUID());

      ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
      String validAssetRequest = objectWriter.writeValueAsString(requests);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/v1/attributes/bulk")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(validAssetRequest);

      doThrow(new AttributeNotFoundException(Map.of("attribute_id", new UUID(123, 123))))
        .doNothing().when(attributesService).deleteAttributesBulk(any(List.class), any(User.class));

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_NOT_FOUND))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Requested attribute not found\",\"details\":{\"attribute_id\":\"" + new UUID(123, 123) + "\"}}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
