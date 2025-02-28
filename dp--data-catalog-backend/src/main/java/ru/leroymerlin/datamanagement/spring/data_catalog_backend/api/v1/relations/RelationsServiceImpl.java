package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.transaction.Transactional;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHierarchy.models.ParentChildAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationTypeComponentAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.CollectionUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.ObjectUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetHierarchy.AssetHierarchyDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.RelationAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.RelationComponentAttributesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypeComponents.RelationTypeComponentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.RelationTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.HierarchyRoleAssets;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.RelationTypeWithSortedRelationTypeComponentList;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.ResponsibilityInheritanceRoleAssets;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesDAO;

/**
 * @author juliwolf
 */

@Service
public class RelationsServiceImpl extends RelationsDAO implements RelationsService {
  private final Integer PAGE_SIZE = 50;

  private final AssetsDAO assetsDAO;

  private final RelationTypesDAO relationTypesDAO;

  private final RelationTypeComponentsDAO relationTypeComponentsDAO;

  private final RelationTypeComponentAssetTypesAssignmentsDAO relationTypeComponentAssetTypesAssignmentsDAO;

  public RelationsServiceImpl (
    AssetsDAO assetsDAO,
    AssetTypesDAO assetTypesDAO,
    RelationTypesDAO relationTypesDAO,
    AssetHierarchyDAO assetHierarchyDAO,
    RelationRepository relationRepository,
    ResponsibilitiesDAO responsibilitiesDAO,
    RelationAttributesDAO relationAttributesDAO,
    RelationTypeComponentsDAO relationTypeComponentsDAO,
    RelationComponentRepository relationComponentRepository,
    RelationComponentAttributesDAO relationComponentAttributesDAO,
    RelationTypeComponentAssetTypesAssignmentsDAO relationTypeComponentAssetTypesAssignmentsDAO
  ) {
    super(relationComponentRepository, relationRepository, assetTypesDAO, responsibilitiesDAO, assetHierarchyDAO, relationAttributesDAO, relationComponentAttributesDAO);

    this.assetsDAO = assetsDAO;
    this.relationTypesDAO = relationTypesDAO;
    this.relationTypeComponentsDAO = relationTypeComponentsDAO;
    this.relationTypeComponentAssetTypesAssignmentsDAO = relationTypeComponentAssetTypesAssignmentsDAO;
  }

  @Override
  @Transactional
  public PostRelationsResponse createRelations (
    PostRelationsRequest relationsRequest,
    User user
  ) throws
    AssetNotFoundException,
    IllegalArgumentException,
    RelationAlreadyExistsException,
    InvalidAssetTypeForComponentException,
    RelationTypeNotFoundException,
    InvalidNumberOfComponentsException,
    InvalidComponentForRelationTypeException,
    InvalidHierarchyBetweenAssetsException,
    RelationTypeComponentNotFoundException,
    RelationTypeDoesNotAllowedRelatedAssetException
  {
    LoggerWrapper.info("Starting creating relation", RelationsServiceImpl.class.getName());

    RelationType relationType = relationTypesDAO.findRelationTypeById(UUID.fromString(relationsRequest.getRelation_type_id()), false);

    if (relationType.getComponentNumber() != relationsRequest.getComponent().size()) {
      throw new InvalidNumberOfComponentsException();
    }

    if (!relationType.getSelfRelatedFlag()) {
      validateRequestAssetIds(relationsRequest);
    }

    validateComponents(relationType, relationsRequest);
    isRelationWithSuchComponentsAlreadyExists(relationType, relationsRequest);

    Map<RelationTypeComponentAsset, PostRelationsRequest> relationComponentAssetSet = relationsRequest.getComponent().stream()
      .collect(Collectors.toMap(
        request -> new RelationTypeComponentAsset(UUID.fromString(request.getComponent_id()),  UUID.fromString(request.getAsset_id())),
        value -> relationsRequest
      ));

    checkIfRelationTypeComponentAssetAssignmentExists(relationComponentAssetSet);

    Relation relation = relationRepository.save(new Relation(relationType, user));

    Map<UUID, Asset> assets = findAssets(relationComponentAssetSet.keySet());
    Map<UUID, RelationTypeComponent> relationTypeComponents = findRelationTypeComponents(relationComponentAssetSet.keySet());

    validateRelationTypeComponentBySingleAssetFlag(relationComponentAssetSet, relationTypeComponents);
    List<PostRelationResponse> relationResponses = createRelationComponentsAndResponsibilities(relationsRequest, relation, assets, relationTypeComponents, user);

    return new PostRelationsResponse(
      relation.getRelationId(),
      relationType.getRelationTypeId(),
      relationResponses
    );
  }

