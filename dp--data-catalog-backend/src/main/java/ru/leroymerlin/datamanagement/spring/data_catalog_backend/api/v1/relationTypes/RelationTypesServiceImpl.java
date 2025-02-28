package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.RelationTypeComponentAssetTypeAssignmentAssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.models.RelationComponentAssetCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.RoleActionCachingService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetHierarchy.AssetHierarchyDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationType.attributeTypes.RelationTypeAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.RelationTypeComponentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.RelationTypeComponentResponseImpl;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.RelationTypeFlagStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.RelationTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.get.GetRelationTypeComponentWithAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.get.GetRelationTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.get.GetRelationTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roleActions.RoleActionsDAO;

@Service
public class RelationTypesServiceImpl extends RelationTypesDAO implements RelationTypesService {
  private final RoleActionCachingService roleActionCachingService;

  private final LanguageService languageService;

  private final RoleActionsDAO roleActionsDAO;

  private final RelationTypeAttributeTypesAssignmentsDAO relationTypeAttributeTypesAssignmentsDAO;

  private final RelationTypeComponentRepository relationTypeComponentRepository;

  private final RelationTypeComponentAssetTypesAssignmentsDAO relationTypeComponentAssetTypesAssignmentsDAO;

  private final RelationTypeComponentAttributeTypesAssignmentsDAO relationTypeComponentAttributeTypesAssignmentsDAO;

  private final RelationsDAO relationsDAO;

  private final AssetHierarchyDAO assetHierarchyDAO;

  private final ResponsibilitiesDAO responsibilitiesDAO;

  public RelationTypesServiceImpl (
    RoleActionCachingService roleActionCachingService,
    LanguageService languageService,
    RoleActionsDAO roleActionsDAO,
    RelationTypeAttributeTypesAssignmentsDAO relationTypeAttributeTypesAssignmentsDAO,
    RelationTypeComponentRepository relationTypeComponentRepository,
    RelationTypeComponentAssetTypesAssignmentsDAO relationTypeComponentAssetTypesAssignmentsDAO,
    RelationTypeComponentAttributeTypesAssignmentsDAO relationTypeComponentAttributeTypesAssignmentsDAO,
    RelationsDAO relationsDAO,
    AssetHierarchyDAO assetHierarchyDAO,
    ResponsibilitiesDAO responsibilitiesDAO
  ) {
    this.roleActionCachingService = roleActionCachingService;
    this.languageService = languageService;
    this.roleActionsDAO = roleActionsDAO;
    this.relationTypeAttributeTypesAssignmentsDAO = relationTypeAttributeTypesAssignmentsDAO;
    this.relationTypeComponentRepository = relationTypeComponentRepository;
    this.relationTypeComponentAssetTypesAssignmentsDAO = relationTypeComponentAssetTypesAssignmentsDAO;
    this.relationTypeComponentAttributeTypesAssignmentsDAO = relationTypeComponentAttributeTypesAssignmentsDAO;
    this.relationsDAO = relationsDAO;
    this.assetHierarchyDAO = assetHierarchyDAO;
    this.responsibilitiesDAO = responsibilitiesDAO;
  }

  @Override
  @Transactional
  public PostRelationTypeResponse createRelationType (
    PostRelationTypeRequest relationTypeRequest,
    User user
  ) throws
    InvalidFieldLengthException,
    IncorrectRoleInHierarchyException,
    SomeRequiredFieldsAreEmptyException,
    IncorrectRoleForResponsibilityInheritanceException
  {
    if (
      relationTypeRequest.getResponsibility_inheritance_flag() != null &&
      relationTypeRequest.getResponsibility_inheritance_flag()
    ) {
      checkResponsibilityInheritanceRolesInComponent(relationTypeRequest.getRelation_type_component());
    }

    if (
      relationTypeRequest.getHierarchy_flag() != null &&
      relationTypeRequest.getHierarchy_flag()
    ) {
      checkHierarchyRolesInComponent(relationTypeRequest.getRelation_type_component());
    }

    Language ru = languageService.getLanguage("ru");

    RelationType relationType = relationTypeRepository.save(new RelationType(
      relationTypeRequest.getRelation_type_name(),
      relationTypeRequest.getRelation_type_description(),
      relationTypeRequest.getRelation_type_component_number(),
      relationTypeRequest.getResponsibility_inheritance_flag(),
      relationTypeRequest.getHierarchy_flag(),
      relationTypeRequest.getUniqueness_flag(),
      relationTypeRequest.getSelf_related_flag(),
      ru,
      user
    ));

    // Clear cache if someone tried to get relation type with this id
    roleActionCachingService.evictByValueInKey(relationType.getRelationTypeId().toString());

    PostRelationTypeResponse postRelationTypeResponse = new PostRelationTypeResponse(
      relationType.getRelationTypeId(),
      relationType.getRelationTypeName(),
      relationType.getRelationTypeDescription(),
      relationType.getComponentNumber(),
      relationType.getResponsibilityInheritanceFlag(),
      relationType.getHierarchyFlag(),
      relationType.getUniquenessFlag(),
      relationType.getSelfRelatedFlag(),
      ru.getLanguage(),
      new Timestamp(System.currentTimeMillis()),
      user.getUserId(),
      new ArrayList<>()
    );

    relationTypeRequest.getRelation_type_component().forEach(component -> {
      if (StringUtils.isEmpty(component.getRelation_type_component_name())) {
        throw new SomeRequiredFieldsAreEmptyException();
      }

      validateFieldsLength(component.getRelation_type_component_name(), component.getRelation_type_component_description());

      RelationTypeComponent relationTypeComponent = createRelationTypeComponent(component, relationType, ru, user);

      postRelationTypeResponse.getRelation_type_component()
        .add(new PostRelationTypeComponentResponse(
          relationTypeComponent.getRelationTypeComponentId(),
          relationTypeComponent.getRelationTypeComponentName(),
          relationTypeComponent.getRelationTypeComponentDescription(),
          relationTypeComponent.getResponsibilityInheritanceRole(),
          relationTypeComponent.getHierarchyRole(),
          relationTypeComponent.getSingleRelationTypeComponentForAssetFlag(),
          new Timestamp(System.currentTimeMillis()),
          user.getUserId()
        ));
    });

    return postRelationTypeResponse;
  }

