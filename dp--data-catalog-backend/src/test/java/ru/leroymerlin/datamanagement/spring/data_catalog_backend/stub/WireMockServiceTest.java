package ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub;

import java.lang.reflect.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ContextConfiguration;
import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.annotation.PostConstruct;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.keycloak.KeycloakService;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * @author juliwolf
 */

@ContextConfiguration(initializers = { WireMockServiceTest.WireMockContextInitializer.class})
public class WireMockServiceTest {
  @Autowired
  protected WireMockServer wireMockServer;

  @Autowired
  private ConfigurableApplicationContext context;

  public static class WireMockContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public void initialize(ConfigurableApplicationContext applicationContext) {
      WireMockServer wmServer = new WireMockServer(options().port(9000));
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
}
