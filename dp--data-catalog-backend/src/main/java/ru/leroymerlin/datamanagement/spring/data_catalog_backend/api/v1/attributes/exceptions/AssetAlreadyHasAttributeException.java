package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.exceptions;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.apache.commons.lang3.StringUtils;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeRequest;

/**
 * @author juliwolf
 */

@Getter
public class AssetAlreadyHasAttributeException extends RuntimeException {
  private PostAttributeRequest details = new PostAttributeRequest();

  public AssetAlreadyHasAttributeException (DataIntegrityViolationException originalException) {
    super("This asset already has this attribute");

    parseErrorToDetails(originalException.getMessage());
  }

  private void parseErrorToDetails (String originalMessage) {
    String pattern = "Detail: Key \\(attribute_type_id, asset_id, deleted_flag\\)=\\((.*), (.*), f\\)";
    String attributeTypeId = null;
    String assetId = null;

    Matcher matcher = Pattern.compile(pattern).matcher(originalMessage);

    if (matcher.find()) {
      attributeTypeId = matcher.group(1);
      assetId = matcher.group(2);
    }

    if (
      StringUtils.isEmpty(assetId) &&
      StringUtils.isEmpty(attributeTypeId)
    ) return;

    details.setAsset_id(UUID.fromString(assetId));
    details.setAttribute_type_id(attributeTypeId);
  }
}