  @Override
  @Transactional
  public List<PostRelationsResponse> createRelationsBulk (
    List<PostRelationsRequest> relationsRequest,
    User user
  ) throws
    AssetNotFoundException,
    IllegalArgumentException,
    RelationAlreadyExistsException,
    InvalidAssetTypeForComponentException,
    RelationTypeNotFoundException,
    InvalidNumberOfComponentsException,
    InvalidComponentForRelationTypeException,
    DuplicateValueInRequestException,
    InvalidHierarchyBetweenAssetsException,
    RelationTypeComponentNotFoundException,
    RelationTypeDoesNotAllowedRelatedAssetException
  {
    LoggerWrapper.info("Starting creating relations bulk", RelationsServiceImpl.class.getName());

    Set<UUID> relationTypeIds = new HashSet<>();
    Map<RelationTypeComponentAsset, PostRelationsRequest> relationTypeComponentAssetWithRequestMap = new HashMap<>();
    Map<RelationTypeWithSortedRelationTypeComponentList, PostRelationsRequest> relationTypeComponentAssetMap = new HashMap<>();

    parseBulkRequestValues(relationsRequest, relationTypeIds, relationTypeComponentAssetWithRequestMap, relationTypeComponentAssetMap);

    Map<UUID, RelationType> relaionTypeMap = findRelationTypes(relationTypeIds);

    isRelationsWithSuchComponentsAlreadyExists(relationsRequest, relaionTypeMap, relationTypeComponentAssetMap);
    checkIfRelationTypeComponentAssetAssignmentExists(relationTypeComponentAssetWithRequestMap);

    relationsRequest.forEach(request -> {
      RelationType relationType = relaionTypeMap.get(UUID.fromString(request.getRelation_type_id()));

      if (relationType.getComponentNumber() != request.getComponent().size()) {
        throw new InvalidNumberOfComponentsException(ObjectUtils.convertObjectToMap(request));
      }

      validateComponents(relationType, request);
    });

    Map<UUID, Asset> assets = findAssets(relationTypeComponentAssetWithRequestMap.keySet());
    Map<UUID, RelationTypeComponent> relationTypeComponents = findRelationTypeComponents(relationTypeComponentAssetWithRequestMap.keySet());

    validateRelationTypeComponentBySingleAssetFlag(relationTypeComponentAssetWithRequestMap, relationTypeComponents);

    return createBulkRelationComponentsAndResponsibilities(relationsRequest, relaionTypeMap, assets, relationTypeComponents, user);
  }

  @Override
  public GetRelationResponse getRelationById (UUID relationId) throws RelationNotFoundException {
    LoggerWrapper.info("Starting getting relation by id " + relationId, RelationsServiceImpl.class.getName());

    List<RelationWithRelationComponent> relationWithRelationComponent = relationRepository.findByIdWithConnectedValues(relationId);

    if (relationWithRelationComponent.isEmpty()) {
      throw new RelationNotFoundException();
    }

    return mapRelation(
      relationWithRelationComponent.get(0).getRelationWithRelationType(),
      relationWithRelationComponent.stream()
        .map(RelationWithRelationComponent::getRelationComponentWithConnectedValues).toList()
    );
  }

  @Override
  public GetRelationsResponse getRelationsByParams (
    UUID assetId,
    UUID relationTypeId,
    UUID relationTypeComponentId,
    Boolean hierarchyFlag,
    Boolean responsibilityInheritanceFlag,
    Integer pageNumber,
    Integer pageSize
  ) {
    LoggerWrapper.info("Starting getting relations by params", RelationsServiceImpl.class.getName());

    pageSize = PageableUtils.getPageSize(pageSize, PAGE_SIZE);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<RelationWithRelationType> responses = relationRepository.findAllByParamsPageable(
      assetId,
      relationTypeId,
      relationTypeComponentId,
      hierarchyFlag,
      responsibilityInheritanceFlag,
      PageRequest.of(pageNumber, pageSize, Sort.by("rt.relationTypeName").ascending())
    );

    List<UUID> relationIds = responses.stream().map(RelationWithRelationType::getRelationId).toList();
    List<RelationComponentWithConnectedValues> relationComponents = relationComponentRepository.findAllByRelationIdsWithConnectedValues(relationIds);
    Map<UUID, List<RelationComponentWithConnectedValues>> relationComponentMap = relationComponents.stream().collect(Collectors.groupingBy(RelationComponentWithConnectedValues::getRelationId));

    List<GetRelationResponse> relationResponses = responses.stream()
      .map(relation -> mapRelation(relation, relationComponentMap.get(relation.getRelationId()))).toList();

    return new GetRelationsResponse(
      responses.getTotalElements(),
      pageSize,
      pageNumber,
      relationResponses
    );
  }

  @Override
  @Transactional
  public void deleteRelation (UUID relationId, User user) throws RelationNotFoundException {
    LoggerWrapper.info("Starting deleting relation by id " + relationId, RelationsServiceImpl.class.getName());

    deleteRelationsByIds(List.of(relationId), user);
  }

  @Override
  @Transactional
  public void deleteRelationsBulk (List<UUID> relationIds, User user) throws RelationNotFoundException {
    LoggerWrapper.info("Starting deleting relations bulk", RelationsServiceImpl.class.getName());

    deleteRelationsByIds(relationIds, user);
  }

  @Override
  public GetRelationsAttributesResponse getRelationAttributes (UUID relationId) throws RelationNotFoundException {
    LoggerWrapper.info("Starting getting relation attributes for relation " + relationId, RelationsServiceImpl.class.getName());

    List<RelationWithAttributes> relationWithAttributes = relationRepository.findAllRelationAttributesByRelationId(relationId);

    if (relationWithAttributes.isEmpty()) {
      throw new RelationNotFoundException();
    }

    RelationWithAttributes relation = relationWithAttributes.get(0);

    List<RelationComponentWithRelationComponentAttributes> relationComponentsWithAttributes = relationComponentRepository.findAllRelationComponentAttributeByRelationIds(relation.getRelationId());

    Map<UUID, List<RelationComponentWithRelationComponentAttributes>> relationComponentsByRelationComponentIdMap = relationComponentsWithAttributes
      .stream()
      .collect(
        Collectors
          .groupingBy(RelationComponentWithRelationComponentAttributes::getRelationComponentId)
        );

    List<GetRelationAttributeResponse> relationAttributes = mapRelationAttributes(relationWithAttributes);
    List<GetRelationComponentWithAttributesResponse> relationComponents = relationComponentsByRelationComponentIdMap
      .values()
      .stream()
      .map(entry -> {
        RelationComponentWithRelationComponentAttributes firstRelationComponent = entry.get(0);
        List<GetRelationComponentAttributesResponse> relationComponentAttributes = mapRelationComponentWithAttributes(entry);

        return new GetRelationComponentWithAttributesResponse(
          firstRelationComponent.getRelationComponentId(),
          firstRelationComponent.getAssetId(),
          firstRelationComponent.getAssetDisplayName(),
          relationComponentAttributes,
          firstRelationComponent.getHierarchyRole(),
          firstRelationComponent.getResponsibilityInheritanceRole(),
          firstRelationComponent.getRelationTypeComponentId(),
          firstRelationComponent.getRelationTypeComponentName(),
          firstRelationComponent.getCreatedOn(),
          firstRelationComponent.getCreatedBy()
        );
      })
      .sorted(Comparator.comparing(GetRelationComponentWithAttributesResponse::getRelation_type_component_name))
      .toList();

    return new GetRelationsAttributesResponse(
      relation.getRelationId(),
      relation.getRelationTypeId(),
      relation.getRelationTypeName(),
      relation.getResponsibilityInheritanceFlag(),
      relation.getHierarchyFlag(),
      relationAttributes,
      relation.getCreatedOn(),
      relation.getCreatedBy(),
      relationComponents
    );
  }

