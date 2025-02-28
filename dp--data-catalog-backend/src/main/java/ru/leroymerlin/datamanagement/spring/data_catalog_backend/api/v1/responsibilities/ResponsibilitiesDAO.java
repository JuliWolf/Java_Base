package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Relation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Responsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.SourceAssetRelationComponent;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class ResponsibilitiesDAO {
  protected final ResponsibilityRepository responsibilityRepository;

  protected final RelationComponentRepository relationComponentRepository;

  private final EntityManager entityManager;

  @Autowired
  public ResponsibilitiesDAO (
    ResponsibilityRepository responsibilityRepository,
    RelationComponentRepository relationComponentRepository,
    EntityManager entityManager
  ) {
    this.responsibilityRepository = responsibilityRepository;
    this.relationComponentRepository = relationComponentRepository;
    this.entityManager = entityManager;
  }

  public Responsibility createChildResponsibility (Responsibility responsibility, Asset asset, Relation relation, User user) {
    Responsibility createdResponsibility = new Responsibility(
      responsibility.getUser(),
      responsibility.getGroup(),
      asset,
      responsibility.getRole(),
      responsibility.getResponsibleType(),
      user
    );

    createdResponsibility.setRelation(relation);
    createdResponsibility.setInheritedFlag(true);
    createdResponsibility.setParentResponsibility(responsibility);

    return createdResponsibility;
  }

  public Responsibility findResponsibilityById (UUID responsibilityId, boolean isJoinFetch) throws ResponsibilityNotFoundException {
    Optional<Responsibility> foundResponsibility;

    if (isJoinFetch) {
      foundResponsibility = responsibilityRepository.findByIdWithJoinedTables(responsibilityId);
    } else {
      foundResponsibility = responsibilityRepository.findById(responsibilityId);
    }

    if (foundResponsibility.isEmpty()) {
      throw new ResponsibilityNotFoundException();
    }

    if (foundResponsibility.get().getIsDeleted()) {
      throw new ResponsibilityNotFoundException();
    }

    return foundResponsibility.get();
  }

  public List<Responsibility> findAllByParams (UUID assetId, UUID roleId, UUID userId, UUID groupId, UUID parentResponsibilityId, Boolean inheritedFlag) {
    return responsibilityRepository.findAllByParams(assetId, roleId, userId, groupId, parentResponsibilityId, inheritedFlag);
  }

  public List<Responsibility> findAllByAssetIds (Set<UUID> assetIds) {
    return responsibilityRepository.findAllByAssetIds(assetIds);
  }

  public List<Responsibility> findAllByResponsibilitiesIds (List<UUID> responsibilitiesIds) {
    return responsibilityRepository.findAllByResponsibilitiesIds(responsibilitiesIds);
  }

  public void deleteAllByParams (UUID assetId, UUID roleId, UUID userId, UUID groupId, UUID parentResponsibilityId, Boolean inheritedFlag, User user) {
    responsibilityRepository.deleteAllByParams(assetId, roleId, userId, groupId, parentResponsibilityId, inheritedFlag, user.getUserId());
  }

  public void deleteAllByParentResponsibilityId (List<UUID> parentResponsibilityIds, User user) {
    responsibilityRepository.deleteAllByParentResponsibilityId(parentResponsibilityIds, user.getUserId());
  }

  public void deleteAllByAssetIds (List<UUID> assetIds, User user) {
    responsibilityRepository.deleteAllByAssetIds(assetIds, user.getUserId());
  }

  public Responsibility saveResponsibility (Responsibility responsibility) {
    return responsibilityRepository.save(responsibility);
  }

  public List<Responsibility> saveAllResponsibility (List<Responsibility> responsibilities) {
    return responsibilityRepository.saveAll(responsibilities);
  }

  public void deleteAllByRelationIds (List<UUID> relationIds, User user) {
    responsibilityRepository.deleteAllByRelationIds(relationIds, user.getUserId());
  }

  public Optional<Responsibility> findResponsibilityByParams (UUID userId, UUID groupId, UUID assetId, UUID roleId, ResponsibleType responsibleType, UUID relationId) {
    return responsibilityRepository.findResponsibilityByParams(userId, groupId, assetId, roleId, responsibleType, relationId);
  }

  public void createChildResponsibilitiesForAsset (Map<UUID, List<Responsibility>> parentResponsibilitiesMap, User user) {
    Set<UUID> sourceAssetIds = parentResponsibilitiesMap.keySet();

    LoggerWrapper.info("Starting creating child responsibilities for asset ids " + sourceAssetIds, ResponsibilitiesDAO.class.getName());

    Map<UUID, List<UUID>> sourceAssetParentResponsibilitiesIds = mapParentResponsibilities(parentResponsibilitiesMap);

    int pageSize = 8000;
    double maxOperationCount = countMaxChildResponsibilityIterations(sourceAssetIds, pageSize);
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    AtomicInteger iterator = new AtomicInteger(0);
    for (int i = 0; i < maxOperationCount; i++) {
      int pageNumber = iterator.getAndIncrement();

      List<SourceAssetRelationComponent> connectedComponentsHierarchy = relationComponentRepository.findAllRelationComponentHierarchyByAssetIdsPageable(
        sourceAssetIds,
        PageRequest.of(pageNumber, pageSize)
      );

      connectedComponentsHierarchy.forEach(component -> {
        Relation relation = entityManager.getReference(Relation.class, component.getRelationId());
        Asset consumerAsset = entityManager.getReference(Asset.class, component.getConsumerAssetId());

        List<UUID> parents = computeParentResponsibility(timestamp, sourceAssetParentResponsibilitiesIds, component);

        createChildResponsibilitiesFromParents(parents, consumerAsset, relation, user, timestamp);
      });
    }
  }

  private Map<UUID, List<UUID>> mapParentResponsibilities (
    Map<UUID, List<Responsibility>> parentResponsibilitiesMap
  ) {
    Map<UUID, List<UUID>> sourceAssetParentResponsibilitiesIds = new HashMap<>();

    parentResponsibilitiesMap.forEach((key, values) -> {
      List<UUID> responsbilitiesList = values.stream().map(Responsibility::getResponsibilityId).toList();

      sourceAssetParentResponsibilitiesIds.put(key, responsbilitiesList);
    });

    return sourceAssetParentResponsibilitiesIds;
  }

  private void createChildResponsibilitiesFromParents (List<UUID> parentsIds, Asset asset, Relation relation, User user, Timestamp createdOn) {
    parentsIds
      .forEach(parentRespId ->
        responsibilityRepository.createResponsibilityFromParentResponsibility(
          UUID.randomUUID(),
          asset.getAssetId(),
          relation.getRelationId(),
          user.getUserId(),
          createdOn,
          parentRespId
        )
      );
  }

  private Double countMaxChildResponsibilityIterations (Set<UUID> assetsSet, int pageSize) {
    Long relationComponentHierarchyCount = relationComponentRepository.countRelationComponentHierarchyByAssetIds(assetsSet);

    return Math.ceil((double) relationComponentHierarchyCount / pageSize);
  }

  private List<UUID> computeParentResponsibility (
    Timestamp timestamp,
    Map<UUID, List<UUID>> parentResponsibilitiesMap,
    SourceAssetRelationComponent component
  ) {
    if (parentResponsibilitiesMap.containsKey(component.getSourceAssetId())) {
      return parentResponsibilitiesMap.get(component.getSourceAssetId());
    }

    return responsibilityRepository.findParentResponsibilitiesByParams(
      component.getSourceAssetId(),
      timestamp
    );
  }
}
