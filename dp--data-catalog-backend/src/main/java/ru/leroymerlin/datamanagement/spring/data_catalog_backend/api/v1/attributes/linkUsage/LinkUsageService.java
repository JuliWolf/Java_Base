package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.linkUsage;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.s3.S3Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.UUIDUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetLinkUsage.AssetLinkUsageDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.imageLinkUsage.ImageLinkUsageDAO;

/**
 * @author juliwolf
 */

@Service
public class LinkUsageService {
  private final AssetsDAO assetsDAO;

  private final AssetLinkUsageDAO assetLinkUsageDAO;

  private final S3Service s3Service;

  private final ImageLinkUsageDAO imageLinkUsageDAO;

  @Autowired
  public LinkUsageService (AssetsDAO assetsDAO, AssetLinkUsageDAO assetLinkUsageDAO, S3Service s3Service, ImageLinkUsageDAO imageLinkUsageDAO) {
    this.assetsDAO = assetsDAO;
    this.assetLinkUsageDAO = assetLinkUsageDAO;
    this.s3Service = s3Service;
    this.imageLinkUsageDAO = imageLinkUsageDAO;
  }

  public void parseAttributeValueToAttributeLink (Attribute attribute, User user) {
    HashSet<UUID> assetIds = findAllAssetIdsInString(attribute.getValue());

    List<Asset> foundAssets = assetsDAO.findAllByAssetIds(assetIds.stream().toList());

    foundAssets.forEach(asset -> {
      assetLinkUsageDAO.saveAssetLinkUsage(new AssetLinkUsage(
        attribute,
        asset,
        user
      ));
    });
  }

  public void parseAttributeValueToImageLink (Attribute attribute, User user) {
    HashSet<String> imagesNameSet = findAllImageIdsInString(attribute.getValue());

    imagesNameSet.forEach(imageName -> {
      if (!s3Service.doesObjectExists(imageName)) return;

      imageLinkUsageDAO.saveImageLinkUsage(new ImageLinkUsage(
        imageName,
        attribute,
        user
      ));
    });
  }

  private HashSet<UUID> findAllAssetIdsInString (String value) {
    HashSet<UUID> UUIDSet = new HashSet<>();

    try {
      // ‘<a href=\"9f76d998-7cf5-4311-989e-5c47b7b39393\"\/>Some text<\/a>’ -> 9f76d998-7cf5-4311-989e-5c47b7b39393
      // ‘<a href=\"https://<md.hostname>/asset/9f76d998-7cf5-4311-989e-5c47b7b39393\"\/>Some text<\/a>’ -> 9f76d998-7cf5-4311-989e-5c47b7b39393
      String UUIDPattern = "(\\w{8}\\-\\w{4}\\-\\w{4}\\-\\w{4}\\-\\w{12})";
      String pattern = "/asset/" + UUIDPattern + "|href=\"" + UUIDPattern;

      Matcher matcher = parseString(value, pattern);

      while (matcher.find()) {
        String assetId = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);

        if (UUIDUtils.isValidUUID(assetId)) {
          UUIDSet.add(UUID.fromString(assetId));
        }
      }

      return UUIDSet;
    } catch (PatternSyntaxException ignored) {
      return UUIDSet;
    }
  }

  private HashSet<String> findAllImageIdsInString (String value) {
    HashSet<String> imagesNameSet = new HashSet<>();

    try {
      // ‘<img%src="https://<md.hostname>/9f76d998-7cf5-4311-989e-5c47b7b39393"/>’ -> 9f76d998-7cf5-4311-989e-5c47b7b39393
      String UUIDPattern = "(\\w{8}\\-\\w{4}\\-\\w{4}\\-\\w{4}\\-\\w{12}\\.{1}(png|jpeg|tiff))";
      String pattern = "img[\\s\\w=\"]+src\\s*=\\s*\"https?:.{2}[\\w\\.-]+.{1}" + UUIDPattern;

      Matcher matcher = parseString(value, pattern);

      while (matcher.find()) {
        String imageName = matcher.group(1);

        if (StringUtils.isNotEmpty(imageName)) {
          imagesNameSet.add(imageName);
        }
      }

      return imagesNameSet;
    } catch (PatternSyntaxException ignored) {
      return imagesNameSet;
    }
  }

  private Matcher parseString (String value, String pattern) {
    Pattern compiledPattern = Pattern.compile(pattern);

    return compiledPattern.matcher(value);
  }
}
