package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostAssetTypeCardHeaderAssignmentResponse implements Response {
  private UUID asset_type_card_header_assignment_id;

  private UUID asset_type_id;

  private UUID description_field_attribute_type_id;

  private UUID owner_field_role_id;

  private java.sql.Timestamp created_on;

  private UUID created_by;
}
