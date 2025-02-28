package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.ImagesController;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.ImagesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration.AuthFilterConfigurationMock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.GlobalExceptionHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.handlers.RestResponseAccessDeniedHandler;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.interfaces.WithMockCustomUser;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * @author juliwolf
 */

@WebMvcTest(ImagesController.class)
@Import(ImagesController.class)
@ContextConfiguration(classes = { AuthFilterConfigurationMock.class, RestResponseAccessDeniedHandler.class, GlobalExceptionHandler.class })
public class ImagesControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ImagesService imagesService;

  @Test
  @WithMockCustomUser
  public void getImagesTest () {
    try {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/images")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON);

      when(imagesService.getImages()).thenReturn(new ArrayList<>());

      mockMvc.perform(requestBuilder)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @WithMockCustomUser
  public void uploadImageInvalidTypeTest () {
    try {
      MockMultipartFile file
        = new MockMultipartFile(
        "file",
        "hello.txt",
        MediaType.TEXT_XML_VALUE,
        "Hello, World!".getBytes()
      );

      mockMvc.perform(multipart("/v1/images").file(file))
        .andExpect(MockMvcResultMatchers.status().is(HttpURLConnection.HTTP_BAD_REQUEST))
        .andExpect(MockMvcResultMatchers.content().string("{\"error\":\"Wrong file format\"}"))
        .andReturn();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
