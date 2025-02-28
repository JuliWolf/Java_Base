package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.io.IOException;
import logger.LoggerWrapper;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.AuthConstants;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.HTTPRequestService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.models.HttpRequestResponse;

/**
 * @author juliwolf
 */

public class HTTPRequestUtilsStub extends HTTPRequestService {
  public HttpRequestResponse<String> createRequest (RequestType requestType, String url, String payload, User user)  {
    String token = JWTUtilsStub.generateJWTToken(AuthConstants.JWT_SECRET_KEY, user.getUsername());
    Header header = new BasicHeader(AuthConstants.HEADER_TOKEN, token);

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpRequestBase request = getHttpRequest(requestType, url, payload);
      request.addHeader(header);

      // Execute the request and get the response
      CloseableHttpResponse response = httpClient.execute(request);

      String responseBody = EntityUtils.toString(response.getEntity());
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
}