  private void validateComponents (RelationType relationType, PostRelationsRequest relationsRequest) throws
    IllegalArgumentException,
    RelationAlreadyExistsException,
    InvalidNumberOfComponentsException,
    RelationTypeDoesNotAllowedRelatedAssetException
  {
    List<PostRelationRequest> requestList = relationsRequest.getComponent();

    Map<UUID, Long> componentsCollection = requestList.stream()
      .collect(Collectors.groupingBy(c -> UUID.fromString(c.getComponent_id()), Collectors.counting()));

    for (Map.Entry<UUID, Long> entry: componentsCollection.entrySet()) {
      if (entry.getValue() > 1) {
        throw new InvalidNumberOfComponentsException(ObjectUtils.convertObjectToMap(relationsRequest));
      }
    }

    if (relationType.getSelfRelatedFlag()) return;

    Map<UUID, Long> assetsCollection = requestList.stream()
      .collect(Collectors.groupingBy(c -> UUID.fromString(c.getAsset_id()), Collectors.counting()));

    for (Map.Entry<UUID, Long> entry: assetsCollection.entrySet()) {
      if (entry.getValue() > 1) {
        throw new RelationTypeDoesNotAllowedRelatedAssetException(ObjectUtils.convertObjectToMap(relationsRequest));
      }
    }
  }

  private void validateRequestAssetIds (PostRelationsRequest relationsRequest) throws RelationTypeDoesNotAllowedRelatedAssetException {
    Set<String> assetIdSet = relationsRequest.getComponent().stream().map(PostRelationRequest::getAsset_id).collect(Collectors.toSet());

    if (assetIdSet.size() == relationsRequest.getComponent().size()) return;

    throw new RelationTypeDoesNotAllowedRelatedAssetException();
  }

  private List<PostRelationResponse> createRelationComponentsAndResponsibilities (
    PostRelationsRequest relationsRequest,
    Relation relation,
    Map<UUID, Asset> assets,
    Map<UUID, RelationTypeComponent> relationTypeComponents,
    User user
  ) throws
    InvalidComponentForRelationTypeException,
    InvalidHierarchyBetweenAssetsException
  {
    Map<UUID, List<ResponsibilityInheritanceRoleAssets>> sourceAssetResponsibilityInheritanceRole = new HashMap<>();
    Map<Relation, AbstractMap.SimpleEntry<HierarchyRoleAssets, PostRelationsRequest>> hierarchyRoleAssetHashMap = new HashMap<>();
    Map<Relation, AbstractMap.SimpleEntry<ResponsibilityInheritanceRoleAssets, PostRelationsRequest>> responsibilityInheritanceRoleAssetMap = new HashMap<>();

    List<PostRelationResponse> relationResponses = createRelationComponents(
      relationsRequest,
      relation,
      assets,
      relationTypeComponents,
      hierarchyRoleAssetHashMap,
      responsibilityInheritanceRoleAssetMap,
      user
    );

    if (responsibilityInheritanceRoleAssetMap.get(relation) != null) {
      computeSourceAsset(relation, responsibilityInheritanceRoleAssetMap.get(relation).getKey(), sourceAssetResponsibilityInheritanceRole);
    }

    if (hierarchyRoleAssetHashMap.get(relation) != null) {
      createHierarchyResponsibilities(relation, hierarchyRoleAssetHashMap.get(relation), user);
    }

    createInheritanceResponsibilities(sourceAssetResponsibilityInheritanceRole, user);

    return relationResponses;
  }

  private List<PostRelationsResponse> createBulkRelationComponentsAndResponsibilities (
    List<PostRelationsRequest> relationsRequest,
    Map<UUID, RelationType> relaionTypeMap,
    Map<UUID, Asset> assets,
    Map<UUID, RelationTypeComponent> relationTypeComponents,
    User user
  ) throws
    InvalidComponentForRelationTypeException,
    InvalidHierarchyBetweenAssetsException
  {
    Map<UUID, List<ResponsibilityInheritanceRoleAssets>> sourceAssetResponsibilityInheritanceRole = new HashMap<>();
    Map<Relation, AbstractMap.SimpleEntry<HierarchyRoleAssets, PostRelationsRequest>> hierarchyRoleAssetHashMap = new HashMap<>();
    Map<Relation, AbstractMap.SimpleEntry<ResponsibilityInheritanceRoleAssets, PostRelationsRequest>> responsibilityInheritanceRoleAssetMap = new HashMap<>();

    List<PostRelationsResponse> responses = relationsRequest.stream().map(request -> {
      RelationType relationType = relaionTypeMap.get(UUID.fromString(request.getRelation_type_id()));

      Relation relation = relationRepository.save(new Relation(relationType, user));

      List<PostRelationResponse> relationResponses = createRelationComponents(
        request,
        relation,
        assets,
        relationTypeComponents,
        hierarchyRoleAssetHashMap,
        responsibilityInheritanceRoleAssetMap,
        user
      );

      if (responsibilityInheritanceRoleAssetMap.get(relation) != null) {
        computeSourceAsset(relation, responsibilityInheritanceRoleAssetMap.get(relation).getKey(), sourceAssetResponsibilityInheritanceRole);
      }

      return new PostRelationsResponse(
        relation.getRelationId(),
        relationType.getRelationTypeId(),
        relationResponses
      );
    }).toList();

    createInheritanceResponsibilities(sourceAssetResponsibilityInheritanceRole, user);
    createHierarchyResponsibilitiesFromBulk(hierarchyRoleAssetHashMap, user);

    return responses;
  }

