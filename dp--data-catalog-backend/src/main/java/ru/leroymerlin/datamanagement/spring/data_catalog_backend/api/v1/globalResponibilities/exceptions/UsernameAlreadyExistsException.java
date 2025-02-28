package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.globalResponibilities.exceptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.apache.commons.lang3.StringUtils;
import lombok.Getter;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post.PostOrPatchUserRequest;

/**
 * @author juliwolf
 */

@Getter
@Setter
public class UsernameAlreadyExistsException extends RuntimeException {
  private PostOrPatchUserRequest details = new PostOrPatchUserRequest();

  private DataIntegrityViolationException originalException;

  public UsernameAlreadyExistsException (DataIntegrityViolationException originalException) {
    super("User already exists.");

    parseErrorToDetails(originalException.getMessage());
  }

  private void parseErrorToDetails (String originalMessage) {
    String pattern = "Detail: Key \\(username, deleted_flag\\)=\\((.*?), f\\) already exists\\.";
    String username = null;

    Matcher matcher = Pattern.compile(pattern).matcher(originalMessage);

    if (matcher.find()) {
      username = matcher.group(1);
    }

    if (StringUtils.isEmpty(username)) return;

    details.setUsername(username);
  }
}
