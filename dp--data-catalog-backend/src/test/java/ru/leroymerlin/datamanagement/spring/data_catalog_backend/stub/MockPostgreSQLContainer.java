package ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * @author juliwolf
 */

public class MockPostgreSQLContainer extends PostgreSQLContainer<MockPostgreSQLContainer> {
  public static final String IMAGE_VERSION = "postgres:15";
  private static MockPostgreSQLContainer container;

  private MockPostgreSQLContainer() {
    super(IMAGE_VERSION);
  }

  public static MockPostgreSQLContainer getInstance() {
    if (container == null) {
      container = new MockPostgreSQLContainer();
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    System.setProperty("DB_URL", container.getJdbcUrl());
    System.setProperty("DB_USERNAME", container.getUsername());
    System.setProperty("DB_PASSWORD", container.getPassword());
  }

  @Override
  public void stop() {
//    super.stop();
  }
}
