package ru.leroymerlin.datamanagement.spring.data_catalog_backend.s3;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import logger.LoggerWrapper;

/**
 * @author juliwolf
 */

@Service
public class S3Service {
  private AmazonS3 s3Client;

  @Value("${service.s3.account-id}")
  private String accountId;

  @Value("${service.s3.secret-key}")
  private String secretKey;

  @Value("${service.s3.bucket-name}")
  private String bucketName;

  public AmazonS3 getS3Client () throws SdkClientException {
    if (this.s3Client == null) {
      init();
    }

    init();

    return s3Client;
  }

  private void init() throws SdkClientException {
    try {
      s3Client = AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(
          new AwsClientBuilder.EndpointConfiguration(
            "https://storage.yandexcloud.net",
            "ru-central1"
          )
        )
        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accountId, secretKey)))
        .build();
    } catch (SdkClientException exception) {
      LoggerWrapper.error("Error creating client for Object Storage via AWS SDK. Reason: {} ",
        exception.getStackTrace(),
        null,
        S3Service.class.getName()
      );

      throw new SdkClientException(exception.getMessage());
    }
  }

  public UploadedFile putObject (String originalFileName, String contentType, byte[] photoBytes) throws AmazonS3Exception {
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(photoBytes.length);
      metadata.setContentType(contentType);

      String fileName = generateUniqueName(originalFileName);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(photoBytes);

      getS3Client().putObject(bucketName, fileName, inputStream, metadata);

      LoggerWrapper.info(
        "Upload Service. Added file: " + fileName + " to bucket: " + bucketName,
        S3Service.class.getName()
      );

      String url = getUrl(fileName);

      return new UploadedFile(
        fileName,
        url
      );
    } catch (AmazonS3Exception exception) {
      LoggerWrapper.error("Error uploading photos to Object Storage. Reason: {}" + exception.getMessage(),
        exception.getStackTrace(),
        null,
        S3Service.class.getName()
      );

      throw new AmazonS3Exception(exception.getMessage());
    }
  }

  public void deleteObject (String fileName) {
    getS3Client().deleteObject(bucketName, fileName);
  }

  public String getUrl (String fileName) {
    return getS3Client().getUrl(bucketName, fileName).toExternalForm();
  }

  public Boolean doesObjectExists (String fileName) {
    return getS3Client().doesObjectExist(bucketName, fileName);
  }

  public List<S3ObjectSummary> getAllObjects () {
    ObjectListing allObjects = getS3Client().listObjects(bucketName);
    List<S3ObjectSummary> list = allObjects.getObjectSummaries();

    while (allObjects.isTruncated()) {
      allObjects = getS3Client().listNextBatchOfObjects(allObjects);

      list.addAll(allObjects.getObjectSummaries());
    }

    return list;
  }

  private String generateUniqueName(String originalFileName) {
    UUID uuid = UUID.randomUUID();

    String extension = StringUtils.getFilenameExtension(originalFileName);
    String fileName = originalFileName.replace("." + extension, "");

    return fileName + "_" + uuid + "." + extension;
  }
}
