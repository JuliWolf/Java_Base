package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.AccessTokenResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.CertificatesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.TokenInfoResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.UserInfoResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.JWTUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class KeycloakService {
  @Value("${keycloak.server-url}")
  private String serverUrl;

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.client-id}")
  private String clientId;

  @Value("${keycloak.credentials.secret}")
  private String clientSecret;

  public static final String URL_TOKEN = "/protocol/openid-connect/token";
  public static final String URL_USER_INFO = "/protocol/openid-connect/userinfo";
  public static final String URL_TOKEN_INFO = "/protocol/openid-connect/token/introspect";
  public static final String URL_CERT_INFO = "/protocol/openid-connect/certs";

  private HttpClient client;

  private CertificatesResponse certificatesResponse;

  private HttpClient getHttpClient() {
    if (client == null) client = HttpClient.newHttpClient();

    return client;
  }

  private CertificatesResponse getCertificatesResponse () {
    LoggerWrapper.info("Getting certificates", this.getClass().getName());

    if (certificatesResponse == null) {
      certificatesResponse = getCertificates();
    }

    return certificatesResponse;
  }

  private CertificatesResponse getCertificates () {
    try {
      Optional<CertificatesResponse> response = requestCertificates();

      return response.orElse(new CertificatesResponse());
    } catch (Exception exception) {
      LoggerWrapper.error("Error while receiving certificates info", this.getClass().getName());

      return null;
    }
  }

  public User getUserInfo (String accessToken) throws UserNotFoundException {
    try {
      Optional<UserInfoResponse> userInfoResponse = requestUserInfo(accessToken);

      if (userInfoResponse.isEmpty()) {
        throw new UserNotFoundException();
      };

      UserInfoResponse userInfo = userInfoResponse.get();

      LoggerWrapper.info("Found user info: " + userInfo.getUid(), this.getClass().getName());

      return new User(userInfo.getUid(), userInfo.getGiven_name(), userInfo.getFamily_name(), SourceType.KEYCLOAK, userInfo.getEmail());
    } catch (Exception exception) {
      LoggerWrapper.error("Error while receiving user info", this.getClass().getName());

      throw new UserNotFoundException();
    }
  }

  public String getUsernameFromToken (String token) {
    LoggerWrapper.info("Getting token info", this.getClass().getName());

    TokenInfoResponse tokenInfoResponse = parseToken(token);

    return tokenInfoResponse.getUid();
  }

  public AccessTokenResponse checkUserAndGetToken (String username, String password) throws UserNotFoundException {
    try {
      LoggerWrapper.info("Try to get token for user: " + username, this.getClass().getName());

      Optional<AccessTokenResponse> tokenResponse = requestToken(username, password);

      if (tokenResponse.isEmpty()) {
        LoggerWrapper.info("User with username: " + username + " not found", this.getClass().getName());

        throw new UserNotFoundException();
      };

      return tokenResponse.get();
    } catch (IOException ioException) {
      LoggerWrapper.error("Error while parsing request answer: " + username + " error: " + ioException.getMessage(), this.getClass().getName());

      throw new UserNotFoundException();
    } catch (Exception exception) {
      LoggerWrapper.error("Error while receiving token for user: " + username, this.getClass().getName());

      throw new UserNotFoundException();
    }
  }

  private TokenInfoResponse parseToken (String token) {
    CertificatesResponse certificates = getCertificatesResponse();

    if (certificates.getKeys().isEmpty()) {
      return checkIfTokenIsActive(token);
    }

    CertificatesResponse.CertificateResponse firstKey = certificates.getRS256Certificate();

    Claims claims = JWTUtils.decodeJWT(token, firstKey.getFirstCertificate());

    if (claims == null) {
      return checkIfTokenIsActive(token);
    }

    LoggerWrapper.info("Token decoded successfully", this.getClass().getName());
    return new TokenInfoResponse(true, (String) claims.get("uid"));
  }

  private TokenInfoResponse checkIfTokenIsActive (String token) {
    try {
      LoggerWrapper.info("Check if token active", this.getClass().getName());

      Optional<TokenInfoResponse> tokenInfoResponse = requestIsTokenActive(token);

      if (tokenInfoResponse.isEmpty()) return null;

      TokenInfoResponse tokenInfo = tokenInfoResponse.get();

      LoggerWrapper.info("Token info: " + tokenInfo, this.getClass().getName());

      if (!tokenInfo.getActive()) {
        return new TokenInfoResponse(false, null);
      }

      return tokenInfo;
    } catch (Exception exception) {
      LoggerWrapper.error("Error while parsing token: " + exception.getMessage(), this.getClass().getName());

      throw new UserNotFoundException();
    }
  }

  private Optional<TokenInfoResponse> requestIsTokenActive (String token) throws IOException, InterruptedException {
    HttpRequest.BodyPublisher body = buildFormDataFromMap(Map.of(
      "token", token,
      "client_id", clientId,
      "client_secret", clientSecret
    ));

    HttpResponse<String> response = sendRequest(
      RequestType.POST,
      serverUrl + "/realms/" + realm + URL_TOKEN_INFO,
      new String[]{
        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      },
      body
    );

    if (response.statusCode() != HttpStatus.OK.value()) return Optional.empty();

    ObjectMapper objectMapper = new ObjectMapper();

    return Optional.of(objectMapper.readValue(response.body(), TokenInfoResponse.class));
  }

  private Optional<UserInfoResponse> requestUserInfo (String token) throws IOException, InterruptedException {
    HttpResponse<String> response = sendRequest(
      RequestType.GET,
      serverUrl + "/realms/" + realm + URL_USER_INFO,
      new String[]{
        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        "Authorization", "Bearer " + token
      },
      null
    );

    if (response.statusCode() != HttpStatus.OK.value()) return Optional.empty();

    ObjectMapper objectMapper = new ObjectMapper();

    return Optional.of(objectMapper.readValue(response.body(), UserInfoResponse.class));
  }

  public Optional<AccessTokenResponse> requestToken (String username, String password) throws IOException, InterruptedException {
    HttpRequest.BodyPublisher body = buildFormDataFromMap(Map.of(
      "grant_type", "password",
      "client_id", clientId,
      "client_secret", clientSecret,
      "scope", "openid profile email",
      "username", username,
      "password", password
    ));

    HttpResponse<String> response = sendRequest(
      RequestType.POST,
      serverUrl + "/realms/" + realm + URL_TOKEN,
      new String[]{HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE},
      body
    );

    if (response.statusCode() != HttpStatus.OK.value()) return Optional.empty();

    ObjectMapper objectMapper = new ObjectMapper();

    return Optional.of(objectMapper.readValue(response.body(), AccessTokenResponse.class));
  }

  public Optional<CertificatesResponse> requestCertificates () throws IOException, InterruptedException {
    HttpResponse<String> response = sendRequest(
      RequestType.GET,
      serverUrl + "/realms/" + realm + URL_CERT_INFO,
      new String[]{HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE},
      null
    );

    if (response.statusCode() != HttpStatus.OK.value()) return Optional.empty();

    ObjectMapper objectMapper = new ObjectMapper();

    return Optional.of(objectMapper.readValue(response.body(), CertificatesResponse.class));
  }

  private HttpResponse<String> sendRequest (
    RequestType requestType,
    String path,
    String[] headers,
    HttpRequest.BodyPublisher body
  ) throws IOException, InterruptedException {
    URI uri = URI.create(path);

    LoggerWrapper.info("Sending request to URI " + uri, this.getClass().getName());

    try {
      HttpRequest request = createRequest(requestType, uri, headers, body);

      HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

      LoggerWrapper.info("Response status code: " + response.statusCode(), this.getClass().getName());

      return response;
    } catch (Exception e) {
      LoggerWrapper.info("Error sending request with URI: " + uri + ": " + e.getMessage(), this.getClass().getName());
      throw e;
    }
  }

  private HttpRequest createRequest (RequestType requestType, URI uri, String[] headers, HttpRequest.BodyPublisher body) {
    HttpRequest request = null;

    switch (requestType) {
      case GET -> {
        request  = HttpRequest.newBuilder(uri)
          .headers(headers)
          .build();
      }
      case POST -> {
        request  = HttpRequest.newBuilder(uri)
          .headers(headers)
          .POST(body)
          .build();
      }
    }

    return request;
  }

  private HttpRequest.BodyPublisher buildFormDataFromMap(Map<String, String> data) {
    String formData = data.entrySet().stream()
      .map(entry -> entry.getKey() + "=" + encode(entry.getValue()))
      .collect(Collectors.joining("&"));

    return HttpRequest.BodyPublishers.ofString(formData);
  }

  private String encode (String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException unsupportedEncodingException) {
      return "";
    }
  }
}

