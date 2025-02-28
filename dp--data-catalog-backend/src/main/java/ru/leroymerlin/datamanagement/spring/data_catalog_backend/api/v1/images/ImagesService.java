package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images;

import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.models.ImageResponse;

/**
 * @author juliwolf
 */

public interface ImagesService {
  ImageResponse uploadImage (MultipartFile file) throws IOException;

  void deleteImage (String fileName);

  List<ImageResponse> getImages();

}