  private List<PostRelationResponse> createRelationComponents (
    PostRelationsRequest relationsRequest,
    Relation relation,
    Map<UUID, Asset> assets,
    Map<UUID, RelationTypeComponent> relationTypeComponents,
    Map<Relation, AbstractMap.SimpleEntry<HierarchyRoleAssets, PostRelationsRequest>> hierarchyRoleAssetHashMap,
    Map<Relation, AbstractMap.SimpleEntry<ResponsibilityInheritanceRoleAssets, PostRelationsRequest>> responsibilityInheritanceRoleAssetMap,
    User user
  ) throws
    InvalidComponentForRelationTypeException,
    InvalidHierarchyBetweenAssetsException
  {
    RelationType relationType = relation.getRelationType();

    return relationsRequest.getComponent()
      .stream()
      .map(component -> {
        Asset asset = assets.get(UUID.fromString(component.getAsset_id()));
        RelationTypeComponent relationTypeComponent = relationTypeComponents.get(UUID.fromString(component.getComponent_id()));

        if (!relationTypeComponent.getRelationType().getRelationTypeId().equals(relationType.getRelationTypeId())) {
          PostRelationsRequest request = new PostRelationsRequest(relationType.getRelationTypeId().toString(), List.of(component));
          throw new InvalidComponentForRelationTypeException(ObjectUtils.convertObjectToMap(request));
        }

        if (relationType.getResponsibilityInheritanceFlag() != null && relationType.getResponsibilityInheritanceFlag()) {
          if (relationTypeComponent.getResponsibilityInheritanceRole().equals(ResponsibilityInheritanceRole.SOURCE)) {
            responsibilityInheritanceRoleAssetMap.compute(relation,
              (key, value) -> {
                value = value != null ? value : new AbstractMap.SimpleEntry<>(new ResponsibilityInheritanceRoleAssets(), relationsRequest);

                value.getKey().setSourceAsset(asset);
                value.getKey().setRelation(relation);

                return value;
              }
            );
          } else if (relationTypeComponent.getResponsibilityInheritanceRole().equals(ResponsibilityInheritanceRole.CONSUMER)) {
            responsibilityInheritanceRoleAssetMap.compute(relation,
              (key, value) -> {
                value = value != null ? value : new AbstractMap.SimpleEntry<>(new ResponsibilityInheritanceRoleAssets(), relationsRequest);

                value.getKey().setConsumerAsset(asset);
                value.getKey().setRelation(relation);

                return value;
              }
            );
          }
        }

        if (relationType.getHierarchyFlag() != null && relationType.getHierarchyFlag()) {
          if (relationTypeComponent.getHierarchyRole().equals(HierarchyRole.PARENT)) {
            hierarchyRoleAssetHashMap.compute(relation,
              (key, value) -> {
                value = value != null ? value : new AbstractMap.SimpleEntry<>(new HierarchyRoleAssets(), relationsRequest);

                value.getKey().setParentAsset(asset);

                return value;
              }
            );
          } else if (relationTypeComponent.getHierarchyRole().equals(HierarchyRole.CHILD)) {
            hierarchyRoleAssetHashMap.compute(relation,
              (key, value) -> {
                value = value != null ? value : new AbstractMap.SimpleEntry<>(new HierarchyRoleAssets(), relationsRequest);

                value.getKey().setChildAsset(asset);

                return value;
              }
            );
          }
        }

        RelationComponent relationComponent = relationComponentRepository.save(new RelationComponent(
          relation,
          relationTypeComponent,
          asset,
          relationTypeComponent.getHierarchyRole(),
          relationTypeComponent.getResponsibilityInheritanceRole(),
          user
        ));

        return new PostRelationResponse(
          relationComponent.getRelationComponentId(),
          asset.getAssetId(),
          relationTypeComponent.getRelationTypeComponentId(),
          relationTypeComponent.getHierarchyRole(),
          relationTypeComponent.getResponsibilityInheritanceRole(),
          new Timestamp(System.currentTimeMillis()),
          user.getUserId()
        );
      }).toList();
  }

  private void checkIfRelationTypeComponentAssetAssignmentExists (Map<RelationTypeComponentAsset, PostRelationsRequest> relationComponentAssetMap) {
    Set<UUID> assetsList = new HashSet<>();
    Set<UUID> relationTypeComponentList = new HashSet<>();

    relationComponentAssetMap
      .keySet()
      .forEach(key -> {
        assetsList.add(key.getAssetId());
        relationTypeComponentList.add(key.getRelationTypeComponentId());
      });

    Set<RelationTypeComponentAsset> assignments = relationTypeComponentAssetTypesAssignmentsDAO.findAllByRelationTypeComponentAndAsset(assetsList.stream().toList(), relationTypeComponentList.stream().toList());

    relationComponentAssetMap
      .forEach((key, value) -> {
        if (!assignments.contains(key)) {
          throw new InvalidAssetTypeForComponentException(ObjectUtils.convertObjectToMap(value));
        }
      });
  }

