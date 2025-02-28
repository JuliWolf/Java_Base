package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Relation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationComponent;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationComponentAssetCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationTypeComponentAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationWithRelationComponentAndAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetHierarchy.AssetHierarchyDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.RelationAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.RelationComponentAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationComponentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.RelationNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.RelationConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesDAO;

/**
 * @author juliwolf
 */

@Service
public class RelationsDAO {
  protected final RelationComponentRepository relationComponentRepository;

  protected final RelationRepository relationRepository;

  protected final AssetTypesDAO assetTypesDAO;

  protected final ResponsibilitiesDAO responsibilitiesDAO;

  protected final AssetHierarchyDAO assetHierarchyDAO;

  protected final RelationAttributesDAO relationAttributesDAO;

  protected final RelationComponentAttributesDAO relationComponentAttributesDAO;

  @Autowired
  public RelationsDAO (
    RelationComponentRepository relationComponentRepository,
    RelationRepository relationRepository,
    AssetTypesDAO assetTypesDAO,
    ResponsibilitiesDAO responsibilitiesDAO,
    AssetHierarchyDAO assetHierarchyDAO,
    RelationAttributesDAO relationAttributesDAO,
    RelationComponentAttributesDAO relationComponentAttributesDAO
  ) {
    this.relationComponentRepository = relationComponentRepository;
    this.relationRepository = relationRepository;
    this.assetTypesDAO = assetTypesDAO;
    this.responsibilitiesDAO = responsibilitiesDAO;
    this.assetHierarchyDAO = assetHierarchyDAO;
    this.relationAttributesDAO = relationAttributesDAO;
    this.relationComponentAttributesDAO = relationComponentAttributesDAO;
  }

  public Relation findRelationById (UUID relationId) throws RelationNotFoundException {
    Optional<Relation> foundRelation = relationRepository.findById(relationId);

    if (foundRelation.isEmpty()) {
      throw new RelationNotFoundException();
    }

    if (foundRelation.get().getIsDeleted()) {
      throw new RelationNotFoundException();
    }

    return foundRelation.get();
  }

  public RelationComponent findRelationComponentById (UUID relationComponentId) throws RelationComponentNotFoundException {
    Optional<RelationComponent> foundRelationComponent = relationComponentRepository.findById(relationComponentId);

    if (foundRelationComponent.isEmpty()) {
      throw new RelationComponentNotFoundException();
    }

    if (foundRelationComponent.get().getIsDeleted()) {
      throw new RelationComponentNotFoundException();
    }

    return foundRelationComponent.get();
  }

  public List<Relation> findAllByAssetIds (List<UUID> assetIds) {
    return relationRepository.findAllByAssetIds(assetIds);
  }

  public Set<UUID> findAllRelationIdByAssetIds (List<UUID> assetIds) {
    return relationRepository.findAllRelationIdByAssetIds(assetIds);
  }

  public List<RelationComponent> findAllRelationComponentsByRelationTypeId (UUID relationTypeId) {
    return relationComponentRepository.findAllRelationComponentsByRelationTypeId(relationTypeId);
  }

  public List<UUID> findAllSourceAssetIdsByAssetIds(List<UUID> assetIds) {
    return relationComponentRepository.findAllSourceAssetIdsByAssetIds(assetIds);
  }

  public List<RelationWithRelationComponentAndAsset> findAllByRelationComponentsIds (List<UUID> relationIds) {
    return relationComponentRepository.findAllByRelationIds(relationIds);
  }

  public RelationComponent saveRelationComponent (RelationComponent relationComponent) {
    return relationComponentRepository.save(relationComponent);
  }

  public Boolean isRelationsExistsByAssetId (UUID assetTypeId, UUID relationTypeComponentId) {
    return relationComponentRepository.isRelationsExistsByAssetId(assetTypeId, relationTypeComponentId);
  }

  public void deleteRelationsByIds (List<UUID> relationIds, User user) throws RelationNotFoundException {
    Integer relationsCount = relationRepository.countIdsByRelationIds(relationIds);

    if (relationsCount != relationIds.size()) {
      throw new RelationNotFoundException();
    }

    assetHierarchyDAO.deleteAllByRelationId(relationIds, user);
    responsibilitiesDAO.deleteAllByRelationIds(relationIds, user);
    relationAttributesDAO.deleteAllByRelationIds(relationIds, user);
    relationComponentRepository.deleteAllByRelationIds(relationIds, user.getUserId());
    relationComponentAttributesDAO.deleteAllByRelationIds(relationIds, user);

    relationRepository.deleteAllByRelationIds(relationIds, user.getUserId());
  }

  public List<RelationComponent> findAllResponsibilityInheritanceRoleHierarchyByAssetId (UUID assetId) {
    return relationComponentRepository.findAllResponsibilityInheritanceRoleHierarchyByAssetId(assetId);
  }

  public RelationConnectedValues findAllRelationAssetIds (UUID relationId) {
    return new RelationConnectedValues(relationRepository.findAllRelationAssetIds(relationId));
  }

  public Integer countExistingRelationComponents (
    UUID relationType,
    UUID assetId,
    UUID componentId,
    List<UUID> componentAssetIds,
    List<UUID> componentIds
  ) {
    return relationComponentRepository.countExistingRelationComponents(
      relationType,
      assetId,
      componentId,
      componentAssetIds,
      componentIds
    );
  }

  public List<RelationComponentAssetCount> countRelationTypeComponentAsset (UUID relationTypeId) {
    return relationComponentRepository.countRelationComponentAssetByRelationType(relationTypeId);
  }

  public Integer countRelationTypeComponentAssets (UUID relationTypeComponentId) {
    return relationComponentRepository.countRelationTypeComponentAssets(relationTypeComponentId);
  }

  public List<RelationTypeComponentAsset> countRelationTypeComponentAssetsByComponentAndAssetIds (List<UUID> assetIds, List<UUID> relationTypeComponentIds) {
    return relationComponentRepository.countRelationTypeComponentAssetsByComponentAndAssetIds(assetIds, relationTypeComponentIds);
  }
}