  @Override
  @Transactional
  public RelationTypeResponse updateRelationType (
    UUID relationTypeID,
    PatchRelationTypeRequest relationTypeRequest,
    User user
  ) throws
    InvalidRolesException,
    IllegalArgumentException,
    InvalidFieldLengthException,
    RelationTypeNotFoundException,
    SelfRelatedAssetExistsException,
    IncorrectRoleInHierarchyException,
    InvalidNumberOfComponentsException,
    RelationTypeComponentNotFoundException,
    MultipleRelationExistsWithSameAssetException,
    IncorrectRoleForResponsibilityInheritanceException
  {
    RelationType foundRelationType = findRelationTypeById(relationTypeID, true);

    OptionalUtils.doActionIfPresent(relationTypeRequest.getRelation_type_name(), name -> foundRelationType.setRelationTypeName(name.orElse(foundRelationType.getRelationTypeName())));
    OptionalUtils.doActionIfPresent(relationTypeRequest.getRelation_type_description(), description -> foundRelationType.setRelationTypeDescription(description.orElse(null)));

    RelationTypeFlagStatus uniquenessFlagStatus = getFlagStatus(foundRelationType.getUniquenessFlag(), relationTypeRequest.getUniqueness_flag());
    RelationTypeFlagStatus hierarchyFlagStatus = getFlagStatus(foundRelationType.getHierarchyFlag(), relationTypeRequest.getHierarchy_flag());
    RelationTypeFlagStatus selfRelatedFlagStatus = getFlagStatus(foundRelationType.getSelfRelatedFlag(), relationTypeRequest.getSelf_related_flag());
    RelationTypeFlagStatus responsibilityInheritanceFlagStatus = getFlagStatus(foundRelationType.getResponsibilityInheritanceFlag(), relationTypeRequest.getResponsibility_inheritance_flag());

    List<RelationComponent> relationComponents = relationsDAO.findAllRelationComponentsByRelationTypeId(foundRelationType.getRelationTypeId());
    List<RelationTypeComponentResponse> componentsResponseList = updateRelationTypeComponents(relationTypeRequest, foundRelationType, hierarchyFlagStatus, responsibilityInheritanceFlagStatus, relationComponents, user);

    if (uniquenessFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_TRUE)) {
      checkConnectedRelations(foundRelationType, relationComponents);
    }

