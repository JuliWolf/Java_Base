package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post;

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
@Getter
@Setter
public class PostExecuteUpdateRequest implements Request {
  private UUID jobId;
}
