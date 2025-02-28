package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PostAllowedValueRequest implements Request {
  private String attribute_type_id;

  private String value;
}