  private void computeSourceAsset (
    Relation relation,
    ResponsibilityInheritanceRoleAssets responsibilityInheritanceRoleAssets,
    Map<UUID, List<ResponsibilityInheritanceRoleAssets>> sourceAssetResponsibilityInheritanceRole
  ) {
    RelationType relationType = relation.getRelationType();

    if (relationType.getResponsibilityInheritanceFlag() == null || !relationType.getResponsibilityInheritanceFlag()) return;

    sourceAssetResponsibilityInheritanceRole.compute(responsibilityInheritanceRoleAssets.getSourceAsset().getAssetId(), (key, value) -> {
      if (value == null) {
        value = new ArrayList<>();
        value.add(responsibilityInheritanceRoleAssets);
      } else {
        value.add(responsibilityInheritanceRoleAssets);
      }

      return value;
    });
  }

  private void createInheritanceResponsibilities(
    Map<UUID, List<ResponsibilityInheritanceRoleAssets>> sourceAssetResponsibilityInheritanceRole,
    User user
  ) {
    Map<ResponsibilityInheritanceRoleAssets, List<Responsibility>> relationSourceAssetResponsibilities = loadSourceAssetResponsibilities(sourceAssetResponsibilityInheritanceRole);

    List<Responsibility> createdConsumerAssetResponsibilities = relationSourceAssetResponsibilities.entrySet().stream()
      .flatMap(entry -> entry.getValue().stream().map(resp -> responsibilitiesDAO.createChildResponsibility(resp, entry.getKey().getConsumerAsset(), entry.getKey().getRelation(), user)))
      .toList();

    responsibilitiesDAO.saveAllResponsibility(createdConsumerAssetResponsibilities);

    Map<UUID, List<Responsibility>> sourceAssetResponsibilities = createdConsumerAssetResponsibilities.stream().collect(Collectors.groupingBy(resp -> resp.getAsset().getAssetId()));

    responsibilitiesDAO.createChildResponsibilitiesForAsset(sourceAssetResponsibilities, user);
  }

  private Map<ResponsibilityInheritanceRoleAssets, List<Responsibility>> loadSourceAssetResponsibilities (
    Map<UUID, List<ResponsibilityInheritanceRoleAssets>> sourceAssetResponsibilityInheritanceRole
  ) {
    List<Responsibility> sourceAssetResponsibilities = responsibilitiesDAO.findAllByAssetIds(sourceAssetResponsibilityInheritanceRole.keySet());

    Map<ResponsibilityInheritanceRoleAssets, List<Responsibility>> responsibilityInheritanceRoleAssetResponsibilities = new HashMap<>();

    sourceAssetResponsibilities.forEach(resp -> {
      List<ResponsibilityInheritanceRoleAssets> responsibilityInheritanceRoleAssets = sourceAssetResponsibilityInheritanceRole.get(resp.getAsset().getAssetId());

      responsibilityInheritanceRoleAssets.forEach(inheritanceRoleAssets -> {
        responsibilityInheritanceRoleAssetResponsibilities.compute(inheritanceRoleAssets, (key, value) -> {
          if (value == null) {
            value = new ArrayList<>();
            value.add(resp);
          } else {
            value.add(resp);
          }

          return value;
        });
      });
    });

    return responsibilityInheritanceRoleAssetResponsibilities;
  }

  private void createHierarchyResponsibilities (
    Relation relation,
    AbstractMap.SimpleEntry<HierarchyRoleAssets, PostRelationsRequest> hierarchyRoleAssetsEntry,
    User user
  ) throws InvalidHierarchyBetweenAssetsException {
    RelationType relationType = relation.getRelationType();

    if (relationType.getHierarchyFlag() == null || !relationType.getHierarchyFlag()) return;

    HierarchyRoleAssets hierarchyRoleAssets = hierarchyRoleAssetsEntry.getKey();

    Boolean isExists = assetHierarchyDAO.isParentChildAssetsConnectionExists(
      hierarchyRoleAssets.getParentAsset().getAssetId(),
      hierarchyRoleAssets.getChildAsset().getAssetId()
    );
    if (isExists) {
      throw new InvalidHierarchyBetweenAssetsException(ObjectUtils.convertObjectToMap(hierarchyRoleAssetsEntry.getValue()));
    }

    assetHierarchyDAO.saveAssetHierarchy(new AssetHierarchy(
      hierarchyRoleAssets.getParentAsset(),
      hierarchyRoleAssets.getChildAsset(),
      relation,
      user
    ));
  }

  private void createHierarchyResponsibilitiesFromBulk (
    Map<Relation, AbstractMap.SimpleEntry<HierarchyRoleAssets, PostRelationsRequest>> hierarchyRoleAssetHashMap,
    User user
  ) throws InvalidHierarchyBetweenAssetsException {
    Map<HierarchyRoleAssets, Relation> hierarchyRoleAssetsMap = new HashMap<>();
    List<UUID> childAssets = new ArrayList<>();
    List<UUID> parentAssets = new ArrayList<>();

    hierarchyRoleAssetHashMap.forEach((key, value) -> {
      RelationType relationType = key.getRelationType();

      if (relationType.getHierarchyFlag() == null || !relationType.getHierarchyFlag()) return;

      if (hierarchyRoleAssetsMap.containsKey(value.getKey())) {
        throw new InvalidHierarchyBetweenAssetsException(ObjectUtils.convertObjectToMap(value.getValue()));
      }

      hierarchyRoleAssetsMap.put(value.getKey(), key);

      childAssets.add(value.getKey().getChildAsset().getAssetId());
      parentAssets.add(value.getKey().getParentAsset().getAssetId());
    });

    checkChildParentAssets(childAssets, parentAssets, hierarchyRoleAssetsMap.keySet());

    hierarchyRoleAssetsMap.forEach((key, value) -> {
      assetHierarchyDAO.saveAssetHierarchy(new AssetHierarchy(
        key.getParentAsset(),
        key.getChildAsset(),
        value,
        user
      ));
    });
  }

