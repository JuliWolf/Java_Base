package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract;

import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageRelation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.StageAssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.StageAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.StageRelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.StageResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;

/**
 * @author juliwolf
 */

public class JobsServiceUtils {
  private final StageAssetRepository stageAssetRepository;
  private final StageAttributeRepository stageAttributeRepository;
  private final StageRelationRepository stageRelationRepository;
  private final StageResponsibilityRepository stageResponsibilityRepository;

  private final AssetTypeRepository assetTypeRepository;

  private final AssetRepository assetRepository;
  private final RoleRepository roleRepository;
  private final RelationRepository relationRepository;
  private final AttributeRepository attributeRepository;
  private final RelationTypeRepository relationTypeRepository;
  private final AttributeTypeRepository attributeTypeRepository;
  private final ResponsibilityRepository responsibilityRepository;
  private final RelationComponentRepository relationComponentRepository;
  private final RelationTypeComponentRepository relationTypeComponentRepository;
  private final AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;
  private final RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;

  private final User user;
  private final Language language;

  public JobsServiceUtils (
    StageAssetRepository stageAssetRepository,
    StageAttributeRepository stageAttributeRepository,
    StageRelationRepository stageRelationRepository,
    StageResponsibilityRepository stageResponsibilityRepository,
    AssetTypeRepository assetTypeRepository,
    AssetRepository assetRepository,
    RoleRepository roleRepository, RelationRepository relationRepository,
    AttributeRepository attributeRepository,
    RelationTypeRepository relationTypeRepository,
    AttributeTypeRepository attributeTypeRepository,
    ResponsibilityRepository responsibilityRepository, RelationComponentRepository relationComponentRepository,
    RelationTypeComponentRepository relationTypeComponentRepository,
    AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository,
    RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository,

    User user,
    Language language
  ) {
    this.stageAssetRepository = stageAssetRepository;
    this.stageAttributeRepository = stageAttributeRepository;
    this.stageRelationRepository = stageRelationRepository;
    this.stageResponsibilityRepository = stageResponsibilityRepository;
    this.assetTypeRepository = assetTypeRepository;
    this.assetRepository = assetRepository;
    this.roleRepository = roleRepository;
    this.relationRepository = relationRepository;
    this.attributeRepository = attributeRepository;
    this.relationTypeRepository = relationTypeRepository;
    this.attributeTypeRepository = attributeTypeRepository;
    this.responsibilityRepository = responsibilityRepository;
    this.relationComponentRepository = relationComponentRepository;
    this.relationTypeComponentRepository = relationTypeComponentRepository;
    this.assetTypeAttributeTypeAssignmentRepository = assetTypeAttributeTypeAssignmentRepository;
    this.relationTypeComponentAssetTypeAssignmentRepository = relationTypeComponentAssetTypeAssignmentRepository;

    this.user = user;
    this.language = language;
  }


  public void prepareStageAssets (ExtractJob extractJob, Integer count, ActionDecision actionDecision) {
    if (actionDecision.equals(ActionDecision.I)) {
      for (int i = 0; i < count; i++) {
        AssetType assetType = assetTypeRepository.save(new AssetType("inserted_asset_type_name_" + i + "_" + Math.random() * 100, "inserted_asset_type_description_" + i, "acr_" + i, "color", language, user));
        stageAssetRepository.save(new StageAsset(extractJob, "inserted_asset_name_" + i + Math.random() * 100, assetType.getAssetTypeId(), "asset_displayname_" + i, null, null, actionDecision));
      }
    }

    if (actionDecision.equals(ActionDecision.U)) {
      for (int i = 0; i < count; i++) {
        AssetType assetType = assetTypeRepository.save(new AssetType("updated_asset_type_name_" + i + "_" + Math.random() * 100, "asset_type_description_" + i, "acr_" + i, "color", language, user));
        Asset asset = assetRepository.save(new Asset("updated_asset_name_" + i + "_" + Math.random() * 100, assetType, "displayed name", language, null, null, user));

        StageAsset stageAsset = new StageAsset(extractJob, "updated_new_asset_name_" + i + "_" + Math.random() * 100, assetType.getAssetTypeId(), "asset_displayname_" + i, null, null, actionDecision);
        stageAsset.setMatchedAssetId(asset.getAssetId());
        stageAssetRepository.save(stageAsset);
      }
    }

    if (actionDecision.equals(ActionDecision.D)) {
      for (int i = 0; i < count; i++) {
        AssetType assetType = assetTypeRepository.save(new AssetType("delete_asset_type_name_" + i + "_" + Math.random() * 100, "delete_asset_type_description_" + i, "acr_" + i, "color", language, user));
        Asset asset = assetRepository.save(new Asset("delete_asset_name_" + i + "_" + Math.random() * 100, assetType, "delete_displayed name", language, null, null, user));

        StageAsset stageAsset = new StageAsset(extractJob, null, assetType.getAssetTypeId(), "delete_asset_displayname_" + i, null, null, actionDecision);
        stageAsset.setMatchedAssetId(asset.getAssetId());
        stageAssetRepository.save(stageAsset);
      }
    }
  }