    if (selfRelatedFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_FALSE)) {
      checkConnectedRelationTypeComponentAsset(foundRelationType);
    }

    if (hierarchyFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_FALSE)) {
      foundRelationType.setHierarchyFlag(false);

      // Delete parent -> child connection
      deleteAssetHierarchy(relationComponents, user);
    } else if (hierarchyFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_TRUE)) {
      foundRelationType.setHierarchyFlag(true);

      // Create parent -> child connection
      createAssetHierarchy(relationComponents, user);
    }

    if (responsibilityInheritanceFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_FALSE)) {
      foundRelationType.setResponsibilityInheritanceFlag(false);

      deleteResponsibilities(relationComponents, user);
    } else if (responsibilityInheritanceFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_TRUE)) {
      foundRelationType.setResponsibilityInheritanceFlag(true);

      createResponsibilities(relationComponents, user);
    }

    if (!selfRelatedFlagStatus.equals(RelationTypeFlagStatus.NOT_CHANGED)) {
      foundRelationType.setSelfRelatedFlag(relationTypeRequest.getSelf_related_flag());
    }

    if (!uniquenessFlagStatus.equals(RelationTypeFlagStatus.NOT_CHANGED)) {
      foundRelationType.setUniquenessFlag(relationTypeRequest.getUniqueness_flag());
    }

    foundRelationType.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    foundRelationType.setModifiedBy(user);
    RelationType relationType = relationTypeRepository.save(foundRelationType);

    return new RelationTypeResponse(
      relationType.getRelationTypeId(),
      relationType.getRelationTypeName(),
      relationType.getRelationTypeDescription(),
      relationType.getComponentNumber(),
      relationType.getResponsibilityInheritanceFlag(),
      relationType.getHierarchyFlag(),
      relationType.getUniquenessFlag(),
      relationType.getSelfRelatedFlag(),
      relationType.getLanguageName(),
      relationType.getCreatedOn(),
      relationType.getCreatedByUUID(),
      relationType.getLastModifiedOn(),
      user.getUserId(),
      componentsResponseList
    );
  }

  @Override
  public RelationTypeResponse getRelationTypeById (UUID relationTypeId) throws RelationTypeNotFoundException {
    RelationType relationType = findRelationTypeById(relationTypeId, true);

    List<RelationTypeComponent> relationTypeComponentsList = relationTypeComponentRepository.findAllRelationTypeComponentsByRelationTypeIds(List.of(relationType.getRelationTypeId()));
    List<UUID> relationTypeComponentList = relationTypeComponentsList.stream().map(RelationTypeComponent::getRelationTypeComponentId).toList();

    List<RelationTypeComponentAssetTypeAssignmentAssetType> assignments = relationTypeComponentAssetTypesAssignmentsDAO.findAllByRelationTypeComponentIds(relationTypeComponentList);
    Map<UUID, List<GetRelationTypeComponentWithAssignmentsResponse.RelationTypeComponentAssetTypeAssignment>> assignmentByRelationTypeComponent = assignments.stream()
      .collect(Collectors.groupingBy(
          RelationTypeComponentAssetTypeAssignmentAssetType::getRelationTypeComponentId,
          Collectors.mapping(
            entry -> new GetRelationTypeComponentWithAssignmentsResponse.RelationTypeComponentAssetTypeAssignment(entry.getRelationTypeComponentAssetTypeAssignmentId(), entry.getAssetTypeId()),
            Collectors.toList()
          )
        )
      );

    List<RelationTypeComponentResponse> relationTypeComponentsResponse = relationTypeComponentsList
      .stream()
      .sorted(Comparator.comparing(RelationTypeComponent::getRelationTypeComponentName))
      .map(relationTypeComponent -> mapRelationTypeComponentWithAssignments(relationTypeComponent, assignmentByRelationTypeComponent.get(relationTypeComponent.getRelationTypeComponentId())))
      .toList();

    return mapRelationType(relationType, relationTypeComponentsResponse);
  }

  @Override
  public GetRelationTypesResponse getRelationTypesByParams (
    String relationTypeName,
    Integer componentNumber,
    Boolean hierarchyFlag,
    Boolean responsibilityInheritanceFlag,
    UUID allowedAssetTypeId,
    Boolean selfRelatedFlag,
    Boolean uniquenessFlag,
    Integer pageNumber, 
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<RelationType> relationTypes;

    if (allowedAssetTypeId != null) {
      relationTypes = relationTypeRepository.findAllByParamsAndAllowedAssetTypePageable(
        relationTypeName,
        componentNumber,
        hierarchyFlag,
        responsibilityInheritanceFlag,
        allowedAssetTypeId,
        selfRelatedFlag,
        uniquenessFlag,
        PageRequest.of(pageNumber, pageSize, Sort.by("relation_type_name").ascending())
      );
    } else {
      relationTypes = relationTypeRepository.findAllByParamsWithJoinedTablesPageable(
        relationTypeName,
        componentNumber,
        hierarchyFlag,
        responsibilityInheritanceFlag,
        selfRelatedFlag,
        uniquenessFlag,
        PageRequest.of(pageNumber, pageSize, Sort.by("relationTypeName").ascending())
      );
    }

    List<GetRelationTypeResponse> result = relationTypes.stream()
      .map(relationType -> {
        UUID lastModifiedBy = relationType.getModifiedBy() != null ? relationType.getModifiedBy().getUserId() : null;

        return new GetRelationTypeResponse(
          relationType.getRelationTypeId(),
          relationType.getRelationTypeName(),
          relationType.getRelationTypeDescription(),
          relationType.getComponentNumber(),
          relationType.getResponsibilityInheritanceFlag(),
          relationType.getHierarchyFlag(),
          relationType.getUniquenessFlag(),
          relationType.getSelfRelatedFlag(),
          relationType.getLanguageName(),
          relationType.getCreatedOn(),
          relationType.getCreatedByUUID(),
          relationType.getLastModifiedOn(),
          lastModifiedBy
        );
      }).toList();

    return new GetRelationTypesResponse(
      relationTypes.getTotalElements(),
      pageSize,
      pageNumber,
      result
    );
  }

  @Override
  @Transactional
  public void deleteRelationTypeById (UUID relationTypeId, User user) throws RelationTypeNotFoundException {
    RelationType relationType = findRelationTypeById(relationTypeId, false);

    List<UUID> relationTypeComponentIds = new ArrayList<>();
    List<RelationTypeComponent> relationTypeComponentsList = relationTypeComponentRepository.findAllRelationTypeComponentsByRelationTypeIds(List.of(relationType.getRelationTypeId()));
    relationTypeComponentsList.forEach(component -> {
      component.setIsDeleted(true);
      component.setDeletedBy(user);
      component.setDeletedOn(new Timestamp(System.currentTimeMillis()));

      relationTypeComponentIds.add(component.getRelationTypeComponentId());

      relationTypeComponentRepository.save(component);
    });

    relationTypeAttributeTypesAssignmentsDAO.deleteAllByRelationTypeId(relationTypeId, user);
    relationTypeComponentAssetTypesAssignmentsDAO.deleteAllByRelationTypeComponentIds(relationTypeComponentIds, user);
    relationTypeComponentAttributeTypesAssignmentsDAO.deleteAllByRelationTypeComponentIds(relationTypeComponentIds, user);

    roleActionsDAO.deleteAllByParams(null, null, null, relationTypeId, user);

    relationType.setIsDeleted(true);
    relationType.setDeletedOn(new Timestamp(System.currentTimeMillis()));
    relationType.setDeletedBy(user);

    relationTypeRepository.save(relationType);

    roleActionCachingService.evictByValueInKey(relationTypeId.toString());
  }

  private void checkConnectedRelationTypeComponentAsset (RelationType relationType) throws SelfRelatedAssetExistsException {
    List<RelationComponentAssetCount> relationComponentAssetCounts = relationsDAO.countRelationTypeComponentAsset(relationType.getRelationTypeId());

    if (relationComponentAssetCounts.isEmpty()) return;

    throw new SelfRelatedAssetExistsException();
  }

  private void checkConnectedRelations (RelationType relationType, List<RelationComponent> relationComponents) {
    if (relationComponents.isEmpty()) return;

    RelationComponent firsRelationComponent = relationComponents.get(0);
    List<RelationComponent> otherComponentsList = relationComponents.stream().skip(1).toList();

    Integer count = relationsDAO.countExistingRelationComponents(
      relationType.getRelationTypeId(),
      firsRelationComponent.getAsset().getAssetId(),
      firsRelationComponent.getRelationTypeComponent().getRelationTypeComponentId(),
      otherComponentsList.stream().map(component -> component.getAsset().getAssetId()).toList(),
      otherComponentsList.stream().map(component -> component.getRelationTypeComponent().getRelationTypeComponentId()).toList()
    );

    if (!Objects.equals(count, relationType.getComponentNumber())) {
      throw new MultipleRelationExistsWithAssetException(relationType.getRelationTypeId());
    }
  }

  private void checkResponsibilityInheritanceRolesInComponent (
    List<? extends RelationTypeComponentWithRoles> relationTypeComponentWithRoles
  ) throws IncorrectRoleForResponsibilityInheritanceException {
    Map<String, Long> inheritanceRoleCollection = relationTypeComponentWithRoles
      .stream()
      .filter(c -> c.getResponsibility_inheritance_role() != null)
      .collect(Collectors.groupingBy(RelationTypeComponentWithRoles::getResponsibility_inheritance_role, Collectors.counting()));

    String sourceRole = ResponsibilityInheritanceRole.SOURCE.toString();
    String consumerRole = ResponsibilityInheritanceRole.CONSUMER.toString();

    if (
      !inheritanceRoleCollection.containsKey(sourceRole) ||
      inheritanceRoleCollection.get(sourceRole) != 1
    ) {
      throw new IncorrectRoleForResponsibilityInheritanceException();
    }

    if (
      !inheritanceRoleCollection.containsKey(consumerRole) ||
      inheritanceRoleCollection.get(consumerRole) != 1
    ) {
      throw new IncorrectRoleForResponsibilityInheritanceException();
    }
  }

  private void checkHierarchyRolesInComponent (List<? extends RelationTypeComponentWithRoles> relationTypeComponentWithRoles) throws IncorrectRoleInHierarchyException {
    Map<String, Long> hierarchyRoleCollection = relationTypeComponentWithRoles
      .stream()
      .filter(c -> c.getHierarchy_role() != null)
      .collect(Collectors.groupingBy(RelationTypeComponentWithRoles::getHierarchy_role, Collectors.counting()));

    String parentRole = HierarchyRole.PARENT.toString();
    String childRole = HierarchyRole.CHILD.toString();

    if (
      !hierarchyRoleCollection.containsKey(parentRole) ||
      hierarchyRoleCollection.get(parentRole) != 1
    ) {
      throw new IncorrectRoleInHierarchyException();
    }

    if (
      !hierarchyRoleCollection.containsKey(childRole) ||
      hierarchyRoleCollection.get(childRole) != 1
    ) {
      throw new IncorrectRoleInHierarchyException();
    }
  }

  private RelationTypeComponent createRelationTypeComponent (
    PostRelationTypeComponentRequest component,
    RelationType relationType,
    Language language,
    User user
  ) throws IllegalArgumentException {
    HierarchyRole hierarchyRole = null;
    ResponsibilityInheritanceRole responsibilityInheritanceRole = null;

    if (
      relationType.getResponsibilityInheritanceFlag() &&
      StringUtils.isNotEmpty(component.getResponsibility_inheritance_role())
    ) {
      responsibilityInheritanceRole = ResponsibilityInheritanceRole.valueOf(component.getResponsibility_inheritance_role());
    }

    if (
      relationType.getHierarchyFlag() &&
      StringUtils.isNotEmpty(component.getHierarchy_role())
    ) {
      hierarchyRole = HierarchyRole.valueOf(component.getHierarchy_role());
    }

    return relationTypeComponentRepository.save(new RelationTypeComponent(
      component.getRelation_type_component_name(),
      component.getRelation_type_component_description(),
      responsibilityInheritanceRole,
      hierarchyRole,
      component.getSingle_relation_type_component_for_asset_flag(),
      language,
      relationType,
      user
    ));
  }

  private List<RelationTypeComponentResponse> updateRelationTypeComponents (
    PatchRelationTypeRequest relationTypeRequest,
    RelationType relationType,
    RelationTypeFlagStatus hierarchyFlagStatus,
    RelationTypeFlagStatus responsibilityInheritanceFlagStatus,
    List<RelationComponent> relationComponents,
    User user
  ) throws
    InvalidRolesException,
    IllegalArgumentException,
    IncorrectRoleInHierarchyException,
    InvalidNumberOfComponentsException,
    RelationTypeComponentNotFoundException,
    MultipleRelationExistsWithSameAssetException,
    IncorrectRoleForResponsibilityInheritanceException
  {
    checkRelationTypeComponentsRequest(relationTypeRequest, relationType, hierarchyFlagStatus, responsibilityInheritanceFlagStatus);

    /*
     * if flags were changed from true -> false
     * Clear roles by role type in all connected relation type components
     */
    if (
      hierarchyFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_FALSE) ||
      responsibilityInheritanceFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_FALSE)
    ) {
      return updateAllConnectedRelationTypeComponents(relationTypeRequest, relationType, user, hierarchyFlagStatus, responsibilityInheritanceFlagStatus, relationComponents);
    }

    /*
    * If flags were changed from false -> true
    * Or flags weren't changed
    */

    return updateRelationTypeComponentsFromRequest(relationTypeRequest, relationType, user, hierarchyFlagStatus, responsibilityInheritanceFlagStatus, relationComponents);
  }

  private List<RelationTypeComponent> loadRelationTypeComponents (
    PatchRelationTypeRequest relationTypeRequest,
    UUID relationTypeId
  ) throws IllegalArgumentException, RelationTypeComponentNotFoundException {
    if (
      relationTypeRequest.getRelation_type_component() == null ||
      relationTypeRequest.getRelation_type_component().isEmpty()
    ) return new ArrayList<>();

    List<UUID> componentIds = relationTypeRequest.getRelation_type_component().stream()
      .map(component -> UUID.fromString(component.getRelation_type_component_id()))
      .toList();

    List<RelationTypeComponent> relationTypeComponents = relationTypeComponentRepository.findAllByComponentIdsAndRelationTypeId(componentIds, relationTypeId);

    if (relationTypeComponents.size() != relationTypeRequest.getRelation_type_component().size()) {
      throw new RelationTypeComponentNotFoundException();
    }

    return relationTypeComponents;
  }

  private void checkRelationTypeComponentsRequest (
    PatchRelationTypeRequest relationTypeRequest,
    RelationType relationType,
    RelationTypeFlagStatus hierarchyFlagStatus,
    RelationTypeFlagStatus responsibilityInheritanceFlagStatus
  ) throws
    IncorrectRoleInHierarchyException,
    InvalidNumberOfComponentsException,
    IncorrectRoleForResponsibilityInheritanceException
  {
    if (
      hierarchyFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_TRUE) ||
      responsibilityInheritanceFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_TRUE)
    ) {
      boolean hasNoComponents = (
        relationTypeRequest.getRelation_type_component() == null ||
        relationTypeRequest.getRelation_type_component().isEmpty()
      );

      if (hasNoComponents) {
        throw new InvalidNumberOfComponentsException();
      }

      Integer countRelationTypeComponents = relationTypeComponentRepository.countRelationTypeComponentByRelationTypeId(relationType.getRelationTypeId());
      if (countRelationTypeComponents > 2) {
        throw new InvalidNumberOfComponentsException();
      }
    }

    if (hierarchyFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_TRUE)) {
      checkHierarchyRolesInComponent(relationTypeRequest.getRelation_type_component());
    }

    if (responsibilityInheritanceFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_TRUE)) {
      checkResponsibilityInheritanceRolesInComponent(relationTypeRequest.getRelation_type_component());
    }
  }

  private List<RelationTypeComponentResponse> updateRelationTypeComponentsFromRequest (
    PatchRelationTypeRequest relationTypeRequest,
    RelationType relationType,
    User user,
    RelationTypeFlagStatus hierarchyFlagStatus,
    RelationTypeFlagStatus responsibilityInheritanceFlagStatus,
    List<RelationComponent> relationComponents
  )
    throws
    InvalidRolesException,
    IllegalArgumentException,
    InvalidFieldLengthException
  {
    List<RelationTypeComponent> relationTypeComponents = loadRelationTypeComponents(relationTypeRequest, relationType.getRelationTypeId());

    Map<UUID, List<PatchRelationTypeComponentRequest>> updatedRelationTypeComponentsDict = getRelationTypeComponentsRequestDict(relationTypeRequest.getRelation_type_component());

    return relationTypeComponents.stream()
      .map(component -> {
        List<PatchRelationTypeComponentRequest> updatedComponent = updatedRelationTypeComponentsDict.get(component.getRelationTypeComponentId());
        PatchRelationTypeComponentRequest firstComponent = updatedComponent.get(0);

        RelationTypeComponent updatedRelationTypeComponent;
        Optional<String> relationTypeComponentName = OptionalUtils.getOptionalFromField(updatedComponent.get(0).getRelation_type_component_name());
        Optional<String> relationTypeComponentDescription = OptionalUtils.getOptionalFromField(updatedComponent.get(0).getRelation_type_component_description());

        validateFieldsLength(relationTypeComponentName.orElse(null), relationTypeComponentDescription.orElse(null));
        checkSingleRelationTypeComponentForAssetFlag(updatedComponent.get(0), component);

        if (
          hierarchyFlagStatus.equals(RelationTypeFlagStatus.NOT_CHANGED) &&
          StringUtils.isNotEmpty(firstComponent.getHierarchy_role())
        ) {
          throw new InvalidRolesException();
        }

        if (
          responsibilityInheritanceFlagStatus.equals(RelationTypeFlagStatus.NOT_CHANGED) &&
          StringUtils.isNotEmpty(firstComponent.getResponsibility_inheritance_role())
        ) {
          throw new InvalidRolesException();
        }

        HierarchyRole foundComponentHierarchyRole = parseRelationTypeComponentHierarchyRoleFromRequest(firstComponent);
        ResponsibilityInheritanceRole foundComponentResponsibilityInheritanceRole = parseRelationTypeComponentResponsibilityInheritanceRoleFromRequest(firstComponent);

        HierarchyRole hierarchyRole = getRelationTypeComponentRoleValue(hierarchyFlagStatus, component.getHierarchyRole(), foundComponentHierarchyRole);
        ResponsibilityInheritanceRole responsibilityInheritanceRole = getRelationTypeComponentRoleValue(responsibilityInheritanceFlagStatus, component.getResponsibilityInheritanceRole(), foundComponentResponsibilityInheritanceRole);

        updateRelationComponent(relationComponents, component, hierarchyRole, responsibilityInheritanceRole, user);

        updatedRelationTypeComponent = updateRelationTypeComponent(
          component,
          firstComponent.getRelation_type_component_name(),
          firstComponent.getRelation_type_component_description(),
          responsibilityInheritanceRole,
          hierarchyRole,
          Optional.ofNullable(firstComponent.getSingle_relation_type_component_for_asset_flag()),
          user
        );

        return mapRelationTypeComponent(updatedRelationTypeComponent);
      }).toList();
  }

  private List<RelationTypeComponentResponse> updateAllConnectedRelationTypeComponents (
    PatchRelationTypeRequest relationTypeRequest,
    RelationType relationType,
    User user,
    RelationTypeFlagStatus hierarchyFlagStatus,
    RelationTypeFlagStatus responsibilityInheritanceFlagStatus,
    List<RelationComponent> relationComponents
  )
    throws
    IllegalArgumentException,
    InvalidFieldLengthException,
    MultipleRelationExistsWithSameAssetException
  {
    List<RelationTypeComponent> relationTypeComponentList = relationTypeComponentRepository.findAllRelationTypeComponentsByRelationTypeIds(List.of(relationType.getRelationTypeId()));

    Map<UUID, List<PatchRelationTypeComponentRequest>> updatedRelationTypeComponentsDict = getRelationTypeComponentsRequestDict(relationTypeRequest.getRelation_type_component());

    return relationTypeComponentList.stream()
      .map(component -> {
        List<PatchRelationTypeComponentRequest> updatedComponent = updatedRelationTypeComponentsDict.get(component.getRelationTypeComponentId());
        PatchRelationTypeComponentRequest firstComponent = updatedComponent != null
          ? updatedComponent.get(0)
          : null;

        if (updatedComponent != null) {
          Optional<String> relationTypeComponentName = OptionalUtils.getOptionalFromField(updatedComponent.get(0).getRelation_type_component_name());
          Optional<String> relationTypeComponentDescription = OptionalUtils.getOptionalFromField(updatedComponent.get(0).getRelation_type_component_description());

          validateFieldsLength(relationTypeComponentName.orElse(null), relationTypeComponentDescription.orElse(null));

          checkSingleRelationTypeComponentForAssetFlag(updatedComponent.get(0), component);
        }

        RelationTypeComponent updatedRelationTypeComponent;

        HierarchyRole foundComponentHierarchyRole = parseRelationTypeComponentHierarchyRoleFromRequest(firstComponent);
        ResponsibilityInheritanceRole foundComponentResponsibilityInheritanceRole = parseRelationTypeComponentResponsibilityInheritanceRoleFromRequest(firstComponent);

        HierarchyRole hierarchyRole = getRelationTypeComponentRoleValue(hierarchyFlagStatus, component.getHierarchyRole(), foundComponentHierarchyRole);
        ResponsibilityInheritanceRole responsibilityInheritanceRole = getRelationTypeComponentRoleValue(responsibilityInheritanceFlagStatus, component.getResponsibilityInheritanceRole(), foundComponentResponsibilityInheritanceRole);

        updateRelationComponent(relationComponents, component, hierarchyRole, responsibilityInheritanceRole, user);

        // Drop role even if component is not mentioned in request body
        if (firstComponent == null) {
          updatedRelationTypeComponent = updateRelationTypeComponent(
            component,
            null,
            null,
            responsibilityInheritanceRole,
            hierarchyRole,
            Optional.empty(),
            user
          );
        } else {
          updatedRelationTypeComponent = updateRelationTypeComponent(
            component,
            firstComponent.getRelation_type_component_name(),
            firstComponent.getRelation_type_component_description(),
            responsibilityInheritanceRole,
            hierarchyRole,
            Optional.ofNullable(firstComponent.getSingle_relation_type_component_for_asset_flag()),
            user
          );
        }

        return mapRelationTypeComponent(updatedRelationTypeComponent);
      })
        .filter(component -> updatedRelationTypeComponentsDict.containsKey(component.getRelation_type_component_id()))
        .toList();
  }

  private RelationTypeComponent updateRelationTypeComponent (
    RelationTypeComponent relationTypeComponent,
    Optional<String> relationTypeComponentName,
    Optional<String> relationTypeComponentDescription,
    ResponsibilityInheritanceRole responsibilityInheritanceRole,
    HierarchyRole hierarchyRole,
    Optional<Boolean> singleRelationTypeComponentForAssetFlag,
    User user
  ) {
    OptionalUtils.doActionIfPresent(relationTypeComponentName, name -> relationTypeComponent.setRelationTypeComponentName(name.orElse(relationTypeComponent.getRelationTypeComponentName())));
    OptionalUtils.doActionIfPresent(relationTypeComponentDescription, description -> relationTypeComponent.setRelationTypeComponentDescription(description.orElse(null)));
    OptionalUtils.doActionIfPresent(singleRelationTypeComponentForAssetFlag, flag -> relationTypeComponent.setSingleRelationTypeComponentForAssetFlag(flag.orElse(null)));

    relationTypeComponent.setHierarchyRole(hierarchyRole);
    relationTypeComponent.setResponsibilityInheritanceRole(responsibilityInheritanceRole);

    relationTypeComponent.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    relationTypeComponent.setModifiedBy(user);

    return relationTypeComponentRepository.save(relationTypeComponent);
  }

  private Map<UUID, List<PatchRelationTypeComponentRequest>> getRelationTypeComponentsRequestDict (List<PatchRelationTypeComponentRequest> relationTypeRequest) {
    if (relationTypeRequest == null) {
      relationTypeRequest = new ArrayList<>();
    }

    return relationTypeRequest.stream()
      .collect(Collectors.groupingBy(c -> UUID.fromString(c.getRelation_type_component_id())));
  }

  private RelationTypeResponse mapRelationType (RelationType relationType, List<RelationTypeComponentResponse> relationTypeComponents) {
    UUID lastModifiedBy = relationType.getModifiedBy() != null ? relationType.getModifiedBy().getUserId() : null;

    return new RelationTypeResponse(
      relationType.getRelationTypeId(),
      relationType.getRelationTypeName(),
      relationType.getRelationTypeDescription(),
      relationType.getComponentNumber(),
      relationType.getResponsibilityInheritanceFlag(),
      relationType.getHierarchyFlag(),
      relationType.getUniquenessFlag(),
      relationType.getSelfRelatedFlag(),
      relationType.getLanguageName(),
      relationType.getCreatedOn(),
      relationType.getCreatedByUUID(),
      relationType.getLastModifiedOn(),
      lastModifiedBy,
      relationTypeComponents
    );
  }

  private RelationTypeComponentResponse mapRelationTypeComponentWithAssignments (
    RelationTypeComponent relationTypeComponent,
    List<GetRelationTypeComponentWithAssignmentsResponse.RelationTypeComponentAssetTypeAssignment> assignments
  ) {
    UUID lastModifiedBy = relationTypeComponent.getModifiedBy() != null ? relationTypeComponent.getModifiedBy().getUserId() : null;

    return new GetRelationTypeComponentWithAssignmentsResponse(
      relationTypeComponent.getRelationTypeComponentId(),
      relationTypeComponent.getRelationTypeComponentName(),
      relationTypeComponent.getRelationTypeComponentDescription(),
      relationTypeComponent.getResponsibilityInheritanceRole(),
      relationTypeComponent.getHierarchyRole(),
      relationTypeComponent.getSingleRelationTypeComponentForAssetFlag(),
      relationTypeComponent.getCreatedOn(),
      relationTypeComponent.getCreatedByUUID(),
      relationTypeComponent.getLastModifiedOn(),
      lastModifiedBy,
      assignments
    );
  }

  private RelationTypeComponentResponse mapRelationTypeComponent (RelationTypeComponent relationTypeComponent) {
      UUID lastModifiedBy = relationTypeComponent.getModifiedBy() != null ? relationTypeComponent.getModifiedBy().getUserId() : null;

      return new RelationTypeComponentResponseImpl(
        relationTypeComponent.getRelationTypeComponentId(),
        relationTypeComponent.getRelationTypeComponentName(),
        relationTypeComponent.getRelationTypeComponentDescription(),
        relationTypeComponent.getResponsibilityInheritanceRole(),
        relationTypeComponent.getHierarchyRole(),
        relationTypeComponent.getSingleRelationTypeComponentForAssetFlag(),
        relationTypeComponent.getCreatedOn(),
        relationTypeComponent.getCreatedByUUID(),
        relationTypeComponent.getLastModifiedOn(),
        lastModifiedBy
      );
  }

  private HierarchyRole parseRelationTypeComponentHierarchyRoleFromRequest (PatchRelationTypeComponentRequest requestComponent) {
    HierarchyRole foundComponentHierarchyRole = null;
    if (requestComponent != null && StringUtils.isNotEmpty(requestComponent.getHierarchy_role())) {
      foundComponentHierarchyRole = HierarchyRole.valueOf(requestComponent.getHierarchy_role());
    }

    return foundComponentHierarchyRole;
  }

  private ResponsibilityInheritanceRole parseRelationTypeComponentResponsibilityInheritanceRoleFromRequest (PatchRelationTypeComponentRequest requestComponent) {
    ResponsibilityInheritanceRole foundComponentResponsibilityInheritanceRole = null;

    if (requestComponent != null && StringUtils.isNotEmpty(requestComponent.getResponsibility_inheritance_role())) {
      foundComponentResponsibilityInheritanceRole = ResponsibilityInheritanceRole.valueOf(requestComponent.getResponsibility_inheritance_role());
    }

    return foundComponentResponsibilityInheritanceRole;
  }

  private RelationTypeFlagStatus getFlagStatus (Boolean previous, Boolean next) {
    if (next == null || next == previous) {
      return RelationTypeFlagStatus.NOT_CHANGED;
    }

    boolean isPreviousFalse = previous == null || !previous;
    if (next && isPreviousFalse) {
      return RelationTypeFlagStatus.CHANGED_TO_TRUE;
    }

    return RelationTypeFlagStatus.CHANGED_TO_FALSE;
  }

  private <T> T getRelationTypeComponentRoleValue (RelationTypeFlagStatus flagStatus, T previousRole, T newRole) {
    if (flagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_FALSE)) {
      return null;
    }

    if (flagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_TRUE)) {
      return newRole;
    }

    return previousRole;
  }

  private void updateRelationComponent (
    List<RelationComponent> relationComponents,
    RelationTypeComponent relationTypeComponent,
    HierarchyRole hierarchyRole,
    ResponsibilityInheritanceRole responsibilityInheritanceRole,
    User user
  ) {
    Optional<RelationComponent> optionalRelationComponent = relationComponents
      .stream()
      .filter(item -> item.getRelationTypeComponent().getRelationTypeComponentId().equals(relationTypeComponent.getRelationTypeComponentId()))
      .findFirst();

    if (optionalRelationComponent.isPresent()) {
      RelationComponent relationComponent = optionalRelationComponent.get();
      relationComponent.setHierarchyRole(hierarchyRole);
      relationComponent.setResponsibilityInheritanceRole(responsibilityInheritanceRole);
      relationComponent.setModifiedBy(user);
      relationComponent.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));

      relationsDAO.saveRelationComponent(relationComponent);
    }
  }

  private void createAssetHierarchy (List<RelationComponent> relationComponents, User user) {
    if (relationComponents.isEmpty()) return;

    Map<HierarchyRole, List<RelationComponent>> hierarchyRoleListMap = relationComponents.stream().collect(Collectors.groupingBy(RelationComponent::getHierarchyRole));
    RelationComponent parentComponents = hierarchyRoleListMap.get(HierarchyRole.PARENT).get(0);
    RelationComponent childComponents = hierarchyRoleListMap.get(HierarchyRole.CHILD).get(0);

    assetHierarchyDAO.saveAssetHierarchy(new AssetHierarchy(
      parentComponents.getAsset(),
      childComponents.getAsset(),
      parentComponents.getRelation(),
      user
    ));
  }

  private void deleteAssetHierarchy (List<RelationComponent> relationComponents, User user) {
    if (relationComponents.isEmpty()) return;

    // 1 relation_type = 1 relation
    assetHierarchyDAO.deleteAllByRelationId(List.of(relationComponents.get(0).getRelation().getRelationId()), user);
  }

  private void deleteResponsibilities (List<RelationComponent> relationComponents, User user) {
    if (relationComponents.isEmpty()) return;

    List<UUID> relationsList = relationComponents.stream()
      .map(component -> component.getRelation().getRelationId())
      .collect(Collectors.toSet())
      .stream().toList();

    responsibilitiesDAO.deleteAllByRelationIds(relationsList, user);
  }

  @Override
  public void createResponsibilities (List<RelationComponent> relationComponents, User user) {
    if (relationComponents.isEmpty()) return;

    Map<ResponsibilityInheritanceRole, List<RelationComponent>> hierarchyRoleListMap = relationComponents.stream().collect(Collectors.groupingBy(RelationComponent::getResponsibilityInheritanceRole));
    RelationComponent sourceComponent = hierarchyRoleListMap.get(ResponsibilityInheritanceRole.SOURCE).get(0);
    RelationComponent consumerComponent = hierarchyRoleListMap.get(ResponsibilityInheritanceRole.CONSUMER).get(0);

    // Find all source responsibilities
    List<Responsibility> sourceResponsibilities = responsibilitiesDAO.findAllByParams(sourceComponent.getAsset().getAssetId(), null, null, null, null, null);
    // Copy source responsibilities on consumer asset
    List<Responsibility> createdSourceResponsibilities = sourceResponsibilities.stream()
      .map(responsibility -> {
        Responsibility parentResponsibility = responsibility.getParentResponsibility() != null ? responsibility.getParentResponsibility() : responsibility;
        return createResponsibility(responsibility, consumerComponent.getAsset(), consumerComponent.getRelation(), parentResponsibility, user);
      }).toList();

    // 1. Collect all responsibilities for consumer asset
    List<Responsibility> consumerResponsibilities = responsibilitiesDAO.findAllByParams(consumerComponent.getAsset().getAssetId(), null, null, null, null, null);

    // 2. Recursively find all consumers for source(consumer asset)
    // source -> consumer (pair1)
    // source(part1 consumer) -> consumer (pair2)
    // source(part2 consumer) -> consumer (pair3)
    // source(part3 consumer) -> consumer (pair4)
    List<RelationComponent> connectedRelationComponents = relationsDAO.findAllResponsibilityInheritanceRoleHierarchyByAssetId(consumerComponent.getAsset().getAssetId());

    // 3. Copy all responsibilities from consumer to connected assets
    consumerResponsibilities.forEach(_responsibility -> {
      connectedRelationComponents.forEach(relationComponent -> {
        Relation relation = relationComponent.getRelation();
        Responsibility parentResponsibility = _responsibility.getParentResponsibility() != null ? _responsibility.getParentResponsibility() : _responsibility;

        // Check if responsibility already exists
        Responsibility responsibility = findResponsibilityByParams(_responsibility, relationComponent.getAsset(), relation);
        if (responsibility == null) {
          responsibility = createResponsibility(_responsibility, relationComponent.getAsset(), relation, parentResponsibility, user);
        }
      });
    });
  }

  private Responsibility findResponsibilityByParams (Responsibility responsibility, Asset asset, Relation relation) {
    UUID userId = null;
    if (responsibility.getUser() != null) {
      userId = responsibility.getUser().getUserId();
    }

    UUID groupId = null;
    if (responsibility.getGroup() != null) {
      groupId = responsibility.getGroup().getGroupId();
    }

    UUID relationId = relation.getRelationId();

    return responsibilitiesDAO.findResponsibilityByParams(
      userId,
      groupId,
      asset.getAssetId(),
      responsibility.getRole().getRoleId(),
      responsibility.getResponsibleType(),
      relationId
    ).orElse(null);
  }

  private Responsibility createResponsibility (
    Responsibility responsibility,
    Asset asset,
    Relation relation,
    Responsibility parentResponsibility,
    User user
  ) {
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
    createdResponsibility.setParentResponsibility(parentResponsibility);

    return responsibilitiesDAO.saveResponsibility(createdResponsibility);
  }

  private void validateFieldsLength(
    String relationTypeComponentName,
    String relationTypeComponentDescription
  ) throws InvalidFieldLengthException {
    if (
      StringUtils.isNotEmpty(relationTypeComponentName) &&
      relationTypeComponentName.length() > 255
    ) {
      throw new InvalidFieldLengthException("relation_type_component_name", 255);
    }

    if (
      StringUtils.isNotEmpty(relationTypeComponentDescription) &&
      relationTypeComponentDescription.length() > 512
    ) {
      throw new InvalidFieldLengthException("relation_type_component_description", 512);
    }
  }

  private void checkSingleRelationTypeComponentForAssetFlag (
    PatchRelationTypeComponentRequest requestComponent,
    RelationTypeComponent component
  ) throws MultipleRelationExistsWithSameAssetException {
    RelationTypeFlagStatus singleRelationTypeComponentForAssetFlagStatus = getFlagStatus(component.getSingleRelationTypeComponentForAssetFlag(), requestComponent.getSingle_relation_type_component_for_asset_flag());

    if (!singleRelationTypeComponentForAssetFlagStatus.equals(RelationTypeFlagStatus.CHANGED_TO_TRUE)) return;

    Integer count = relationsDAO.countRelationTypeComponentAssets(component.getRelationTypeComponentId());
    if (count == 0) return;

    throw new MultipleRelationExistsWithSameAssetException(component.getRelationTypeComponentId());
  }
}
