package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.StatusRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.groups.GroupRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.groups.GroupNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.ResponsibilityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.exceptions.SourceAssetIsNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.get.GetResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.exceptions.UserNotFoundException;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

public class ResponsibilitiesServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private ResponsibilitiesService responsibilitiesService;

  @Autowired
  private ResponsibilityRepository responsibilityRepository;

  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private GroupRepository groupRepository;
  @Autowired
  private RelationComponentRepository relationComponentRepository;
  @Autowired
  private RelationRepository relationRepository;
  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private StatusRepository statusRepository;

  Role role;
  Role anotherRole;
  Asset asset;
  AssetType assetType;
  Asset anotherAsset;
  AssetType anotherAssetType;
  Group group;
  Group anotherGroup;
  Status lifecycleStatus;
  Status stewardshipStatus;

  @BeforeAll
  public void prepareData () {
    lifecycleStatus = statusRepository.save(new Status("lifecycleStatus status", "new desc", language, user));
    stewardshipStatus = statusRepository.save(new Status("stewardshipStatus status", "new desc", language, user));
    assetType = assetTypeRepository.save(new AssetType("test asset type", "desc", "acr", "color", language, user));
    anotherAssetType = assetTypeRepository.save(new AssetType("another asset type", "desc", "acr", "color", language, user));
    role = roleRepository.save(new Role( "role name", "some desc", language, user));
    anotherRole = roleRepository.save(new Role( "another role name", "some desc", language, user));
    asset = assetRepository.save(new Asset("asset name", assetType, "an", language, lifecycleStatus, null, user));
    anotherAsset = assetRepository.save(new Asset("another asset name", anotherAssetType, "aan", language, null, stewardshipStatus, user));
    group = groupRepository.save(new Group("group name", "group desc", "email", "loop", user));
    anotherGroup = groupRepository.save(new Group("another group name", "group desc", "email", "loop", user));
  }

  @AfterAll
  public void cleatData () {
    roleRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    groupRepository.deleteAll();
    statusRepository.deleteAll();
  }

  @AfterEach
  public void clearResponsibilities () {
    responsibilityRepository.deleteAll();
    relationComponentRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationRepository.deleteAll();
    relationTypeRepository.deleteAll();
  }

  @Test
  public void createResponsibilityAssetNotFoundIntegrationTest () {
    PostResponsibilityRequest request = new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), "GROUP", UUID.randomUUID());

    assertThrows(AssetNotFoundException.class, () -> responsibilitiesService.createResponsibility(request, user));
  }

  @Test
  public void createResponsibilityRoleNotFoundIntegrationTest () {
    PostResponsibilityRequest request = new PostResponsibilityRequest(asset.getAssetId(), UUID.randomUUID(), "GROUP", UUID.randomUUID());

    assertThrows(RoleNotFoundException.class, () -> responsibilitiesService.createResponsibility(request, user));
  }

  @Test
  public void createResponsibilityUserNotFoundIntegrationTest () {
    PostResponsibilityRequest request = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), "USER", UUID.randomUUID());

    assertThrows(UserNotFoundException.class, () -> responsibilitiesService.createResponsibility(request, user));
  }

  @Test
  public void createResponsibilityGroupNotFoundIntegrationTest () {
    PostResponsibilityRequest request = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), "GROUP", UUID.randomUUID());

    assertThrows(GroupNotFoundException.class, () -> responsibilitiesService.createResponsibility(request, user));
  }

  @Test
  public void createResponsibilityWithUserIntegrationTest () {
    PostResponsibilityRequest request = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), "USER", user.getUserId());
    PostResponsibilityResponse response = responsibilitiesService.createResponsibility(request, user);

    assertAll(
      () -> assertEquals(user.getUserId(), response.getResponsible_id()),
      () -> assertEquals(request.getAsset_id(), response.getAsset_id()),
      () -> assertEquals(request.getRole_id(), response.getRole_id())
    );
  }

  @Test
  public void createResponsibilityWithGroupIntegrationTest () {
    PostResponsibilityRequest request = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), "GROUP", group.getGroupId());
    PostResponsibilityResponse response = responsibilitiesService.createResponsibility(request, user);

    assertAll(
      () -> assertEquals(group.getGroupId(), response.getResponsible_id()),
      () -> assertEquals(request.getAsset_id(), response.getAsset_id()),
      () -> assertEquals(request.getRole_id(), response.getRole_id())
    );
  }

  @Test
  public void createResponsibilityResponsibilityAlreadyExistsIntegrationTest () {
    Responsibility responsibility = responsibilityRepository.save(new Responsibility(null, group, asset, role, ResponsibleType.GROUP, user));

    PostResponsibilityRequest request = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), "GROUP", group.getGroupId());

    assertThrows(DataIntegrityViolationException.class, () -> responsibilitiesService.createResponsibility(request, user));
  }

  @Test
  public void createResponsibilityCreateChildResponsibilitiesIntegrationTest () {
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("some name", "desc", 2, true, false, language, user));
    RelationType secondRelationType = relationTypeRepository.save(new RelationType("second relation name", "desc", 2, true, false, language, user));
    RelationType thirdRelationType = relationTypeRepository.save(new RelationType("third relation name", "desc", 2, true, false, language, user));
    RelationTypeComponent firstConsumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first consumer comp", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, firstRelationType, user));
    RelationTypeComponent firstSourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first source comp", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, firstRelationType, user));
    RelationTypeComponent secondConsumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second consumer comp", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, secondRelationType, user));
    RelationTypeComponent secondSourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second source comp", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, secondRelationType, user));
    RelationTypeComponent thirdConsumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("third consumer comp", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, thirdRelationType, user));
    RelationTypeComponent thirdSourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("third source comp", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, thirdRelationType, user));

    Asset secondAsset = assetRepository.save(new Asset("second asset name", null, "an", language, null, null, user));
    Asset thirdAsset = assetRepository.save(new Asset("third asset name", null, "an", language, null, null, user));
    Asset forthAsset = assetRepository.save(new Asset("forth asset name", null, "an", language, null, null, user));
    Asset fifthAsset = assetRepository.save(new Asset("fifth asset name", null, "an", language, null, null, user));

    // source asset(asset) -> consumer asset (secondAsset)
    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    RelationComponent sourceRelationComponent = relationComponentRepository.save(new RelationComponent(relation, firstSourceComponent, asset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent consumerRelationComponent = relationComponentRepository.save(new RelationComponent(relation, firstConsumerComponent, secondAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));

    // source asset(asset) -> consumer asset (fifthAsset)
    Relation forthRelation = relationRepository.save(new Relation(firstRelationType, user));
    RelationComponent forthSourceRelationComponent = relationComponentRepository.save(new RelationComponent(forthRelation, firstSourceComponent, asset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent forthConsumerRelationComponent = relationComponentRepository.save(new RelationComponent(forthRelation, firstConsumerComponent, fifthAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));

    // source asset(secondAsset) -> consumer asset (thirdAsset)
    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));
    RelationComponent secondSourceRelationComponent = relationComponentRepository.save(new RelationComponent(secondRelation, secondSourceComponent, secondAsset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent secondConsumerRelationComponent = relationComponentRepository.save(new RelationComponent(secondRelation, secondConsumerComponent, thirdAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));

    // source asset(thirdAsset) -> consumer asset (forthAsset)
    Relation thirdRelation = relationRepository.save(new Relation(secondRelationType, user));
    RelationComponent thirdSourceRelationComponent = relationComponentRepository.save(new RelationComponent(thirdRelation, thirdSourceComponent, thirdAsset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent thirdConsumerRelationComponent = relationComponentRepository.save(new RelationComponent(thirdRelation, thirdConsumerComponent, forthAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));

    responsibilityRepository.save(new Responsibility(user, null, thirdAsset, role, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(null, anotherGroup, thirdAsset, role, ResponsibleType.GROUP, user));

    PostResponsibilityRequest request = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), "GROUP", group.getGroupId());
    PostResponsibilityResponse response = responsibilitiesService.createResponsibility(request, user);

    List<Responsibility> responsibilities = responsibilityRepository.findAll();
    List<Responsibility> withRelation = responsibilities.stream().filter(r -> r.getRelation() != null && r.getRelation().getRelationId().equals(relation.getRelationId())).toList();
    List<Responsibility> withSecondRelation = responsibilities.stream().filter(r -> r.getRelation() != null && r.getRelation().getRelationId().equals(secondRelation.getRelationId())).toList();
    List<Responsibility> withThirdRelation = responsibilities.stream().filter(r -> r.getRelation() != null && r.getRelation().getRelationId().equals(thirdRelation.getRelationId())).toList();

    assertAll(
      () -> assertEquals(7, responsibilities.size(), "responsibilities size"),
      () -> assertEquals(1, withRelation.size(), "responsibilities with relation"),
      () -> assertEquals(consumerRelationComponent.getAsset().getAssetId(), withRelation.get(0).getAsset().getAssetId(), "consumer relation component in responsibilities with relation"),
      () -> assertEquals(response.getResponsibility_id(), withRelation.get(0).getParentResponsibility().getResponsibilityId(), "responsibility id in responsibilities with relation"),
      () -> assertEquals(1, withSecondRelation.size(), "with second relation responsibilities size"),
      () -> assertEquals(secondConsumerRelationComponent.getAsset().getAssetId(), withSecondRelation.get(0).getAsset().getAssetId(), "second consumer relation component in second relation responsibilities"),
      () -> assertEquals(responsibilities.stream().filter(resp -> resp.getParentResponsibility() != null && resp.getAsset().getAssetId().equals(thirdAsset.getAssetId()) && response.getResponsibility_id() != resp.getParentResponsibility().getResponsibilityId()).findFirst().get().getParentResponsibility().getResponsibilityId(), withSecondRelation.get(0).getParentResponsibility().getResponsibilityId(), "responsibility id in second relation responsibilities"),
      () -> assertEquals(1, withThirdRelation.size(), "with third relation responsibilities size"),
      () -> assertEquals(thirdConsumerRelationComponent.getAsset().getAssetId(), withThirdRelation.get(0).getAsset().getAssetId(), "third consumer relation component in third relation responsibilities"),
      () -> assertEquals(responsibilities.stream().filter(resp -> resp.getParentResponsibility() != null && resp.getAsset().getAssetId().equals(forthAsset.getAssetId()) && response.getResponsibility_id() != resp.getParentResponsibility().getResponsibilityId())
        .findFirst().get().getParentResponsibility().getResponsibilityId(), withThirdRelation.get(0).getParentResponsibility().getResponsibilityId(), "responsibility id in third relation responsibilities")
    );
  }

  @Test
  public void createResponsibilitiesBulkSomeRequiredFieldsAreEmptyExceptionIntegrationTest () {
    PostResponsibilityRequest firstRequest = new PostResponsibilityRequest(null, UUID.randomUUID(), "GROUP", UUID.randomUUID());
    PostResponsibilityRequest secondRequest = new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), "GROUP", UUID.randomUUID());

    assertThrows(SomeRequiredFieldsAreEmptyException.class, () -> responsibilitiesService.createResponsibilitiesBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createResponsibilitiesBulkAssetNotFoundExceptionIntegrationTest () {
    PostResponsibilityRequest firstRequest = new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), "GROUP", UUID.randomUUID());
    PostResponsibilityRequest secondRequest = new PostResponsibilityRequest(UUID.randomUUID(), UUID.randomUUID(), "GROUP", UUID.randomUUID());

    assertThrows(AssetNotFoundException.class, () -> responsibilitiesService.createResponsibilitiesBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createResponsibilitiesBulkSourceAssetIsNotAllowedExceptionIntegrationTest () {
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("some name", "desc", 2, true, false, language, user));

    RelationTypeComponent firstConsumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first consumer comp", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, firstRelationType, user));
    RelationTypeComponent firstSourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first source comp", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, firstRelationType, user));

    // source asset(asset) -> consumer asset (secondAsset)
    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    RelationComponent sourceRelationComponent = relationComponentRepository.save(new RelationComponent(relation, firstSourceComponent, asset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent consumerRelationComponent = relationComponentRepository.save(new RelationComponent(relation, firstConsumerComponent, anotherAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));

    PostResponsibilityRequest firstRequest = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), "GROUP", user.getUserId());
    PostResponsibilityRequest secondRequest = new PostResponsibilityRequest(anotherAsset.getAssetId(), role.getRoleId(), "GROUP", user.getUserId());


    assertThrows(SourceAssetIsNotAllowedException.class, () -> responsibilitiesService.createResponsibilitiesBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createResponsibilitiesBulkUserNotFoundExceptionIntegrationTest () {
    PostResponsibilityRequest firstRequest = new PostResponsibilityRequest(asset.getAssetId(), UUID.randomUUID(), "USER", UUID.randomUUID());
    PostResponsibilityRequest secondRequest = new PostResponsibilityRequest(anotherAsset.getAssetId(), UUID.randomUUID(), "USER", UUID.randomUUID());

    assertThrows(UserNotFoundException.class, () -> responsibilitiesService.createResponsibilitiesBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createResponsibilitiesBulkRoleNotFoundExceptionIntegrationTest () {
    PostResponsibilityRequest firstRequest = new PostResponsibilityRequest(asset.getAssetId(), UUID.randomUUID(), "USER", user.getUserId());
    PostResponsibilityRequest secondRequest = new PostResponsibilityRequest(anotherAsset.getAssetId(), UUID.randomUUID(), "USER", user.getUserId());

    assertThrows(RoleNotFoundException.class, () -> responsibilitiesService.createResponsibilitiesBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createResponsibilitiesBulkGroupNotFoundExceptionIntegrationTest () {
    PostResponsibilityRequest firstRequest = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), "GROUP", UUID.randomUUID());
    PostResponsibilityRequest secondRequest = new PostResponsibilityRequest(anotherAsset.getAssetId(), role.getRoleId(), "GROUP", UUID.randomUUID());

    assertThrows(GroupNotFoundException.class, () -> responsibilitiesService.createResponsibilitiesBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createResponsibilitiesBulkResponsibilityAlreadyExistsExceptionIntegrationTest () {
    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));

    PostResponsibilityRequest firstRequest = new PostResponsibilityRequest(asset.getAssetId(), role.getRoleId(), "USER", user.getUserId());
    PostResponsibilityRequest secondRequest = new PostResponsibilityRequest(anotherAsset.getAssetId(), role.getRoleId(), "USER", user.getUserId());

    assertThrows(DataIntegrityViolationException.class, () -> responsibilitiesService.createResponsibilitiesBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void getResponsibilityByIdResponsibilityNotFoundIntegrationTest () {
    assertThrows(ResponsibilityNotFoundException.class, () -> responsibilitiesService.getResponsibilityById(UUID.randomUUID()));
  }

  @Test
  public void getResponsibilityByIdSuccessIntegrationTest () {
    Responsibility responsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));

    GetResponsibilityResponse response = responsibilitiesService.getResponsibilityById(responsibility.getResponsibilityId());

    assertAll(
      () -> assertEquals(asset.getAssetId(), response.getAsset_id()),
      () -> assertEquals(asset.getAssetName(), response.getAsset_name()),
      () -> assertEquals(user.getUserId(), response.getResponsible_id()),
      () -> assertEquals(user.getUsername(), response.getResponsible_name()),
      () -> assertEquals(role.getRoleId(), response.getRole_id()),
      () -> assertEquals(role.getRoleName(), response.getRole_name())
    );
  }

  @Test
  public void getResponsibilitiesByParamsPaginationIntegrationTest () {
    generateResponsibilities(130);

    assertAll(
      () -> assertEquals(50, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, null, null, null, null,null, null, 3, 50).getResults().size()),
      () -> assertEquals(100, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, null, null, null, null,null, null, 0, 140).getResults().size()),
      () -> assertEquals(130, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, null, null, null, null,null, null, 0, 50).getTotal())
    );
  }

  @Test
  public void getResponsibilitiesByParamsIntegrationTest () {
    Responsibility baseUserResponsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));
    Responsibility baseGroupResponsibility = responsibilityRepository.save(new Responsibility(null, group, asset, role, ResponsibleType.GROUP, user));
    Responsibility anotherGroupResponsibility = responsibilityRepository.save(new Responsibility(null, anotherGroup, anotherAsset, anotherRole, ResponsibleType.GROUP, user));
    Responsibility anotherUserResponsibility = responsibilityRepository.save(new Responsibility(user, null, anotherAsset, anotherRole, ResponsibleType.USER, user));
    Responsibility anotherGroupWithBaseAssetResponsibility = responsibilityRepository.save(new Responsibility(null, anotherGroup, asset, anotherRole, ResponsibleType.GROUP, user));
    Responsibility anotherGroupWithBaseRoleResponsibility = responsibilityRepository.save(new Responsibility(null, anotherGroup, anotherAsset, role, ResponsibleType.GROUP, user));

    Responsibility parentResponsibility = responsibilityRepository.save(new Responsibility(user, null, anotherAsset, role, ResponsibleType.USER, user));
    Responsibility inheritedResponsibility = new Responsibility(null, group, anotherAsset, anotherRole, ResponsibleType.GROUP, user);
    inheritedResponsibility.setInheritedFlag(true);
    inheritedResponsibility.setParentResponsibility(parentResponsibility);
    responsibilityRepository.save(inheritedResponsibility);

    assertAll(
      () -> assertEquals(3, responsibilitiesService.getResponsibilitiesByParams(List.of(asset.getAssetId()), null, null, null, null, null, null, null,null, null, 0, 50).getResults().size()),
      () -> assertEquals(4, responsibilitiesService.getResponsibilitiesByParams(null, List.of(role.getRoleId()), null, null, null, null, null, null,null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, responsibilitiesService.getResponsibilitiesByParams(null, List.of(anotherRole.getRoleId()), null, null, null, null, null,false, null, null, 0, 50).getResults().size()),
      () -> assertEquals(1, responsibilitiesService.getResponsibilitiesByParams(null, List.of(anotherRole.getRoleId()), null, null, null, null, null,true, null, null, 0, 50).getResults().size()),
      () -> assertEquals(4, responsibilitiesService.getResponsibilitiesByParams(null, List.of(role.getRoleId()), null, null, null, null, null, null,null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, responsibilitiesService.getResponsibilitiesByParams(null, null, List.of(user.getUserId()), null, null, null, null, null,null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, responsibilitiesService.getResponsibilitiesByParams(null, null, List.of(user.getUserId()), List.of(anotherGroup.getGroupId()), null, null, null, null,null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, responsibilitiesService.getResponsibilitiesByParams(null, null, null, List.of(anotherGroup.getGroupId()), null, null, null, null,null, null, 0, 50).getResults().size()),
      () -> assertEquals(8, responsibilitiesService.getResponsibilitiesByParams(null, List.of(anotherRole.getRoleId(), role.getRoleId()), null, null, null, null, null, null,null, null, 0, 50).getResults().size()),
      () -> assertEquals(1, responsibilitiesService.getResponsibilitiesByParams(null, List.of(anotherRole.getRoleId(), role.getRoleId()), null, null, null, null, null,true, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, List.of(assetType.getAssetTypeId()), null, null,true, null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, List.of(assetType.getAssetTypeId()), null, null,null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(8, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, List.of(assetType.getAssetTypeId(), anotherAssetType.getAssetTypeId()), null, null,null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(5, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, null, null, List.of(stewardshipStatus.getStatusId()),null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, null, List.of(stewardshipStatus.getStatusId()), null,null, null, null,0, 50).getResults().size()),
      () -> assertEquals(3, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, null, List.of(lifecycleStatus.getStatusId()), null,null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(3, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, List.of(assetType.getAssetTypeId()), List.of(lifecycleStatus.getStatusId()), null,null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(0, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, List.of(anotherAssetType.getAssetTypeId()), List.of(lifecycleStatus.getStatusId()), null,null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(5, responsibilitiesService.getResponsibilitiesByParams(null, null, null, null, List.of(anotherAssetType.getAssetTypeId()), null, List.of(stewardshipStatus.getStatusId()),null, null, null, 0, 50).getResults().size())
    );
  }

  @Test
  public void deleteResponsibilityByIdResponsibilityNotFoundIntegrationTest () {
    assertThrows(ResponsibilityNotFoundException.class, () -> responsibilitiesService.deleteResponsibilityById(UUID.randomUUID(), user));
  }

  @Test
  public void deleteResponsibilityByIdResponsibilityAlreadyDeletedIntegrationTest () {
    Responsibility baseUserResponsibility = new Responsibility(user, null, asset, role, ResponsibleType.USER, user);
    baseUserResponsibility.setIsDeleted(true);
    responsibilityRepository.save(baseUserResponsibility);

    assertThrows(ResponsibilityNotFoundException.class, () -> responsibilitiesService.deleteResponsibilityById(baseUserResponsibility.getResponsibilityId(), user));
  }

  @Test
  public void deleteResponsibilityByIdResponsibilityIsInheritedIntegrationTest () {
    Responsibility baseUserResponsibility = new Responsibility(user, null, asset, role, ResponsibleType.USER, user);
    baseUserResponsibility.setInheritedFlag(true);
    responsibilityRepository.save(baseUserResponsibility);

    assertThrows(ResponsibilityIsInheritedException.class, () -> responsibilitiesService.deleteResponsibilityById(baseUserResponsibility.getResponsibilityId(), user));
  }

  @Test
  public void deleteResponsibilityByIdDeleteInheritedIntegrationTest () {
    Responsibility parentResponsibility = responsibilityRepository.save(new Responsibility(user, null, anotherAsset, role, ResponsibleType.USER, user));
    Responsibility someResponsibility = responsibilityRepository.save(new Responsibility(user, null, anotherAsset, anotherRole, ResponsibleType.USER, user));
    Responsibility inheritedResponsibility = new Responsibility(null, group, anotherAsset, anotherRole, ResponsibleType.GROUP, user);
    inheritedResponsibility.setInheritedFlag(true);
    inheritedResponsibility.setParentResponsibility(parentResponsibility);
    responsibilityRepository.save(inheritedResponsibility);

    Responsibility secondInheritedResponsibility = new Responsibility(null, group, asset, anotherRole, ResponsibleType.GROUP, user);
    secondInheritedResponsibility.setInheritedFlag(true);
    secondInheritedResponsibility.setParentResponsibility(inheritedResponsibility);
    responsibilityRepository.save(secondInheritedResponsibility);

    responsibilitiesService.deleteResponsibilityById(parentResponsibility.getResponsibilityId(), user);

    Optional<Responsibility> foundInheritedResponsibility = responsibilityRepository.findById(inheritedResponsibility.getResponsibilityId());
    Optional<Responsibility> foundSecondInheritedResponsibility = responsibilityRepository.findById(secondInheritedResponsibility.getResponsibilityId());
    Optional<Responsibility> foundSomeResponsibility = responsibilityRepository.findById(someResponsibility.getResponsibilityId());

    assertAll(
      () -> assertTrue(foundInheritedResponsibility.get().getIsDeleted()),
      () -> assertNotNull(foundInheritedResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), foundInheritedResponsibility.get().getDeletedBy().getUserId()),
      () -> assertFalse(foundSomeResponsibility.get().getIsDeleted()),
      () -> assertTrue(foundSecondInheritedResponsibility.get().getIsDeleted()),
      () -> assertNotNull(foundSecondInheritedResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), foundSecondInheritedResponsibility.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteResponsibilityByIdSuccessIntegrationTest () {
    Responsibility baseUserResponsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));

    responsibilitiesService.deleteResponsibilityById(baseUserResponsibility.getResponsibilityId(), user);

    Optional<Responsibility> deletedResponsibility = responsibilityRepository.findById(baseUserResponsibility.getResponsibilityId());

    assertAll(
      () -> assertTrue(deletedResponsibility.get().getIsDeleted()),
      () -> assertNotNull(deletedResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedResponsibility.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteResponsibilitiesBulkDuplicateResponsibilityIdIntegrationTest () {
    Responsibility baseUserResponsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));

    assertThrows(DuplicateValueInRequestException.class, () -> responsibilitiesService.deleteResponsibilitiesBulk(List.of(baseUserResponsibility.getResponsibilityId(), baseUserResponsibility.getResponsibilityId()), user));
  }

  @Test
  public void deleteResponsibilitiesBulkResponsibilityNotFoundIntegrationTest () {
    assertThrows(ResponsibilityNotFoundException.class, () -> responsibilitiesService.deleteResponsibilitiesBulk(List.of(UUID.randomUUID(), UUID.randomUUID()), user));
  }

  @Test
  public void deleteResponsibilitiesBulkResponsibilityIsInheritedIntegrationTest () {
    Responsibility parentResponsibility = responsibilityRepository.save(new Responsibility(user, null, anotherAsset, role, ResponsibleType.USER, user));
    Responsibility inheritedResponsibility = new Responsibility(null, group, anotherAsset, anotherRole, ResponsibleType.GROUP, user);
    inheritedResponsibility.setInheritedFlag(true);
    inheritedResponsibility.setParentResponsibility(parentResponsibility);
    responsibilityRepository.save(inheritedResponsibility);

    Responsibility secondInheritedResponsibility = new Responsibility(null, group, asset, anotherRole, ResponsibleType.GROUP, user);
    secondInheritedResponsibility.setInheritedFlag(true);
    secondInheritedResponsibility.setParentResponsibility(inheritedResponsibility);
    responsibilityRepository.save(secondInheritedResponsibility);

    assertThrows(ResponsibilityIsInheritedException.class, () -> responsibilitiesService.deleteResponsibilitiesBulk(List.of(inheritedResponsibility.getResponsibilityId(), secondInheritedResponsibility.getResponsibilityId()), user));
  }

  @Test
  public void deleteResponsibilitiesBulkSourceAssetIsNotAllowedIntegrationTest () {
    Responsibility firstSourceResponsibility = responsibilityRepository.save(new Responsibility(null, group, anotherAsset, anotherRole, ResponsibleType.GROUP, user));
    Responsibility secondSourceResponsibility = responsibilityRepository.save(new Responsibility(null, group, asset, anotherRole, ResponsibleType.GROUP, user));

    RelationType firstRelationType = relationTypeRepository.save(new RelationType("some name", "desc", 2, true, false, language, user));
    RelationType secondRelationType = relationTypeRepository.save(new RelationType("second relation name", "desc", 2, true, false, language, user));

    RelationTypeComponent firstConsumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first consumer comp", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, firstRelationType, user));
    RelationTypeComponent firstSourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first source comp", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, firstRelationType, user));

    // source asset(asset) -> consumer asset (secondAsset)
    Relation relation = relationRepository.save(new Relation(firstRelationType, user));
    RelationComponent sourceRelationComponent = relationComponentRepository.save(new RelationComponent(relation, firstSourceComponent, asset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent consumerRelationComponent = relationComponentRepository.save(new RelationComponent(relation, firstConsumerComponent, anotherAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));

    assertThrows(SourceAssetIsNotAllowedException.class, () -> responsibilitiesService.deleteResponsibilitiesBulk(List.of(firstSourceResponsibility.getResponsibilityId(), secondSourceResponsibility.getResponsibilityId()), user));
  }

  @Test
  public void deleteResponsibilityBulkDeleteInheritedIntegrationTest () {
    Responsibility parentResponsibility = responsibilityRepository.save(new Responsibility(user, null, anotherAsset, role, ResponsibleType.USER, user));
    Responsibility someResponsibility = responsibilityRepository.save(new Responsibility(user, null, anotherAsset, anotherRole, ResponsibleType.USER, user));
    Responsibility inheritedResponsibility = new Responsibility(null, group, anotherAsset, anotherRole, ResponsibleType.GROUP, user);
    inheritedResponsibility.setInheritedFlag(true);
    inheritedResponsibility.setParentResponsibility(parentResponsibility);
    responsibilityRepository.save(inheritedResponsibility);

    Responsibility secondInheritedResponsibility = new Responsibility(null, group, asset, anotherRole, ResponsibleType.GROUP, user);
    secondInheritedResponsibility.setInheritedFlag(true);
    secondInheritedResponsibility.setParentResponsibility(inheritedResponsibility);
    responsibilityRepository.save(secondInheritedResponsibility);

    responsibilitiesService.deleteResponsibilitiesBulk(List.of(parentResponsibility.getResponsibilityId(), someResponsibility.getResponsibilityId()), user);

    Optional<Responsibility> foundInheritedResponsibility = responsibilityRepository.findById(inheritedResponsibility.getResponsibilityId());
    Optional<Responsibility> foundSecondInheritedResponsibility = responsibilityRepository.findById(secondInheritedResponsibility.getResponsibilityId());
    Optional<Responsibility> foundSomeResponsibility = responsibilityRepository.findById(someResponsibility.getResponsibilityId());

    assertAll(
      () -> assertTrue(foundInheritedResponsibility.get().getIsDeleted()),
      () -> assertNotNull(foundInheritedResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), foundInheritedResponsibility.get().getDeletedBy().getUserId()),
      () -> assertTrue(foundSomeResponsibility.get().getIsDeleted()),
      () -> assertNotNull(foundSomeResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), foundSomeResponsibility.get().getDeletedBy().getUserId()),
      () -> assertTrue(foundSecondInheritedResponsibility.get().getIsDeleted()),
      () -> assertNotNull(foundSecondInheritedResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), foundSecondInheritedResponsibility.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteResponsibilitiesBulkSuccessIntegrationTest () {
    Responsibility baseUserResponsibility = responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));
    Responsibility baseGroupResponsibility = responsibilityRepository.save(new Responsibility(null, group, asset, role, ResponsibleType.GROUP, user));

    responsibilitiesService.deleteResponsibilitiesBulk(List.of(baseUserResponsibility.getResponsibilityId(), baseGroupResponsibility.getResponsibilityId()), user);

    Optional<Responsibility> deletedBaseUserResponsibility = responsibilityRepository.findById(baseUserResponsibility.getResponsibilityId());
    Optional<Responsibility> deletedBaseGroupResponsibility = responsibilityRepository.findById(baseGroupResponsibility.getResponsibilityId());

    assertAll(
      () -> assertTrue(deletedBaseUserResponsibility.get().getIsDeleted()),
      () -> assertNotNull(deletedBaseUserResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedBaseUserResponsibility.get().getDeletedBy().getUserId()),
      () -> assertTrue(deletedBaseGroupResponsibility.get().getIsDeleted()),
      () -> assertNotNull(deletedBaseGroupResponsibility.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedBaseGroupResponsibility.get().getDeletedBy().getUserId())
    );
  }

  private void generateResponsibilities (int count) {
    for (int i = 0; i < count; i++) {
      Asset asset = assetRepository.save(new Asset("asset name_"+ i, null, "an_"+i, language, null, null, user));
      responsibilityRepository.save(new Responsibility(user, null, asset, role, ResponsibleType.USER, user));
    }
  }
}
