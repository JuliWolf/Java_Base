package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.ImagesServiceImpl;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.s3.S3Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.s3.UploadedFile;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.images.exceptions.FailedToUploadImageException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

/**
 * @author juliwolf
 */
@Testable
public class ImagesServiceTest {
  @Mock
  private S3Service s3Service;

  @InjectMocks
  private ImagesServiceImpl imagesService;

  @Test
  public void uploadImageFailedToLoadTest () {
    when(s3Service.putObject(any(String.class), any(String.class), any(byte[].class)))
      .thenThrow(AmazonS3Exception.class);

    MockMultipartFile file
      = new MockMultipartFile(
      "file",
      "big-image.jpeg",
      MediaType.IMAGE_JPEG_VALUE,
      new byte[1024 * 1024 * 3]
    );

    assertThrows(FailedToUploadImageException.class, () -> imagesService.uploadImage(file));
  }

  @Test
  public void uploadImageSuccessTest () {
    when(s3Service.putObject(nullable(String.class), nullable(String.class), nullable(byte[].class)))
      .thenReturn(new UploadedFile("example.jpg", "someurl"));

    MockMultipartFile file
      = new MockMultipartFile(
      "file",
      "big-image.jpeg",
      MediaType.IMAGE_JPEG_VALUE,
      new byte[1024 * 1024 * 3]
    );

    assertDoesNotThrow(() -> imagesService.uploadImage(file));
  }
}
