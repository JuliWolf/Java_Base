package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get;

import java.sql.Timestamp;
import java.util.List;
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
public class GetAssetChangeHistory implements Response {
  private long total;

  private int page_size;

  private int page_number;

  private List<ChangeHistory> results;

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  public static class ChangeHistory implements Response {
    private String action_type_name;

    private String action_type_name_ru;

    private String entity_type_name;

    private String entity_type_name_ru;

    private UUID user_id;

    private String username;

    private String first_name;

    private String last_name;

    private java.sql.Timestamp logged_on;

    private ActionDetails action_details;

    public ChangeHistory (
      Timestamp loggedOn,
      String lastName,
      String firstName,
      String username,
      UUID userId,
      String entityTypeNameRu,
      String entityTypeName,
      String actionTypeNameRu,
      String actionTypeName,
      UUID objectId,
      UUID objectTypeId,
      String objectTypeName,
      String value
    ) {
      this.logged_on = loggedOn;
      this.last_name = lastName;
      this.first_name = firstName;
      this.username = username;
      this.user_id = userId;
      this.entity_type_name_ru = entityTypeNameRu;
      this.entity_type_name = entityTypeName;
      this.action_type_name_ru = actionTypeNameRu;
      this.action_type_name = actionTypeName;

      this.action_details = new ActionDetails(objectId, objectTypeId, objectTypeName, value);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class ActionDetails implements Response {
      private UUID objectId;

      private UUID objectTypeId;

      private String objectTypeName;

      private String value;
    }
  }
}
