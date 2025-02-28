package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post;

import java.util.List;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Service
public class UserPhotoLink {
  private List<Photo> photo;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Service
  public static class Photo {
    private String storage_type;

    private String host;

    private String bucket;

    private String key;

    private String photo_source;
  }
}
