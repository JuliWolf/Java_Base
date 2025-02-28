package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services;

import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import logger.LoggerWrapper;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpDeleteWithBody;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;

/**
 * @author juliwolf
 */

public class HTTPRequestService {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public <T> HttpRequestResponse<T> createRequest (
    RequestType requestType,
    String url,
    Header header,
    String payload,
    TypeReference<T> responseType
  )  {
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpRequestBase request = getHttpRequest(requestType, url, payload);
      request.addHeader(header);

      // Execute the request and get the response
      CloseableHttpResponse response = httpClient.execute(request);

      T responseBody = parseResponseBody(response, responseType);
      int statusCode = response.getStatusLine().getStatusCode();

      response.close();

      return new HttpRequestResponse<>(statusCode, responseBody);
    } catch (IOException | RuntimeException exception) {
      LoggerWrapper.error("Error occurred while making request to url: " +  url + exception.getMessage(),
        exception.getStackTrace(),
        null,
        HTTPRequestService.class.getName()
      );

      return null;
    }
  }

  protected <T> T parseResponseBody (CloseableHttpResponse response, TypeReference<T> responseType) throws IOException {
    try (
      response;
      InputStreamReader reader = new InputStreamReader(response.getEntity().getContent())
    ) {
      return objectMapper.readValue(reader, responseType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected HttpRequestBase getHttpRequest (RequestType requestType, String url, String payload) {
    HttpRequestBase request = null;

    switch (requestType) {
      case GET -> {
        request  = new HttpGet(url);
      }
      case POST -> {
        request = new HttpPost(url);

        if (StringUtils.isNotEmpty(payload)) {
          StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
          ((HttpPost) request).setEntity(entity);
        };
      }
      case PATCH -> {
        request = new HttpPatch(url);

        if (StringUtils.isNotEmpty(payload)) {
          StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
          ((HttpPatch) request).setEntity(entity);
        };
      }
      case DELETE -> {
        request = new HttpDeleteWithBody(url);

        if (StringUtils.isNotEmpty(payload)) {
          StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
          ((HttpDeleteWithBody) request).setEntity(entity);
        };
      }
    };

    return request;
  }
}
