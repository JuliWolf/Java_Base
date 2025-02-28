package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.apache.commons.lang3.StringUtils;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;

/**
 * @author juliwolf
 */

@Getter
public class ResponsibilityAlreadyExistsException extends RuntimeException {
  private final PostResponsibilityRequest details = new PostResponsibilityRequest();

  public ResponsibilityAlreadyExistsException () {
    super("Responsibility with these parameters already exists.");
  }

  public ResponsibilityAlreadyExistsException (DataIntegrityViolationException originalException) {
    this();

    parseErrorToDetails(originalException.getMessage());
  }

  private void parseErrorToDetails (String originalMessage) {
    boolean isFound = checkUserPattern(originalMessage);

    if (isFound) return;

    checkGroupPattern(originalMessage);
  }

  private boolean checkUserPattern (String originalMessage) {
    String userPattern = "Detail: Key \\(asset_id, role_id, responsible_type, user_id, deleted_flag\\)=\\((.*?), (.*?), (.*?), (.*?), f\\) already exists\\.";

    boolean isFound = false;
    Matcher userMatcher = Pattern.compile(userPattern).matcher(originalMessage);

    if (userMatcher.find()) {
      isFound = true;

      UUID assetId = StringUtils.isNotEmpty(userMatcher.group(1)) ? UUID.fromString(userMatcher.group(1)) : null;
      details.setAsset_id(assetId);

      UUID roleId = StringUtils.isNotEmpty(userMatcher.group(2)) ? UUID.fromString(userMatcher.group(2)) : null;
      details.setRole_id(roleId);

      String responsibleType = StringUtils.isNotEmpty(userMatcher.group(3)) ? userMatcher.group(3) : null;
      details.setResponsible_type(responsibleType);

      UUID userId = StringUtils.isNotEmpty(userMatcher.group(4)) ? UUID.fromString(userMatcher.group(4)) : null;
      details.setResponsible_id(userId);
    }

    return isFound;
  }

  private void checkGroupPattern (String originalMessage) {
    String groupPattern = "Detail: Key \\(asset_id, role_id, responsible_type, group_id, deleted_flag\\)=\\((.*?), (.*?), (.*?), (.*?), f\\) already exists\\.";

    Matcher groupMatcher = Pattern.compile(groupPattern).matcher(originalMessage);

    if (groupMatcher.find()) {
      UUID assetId = StringUtils.isNotEmpty(groupMatcher.group(1)) ? UUID.fromString(groupMatcher.group(1)) : null;
      details.setAsset_id(assetId);

      UUID roleId = StringUtils.isNotEmpty(groupMatcher.group(2)) ? UUID.fromString(groupMatcher.group(2)) : null;
      details.setRole_id(roleId);

      String responsibleType = StringUtils.isNotEmpty(groupMatcher.group(3)) ? groupMatcher.group(3) : null;
      details.setResponsible_type(responsibleType);

      UUID groupId = StringUtils.isNotEmpty(groupMatcher.group(4)) ? UUID.fromString(groupMatcher.group(4)) : null;
      details.setResponsible_id(groupId);
    }
  }
}