  public void prepareStageAttributes (ExtractJob extractJob, Integer count, ActionDecision actionDecision) {
    if (actionDecision.equals(ActionDecision.I)) {
      for (int i = 0; i < count; i++) {
        AssetType assetType = assetTypeRepository.save(new AssetType("inserted_asset_type_name_for_asset_" + i + "_" + Math.random() * 100, "inserted_asset_type_description_" + i, "acr_" + i, "color", language, user));
        Asset asset = assetRepository.save(new Asset("inserted_attribute_asset_name_" + i + "_" + Math.random() * 100, assetType, "inserted_displayed name", language, null, null, user));
        AttributeType attributeType = attributeTypeRepository.save(new AttributeType("inserted_attribute_type_name_" + i + "_" + Math.random() * 100, "inserted_attribute_type_description_" + i, AttributeKindType.TEXT, null, null, language, user));

        assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, null));
        stageAttributeRepository.save(new StageAttribute(extractJob, asset.getAssetId(), attributeType.getAttributeTypeId(), "inserted_some text", actionDecision));
      }
    }

    if (actionDecision.equals(ActionDecision.U)) {
      for (int i = 0; i < count; i++) {
        AssetType assetType = assetTypeRepository.save(new AssetType("updated_asset_type_name_for_asset_" + i + "_" + Math.random() * 100, "updated_asset_type_description_" + i, "acr_" + i, "color", language, user));
        Asset asset = assetRepository.save(new Asset("updated_attribute_asset_name_" + i + "_" + Math.random() * 100, assetType, "updated_displayed name", language, null, null, user));
        AttributeType attributeType = attributeTypeRepository.save(new AttributeType("updated_attribute_type_name_" + i + "_" + Math.random() * 100, "updated_attribute_type_description_" + i, AttributeKindType.TEXT, null, null, language, user));
        Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

        assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, null));

        StageAttribute stageAttribute = new StageAttribute(extractJob, asset.getAssetId(), attributeType.getAttributeTypeId(), "new some text", actionDecision);
        stageAttribute.setMatchedAttributeId(attribute.getAttributeId());
        stageAttributeRepository.save(stageAttribute);
      }
    }

    if (actionDecision.equals(ActionDecision.D)) {
      for (int i = 0; i < count; i++) {
        AssetType assetType = assetTypeRepository.save(new AssetType("delete_asset_type_name_for_asset_" + i + "_" + Math.random() * 100, "delete_asset_type_description_" + i, "acr_" + i, "color", language, user));
        Asset asset = assetRepository.save(new Asset("delete_attribute_asset_name_" + i + "_" + Math.random() * 100, assetType, "delete_displayed name", language, null, null, user));
        AttributeType attributeType = attributeTypeRepository.save(new AttributeType("delete_attribute_type_name_" + i + "_" + Math.random() * 100, "delete_attribute_type_description_" + i, AttributeKindType.TEXT, null, null, language, user));
        Attribute attribute = attributeRepository.save(new Attribute(attributeType, asset, language, user));

        assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, null));

        StageAttribute stageAttribute = new StageAttribute(extractJob, null, null, "new some text", actionDecision);
        stageAttribute.setMatchedAttributeId(attribute.getAttributeId());
        stageAttributeRepository.save(stageAttribute);
      }
    }
  }

  public void prepareStageRelations (ExtractJob extractJob, Integer count, ActionDecision actionDecision) {
    if (actionDecision.equals(ActionDecision.I)) {
      for (int i = 0; i < count; i++) {
        AssetType firstAssetType = assetTypeRepository.save(new AssetType("Inserted_first_asset_type_name_for_relation_" + i + "_" + Math.random() * 100, "Inserted_asset_type_description_" + i, "acr_" + i, "color", language, user));
        AssetType secondAssetType = assetTypeRepository.save(new AssetType("Inserted_second_asset_type_name_for_relation_" + i + "_" + Math.random() * 100, "Inserted_asset_type_description_" + i, "acr_" + i, "color", language, user));
        Asset firtAsset = assetRepository.save(new Asset("Inserted_first_asset_name_for_relation_" + i + "_" + Math.random() * 100, firstAssetType, "displayed name", language, null, null, user));
        Asset secondAsset = assetRepository.save(new Asset("Inserted_second_asset_name_for_relation_" + i + "_" + Math.random() * 100, secondAssetType, "displayed name", language, null, null, user));

        RelationType relationType = relationTypeRepository.save(new RelationType("Inserted_relation_type_name_" + i + "_" + Math.random() * 100, "description", 2, false, false, language, user));
        RelationTypeComponent firstComponent = relationTypeComponentRepository.save(new RelationTypeComponent("Inserted_child_component_" + i + "_" + Math.random() * 100, "desc", null, null, false,language, relationType, user));
        RelationTypeComponent secondComponent = relationTypeComponentRepository.save(new RelationTypeComponent("Inserted_some_name_" + i + "_" + Math.random() * 100, "desc", null, null, false, language, relationType, user));

        relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstComponent, firstAssetType, false, null, null));
        relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondComponent, secondAssetType, false, null, null));

        stageRelationRepository.save(new StageRelation(extractJob, relationType.getRelationTypeId(), firstComponent.getRelationTypeComponentId(), firtAsset.getAssetId(), secondComponent.getRelationTypeComponentId(), secondAsset.getAssetId(), actionDecision));
      }
    }

    if (actionDecision.equals(ActionDecision.D)) {
      for (int i = 0; i < count; i++) {
        AssetType firstAssetType = assetTypeRepository.save(new AssetType("delete_first_asset_type_name_for_relation_" + i + "_" + Math.random() * 100, "asset_type_description_" + i, "acr_" + i, "color", language, user));
        AssetType secondAssetType = assetTypeRepository.save(new AssetType("delete_second_asset_type_name_for_relation_" + i + "_" + Math.random() * 100, "asset_type_description_" + i, "acr_" + i, "color", language, user));
        Asset firtAsset = assetRepository.save(new Asset("delete_first_asset_name_for_relation_" + i + "_" + Math.random() * 100, firstAssetType, "displayed name", language, null, null, user));
        Asset secondAsset = assetRepository.save(new Asset("delete_second_asset_name_for_relation_" + i + "_" + Math.random() * 100, secondAssetType, "displayed name", language, null, null, user));

        RelationType relationType = relationTypeRepository.save(new RelationType("delete_relation_type_name_" + i + "_" + Math.random() * 100, "description", 2, false, false, language, user));
        RelationTypeComponent firstComponent = relationTypeComponentRepository.save(new RelationTypeComponent("delete_child_component_" + i + "_" + Math.random() * 100, "desc", null, null, null, language, relationType, user));
        RelationTypeComponent secondComponent = relationTypeComponentRepository.save(new RelationTypeComponent("delete_some_name_" + i + "_" + Math.random() * 100, "desc", null, null, null, language, relationType, user));

        relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstComponent, firstAssetType, false, null, null));
        relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondComponent, secondAssetType, false, null, null));

        Relation relation = relationRepository.save(new Relation(relationType, user));
        relationComponentRepository.save(new RelationComponent(relation, firstComponent, firtAsset, null, null, user));
        relationComponentRepository.save(new RelationComponent(relation, secondComponent, secondAsset, null, null, user));

        StageRelation stageRelation = new StageRelation(extractJob, relationType.getRelationTypeId(), firstComponent.getRelationTypeComponentId(), firtAsset.getAssetId(), secondComponent.getRelationTypeComponentId(), secondAsset.getAssetId(), actionDecision);
        stageRelation.setMatchedRelationId(relation.getRelationId());
        stageRelationRepository.save(stageRelation);
      }
    }
  }

  public void prepareStageResponsibilities (ExtractJob extractJob, Integer count, ActionDecision actionDecision) {
    if (actionDecision.equals(ActionDecision.I)) {
      for (int i = 0; i < count; i++) {
        AssetType assetType = assetTypeRepository.save(new AssetType("insert_asset_type_name_for_asset_" + i + "_" + Math.random() * 100, "insert_asset_type_description_" + i, "acr_" + i, "color", language, user));
        Asset asset = assetRepository.save(new Asset("insert_attribute_asset_name_" + i + "_" + Math.random() * 100, assetType, "insert_displayed name", language, null, null, user));
        Role role = roleRepository.save(new Role("insert_role_name_" + i + "_" + Math.random() * 100,"insert_role_description_" + i, language, user));

        stageResponsibilityRepository.save(new StageResponsibility(extractJob, role.getRoleId(), asset.getAssetId(), user.getUserId(), null, ResponsibleType.USER, actionDecision));
      }
    }

    if (actionDecision.equals(ActionDecision.D)) {
      for (int i = 0; i < count; i++) {
        AssetType assetType = assetTypeRepository.save(new AssetType("delete_asset_type_name_for_asset_" + i + "_" + Math.random() * 100, "delete_asset_type_description_" + i, "acr_" + i, "color", language, user));
        Asset asset = assetRepository.save(new Asset("delete_attribute_asset_name_" + i + "_" + Math.random() * 100, assetType, "delete_displayed name", language, null, null, user));
        Role role = roleRepository.save(new Role("delete_role_name_" + i + "_" + Math.random() * 100,"delete_role_description_" + i, language, user));

        Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));

        StageResponsibility stageResponsibility = new StageResponsibility(extractJob, role.getRoleId(), asset.getAssetId(), user.getUserId(), null, ResponsibleType.USER, actionDecision);
        stageResponsibility.setMatchedResponsibilityId(responsibility.getResponsibilityId());
        stageResponsibilityRepository.save(stageResponsibility);
      }
    }
  }
}