  private void checkChildParentAssets (
    List<UUID> childAssets,
    List<UUID> parentAssets,
    Set<HierarchyRoleAssets> hierarchyRoleAssets
  ) throws InvalidHierarchyBetweenAssetsException {
    List<ParentChildAsset> parentChildAssets = assetHierarchyDAO.findAllByChildParentAssets(childAssets, parentAssets);
    if (parentChildAssets.isEmpty()) return;

    Optional<ParentChildAsset> parentChildAsset = hierarchyRoleAssets.stream()
      .map(item -> new ParentChildAsset(item.getParentAsset().getAssetId(), item.getChildAsset().getAssetId()))
      .filter(parentChildAssets::contains)
      .findFirst();

    if (parentChildAsset.isEmpty()) return;

    ParentChildAsset assets = parentChildAsset.get();

    PostRelationsRequest request = new PostRelationsRequest();
    PostRelationRequest childAssetRequest = new PostRelationRequest(assets.getChildAssetId().toString(), null);
    PostRelationRequest parentAssetRequest = new PostRelationRequest(assets.getParentAssetId().toString(), null);

    request.getComponent().add(childAssetRequest);
    request.getComponent().add(parentAssetRequest);

    throw new InvalidHierarchyBetweenAssetsException(ObjectUtils.convertObjectToMap(request));

  }

