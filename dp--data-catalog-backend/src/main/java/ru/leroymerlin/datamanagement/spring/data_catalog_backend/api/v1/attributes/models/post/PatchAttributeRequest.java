package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PatchAttributeRequest implements Request {
  private String value;
}
