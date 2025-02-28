package ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author juliwolf
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Configuration
@Testcontainers
@ComponentScan(lazyInit = true)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
  properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public @interface Testable {
}
