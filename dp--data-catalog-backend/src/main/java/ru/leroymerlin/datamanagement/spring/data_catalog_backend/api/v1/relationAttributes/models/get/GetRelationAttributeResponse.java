package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.models.get;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.interfaces.Response;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.models.RelationAttributeWithConnectedValues;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetRelationAttributeResponse implements Response {
  private UUID relation_attribute_id;

  private UUID attribute_type_id;

  private String attribute_type_name;

  private AttributeKindType attribute_kind;

  private UUID relation_id;

  private String value;

  private Boolean integer_flag;

  private Double value_numeric;

  private Boolean value_bool;

  private java.sql.Timestamp value_datetime;

  private String source_language;

  private java.sql.Timestamp created_on;

  private UUID created_by;

  private java.sql.Timestamp last_modified_on;

  private UUID last_modified_by;

  public GetRelationAttributeResponse (RelationAttributeWithConnectedValues relationAttributeWithConnectedValues) {
    this.relation_attribute_id = relationAttributeWithConnectedValues.getRelationAttributeId();
    this.attribute_type_id = relationAttributeWithConnectedValues.getAttributeTypeId();
    this.attribute_type_name = relationAttributeWithConnectedValues.getAttributeTypeName();
    this.attribute_kind = relationAttributeWithConnectedValues.getAttributeKind();
    this.relation_id = relationAttributeWithConnectedValues.getRelationId();
    this.value = relationAttributeWithConnectedValues.getValue();
    this.integer_flag = relationAttributeWithConnectedValues.getIsInteger();
    this.value_numeric = relationAttributeWithConnectedValues.getValueNumeric();
    this.value_bool = relationAttributeWithConnectedValues.getValueBool();
    this.value_datetime = relationAttributeWithConnectedValues.getValueDatetime();
    this.source_language = relationAttributeWithConnectedValues.getSourceLanguage();
    this.created_on = relationAttributeWithConnectedValues.getCreatedOn();
    this.created_by = relationAttributeWithConnectedValues.getCreatedBy();
    this.last_modified_on = relationAttributeWithConnectedValues.getLastModifiedOn();
    this.last_modified_by = relationAttributeWithConnectedValues.getLastModifiedBy();
  }
}
