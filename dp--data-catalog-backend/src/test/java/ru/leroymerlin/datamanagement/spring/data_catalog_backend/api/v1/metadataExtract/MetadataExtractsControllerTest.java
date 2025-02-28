package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.MetadataExtractsController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.MetadataExtractsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.GlobalExceptionHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models.GetMetadataExtractRequestParams;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.metadataExtract.models.GetMetadataExtractsResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * @author juliwolf
 */

@WebMvcTest(MetadataExtractsController.class)
@Import(MetadataExtractsController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class, GlobalExceptionHandler.class })
public class MetadataExtractsControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MetadataExtractsService metadataExtractsService;

  @Test
  @WithMockCustomUser
  public void getMetadataExtractsSuccessTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/metadataExtracts")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(metadataExtractsService.getMetadataExtracts(any(GetMetadataExtractRequestParams.class)))
        .thenReturn(new GetMetadataExtractsResponse());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
