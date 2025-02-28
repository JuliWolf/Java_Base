package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.s3.S3Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.s3.UploadedFile;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.exceptions.FailedToUploadImageException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.exceptions.WrongFileTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.models.ImageResponse;

/**
 * @author juliwolf
 */

@Service
public class ImagesServiceImpl implements ImagesService {

  @Autowired
  private S3Service s3Service;

  @Override
  public ImageResponse uploadImage (
    MultipartFile file
  ) throws WrongFileTypeException, FailedToUploadImageException
  {
    try {
      UploadedFile uploadedFile = s3Service.putObject(file.getOriginalFilename(), file.getContentType(), file.getBytes());

      return new ImageResponse(
        uploadedFile.getFileName(),
        uploadedFile.getFileUrl()
      );
    } catch (Exception exception) {
      throw new FailedToUploadImageException(file.getName());
    }
  }

  @Override
  public void deleteImage (String fileName) {
    s3Service.deleteObject(fileName);
  }

  @Override
  public List<ImageResponse> getImages () {
    List<S3ObjectSummary> s3Images = s3Service.getAllObjects();

    return s3Images.stream()
      .map(image -> new ImageResponse(
          image.getKey(),
          s3Service.getUrl(image.getKey())
        ))
      .toList();
  }
}
