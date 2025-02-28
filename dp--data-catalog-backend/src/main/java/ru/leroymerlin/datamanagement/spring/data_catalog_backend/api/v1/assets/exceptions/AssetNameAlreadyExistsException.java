package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.apache.commons.lang3.StringUtils;
import lombok.Getter;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;

/**
 * @author juliwolf
 */

@Getter
@Setter
public class AssetNameAlreadyExistsException extends RuntimeException {
  private PostOrPatchAssetRequest details = new PostOrPatchAssetRequest();

  private DataIntegrityViolationException originalException;

  public AssetNameAlreadyExistsException (DataIntegrityViolationException originalException) {
    super("Asset already exists.");

    parseErrorToDetails(originalException.getMessage());
  }

  private void parseErrorToDetails (String originalMessage) {
    String pattern = "Detail: Key \\(asset_name, deleted_flag\\)=\\((.*?), f\\) already exists\\.";
    String assetName = null;

    Matcher matcher = Pattern.compile(pattern).matcher(originalMessage);

    if (matcher.find()) {
      assetName = matcher.group(1);
    }

    if (StringUtils.isEmpty(assetName)) return;

    details.setAsset_name(assetName);
  }
}
