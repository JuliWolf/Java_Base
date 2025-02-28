package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration;

import javax.sql.DataSource;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "backendEntityManagerFactory",
        transactionManagerRef = "backendTransactionManager",
        basePackages = {
          "ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend",
          "ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta",
          "ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta"
        }
)
public class BackendJpaConfiguration {
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean backendEntityManagerFactory(
            @Qualifier("backendDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages(
                        "ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend",
                        "ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta",
                        "ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta"
                )
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager backendTransactionManager(
            @Qualifier("backendEntityManagerFactory") LocalContainerEntityManagerFactoryBean backendEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(backendEntityManagerFactory.getObject()));
    }
}
