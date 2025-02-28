package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ImageResponse implements Response {
  private String file_name;

  private String file_public_url;
}
