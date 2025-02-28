package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.models.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PatchRelationComponentAttributeRequest implements Request {
  private String value;
}
