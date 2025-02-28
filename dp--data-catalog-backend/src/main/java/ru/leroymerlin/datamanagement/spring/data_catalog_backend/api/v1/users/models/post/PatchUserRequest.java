package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.models.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PatchUserRequest extends PostOrPatchUserRequest implements Request {
  private UUID user_id;
}
