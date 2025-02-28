package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Request;

/**
 * @author JuliWolf
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PatchStatusRequest implements Request {
  private Optional<String> status_name;

  private Optional<String> status_description;
}
