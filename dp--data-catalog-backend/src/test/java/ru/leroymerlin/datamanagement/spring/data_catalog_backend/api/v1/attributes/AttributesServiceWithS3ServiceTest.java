package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesServiceImpl;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetLinkUsage.AssetLinkUsageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.imageLinkUsage.ImageLinkUsageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.s3.S3Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PatchAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeRequest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

/**
 * @author juliwolf
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AttributesServiceWithS3ServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private AttributeRepository attributeRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;
  @Autowired
  private AssetLinkUsageRepository assetLinkUsageRepository;
  @Autowired
  private ImageLinkUsageRepository imageLinkUsageRepository;

  @MockBean
  private S3Service s3Service;

  @Autowired
  private AttributesServiceImpl attributesService;

  AssetType simpleAssetType;
  AssetType differentAssetType;
  Asset simpleAsset;
  Asset differentAsset;
  AttributeType booleanAttributeType;
  AttributeType textAttributeType;
  AssetTypeAttributeTypeAssignment simpleAssetBooleanAttributeTypeAssignment;
  AssetTypeAttributeTypeAssignment differentAssetTextAttributeTypeAssignment;

  @BeforeAll
  public void prepareData () {
    simpleAssetType = assetTypeRepository.save(new AssetType("simple asset type", "some simple description", "sa", "blue", language, user));
    differentAssetType = assetTypeRepository.save(new AssetType("different asset type", "different desc", "da", "purple", language, user));

    simpleAsset = assetRepository.save(new Asset("simple asset", simpleAssetType, "simple", language, null, null, user));
    differentAsset = assetRepository.save(new Asset("different asset", differentAssetType, "different", language, null, null, user));

    booleanAttributeType = attributeTypeRepository.save(new AttributeType("boolean attribute type", "attribute with boolean type", AttributeKindType.BOOLEAN, null, null, language, user));
    textAttributeType = attributeTypeRepository.save(new AttributeType("text attribute type", "attribute with text type", AttributeKindType.RTF, null, null, language, user));

    simpleAssetBooleanAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(simpleAssetType, booleanAttributeType, user));
    differentAssetTextAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(differentAssetType, textAttributeType, user));
  }

  @AfterAll
  public void clearData () {
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearAttributes () {
    attributeRepository.deleteAll();
    assetLinkUsageRepository.deleteAll();
    imageLinkUsageRepository.deleteAll();
  }

  @Test
  public void createAttributeCreateImageLinkUsageIntegrationTest () {
    try {
      String firstImageUrl = UUID.randomUUID() + ".png";
      String secondImageUrl = UUID.randomUUID() + ".jpeg";

      when(s3Service.doesObjectExists(nullable(String.class))).thenReturn(true);

      attributesService.createAttribute(
        new PostAttributeRequest(
          textAttributeType.getAttributeTypeId().toString(),
          differentAsset.getAssetId(),
          new StringBuilder()
            .append("<p><img src=\"https://t-data-catalog-images.storage.yandexcloud.net/")
            .append(firstImageUrl)
            .append("\"/><p>Some text</p>")
            .append("<img class=\"image\" src=\"https://t-data-catalog-images.storage.yandexcloud.net/")
            .append(secondImageUrl)
            .append("\"/>")
            .toString()
        ), user
      );

      assertAll(
        () -> assertEquals(2, imageLinkUsageRepository.findAll().size()),
        () -> assertEquals(firstImageUrl, imageLinkUsageRepository.findAll().stream().filter(image -> image.getImageName().equals(firstImageUrl)).findFirst().get().getImageName())
      );

    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  @Test
  public void createAttributesBulkCreateImageLinksIntegrationTest () {
    try {
      String firstImageUrl = UUID.randomUUID() + ".png";
      String secondImageUrl = UUID.randomUUID() + ".jpeg";

      when(s3Service.doesObjectExists(nullable(String.class))).thenReturn(true);

      PostAttributeRequest firstRequest = new PostAttributeRequest(booleanAttributeType.getAttributeTypeId().toString(), simpleAsset.getAssetId(), "true");
      PostAttributeRequest secondRequest = new PostAttributeRequest(
        textAttributeType.getAttributeTypeId().toString(),
        differentAsset.getAssetId(),
        new StringBuilder()
          .append("<p><img src=\"https://t-data-catalog-images.storage.yandexcloud.net/")
          .append(firstImageUrl)
          .append("\"/><p>Some text</p>")
          .append("<img class=\"image\" src=\"https://t-data-catalog-images.storage.yandexcloud.net/")
          .append(secondImageUrl)
          .append("\"/>")
          .toString()
      );
      List<PostAttributeRequest> requests = new ArrayList<>();
      requests.add(firstRequest);
      requests.add(secondRequest);

      attributesService.createAttributesBulk(requests, user);

      assertAll(
        () -> assertEquals(2, imageLinkUsageRepository.findAll().size()),
        () -> assertEquals(firstImageUrl, imageLinkUsageRepository.findAll().stream().filter(image -> image.getImageName().equals(firstImageUrl)).findFirst().get().getImageName())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateAttributeClearExistingImageLinksIntegrationTest () {
    try {
      when(s3Service.doesObjectExists(any(String.class))).thenReturn(true);

      String firstImageUrl = UUID.randomUUID() + ".png";
      String secondImageUrl = UUID.randomUUID() + ".jpeg";
      String thirdImageUrl = UUID.randomUUID() + ".tiff";

      Attribute textAttribute = new Attribute(textAttributeType, differentAsset, language, user);
      textAttribute.setValue(
        new StringBuilder()
          .append("<p><img src=\"https://t-data-catalog-images.storage.yandexcloud.net/")
          .append(firstImageUrl)
          .append("\"/><p>Some text</p>")
          .append("<img class=\"image\" src=\"https://t-data-catalog-images.storage.yandexcloud.net/")
          .append(secondImageUrl)
          .append("\"/>")
          .toString());

      attributeRepository.save(textAttribute);

      imageLinkUsageRepository.save(new ImageLinkUsage(firstImageUrl, textAttribute, user));
      imageLinkUsageRepository.save(new ImageLinkUsage(secondImageUrl, textAttribute, user));

      attributesService.updateAttribute(textAttribute.getAttributeId(), new PatchAttributeRequest(new StringBuilder()
        .append("<p><img src=\"https://t-data-catalog-images.storage.yandexcloud.net/")
        .append(thirdImageUrl)
        .append("\"/><p>Some text</p>")
        .toString()
      ), user);

      assertAll(
        () -> assertEquals(1, imageLinkUsageRepository.findAllByAttributeId(textAttribute.getAttributeId()).size()),
        () -> assertEquals(thirdImageUrl, imageLinkUsageRepository.findAllByAttributeId(textAttribute.getAttributeId()).get(0).getImageName())
      );
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