  private Map<UUID, RelationType> findRelationTypes (Set<UUID> relationTypesSet) {
    List<RelationType> relationTypes = relationTypesDAO.findAllByRelationTypeIds(relationTypesSet.stream().toList());

    Map<UUID, RelationType> relationTypeMap = relationTypes.stream()
      .collect(Collectors.toMap(RelationType::getRelationTypeId, value -> value));

    if (relationTypes.size() != relationTypesSet.size()) {
      UUID absentRelationTypeId = CollectionUtils.findFirstNotFoundValue(relationTypesSet, relationTypeMap.keySet());

      PostRelationsRequest request = new PostRelationsRequest();
      request.setRelation_type_id(absentRelationTypeId.toString());

      throw new RelationTypeNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return relationTypeMap;
  }

  private Map<UUID, RelationTypeComponent> findRelationTypeComponents (Set<RelationTypeComponentAsset> relationTypeComponentsSet) {
    Set<UUID> relationTypeComponentIds = parseRelationTypeComponentAssetMapToSet(relationTypeComponentsSet, true);

    List<RelationTypeComponent> relationTypeComponents = relationTypeComponentsDAO.findAllByRelationTypeComponentIds(relationTypeComponentIds.stream().toList());

    Map<UUID, RelationTypeComponent> relationTypeComponentMap = relationTypeComponents.stream()
      .collect(Collectors.toMap(RelationTypeComponent::getRelationTypeComponentId, value -> value));

    if (relationTypeComponents.size() != relationTypeComponentIds.size()) {
      UUID absentRelationTypeComponentId = CollectionUtils.findFirstNotFoundValue(relationTypeComponentIds, relationTypeComponentMap.keySet());

      PostRelationsRequest request = new PostRelationsRequest();
      List<PostRelationRequest> relationComponents = new ArrayList<>();
      relationComponents.add(new PostRelationRequest(null, absentRelationTypeComponentId.toString()));

      request.setComponent(relationComponents);

      throw new RelationTypeComponentNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return relationTypeComponentMap;
  }

  private Map<UUID, Asset> findAssets (Set<RelationTypeComponentAsset> assetsSet) {
    Set<UUID> assetIds = parseRelationTypeComponentAssetMapToSet(assetsSet, false);

    List<Asset> assets = assetsDAO.findAllByAssetIds(assetIds.stream().toList());

    Map<UUID, Asset> assetsMap = assets.stream()
      .collect(Collectors.toMap(Asset::getAssetId, value -> value));

    if (assets.size() != assetIds.size()) {
      UUID absentAssetId = CollectionUtils.findFirstNotFoundValue(assetIds, assetsMap.keySet());

      PostRelationsRequest request = new PostRelationsRequest();
      List<PostRelationRequest> relationComponents = new ArrayList<>();
      relationComponents.add(new PostRelationRequest(absentAssetId.toString(), null));

      request.setComponent(relationComponents);

      throw new AssetNotFoundException(ObjectUtils.convertObjectToMap(request));
    }

    return assetsMap;
  }

  private void parseBulkRequestValues (
    List<PostRelationsRequest> relationsRequest,
    Set<UUID> relationTypeIds,
    Map<RelationTypeComponentAsset, PostRelationsRequest> relationTypeComponentAssetWithRequestMap,
    Map<RelationTypeWithSortedRelationTypeComponentList, PostRelationsRequest> relationTypeComponentAssetMapCount
  ) throws
    IllegalArgumentException,
    DuplicateValueInRequestException,
    SomeRequiredFieldsAreEmptyException
  {
    relationsRequest.forEach(request -> {
      if (StringUtils.isEmpty(request.getRelation_type_id())) {
        throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
      }

      UUID relationTypeId = UUID.fromString(request.getRelation_type_id());

      relationTypeIds.add(relationTypeId);

      List<AbstractMap.SimpleEntry<UUID, UUID>> componentAssetList = request.getComponent().stream()
        .sorted(Comparator.comparing(PostRelationRequest::getComponent_id))
        .map(component -> {
          if (
            StringUtils.isEmpty(component.getComponent_id()) ||
            StringUtils.isEmpty(component.getAsset_id())
          ) {
            throw new SomeRequiredFieldsAreEmptyException(ObjectUtils.convertObjectToMap(request));
          }

          UUID componentId = UUID.fromString(component.getComponent_id());
          UUID assetId = UUID.fromString(component.getAsset_id());

          relationTypeComponentAssetWithRequestMap.put(
            new RelationTypeComponentAsset(componentId, assetId),
            request
          );

          return new AbstractMap.SimpleEntry<>(componentId, assetId);
        }).toList();

      RelationTypeWithSortedRelationTypeComponentList relationTypeComponentList = new RelationTypeWithSortedRelationTypeComponentList(relationTypeId, componentAssetList);

      if (relationTypeComponentAssetMapCount.containsKey(relationTypeComponentList)) {
        throw new DuplicateValueInRequestException("Duplicate in request.", ObjectUtils.convertObjectToMap(request));
      }

      relationTypeComponentAssetMapCount.put(relationTypeComponentList, request);
    });
  }

  private void isRelationWithSuchComponentsAlreadyExists (
    RelationType relationType,
    PostRelationsRequest relationsRequest
  ) throws RelationAlreadyExistsException {
    if (!relationType.getUniquenessFlag()) return;

    PostRelationRequest firsRelationComponent = relationsRequest.getComponent().get(0);
    List<PostRelationRequest> otherComponentsList = relationsRequest.getComponent().stream().skip(1).toList();

    Integer count = relationComponentRepository.countExistingRelationComponents(
      UUID.fromString(relationsRequest.getRelation_type_id()),
      UUID.fromString(firsRelationComponent.getAsset_id()),
      UUID.fromString(firsRelationComponent.getComponent_id()),
      otherComponentsList.stream().map(component -> UUID.fromString(component.getAsset_id())).toList(),
      otherComponentsList.stream().map(component -> UUID.fromString(component.getComponent_id())).toList()
    );

    if (count.equals(relationsRequest.getComponent().size())) {
      throw new RelationAlreadyExistsException();
    }
  }

  private void validateRelationTypeComponentBySingleAssetFlag (
    Map<RelationTypeComponentAsset, PostRelationsRequest> relationTypeComponentAssetWithRequestMap,
    Map<UUID, RelationTypeComponent> relationTypeComponents
  ) throws RelationTypeComponentWithSameAssetException {
    List<UUID> assetIds = new ArrayList<>();
    List<UUID> relationTypeComponentIds = new ArrayList<>();

    relationTypeComponentAssetWithRequestMap.keySet().stream()
      .filter(key -> {
        RelationTypeComponent relationTypeComponent = relationTypeComponents.get(key.getRelationTypeComponentId());

        return relationTypeComponent.getSingleRelationTypeComponentForAssetFlag();
      })
      .forEach(key -> {
        assetIds.add(key.getAssetId());
        relationTypeComponentIds.add(key.getRelationTypeComponentId());
      });

    if (assetIds.isEmpty()) return;

    List<RelationTypeComponentAsset> relationTypeAssetCounts = countRelationTypeComponentAssetsByComponentAndAssetIds(assetIds, relationTypeComponentIds);
    if (relationTypeAssetCounts.isEmpty()) return;

    RelationTypeComponentAsset firstRelationTypeAssetCount = relationTypeAssetCounts.get(0);
    Optional<Map.Entry<RelationTypeComponentAsset, PostRelationsRequest>> firstEntry = relationTypeComponentAssetWithRequestMap.entrySet().stream()
      .filter(entry -> entry.getKey().equals(firstRelationTypeAssetCount))
      .findFirst();

    if (firstEntry.isEmpty()) return;

    throw new RelationTypeComponentWithSameAssetException(ObjectUtils.convertObjectToMap(firstEntry.get().getValue()));
  }

  private void isRelationsWithSuchComponentsAlreadyExists (
    List<PostRelationsRequest> relationsRequest,
    Map<UUID, RelationType> relaionTypeMap,
    Map<RelationTypeWithSortedRelationTypeComponentList, PostRelationsRequest> relationTypeComponentAssetMap
  ) throws RelationAlreadyExistsException {
    Set<UUID> assetsSet = new HashSet<>();
    Set<UUID> componentssSet = new HashSet<>();
    Set<UUID> relationTypeSet = new HashSet<>();

    relationsRequest.forEach(request -> {
      RelationType relationType = relaionTypeMap.get(UUID.fromString(request.getRelation_type_id()));

      if (!relationType.getUniquenessFlag()) return;

      relationTypeSet.add(relationType.getRelationTypeId());

      request.getComponent()
        .forEach(item -> {
          assetsSet.add(UUID.fromString(item.getAsset_id()));
          componentssSet.add(UUID.fromString(item.getComponent_id()));
        });
    });

    // Get all rows with relation type ids, assetIds and relation tpe component Ids
    List<RelationWithRelationComponentAndAsset> relationsWithRelationComponent = relationComponentRepository.findAllByRelationTypesAsseIdsRelationTypeComponentIds(
      relationTypeSet.stream().toList(),
      assetsSet.stream().toList(),
      componentssSet.stream().toList()
    );

    // Group by relation Id and relation type Id through custom equals and hashCode
    Map<RelationWithRelationComponentAndAsset, List<RelationWithRelationComponentAndAsset>> reltionWithRelationComponentAssetMap = relationsWithRelationComponent
      .stream()
      .collect(Collectors.groupingBy(request -> request));

    // Group by relationId and relationTypeId and collect relation component vs asset id list
    Map<AbstractMap.SimpleEntry<UUID, UUID>, List<AbstractMap.SimpleEntry<UUID, UUID>>> relationWithComponentsMap = reltionWithRelationComponentAssetMap
      .entrySet()
      .stream()
      .collect(
        Collectors.groupingBy(
          entry -> new AbstractMap.SimpleEntry<>(entry.getKey().getRelationId(), entry.getKey().getRelationTypeId()),
          Collectors.mapping(
            entry -> entry
              .getValue()
              .stream()
              .map(value -> new AbstractMap.SimpleEntry<>(value.getRelationTypeComponentId(), value.getAssetId()))
              .toList(),
            Collectors.flatMapping(List::stream, Collectors.toList())
          )
        )
      );

    relationTypeComponentAssetMap
      .forEach((key, value) -> {
        // Filter data from db by relation type Id
        List<Map.Entry<AbstractMap.SimpleEntry<UUID, UUID>, List<AbstractMap.SimpleEntry<UUID, UUID>>>> valuesFromDB = relationWithComponentsMap
          .entrySet()
          .stream()
          .filter(item -> item.getKey().getValue().equals(key.getRelationTypeId()))
          .toList();

        valuesFromDB.forEach(map -> {
          // If db have exact same relation with relation components
          if (new HashSet<>(map.getValue()).containsAll(key.getRelationTypeComponents())) {
            throw new RelationAlreadyExistsException(ObjectUtils.convertObjectToMap(value));
          }
        });
      });
  }

  private Set<UUID> parseRelationTypeComponentAssetMapToSet (Set<RelationTypeComponentAsset> relationTypeComponentAssetMap, boolean setOfRelationTypeComponent) {
    return relationTypeComponentAssetMap
      .stream()
      .map(key -> {
        if (setOfRelationTypeComponent) {
          return key.getRelationTypeComponentId();
        }

        return key.getAssetId();
      })
      .collect(Collectors.toSet());
  }

  private GetRelationResponse mapRelation (RelationWithRelationType relation, List<RelationComponentWithConnectedValues> relationComponents) {
    List<GetRelationComponentResponse> relationResponses = mapRelationComponents(relationComponents);

    return new GetRelationResponse(
      relation.getRelationId(),
      relation.getRelationTypeId(),
      relation.getRelationTypeName(),
      relation.getResponsibilityInheritanceFlag(),
      relation.getHierarchyFlag(),
      relation.getRelationCreatedOn(),
      relation.getRelationCreatedBy(),
      relationResponses
    );
  }

  private List<GetRelationComponentResponse> mapRelationComponents (List<RelationComponentWithConnectedValues> relationComponents) {
    return relationComponents.stream()
      .sorted(Comparator.comparing(RelationComponentWithConnectedValues::getRelationTypeComponentName))
      .map(relationComponent -> new GetRelationComponentResponse(
        relationComponent.getRelationComponentId(),
        relationComponent.getAssetId(),
        relationComponent.getAssetDisplayName(),
        relationComponent.getAssetName(),
        relationComponent.getAssetTypeId(),
        relationComponent.getAssetTypeName(),
        relationComponent.getStewardshipStatusId(),
        relationComponent.getStewardshipStatusName(),
        relationComponent.getLifecycleStatusId(),
        relationComponent.getLifecycleStatusName(),
        relationComponent.getHierarchyRole(),
        relationComponent.getResponsibilityInheritanceRole(),
        relationComponent.getRelationTypeComponentId(),
        relationComponent.getRelationTypeComponentName(),
        relationComponent.getCreatedOn(),
        relationComponent.getCreatedBy()
      )).toList();
  }

  private List<GetRelationAttributeResponse> mapRelationAttributes (List<RelationWithAttributes> relationAttributes) {
    return relationAttributes.stream()
      .filter(relationAttribute -> !Stream.of(
        relationAttribute.getAttributeTypeId(),
        relationAttribute.getAttributeTypeName(),
        relationAttribute.getRelationAttributeId(),
        relationAttribute.getValue()
      ).allMatch(Objects::isNull))
      .sorted(Comparator.comparing(RelationWithAttributes::getAttributeTypeName))
      .map(relationAttribute -> new GetRelationAttributeResponse(
        relationAttribute.getAttributeTypeId(),
        relationAttribute.getAttributeTypeName(),
        relationAttribute.getAttributeKind(),
        relationAttribute.getValidationMask(),
        relationAttribute.getAllowedValues(),
        relationAttribute.getRelationAttributeId(),
        relationAttribute.getValue()
      )).toList();
  }

  private List<GetRelationComponentAttributesResponse> mapRelationComponentWithAttributes (List<RelationComponentWithRelationComponentAttributes> relationComponentWithRelationComponentAttributes) {
    return relationComponentWithRelationComponentAttributes.stream()
      .filter(relationComponent -> !Stream.of(
        relationComponent.getAttributeTypeId(),
        relationComponent.getAttributeTypeName(),
        relationComponent.getRelationComponentAttributeId(),
        relationComponent.getValue()
      ).allMatch(Objects::isNull))
      .map(relationComponent -> new GetRelationComponentAttributesResponse(
        relationComponent.getAttributeTypeId(),
        relationComponent.getAttributeTypeName(),
        relationComponent.getAttributeKind(),
        relationComponent.getValidationMask(),
        relationComponent.getAllowedValues(),
        relationComponent.getRelationComponentAttributeId(),
        relationComponent.getValue()
      ))
      .sorted(Comparator.comparing(GetRelationComponentAttributesResponse::getAttribute_type_name))
      .toList();
  }
}
