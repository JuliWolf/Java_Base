package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class BackendDatasourceConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.backend")
    public DataSourceProperties backendDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.backend.hikari")
    public DataSource backendDataSource() {
        return backendDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.backend.liquibase")
    public LiquibaseProperties backendLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    public SpringLiquibase primaryLiquibase() {
        return springLiquibase(backendDataSource(), backendLiquibaseProperties());
    }

    private static SpringLiquibase springLiquibase(DataSource dataSource, LiquibaseProperties properties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setContexts(properties.getContexts());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isEnabled());
        liquibase.setChangeLogParameters(properties.getParameters());
        liquibase.setRollbackFile(properties.getRollbackFile());
        return liquibase;
    }

    @Bean
    public LocalSessionFactoryBean backendSession() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(backendDataSource());
        sessionFactory.setPackagesToScan("ru.leroymerlin.datamanagement.spring.data_catalog_backend");
        return sessionFactory;
    }
}
