package ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * @author juliwolf
 */

@Getter
@Setter
public class RequestValues {
  public final String ROLE_KEY = "role_id";
  public final String ASSET_TYPE_KEY = "asset_type_id";
  public final String ATTRIBUTE_TYPE_KEY = "attribute_type_id";
  public final String RELATION_TYPE_KEY = "relation_type_id";
  public final String ASSET_ID_KEY = "asset_id";

  public final List<String> SEARCH_FIELDS = List.of(ASSET_ID_KEY, ASSET_TYPE_KEY, ROLE_KEY, ATTRIBUTE_TYPE_KEY, RELATION_TYPE_KEY);

  private List<UUID> roleIds = new ArrayList<>();
  private List<UUID> assetIds = new ArrayList<>();
  private List<UUID> assetTypeIds = new ArrayList<>();
  private List<UUID> attributeTypeIds = new ArrayList<>();
  private List<UUID> relationTypeIds = new ArrayList<>();

  public void putValue (String key, UUID value) {
    switch (key) {
      case ROLE_KEY -> this.roleIds.add(value);
      case ASSET_ID_KEY -> this.assetIds.add(value);
      case ASSET_TYPE_KEY -> this.assetTypeIds.add(value);
      case ATTRIBUTE_TYPE_KEY -> this.attributeTypeIds.add(value);
      case RELATION_TYPE_KEY -> this.relationTypeIds.add(value);
    }
  }

  public void addAllAssetIds (List<UUID> uuids) {
    this.assetIds.addAll(uuids);
  }
}
