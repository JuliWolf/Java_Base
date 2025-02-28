package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums;

import java.util.Set;

/**
 * @author JuliWolf
 */
public enum EntityNameType {
  ROLE("roles"),

  USER("users"),

  GROUP("groups"),

  STATUS("statuses"),

  ASSET("assets"),

  RELATION("relations"),

  ATTRIBUTE("attributes"),

  ASSET_TYPE("assetTypes"),

  CUSTOM_VIEW("customViews"),

  ATTRIBUTE_TYPE("attributeTypes"),

  RELATION_TYPE("relationTypes"),

  SUBSCRIPTION("subscriptions"),

  RESPONSIBILITY("responsibilities"),

  RELATION_ATTRIBUTE("relationAttributes"),

  RESPONSIBILITY_GLOBAL("globalResponsibilities"),

  RELATION_COMPONENT_ATTRIBUTE("relationComponentAttributes");

  private String value;

  public static EntityNameType[] getValues () {
    return values();
  }

  EntityNameType (String value) {
    this.value = value;
  }

  public Set<String> getConnectedValues () {
    return switch (this) {
      case ROLE -> Set.of("role", "roles", "roleActions", "roleAction");
      case USER -> Set.of("user", "users");
      case GROUP -> Set.of("group", "groups", "userGroup", "userGroups");
      case ASSET -> Set.of("asset", "assets");
      case STATUS -> Set.of("status", "statuses");
      case RELATION -> Set.of("relation", "relations");
      case ATTRIBUTE -> Set.of("attribute", "attributes");
      case ASSET_TYPE -> Set.of("assetType", "assetTypes");
      case CUSTOM_VIEW -> Set.of("customView", "customViews");
      case SUBSCRIPTION -> Set.of("subscription", "subscriptions");
      case RELATION_TYPE -> Set.of("relationType", "relationTypes", "relationTypeComponent", "relationTypeComponents");
      case ATTRIBUTE_TYPE -> Set.of("attributeType", "attributeTypes", "allowedValues");
      case RESPONSIBILITY -> Set.of("responsibility", "responsibilities");
      case RELATION_ATTRIBUTE -> Set.of("relationAttribute", "relationAttributes", "relationAttributeComponent");
      case RESPONSIBILITY_GLOBAL -> Set.of("globalResponsibility", "globalResponsibilities");
      case RELATION_COMPONENT_ATTRIBUTE -> Set.of("relationComponentAttribute", "relationComponentAttributes");
    };
  }

  public boolean isEntityAllowed (EntityNameType entityNameType) {
    return switch (this) {
      case ROLE -> entityNameType.equals(ROLE);
      case USER -> entityNameType.equals(USER);
      case GROUP -> entityNameType.equals(GROUP);
      case ASSET -> entityNameType.equals(ASSET) || entityNameType.equals(ASSET_TYPE);
      case STATUS -> entityNameType.equals(STATUS);
      case RELATION -> entityNameType.equals(RELATION) || entityNameType.equals(RELATION_TYPE);
      case ATTRIBUTE -> entityNameType.equals(ATTRIBUTE) || entityNameType.equals(ATTRIBUTE_TYPE);
      case ASSET_TYPE -> entityNameType.equals(ASSET_TYPE);
      case CUSTOM_VIEW -> entityNameType.equals(CUSTOM_VIEW);
      case SUBSCRIPTION -> entityNameType.equals(SUBSCRIPTION);
      case ATTRIBUTE_TYPE -> entityNameType.equals(ATTRIBUTE_TYPE);
      case RELATION_TYPE -> entityNameType.equals(RELATION_TYPE);
      case RESPONSIBILITY -> entityNameType.equals(RESPONSIBILITY) || entityNameType.equals(ROLE);
      case RELATION_ATTRIBUTE -> entityNameType.equals(RELATION_ATTRIBUTE) || entityNameType.equals(ATTRIBUTE_TYPE);
      case RESPONSIBILITY_GLOBAL -> entityNameType.equals(RESPONSIBILITY_GLOBAL) || entityNameType.equals(ROLE);
      case RELATION_COMPONENT_ATTRIBUTE -> entityNameType.equals(RELATION_COMPONENT_ATTRIBUTE) || entityNameType.equals(ATTRIBUTE_TYPE);
    };
  }
}
