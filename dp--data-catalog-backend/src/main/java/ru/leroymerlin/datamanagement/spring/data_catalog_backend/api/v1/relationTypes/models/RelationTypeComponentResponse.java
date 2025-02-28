package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models;

import java.util.UUID;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;

public interface RelationTypeComponentResponse {
  UUID getRelation_type_component_id();

  String getRelation_type_component_name();

  String getRelation_type_component_description();

  ResponsibilityInheritanceRole getResponsibility_inheritance_role();

  HierarchyRole getHierarchy_role();

  Boolean getSingle_relation_type_component_for_asset_flag();

  java.sql.Timestamp getCreated_on();

  UUID getCreated_by();

  java.sql.Timestamp getLast_modified_on();

  UUID getLast_modified_by();
}
