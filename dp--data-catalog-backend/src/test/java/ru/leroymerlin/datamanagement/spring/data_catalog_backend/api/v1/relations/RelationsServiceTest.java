package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.DuplicateValueInRequestException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHierarchy.AssetHierarchyRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.RelationTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibleType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.RelationAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes.RelationComponentAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationComponentWithAttributesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.get.GetRelationResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsResponse;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

public class RelationsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private RelationsService relationsService;

  @Autowired
  private RelationRepository relationRepository;

  @Autowired
  private RelationComponentRepository relationComponentRepository;

  @Autowired
  private AssetHierarchyRepository assetHierarchyRepository;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private RelationAttributeRepository relationAttributeRepository;
  @Autowired
  private RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository;
  @Autowired
  private RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository;
  @Autowired
  private RelationComponentAttributeRepository relationComponentAttributeRepository;
  @Autowired
  private ResponsibilityRepository responsibilityRepository;
  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;

  private Role role;
  private Asset firstAsset;
  private Asset secondAsset;
  private Asset thirdAsset;
  private AssetType assetTypeOne;
  private AssetType assetTypeTwo;
  private AssetType assetTypeThree;
  private RelationType relationTypeWith2Components;
  private RelationTypeComponent relationTypeComponentWithParentHierarchyRole;
  private RelationTypeComponent relationTypeComponentWithChildHierarchyRole;
  private RelationType relationTypeWith3Components;
  private RelationTypeComponent firstRelationTypeComponentWithoutRoles;
  private RelationTypeComponent secondRelationTypeComponentWithoutRoles;
  private RelationTypeComponent thirdRelationTypeComponentWithoutRoles;

  @AfterEach
  public void clearData () {
    relationTypeAttributeTypeAssignmentRepository.deleteAll();
    relationTypeComponentAttributeTypeAssignmentRepository.deleteAll();
    assetHierarchyRepository.deleteAll();
    responsibilityRepository.deleteAll();
    relationAttributeRepository.deleteAll();
    relationComponentAttributeRepository.deleteAll();
    relationRepository.deleteAll();
    relationComponentRepository.deleteAll();
    relationTypeComponentAssetTypeAssignmentRepository.deleteAll();
    roleRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
  }

  public void prepareData () {
    role = roleRepository.save(new Role("role name", "desc", language, user));

    assetTypeOne = assetTypeRepository.save(new AssetType("first asset type", "first asset Type description", "fat", "red", language, user));
    assetTypeTwo = assetTypeRepository.save(new AssetType("second asset type", "second asset Type description", "sat", "blue", language, user));
    assetTypeThree = assetTypeRepository.save(new AssetType("third asset type", "third asset Type description", "sat", "blue", language, user));

    firstAsset = assetRepository.save(new Asset("name was", assetTypeOne, "displayed name", language, null, null, user));
    secondAsset = assetRepository.save(new Asset("second asset was", assetTypeTwo, "displayed name", language, null, null, user));
    thirdAsset = assetRepository.save(new Asset("third asset was", assetTypeThree, "displayed name", language, null, null, user));

    relationTypeWith2Components = relationTypeRepository.save(new RelationType("name", "description", 2, false, true, language, user));
    relationTypeComponentWithChildHierarchyRole = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, HierarchyRole.CHILD, false, language, relationTypeWith2Components, user));
    relationTypeComponentWithParentHierarchyRole = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, HierarchyRole.PARENT, false, language, relationTypeWith2Components, user));

    relationTypeWith3Components = relationTypeRepository.save(new RelationType("relation with 3 components", "has 3 components", 3, false, false, language, user));
    firstRelationTypeComponentWithoutRoles = relationTypeComponentRepository.save(new RelationTypeComponent("child component with not roles 1", "desc", null, null, false, language, relationTypeWith3Components, user));
    secondRelationTypeComponentWithoutRoles = relationTypeComponentRepository.save(new RelationTypeComponent("child component with not roles 2", "desc", null, null, false, language, relationTypeWith3Components, user));
    thirdRelationTypeComponentWithoutRoles = relationTypeComponentRepository.save(new RelationTypeComponent("child component with not roles 3", "desc", null, null, false, language, relationTypeWith3Components, user));
  }

  public void prepareAssignments () {
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponentWithChildHierarchyRole, assetTypeOne, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponentWithParentHierarchyRole, assetTypeTwo, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponentWithParentHierarchyRole, assetTypeOne, false, null, user));
  }

  public void prepareAssignmentsForNoRolesComponents () {
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstRelationTypeComponentWithoutRoles, assetTypeOne, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondRelationTypeComponentWithoutRoles, assetTypeTwo, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(thirdRelationTypeComponentWithoutRoles, assetTypeThree, false, null, user));
  }

  public void prepareResponsibilities () {
    responsibilityRepository.save(new Responsibility(user, null , firstAsset, role, ResponsibleType.USER, user));
  }

  @Test
  public void createRelationsInvalidRelationTypeIdIntegrationTest () {
    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest("123", "123"));
    relations.add(new PostRelationRequest("1235", "657"));
    PostRelationsRequest request = new PostRelationsRequest("123", relations);

    assertThrows(IllegalArgumentException.class, () -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsRelationTypeNotFoundIntegrationTest () {
    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest("123", "123"));
    relations.add(new PostRelationRequest("1235", "657"));
    PostRelationsRequest request = new PostRelationsRequest(UUID.randomUUID().toString(), relations);

    assertThrows(RelationTypeNotFoundException.class, () -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsInvalidNumberOfComponentsIntegrationTest () {
    prepareData();

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest("123", "123"));
    relations.add(new PostRelationRequest("1235", "657"));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith3Components.getRelationTypeId().toString(), relations);

    assertThrows(InvalidNumberOfComponentsException.class, () -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsHasIdenticalComponentsIntegrationTest () {
    prepareData();

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest("123", relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest("1235", relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    assertThrows(InvalidNumberOfComponentsException.class, () -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsHasIdenticalAssetsIntegrationTest () {
    prepareData();

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    assertThrows(RelationTypeDoesNotAllowedRelatedAssetException.class, () -> relationsService.createRelations(request, user));

    prepareAssignments();

    relationTypeWith2Components.setSelfRelatedFlag(true);
    relationTypeRepository.save(relationTypeWith2Components);
    assertDoesNotThrow(() -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsInvalidAssetIdIntegrationTest () {
    prepareData();

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest("123", relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest("1235", relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    assertThrows(IllegalArgumentException.class, () -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsInvalidRelationTypeComponentIdIntegrationTest () {
    prepareData();

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), "123"));
    relations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    assertThrows(IllegalArgumentException.class, () -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsInvalidAssetTypeForComponentIntegrationTest () {
    prepareData();

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    assertThrows(InvalidAssetTypeForComponentException.class, () -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsInvalidHierarchyBetweenAssetsIntegrationTest () {
    prepareData();
    prepareAssignments();

    assetHierarchyRepository.save(new AssetHierarchy(firstAsset, secondAsset, null, user));

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    assertThrows(InvalidHierarchyBetweenAssetsException.class, () -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsRelationAlreadyExistsIntegrationTest () {
    prepareData();
    prepareAssignments();

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    PostRelationsResponse response = relationsService.createRelations(request, user);

    assertThrows(RelationAlreadyExistsException.class, () -> relationsService.createRelations(request, user));

    relationTypeWith2Components.setUniquenessFlag(false);
    relationTypeRepository.save(relationTypeWith2Components);

    assertDoesNotThrow(() -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsSuccessIntegrationTest () {
    prepareData();
    prepareAssignments();

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    PostRelationsResponse response = relationsService.createRelations(request, user);

    assertAll(
      () -> assertEquals(2, response.getComponent().size()),
      () -> assertEquals(relationTypeWith2Components.getRelationTypeId(), response.getRelation_type_id())
    );
  }

  @Test
  public void createRelationsComponentsWithHierarchyRoleIntegrationTest () {
    prepareData();
    prepareAssignments();

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    PostRelationsResponse response = relationsService.createRelations(request, user);

    List<Responsibility> responsibilitiesByRelationId = responsibilityRepository.findAllByRelationIds(List.of(response.getRelation_id()));
    List<AssetHierarchy> assetHierarchiesByRelationId = assetHierarchyRepository.findAllByRelationIds(List.of(response.getRelation_id()));

    assertAll(
      () -> assertEquals(0, responsibilitiesByRelationId.size()),
      () -> assertEquals(1, assetHierarchiesByRelationId.size()),
      () -> assertEquals(firstAsset.getAssetId(), assetHierarchiesByRelationId.get(0).getChildAsset().getAssetId()),
      () -> assertEquals(secondAsset.getAssetId(), assetHierarchiesByRelationId.get(0).getParentAsset().getAssetId())
    );
  }

  @Test
  public void createRelationsComponentsWithResponsibilityInheritanceRoleIntegrationTest () {
    prepareData();
    prepareAssignments();
    prepareResponsibilities();

    relationTypeWith2Components.setResponsibilityInheritanceFlag(true);
    relationTypeRepository.save(relationTypeWith2Components);

    relationTypeComponentWithChildHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.SOURCE);
    relationTypeComponentRepository.save(relationTypeComponentWithChildHierarchyRole);
    relationTypeComponentWithParentHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.CONSUMER);
    relationTypeComponentRepository.save(relationTypeComponentWithParentHierarchyRole);

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    PostRelationsResponse response = relationsService.createRelations(request, user);

    List<Responsibility> responsibilitiesByRelationId = responsibilityRepository.findAllByRelationIds(List.of(response.getRelation_id()));

    assertAll(
      () -> assertEquals(1, responsibilitiesByRelationId.size()),
      () -> assertTrue(responsibilitiesByRelationId.get(0).getInheritedFlag()),
      () -> assertEquals(response.getRelation_id(), responsibilitiesByRelationId.get(0).getRelation().getRelationId())
    );
  }

  @Test
  public void createRelationsWithNoRolesIntegrationTest () {
    prepareData();
    prepareAssignmentsForNoRolesComponents();

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), secondRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), thirdRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith3Components.getRelationTypeId().toString(), relations);

    PostRelationsResponse response = relationsService.createRelations(request, user);

    assertAll(
      () -> assertEquals(3, response.getComponent().size()),
      () -> assertEquals(relationTypeWith3Components.getRelationTypeId(), response.getRelation_type_id())
    );
  }

  @Test
  public void createRelationsComponentsWithResponsibilityInheritanceRoleAndConsumerComponentsIntegrationTest () {
    prepareData();
    prepareAssignments();
    prepareResponsibilities();

    relationTypeWith2Components.setResponsibilityInheritanceFlag(true);
    relationTypeRepository.save(relationTypeWith2Components);

    relationTypeComponentWithChildHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.SOURCE);
    relationTypeComponentRepository.save(relationTypeComponentWithChildHierarchyRole);
    relationTypeComponentWithParentHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.CONSUMER);
    relationTypeComponentRepository.save(relationTypeComponentWithParentHierarchyRole);

    RelationType relationTypeWith2InheritanceComponents = relationTypeRepository.save(new RelationType("some new relation type", "description", 2, true, false, language, user));
    RelationTypeComponent relationTypeComponentWithSourceInheritanceRole = relationTypeComponentRepository.save(new RelationTypeComponent("source component", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, relationTypeWith2InheritanceComponents, user));
    RelationTypeComponent relationTypeComponentWithConsumerInheritanceRole = relationTypeComponentRepository.save(new RelationTypeComponent("consumer name", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, relationTypeWith2InheritanceComponents, user));

    Relation relation = relationRepository.save(new Relation(relationTypeWith2InheritanceComponents, user));
    RelationComponent relationComponentSource = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponentWithConsumerInheritanceRole, secondAsset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent relationComponentConsumer = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponentWithSourceInheritanceRole, thirdAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    PostRelationsResponse response = relationsService.createRelations(request, user);

    List<Responsibility> responsibilities = responsibilityRepository.findAllByRelationIds(List.of(relation.getRelationId()));

    assertAll(
      () -> assertEquals(1, responsibilities.size()),
      () -> assertTrue(responsibilities.get(0).getInheritedFlag()),
      () -> assertEquals(relation.getRelationId(), responsibilities.get(0).getRelation().getRelationId())
    );
  }

  @Test
  public void createRelationsComponents_sameAssetForRelationTypeComponentIsNotAllowed_IntegrationTest () {
    prepareData();
    prepareAssignments();
    prepareResponsibilities();

    relationTypeComponentWithChildHierarchyRole.setSingleRelationTypeComponentForAssetFlag(true);
    relationTypeComponentWithChildHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.SOURCE);
    relationTypeComponentRepository.save(relationTypeComponentWithChildHierarchyRole);
    relationTypeComponentWithParentHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.CONSUMER);
    relationTypeComponentRepository.save(relationTypeComponentWithParentHierarchyRole);

    Relation relation = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentSource = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponentWithChildHierarchyRole, firstAsset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent relationComponentConsumer = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponentWithParentHierarchyRole, firstAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));
    Relation secondRelation = relationRepository.save(new Relation(relationTypeWith3Components, user));
    RelationComponent secondRelationComponentSource = relationComponentRepository.save(new RelationComponent(secondRelation, relationTypeComponentWithChildHierarchyRole, firstAsset, null, null, user));

    ArrayList<PostRelationRequest> relations = new ArrayList<>();
    relations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    relations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest request = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), relations);

    assertThrows(RelationTypeComponentWithSameAssetException.class, () -> relationsService.createRelations(request, user));
  }

  @Test
  public void createRelationsBulkInvalidRelationTypeIdIntegrationTest () {
    prepareData();

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest("123", "123"));
    firstRelations.add(new PostRelationRequest("1235", "657"));
    PostRelationsRequest firstRequest = new PostRelationsRequest("123", firstRelations);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    assertThrows(IllegalArgumentException.class, () -> relationsService.createRelationsBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createRelationsBulkSomeOfRequiredFieldsAreEmptyTest () {
    prepareData();

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest(null, relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(relationTypeWith3Components.getRelationTypeId().toString(), firstRelations);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    assertThrows(SomeRequiredFieldsAreEmptyException.class, () -> relationsService.createRelationsBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createRelationsBulkDuplicateValueInRequestTest () {
    prepareData();

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), firstRelations);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    assertThrows(DuplicateValueInRequestException.class, () -> relationsService.createRelationsBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createRelationsBulkRelationTypeNotFoundTest () {
    prepareData();

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), secondRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), thirdRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(UUID.randomUUID().toString(), firstRelations);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    assertThrows(RelationTypeNotFoundException.class, () -> relationsService.createRelationsBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createRelationsBulkRelationsAlreadyExistsTest () {
    prepareData();
    prepareAssignmentsForNoRolesComponents();

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), secondRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), thirdRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(relationTypeWith3Components.getRelationTypeId().toString(), firstRelations);

    relationsService.createRelations(firstRequest, user);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    assertThrows(RelationAlreadyExistsException.class, () -> relationsService.createRelationsBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createRelationsBulkInvalidAssetTypeForComponentTest () {
    prepareData();

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), secondRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), thirdRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(relationTypeWith3Components.getRelationTypeId().toString(), firstRelations);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    assertThrows(InvalidAssetTypeForComponentException.class, () -> relationsService.createRelationsBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createRelationsBulkInvalidNumberOfComponentsTest () {
    prepareData();
    prepareAssignments();
    prepareAssignmentsForNoRolesComponents();

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), secondRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(relationTypeWith3Components.getRelationTypeId().toString(), firstRelations);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    assertThrows(InvalidNumberOfComponentsException.class, () -> relationsService.createRelationsBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void createRelationsBulkWithComponentsWithHierarchyRoleIntegrationTest () {
    prepareData();
    prepareAssignments();
    prepareAssignmentsForNoRolesComponents();

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), secondRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), thirdRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(relationTypeWith3Components.getRelationTypeId().toString(), firstRelations);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    List<PostRelationsResponse> response = relationsService.createRelationsBulk(List.of(firstRequest, secondRequest), user);

    List<Responsibility> responsibilitiesByRelationId = responsibilityRepository.findAllByRelationIds(response.stream().map(PostRelationsResponse::getRelation_id).toList());
    List<AssetHierarchy> assetHierarchiesByRelationId = assetHierarchyRepository.findAllByRelationIds(response.stream().map(PostRelationsResponse::getRelation_id).toList());

    assertAll(
      () -> assertEquals(0, responsibilitiesByRelationId.size()),
      () -> assertEquals(1, assetHierarchiesByRelationId.size()),
      () -> assertEquals(firstAsset.getAssetId(), assetHierarchiesByRelationId.get(0).getChildAsset().getAssetId()),
      () -> assertEquals(secondAsset.getAssetId(), assetHierarchiesByRelationId.get(0).getParentAsset().getAssetId())
    );
  }

  @Test
  public void createRelationsBulk_ComponentsWithResponsibilityInheritanceRole_IntegrationTest () {
    prepareData();
    prepareAssignments();
    prepareResponsibilities();
    prepareAssignmentsForNoRolesComponents();

    relationTypeWith2Components.setResponsibilityInheritanceFlag(true);
    relationTypeRepository.save(relationTypeWith2Components);

    relationTypeComponentWithChildHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.SOURCE);
    relationTypeComponentRepository.save(relationTypeComponentWithChildHierarchyRole);
    relationTypeComponentWithParentHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.CONSUMER);
    relationTypeComponentRepository.save(relationTypeComponentWithParentHierarchyRole);

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), secondRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), thirdRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(relationTypeWith3Components.getRelationTypeId().toString(), firstRelations);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    List<PostRelationsResponse> response = relationsService.createRelationsBulk(List.of(firstRequest, secondRequest), user);

    List<Responsibility> responsibilitiesByRelationId = responsibilityRepository.findAllByRelationIds(response.stream().map(PostRelationsResponse::getRelation_id).toList());

    assertAll(
      () -> assertEquals(1, responsibilitiesByRelationId.size()),
      () -> assertTrue(responsibilitiesByRelationId.get(0).getInheritedFlag())
    );
  }

  @Test
  public void createRelationsBulk_sameAssetForRelationTypeComponentIsNotAllowed_IntegrationTest () {
    prepareData();
    prepareAssignments();
    prepareResponsibilities();
    prepareAssignmentsForNoRolesComponents();

    relationTypeWith2Components.setResponsibilityInheritanceFlag(true);
    relationTypeRepository.save(relationTypeWith2Components);
    relationTypeComponentWithChildHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.SOURCE);
    relationTypeComponentRepository.save(relationTypeComponentWithChildHierarchyRole);
    relationTypeComponentWithParentHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.CONSUMER);
    relationTypeComponentRepository.save(relationTypeComponentWithParentHierarchyRole);

    RelationType thirdRelationType = relationTypeRepository.save(new RelationType("new relation type", "description", 2, true, false, language, user));
    RelationTypeComponent thirdSourceRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first source component", "desc", ResponsibilityInheritanceRole.SOURCE, null, false, language, thirdRelationType, user));
    RelationTypeComponent thirdConsumerRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first consumer name", "desc", ResponsibilityInheritanceRole.CONSUMER, null, false, language, thirdRelationType, user));

    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(thirdSourceRelationTypeComponent, assetTypeOne, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(thirdConsumerRelationTypeComponent, assetTypeTwo, false, null, user));

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), secondRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), thirdRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(relationTypeWith3Components.getRelationTypeId().toString(), firstRelations);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    ArrayList<PostRelationRequest> thirdRelations = new ArrayList<>();
    thirdRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), thirdSourceRelationTypeComponent.getRelationTypeComponentId().toString()));
    thirdRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), thirdConsumerRelationTypeComponent.getRelationTypeComponentId().toString()));
    PostRelationsRequest thirdRequest = new PostRelationsRequest(thirdRelationType.getRelationTypeId().toString(), thirdRelations);

    assertAll(
      () -> assertDoesNotThrow(() -> relationsService.createRelationsBulk(List.of(firstRequest, secondRequest, thirdRequest), user))
    );
  }

  @Test
  public void createRelationsBulk_Success_IntegrationTest () {
    prepareData();
    prepareAssignments();
    prepareResponsibilities();
    prepareAssignmentsForNoRolesComponents();

    relationTypeComponentWithChildHierarchyRole.setSingleRelationTypeComponentForAssetFlag(true);
    relationTypeComponentWithChildHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.SOURCE);
    relationTypeComponentRepository.save(relationTypeComponentWithChildHierarchyRole);
    relationTypeComponentWithParentHierarchyRole.setResponsibilityInheritanceRole(ResponsibilityInheritanceRole.CONSUMER);
    relationTypeComponentRepository.save(relationTypeComponentWithParentHierarchyRole);

    Relation relation = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentSource = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponentWithChildHierarchyRole, firstAsset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent relationComponentConsumer = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponentWithParentHierarchyRole, firstAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));
    Relation secondRelation = relationRepository.save(new Relation(relationTypeWith3Components, user));
    RelationComponent secondRelationComponentSource = relationComponentRepository.save(new RelationComponent(secondRelation, relationTypeComponentWithChildHierarchyRole, firstAsset, null, null, user));

    List<PostRelationRequest> firstRelations = new ArrayList<>();
    firstRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), firstRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), secondRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    firstRelations.add(new PostRelationRequest(thirdAsset.getAssetId().toString(), thirdRelationTypeComponentWithoutRoles.getRelationTypeComponentId().toString()));
    PostRelationsRequest firstRequest = new PostRelationsRequest(relationTypeWith3Components.getRelationTypeId().toString(), firstRelations);

    ArrayList<PostRelationRequest> secondRelations = new ArrayList<>();
    secondRelations.add(new PostRelationRequest(firstAsset.getAssetId().toString(), relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentId().toString()));
    secondRelations.add(new PostRelationRequest(secondAsset.getAssetId().toString(), relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentId().toString()));
    PostRelationsRequest secondRequest = new PostRelationsRequest(relationTypeWith2Components.getRelationTypeId().toString(), secondRelations);

    assertThrows(RelationTypeComponentWithSameAssetException.class, () -> relationsService.createRelationsBulk(List.of(firstRequest, secondRequest), user));
  }

  @Test
  public void getRelationByIdRelationNotFoundIntegrationTest () {
    assertThrows(RelationNotFoundException.class, () -> relationsService.getRelationById(UUID.randomUUID()));
  }

  @Test
  public void getRelationByIdSuccessIntegrationTest () {
    prepareData();

    Relation relation = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponentWithParentHierarchyRole, secondAsset, HierarchyRole.PARENT, null, user));

    GetRelationResponse response = relationsService.getRelationById(relation.getRelationId());

    assertAll(
      () -> assertEquals(relation.getRelationId(), response.getRelation_id()),
      () -> assertEquals(2, response.getRelation_components().size()),
      () -> assertEquals(relationComponentChild.getRelationComponentId(), response.getRelation_components().stream().filter(c -> c.getHierarchy_role().equals(HierarchyRole.CHILD)).toList().get(0).getRelation_component_id()),
      () -> assertEquals(relationComponentParent.getRelationComponentId(), response.getRelation_components().stream().filter(c -> c.getHierarchy_role().equals(HierarchyRole.PARENT)).toList().get(0).getRelation_component_id()),
      () -> assertEquals(firstAsset.getAssetId(), response.getRelation_components().stream().filter(c -> c.getHierarchy_role().equals(HierarchyRole.CHILD)).toList().get(0).getAsset_id()),
      () -> assertEquals(relationTypeComponentWithChildHierarchyRole.getRelationTypeComponentName(), response.getRelation_components().stream().filter(c -> c.getHierarchy_role().equals(HierarchyRole.CHILD)).toList().get(0).getRelation_type_component_name()),
      () -> assertEquals(secondAsset.getAssetId(), response.getRelation_components().stream().filter(c -> c.getHierarchy_role().equals(HierarchyRole.PARENT)).toList().get(0).getAsset_id()),
      () -> assertEquals(relationTypeComponentWithParentHierarchyRole.getRelationTypeComponentName(), response.getRelation_components().stream().filter(c -> c.getHierarchy_role().equals(HierarchyRole.PARENT)).toList().get(0).getRelation_type_component_name()),
      () -> assertEquals(relationTypeWith2Components.getRelationTypeId(), response.getRelation_type_id()),
      () -> assertEquals(relationTypeWith2Components.getRelationTypeName(), response.getRelation_type_name())
    );
  }

  @Test
  public void getRelationsByParamsPaginationIntegrationTest () {
    generateRelations(70);

    assertAll(
      () -> assertEquals(50, relationsService.getRelationsByParams(null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(2, relationsService.getRelationsByParams(null, null, null, null,null, 0, 2).getResults().size()),
      () -> assertEquals(0, relationsService.getRelationsByParams(null, null, null, null, false, 10, 50).getResults().size()),
      () -> assertEquals(50, relationsService.getRelationsByParams(null, null, null, null, false, 0, 70).getResults().size()),
      () -> assertEquals(70, relationsService.getRelationsByParams(null, null, null, null, false, 0, 50).getTotal())
    );
  }

  @Test
  public void getRelationsByParamsIntegrationTest () {
    prepareData();

    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithParentHierarchyRole, secondAsset, HierarchyRole.PARENT, null, user));

    RelationType relationTypeWith2InheritanceComponents = relationTypeRepository.save(new RelationType("some new name", "description", 2, true, false, language, user));
    RelationTypeComponent relationTypeComponentWithSourceInheritanceRole = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, relationTypeWith2InheritanceComponents, user));
    RelationTypeComponent relationTypeComponentWithConsumerInheritanceRole = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, relationTypeWith2InheritanceComponents, user));
    Relation relationWith2InheritanceComponents = relationRepository.save(new Relation(relationTypeWith2InheritanceComponents, user));
    RelationComponent relationComponentConsumer = relationComponentRepository.save(new RelationComponent(relationWith2InheritanceComponents, relationTypeComponentWithSourceInheritanceRole, firstAsset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent relationComponentSource = relationComponentRepository.save(new RelationComponent(relationWith2InheritanceComponents, relationTypeComponentWithConsumerInheritanceRole, secondAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));

    Relation relationWith3Components = relationRepository.save(new Relation(relationTypeWith3Components, user));
    RelationComponent firstRelationComponent = relationComponentRepository.save(new RelationComponent(relationWith3Components, firstRelationTypeComponentWithoutRoles, firstAsset, null, null, user));
    RelationComponent secondRelationComponent = relationComponentRepository.save(new RelationComponent(relationWith3Components, secondRelationTypeComponentWithoutRoles, secondAsset, null, null, user));
    RelationComponent thirdRelationComponent = relationComponentRepository.save(new RelationComponent(relationWith3Components, thirdRelationTypeComponentWithoutRoles, thirdAsset, null, null, user));

    assertAll(
      () -> assertEquals(3, relationsService.getRelationsByParams(firstAsset.getAssetId(), null, null, null, null, 0, 50).getResults().size(), "first asset"),
      () -> assertEquals(1, relationsService.getRelationsByParams(thirdAsset.getAssetId(), null, null, null, null, 0, 50).getResults().size(), "third asset"),
      () -> assertEquals(3, relationsService.getRelationsByParams(thirdAsset.getAssetId(), null, null, null, null, 0, 50).getResults().get(0).getRelation_components().size(), "third asset check relation components"),
      () -> assertEquals(2, relationsService.getRelationsByParams(null, null, null, null, false, 0, 50).getResults().size(), "responsibility inheritance flag = false"),
      () -> assertEquals(1, relationsService.getRelationsByParams(null, null, null, true, false, 0, 50).getResults().size(), "hierarchy flag = true, responsibility inheritance flag = false"),
      () -> assertEquals(1, relationsService.getRelationsByParams(null, null, null, true, null, 0, 50).getResults().size(), "hierarchy flag = true"),
      () -> assertEquals(1, relationsService.getRelationsByParams(null, relationTypeWith2Components.getRelationTypeId(), null, true, null, 0, 50).getResults().size(), "relation type with 2 components, hierarchy flag = true"),
      () -> assertEquals(2, relationsService.getRelationsByParams(null, relationTypeWith2Components.getRelationTypeId(), null, true, null, 0, 50).getResults().get(0).getRelation_components().size(), "relation type with 2 components, hierarchy flag = true, check relation component"),
      () -> assertEquals(0, relationsService.getRelationsByParams(null, relationTypeWith2Components.getRelationTypeId(), null, true, true, 0, 50).getResults().size(), "relation type with 2 components, hierarchy flag = true, responsibility inheritance flag = true"),
      () -> assertEquals(0, relationsService.getRelationsByParams(null, relationTypeWith3Components.getRelationTypeId(), null, true, true, 0, 50).getResults().size(), "relation type with 3 components, hierarchy flag = true, responsibility inheritance flag = true"),
      () -> assertEquals(1, relationsService.getRelationsByParams(null, relationTypeWith3Components.getRelationTypeId(), null, false, null, 0, 50).getResults().size(), "relation type with 3 components, hierarchy flag = false"),
      () -> assertEquals(1, relationsService.getRelationsByParams(secondAsset.getAssetId(), null, null, null, true, 0, 50).getResults().size(), "second asset, responsibility inheritance flag = true"),
      () -> assertEquals(1, relationsService.getRelationsByParams(null, null, relationTypeComponentWithSourceInheritanceRole.getRelationTypeComponentId(), null, true, 0, 50).getResults().size(), "relation type component with source inheritance role, responsibility inheritance flag = true")
    );
  }

  @Test
  public void getRelationAttributesRelationNotFoundIntegrationTest () {
    assertThrows(RelationNotFoundException.class, () -> relationsService.getRelationAttributes(UUID.randomUUID()));
  }

  @Test
  public void getRelationAttributesIntegrationTest () {
    prepareData();

    AttributeType attributeTypeForRelationAttribute = attributeTypeRepository.save(new AttributeType("attribute type for relation attribute", "desc", AttributeKindType.TEXT, null, null, language, user));
    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithParentHierarchyRole, secondAsset, HierarchyRole.PARENT, null, user));

    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(attributeTypeForRelationAttribute, relationWith2HierarchyComponents, language, user));
    RelationTypeAttributeTypeAssignment relationTypeAttributeTypeAssignment = relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(relationTypeWith2Components, attributeTypeForRelationAttribute, user));

    AttributeType attributeTypeForFirstRelationComponentAttribute = attributeTypeRepository.save(new AttributeType("first attribute type for relation component attribute", "desc", AttributeKindType.TEXT, null, null, language, user));
    AttributeType attributeTypeForSecondRelationComponentAttribute = attributeTypeRepository.save(new AttributeType("second attribute type for relation component attribute", "desc", AttributeKindType.TEXT, null, null, language, user));
    Relation relationWith3Components = relationRepository.save(new Relation(relationTypeWith3Components, user));
    RelationComponent firstRelationComponent = relationComponentRepository.save(new RelationComponent(relationWith3Components, firstRelationTypeComponentWithoutRoles, firstAsset, null, null, user));
    RelationComponent secondRelationComponent = relationComponentRepository.save(new RelationComponent(relationWith3Components, secondRelationTypeComponentWithoutRoles, secondAsset, null, null, user));
    RelationComponent thirdRelationComponent = relationComponentRepository.save(new RelationComponent(relationWith3Components, thirdRelationTypeComponentWithoutRoles, thirdAsset, null, null, user));

    RelationComponentAttribute relationComponentAttributeForFirstComponent = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeTypeForFirstRelationComponentAttribute, firstRelationComponent, language, user));
    RelationComponentAttribute relationComponentAttributeForSecondComponent = relationComponentAttributeRepository.save(new RelationComponentAttribute(attributeTypeForSecondRelationComponentAttribute, secondRelationComponent, language, user));
    RelationTypeComponentAttributeTypeAssignment firstRelationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(firstRelationTypeComponentWithoutRoles, attributeTypeForFirstRelationComponentAttribute, user));
    RelationTypeComponentAttributeTypeAssignment secondRelationTypeComponentAttributeTypeAssignment = relationTypeComponentAttributeTypeAssignmentRepository.save(new RelationTypeComponentAttributeTypeAssignment(secondRelationTypeComponentWithoutRoles, attributeTypeForSecondRelationComponentAttribute, user));

    RelationType relationTypeWith2InheritanceComponents = relationTypeRepository.save(new RelationType("some new name", "description", 2, true, false, language, user));
    RelationTypeComponent relationTypeComponentWithSourceInheritanceRole = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, relationTypeWith2InheritanceComponents, user));
    RelationTypeComponent relationTypeComponentWithConsumerInheritanceRole = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, relationTypeWith2InheritanceComponents, user));
    Relation relationWith2InheritanceComponents = relationRepository.save(new Relation(relationTypeWith2InheritanceComponents, user));
    RelationComponent relationComponentConsumer = relationComponentRepository.save(new RelationComponent(relationWith2InheritanceComponents, relationTypeComponentWithSourceInheritanceRole, firstAsset, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent relationComponentSource = relationComponentRepository.save(new RelationComponent(relationWith2InheritanceComponents, relationTypeComponentWithConsumerInheritanceRole, secondAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));

    List<GetRelationComponentWithAttributesResponse> relationWith3ComponentsResponse = relationsService.getRelationAttributes(relationWith3Components.getRelationId()).getRelation_components();
    List<GetRelationComponentWithAttributesResponse> firstRelationComponentWith3RelationComponentsResponse = relationWith3ComponentsResponse.stream().filter(relationComponent -> relationComponent.getRelation_component_id().equals(firstRelationComponent.getRelationComponentId())).toList();
    List<GetRelationComponentWithAttributesResponse> secondRelationComponentWith3RelationComponentsResponse = relationWith3ComponentsResponse.stream().filter(relationComponent -> relationComponent.getRelation_component_id().equals(secondRelationComponent.getRelationComponentId())).toList();
    List<GetRelationComponentWithAttributesResponse> thirdRelationComponentWith3RelationComponentsResponse = relationWith3ComponentsResponse.stream().filter(relationComponent -> relationComponent.getRelation_component_id().equals(thirdRelationComponent.getRelationComponentId())).toList();

    assertAll(
      // relationWith2HierarchyComponents
      () -> assertEquals(1, relationsService.getRelationAttributes(relationWith2HierarchyComponents.getRelationId()).getRelation_attributes().size(), "relationWith2HierarchyComponents relation attributes size"),
      () -> assertEquals(attributeTypeForRelationAttribute.getAttributeTypeName(), relationsService.getRelationAttributes(relationWith2HierarchyComponents.getRelationId()).getRelation_attributes().get(0).getAttribute_type_name(), "relationWith2HierarchyComponents relation attribute name"),
      () -> assertEquals(2, relationsService.getRelationAttributes(relationWith2HierarchyComponents.getRelationId()).getRelation_components().size(), "relationWith2HierarchyComponents relation components size"),

      // relationWith3Components
      () -> assertEquals(0, relationsService.getRelationAttributes(relationWith3Components.getRelationId()).getRelation_attributes().size(), "relationWith3Components relation attributes size"),
      () -> assertEquals(3, relationWith3ComponentsResponse.size(), "relationWith3Components relation components size"),
      () -> assertEquals(1, firstRelationComponentWith3RelationComponentsResponse.get(0).getRelation_component_attributes().size(), "relationWith3Components first relation component relation components attributes size"),
      () -> assertEquals(attributeTypeForFirstRelationComponentAttribute.getAttributeTypeName(), firstRelationComponentWith3RelationComponentsResponse.get(0).getRelation_component_attributes().get(0).getAttribute_type_name(), "relationWith3Components first relation components attribute name"),
      () -> assertEquals(1, secondRelationComponentWith3RelationComponentsResponse.get(0).getRelation_component_attributes().size(), "relationWith3Components second relation component relation components attributes size"),
      () -> assertEquals(attributeTypeForSecondRelationComponentAttribute.getAttributeTypeName(), secondRelationComponentWith3RelationComponentsResponse.get(0).getRelation_component_attributes().get(0).getAttribute_type_name(), "relationWith3Components second relation components attributes name"),
      () -> assertEquals(1, thirdRelationComponentWith3RelationComponentsResponse.size(), "relationWith3Components third relation component relation components attributes size"),

      // relationTypeWith2InheritanceComponents
      () -> assertEquals(0, relationsService.getRelationAttributes(relationWith2InheritanceComponents.getRelationId()).getRelation_attributes().size(), "relationWith2InheritanceComponents relation attributes size"),
      () -> assertEquals(2, relationsService.getRelationAttributes(relationWith2InheritanceComponents.getRelationId()).getRelation_components().size(), "relationWith2InheritanceComponents relation components size")
    );
  }

  @Test
  public void deleteRelationRelationNotFoundIntegrationTest () {
    assertThrows(RelationNotFoundException.class, () -> relationsService.deleteRelation(UUID.randomUUID(), user));
  }

  @Test
  public void deleteRelationRelationAlreadyDeletedIntegrationTest () {
    prepareData();

    Relation relationWith2HierarchyComponents = new Relation(relationTypeWith2Components, user);
    relationWith2HierarchyComponents.setIsDeleted(true);
    relationRepository.save(relationWith2HierarchyComponents);

    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithParentHierarchyRole, secondAsset, HierarchyRole.PARENT, null, user));

    assertThrows(RelationNotFoundException.class, () -> relationsService.deleteRelation(relationWith2HierarchyComponents.getRelationId(), user));
  }

  @Test
  public void deleteRelationSuccessIntegrationTest () {
    prepareData();

    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithParentHierarchyRole, secondAsset, HierarchyRole.PARENT, null, user));

    relationsService.deleteRelation(relationWith2HierarchyComponents.getRelationId(), user);

    Optional<Relation> deletedRelation = relationRepository.findById(relationWith2HierarchyComponents.getRelationId());

    assertAll(
      () -> assertTrue(deletedRelation.get().getIsDeleted()),
      () -> assertNotNull(deletedRelation.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedRelation.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRelationDeleteAssetHierarchyIntegrationTest () {
    prepareData();

    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithParentHierarchyRole, secondAsset, HierarchyRole.PARENT, null, user));

    AssetHierarchy assetHierarchy = assetHierarchyRepository.save(new AssetHierarchy(firstAsset, secondAsset, relationWith2HierarchyComponents, user));

    relationsService.deleteRelation(relationWith2HierarchyComponents.getRelationId(), user);

    Optional<AssetHierarchy> deletedAssetHierarchy = assetHierarchyRepository.findById(assetHierarchy.getAssetHierarchyId());

    assertAll(
      () -> assertTrue(deletedAssetHierarchy.get().getIsDeleted()),
      () -> assertNotNull(deletedAssetHierarchy.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedAssetHierarchy.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRelationDeleteResponsibilitiesIntegrationTest () {
    prepareData();

    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithParentHierarchyRole, secondAsset, HierarchyRole.PARENT, null, user));

    Responsibility responsibility = new Responsibility(user, null, firstAsset, role, ResponsibleType.USER, user);
    responsibility.setRelation(relationWith2HierarchyComponents);
    responsibilityRepository.save(responsibility);

    Responsibility secondInheritedResponsibility = new Responsibility(user, null, secondAsset, role, ResponsibleType.USER, user);
    secondInheritedResponsibility.setInheritedFlag(true);
    secondInheritedResponsibility.setParentResponsibility(responsibility);
    secondInheritedResponsibility.setRelation(relationWith2HierarchyComponents);
    responsibilityRepository.save(secondInheritedResponsibility);

    relationsService.deleteRelation(relationWith2HierarchyComponents.getRelationId(), user);

    Optional<Responsibility> deletedResponsibility = responsibilityRepository.findById(responsibility.getResponsibilityId());
    Optional<Responsibility> secondDeletedResponsibility = responsibilityRepository.findById(secondInheritedResponsibility.getResponsibilityId());

    assertAll(
      () -> assertTrue(deletedResponsibility.get().getIsDeleted(), "first responsibility is deleted"),
      () -> assertNotNull(deletedResponsibility.get().getDeletedOn(), "first responsibility deleted on"),
      () -> assertEquals(user.getUserId(), deletedResponsibility.get().getDeletedBy().getUserId(), "first responsibility deleted by"),
      () -> assertTrue(secondDeletedResponsibility.get().getIsDeleted(), "second responsibility is deleted"),
      () -> assertNotNull(secondDeletedResponsibility.get().getDeletedOn(), "second responsibility deleted on"),
      () -> assertEquals(user.getUserId(), secondDeletedResponsibility.get().getDeletedBy().getUserId(), "second responsibility deleted by")
    );
  }

  @Test
  public void deleteRelationDeleteRelationComponentsIntegrationTest () {
    prepareData();

    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));
    RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithParentHierarchyRole, secondAsset, HierarchyRole.PARENT, null, user));

    relationsService.deleteRelation(relationWith2HierarchyComponents.getRelationId(), user);

    Optional<RelationComponent> deletedComponent = relationComponentRepository.findById(relationComponentChild.getRelationComponentId());

    assertAll(
      () -> assertTrue(deletedComponent.get().getIsDeleted()),
      () -> assertNotNull(deletedComponent.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedComponent.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRelationDeleteRelationAttributesIntegrationTest () {
    prepareData();

    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationAttribute relationAttribute = relationAttributeRepository.save(new RelationAttribute(null, relationWith2HierarchyComponents, language, user));

    relationsService.deleteRelation(relationWith2HierarchyComponents.getRelationId(), user);

    Optional<RelationAttribute> deletedRelationAttribute = relationAttributeRepository.findById(relationAttribute.getRelationAttributeId());

    assertAll(
      () -> assertTrue(deletedRelationAttribute.get().getIsDeleted()),
      () -> assertNotNull(deletedRelationAttribute.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedRelationAttribute.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRelationDeleteRelationComponentAttributesIntegrationTest () {
    prepareData();

    Relation relationWith2HierarchyComponents = relationRepository.save(new Relation(relationTypeWith2Components, user));
    RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relationWith2HierarchyComponents, relationTypeComponentWithChildHierarchyRole, firstAsset, HierarchyRole.CHILD, null, user));

    RelationComponentAttribute relationComponentAttribute = relationComponentAttributeRepository.save(new RelationComponentAttribute(null, relationComponentChild, language, user));

    relationsService.deleteRelation(relationWith2HierarchyComponents.getRelationId(), user);

    Optional<RelationComponentAttribute> deletedRelationComponentAttribute = relationComponentAttributeRepository.findById(relationComponentAttribute.getRelationComponentAttributeId());

    assertAll(
      () -> assertTrue(deletedRelationComponentAttribute.get().getIsDeleted()),
      () -> assertNotNull(deletedRelationComponentAttribute.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedRelationComponentAttribute.get().getDeletedBy().getUserId())
    );
  }

  private void generateRelations (int count) {
    role = roleRepository.save(new Role("role name", "desc", language, user));

    for (int i = 0; i < count; i++) {
      AssetType assetType1 = assetTypeRepository.save(new AssetType("asset_type_1_" + i, "asset_description" + i, "" + i, "AT"+i, language, user));
      AssetType assetType2 = assetTypeRepository.save(new AssetType("asset_type_2_" + i, "asset_description" + i, "" + i, "AT"+i, language, user));

      Asset asset1 = assetRepository.save(new Asset("asset_1_" + i, assetType1, "displayed name", language, null, null, user));
      Asset asset2 = assetRepository.save(new Asset("asset_2_" + i, assetType2, "displayed name", language, null, null, user));

      RelationType relationType = relationTypeRepository.save(new RelationType("relation_type_" + i, "description", 2, false, true, language, user));
      RelationTypeComponent childRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, HierarchyRole.CHILD, null, language, relationType, user));
      RelationTypeComponent parentRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, HierarchyRole.PARENT, null, language, relationType, user));

      Relation relation = relationRepository.save(new Relation(relationType, user));
      RelationComponent relationComponentChild = relationComponentRepository.save(new RelationComponent(relation, childRelationTypeComponent, asset1, HierarchyRole.CHILD, null, user));
      RelationComponent relationComponentParent = relationComponentRepository.save(new RelationComponent(relation, parentRelationTypeComponent, asset2, HierarchyRole.PARENT, null, user));
    }
  }
}
