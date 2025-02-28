package ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub;

import java.lang.reflect.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ContextConfiguration;
import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeAll;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.KeycloakService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.CertificatesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.models.TokenInfoResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.HTTPRequestUtilsStub;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.TestUtils.MAPPER;

/**
 * @author juliwolf
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = { MockPermissionTest.WireMockContextInitializer.class})
public abstract class MockPermissionTest extends ServiceWithUserIntegrationTest {

  protected static HTTPRequestUtilsStub httpRequestUtilsStub = new HTTPRequestUtilsStub();

  @Value("${keycloak.realm}")
  private String realm;

  @LocalServerPort
  protected int port;

  @Autowired
  protected WireMockServer wireMockServer;

  @Autowired
  private ConfigurableApplicationContext context;

  public static class WireMockContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public void initialize(ConfigurableApplicationContext applicationContext) {
      WireMockServer wmServer = new WireMockServer(options().dynamicPort());
      wmServer.start();

      applicationContext.getBeanFactory().registerSingleton("wireMock", wmServer);

      applicationContext.addApplicationListener(event -> {
        if (event instanceof ContextClosedEvent) {
          wmServer.stop();
        }
      });
    }
  }

  @PostConstruct
  public void configureProperties () throws Exception {
    int wiremockPort = wireMockServer.port();
    KeycloakService keycloakService = context.getBean(KeycloakService.class);
    Field serverUrl = keycloakService.getClass().getDeclaredField("serverUrl");
    serverUrl.setAccessible(true);
    serverUrl.set(keycloakService, "http://localhost:" + wiremockPort);
    serverUrl.setAccessible(false);
  }

  @BeforeAll
  public void mockKeycloakRequest () throws Exception {
    TokenInfoResponse tokenInfoResponse = new TokenInfoResponse(true, ServiceWithUserIntegrationTest.TEST_USERNAME);
    wireMockServer.stubFor(
      post(urlPathEqualTo("/realms/" + realm + KeycloakService.URL_TOKEN_INFO))
        .willReturn(okJson(MAPPER.writeValueAsString(tokenInfoResponse)))
    );

    wireMockServer.stubFor(
      get(urlPathEqualTo("/realms/" + realm + KeycloakService.URL_CERT_INFO))
        .willReturn(okJson(MAPPER.writeValueAsString(new CertificatesResponse()))
    ));
  }
}
