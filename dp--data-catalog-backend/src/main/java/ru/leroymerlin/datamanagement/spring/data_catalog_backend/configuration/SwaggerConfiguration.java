package ru.leroymerlin.datamanagement.spring.data_catalog_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;

import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.AuthConstants.HEADER_TOKEN;

@Configuration
public class SwaggerConfiguration {
  @Bean
  public OperationCustomizer customGlobalHeaders() {

    return (Operation operation, HandlerMethod handlerMethod) -> {

      Parameter headerToken = new Parameter()
        .in(ParameterIn.HEADER.toString())
        .schema(new StringSchema())
        .name(HEADER_TOKEN)
        .required(false);

      operation.addParametersItem(headerToken);

      return operation;
    };
  }
}
