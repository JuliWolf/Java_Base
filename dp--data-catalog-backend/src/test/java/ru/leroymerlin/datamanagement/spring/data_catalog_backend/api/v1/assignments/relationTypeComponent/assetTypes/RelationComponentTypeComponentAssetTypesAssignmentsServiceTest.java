package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypesAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypeInheritance.AssetTypeInheritanceRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.AssetsTypeIsUsedInRelationsException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentIsInherited;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.exceptions.RelationTypeComponentAssetTypeAssignmentNotFound;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.get.GetRelationTypeComponentAssetTypeAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypesRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.relationTypeComponent.assetTypes.models.post.PostRelationTypeComponentAssetTypesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeComponentNotFoundException;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

public class RelationComponentTypeComponentAssetTypesAssignmentsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private RelationTypeComponentAssetTypesAssignmentsService relationTypeComponentAssetTypesAssignmentsService;

  @Autowired
  private RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;

  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private AssetTypeInheritanceRepository assetTypeInheritanceRepository;
  @Autowired
  private RelationRepository relationRepository;
  @Autowired
  private RelationComponentRepository relationComponentRepository;
  @Autowired
  private AssetRepository assetRepository;

  RelationType relationType;
  RelationTypeComponent relationTypeComponent;
  AssetType firstAssetType;
  AssetType secondAssetType;
  AssetType parentAssetType;
  AssetType childAssetType;
  AssetTypeInheritance assetTypeInheritance;


  @BeforeAll
  public void createRelationTypeAndRelationTypeComponent () {
    relationType  = relationTypeRepository.save(new RelationType("relation type name", "desc", 2, null, null, language, user));
    relationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("test name", "test desc", null, null, null, language, relationType, user));
    firstAssetType = assetTypeRepository.save(new AssetType("asset name", "desc", "an", "red", language, user));
    secondAssetType = assetTypeRepository.save(new AssetType("second name", "desc", "sn", "blue", language, user));

    parentAssetType = assetTypeRepository.save(new AssetType("parent asset type", "has child", "pat", "black", language, user));
    childAssetType = assetTypeRepository.save(new AssetType("child asset type", "has parent", "cat", "white", language, user));
    assetTypeInheritance = assetTypeInheritanceRepository.save(new AssetTypeInheritance(parentAssetType, childAssetType, user));
  }

  @AfterAll
  public void clearRelationTypeAndRelationTypeComponent () {
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
    assetTypeInheritanceRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearAssignments () {
    relationComponentRepository.deleteAll();
    relationRepository.deleteAll();
    assetRepository.deleteAll();
    relationTypeComponentAssetTypeAssignmentRepository.deleteAll();
  }

  @Test
  public void createRelationTypeComponentAssetTypesAssignmentsRelationTypeComponentNotFoundIntegrationTest () {
    assertThrows(RelationTypeComponentNotFoundException.class, () -> relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(UUID.randomUUID(), new PostRelationTypeComponentAssetTypesRequest(), user));
  }

  @Test
  public void createRelationTypeComponentAssetTypesAssignmentsInvalidAssetTypeIntegrationTest () {
    PostRelationTypeComponentAssetTypeRequest firstAssetTypeRequest = new PostRelationTypeComponentAssetTypeRequest("123");
    PostRelationTypeComponentAssetTypeRequest secondAssetTypeRequest = new PostRelationTypeComponentAssetTypeRequest(secondAssetType.getAssetTypeId().toString());
    List<PostRelationTypeComponentAssetTypeRequest> requestList = new ArrayList<>();
    requestList.add(firstAssetTypeRequest);
    requestList.add(secondAssetTypeRequest);

    assertThrows(IllegalArgumentException.class, () -> relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(relationTypeComponent.getRelationTypeComponentId(), new PostRelationTypeComponentAssetTypesRequest(requestList), user));
  }

  @Test
  public void createRelationTypeComponentAssetTypesAssignmentsAssetTypeNotFoundIntegrationTest () {
    PostRelationTypeComponentAssetTypeRequest firstAssetTypeRequest = new PostRelationTypeComponentAssetTypeRequest(UUID.randomUUID().toString());
    PostRelationTypeComponentAssetTypeRequest secondAssetTypeRequest = new PostRelationTypeComponentAssetTypeRequest(secondAssetType.getAssetTypeId().toString());
    List<PostRelationTypeComponentAssetTypeRequest> requestList = new ArrayList<>();
    requestList.add(firstAssetTypeRequest);
    requestList.add(secondAssetTypeRequest);

    assertThrows(AssetTypeNotFoundException.class, () -> relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(relationTypeComponent.getRelationTypeComponentId(), new PostRelationTypeComponentAssetTypesRequest(requestList), user));
  }

  @Test
  public void createRelationTypeComponentAssetTypesAssignmentsSuccessIntegrationTest () {
    PostRelationTypeComponentAssetTypeRequest firstAssetTypeRequest = new PostRelationTypeComponentAssetTypeRequest(firstAssetType.getAssetTypeId().toString());
    PostRelationTypeComponentAssetTypeRequest secondAssetTypeRequest = new PostRelationTypeComponentAssetTypeRequest(secondAssetType.getAssetTypeId().toString());
    List<PostRelationTypeComponentAssetTypeRequest> requestList = new ArrayList<>();
    requestList.add(firstAssetTypeRequest);
    requestList.add(secondAssetTypeRequest);

    PostRelationTypeComponentAssetTypesResponse response = relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(relationTypeComponent.getRelationTypeComponentId(), new PostRelationTypeComponentAssetTypesRequest(requestList), user);

    assertAll(
      () -> assertEquals(2, response.getRelation_type_component_asset_types_assignment().size()),
      () -> assertEquals(1, response.getRelation_type_component_asset_types_assignment().stream().filter(a -> a.getAsset_type_id().equals(firstAssetType.getAssetTypeId())).toList().size()),
      () -> assertEquals(1, response.getRelation_type_component_asset_types_assignment().stream().filter(a -> a.getAsset_type_id().equals(secondAssetType.getAssetTypeId())).toList().size())
    );
  }

  @Test
  public void createRelationTypeComponentAssetTypesAssignmentsCreateAssignmentsForChildrenIntegrationTest () {
    PostRelationTypeComponentAssetTypeRequest parentAssetTypeRequest = new PostRelationTypeComponentAssetTypeRequest(parentAssetType.getAssetTypeId().toString());
    List<PostRelationTypeComponentAssetTypeRequest> requestList = new ArrayList<>();
    requestList.add(parentAssetTypeRequest);

    PostRelationTypeComponentAssetTypesResponse response = relationTypeComponentAssetTypesAssignmentsService.createRelationTypeComponentAssetTypesAssignments(relationTypeComponent.getRelationTypeComponentId(), new PostRelationTypeComponentAssetTypesRequest(requestList), user);
    List<RelationTypeComponentAssetTypeAssignment> all = relationTypeComponentAssetTypeAssignmentRepository.findAll();
    assertAll(
      () -> assertEquals(1, response.getRelation_type_component_asset_types_assignment().size()),
      () -> assertEquals(1, response.getRelation_type_component_asset_types_assignment().stream().filter(a -> a.getAsset_type_id().equals(parentAssetType.getAssetTypeId())).toList().size()),
      () -> assertEquals(2, all.size())
    );
  }

  @Test
  public void getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentIdRelationTypeComponentNotFoundIntegrationTest () {
    assertThrows(RelationTypeComponentNotFoundException.class, () -> relationTypeComponentAssetTypesAssignmentsService.getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentId(UUID.randomUUID()));
  }

  @Test
  public void getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentIdSuccessIntegrationTest () {
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, firstAssetType, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, secondAssetType, false, null, user));

    GetRelationTypeComponentAssetTypeAssignmentsResponse response = relationTypeComponentAssetTypesAssignmentsService.getRelationTypeComponentAssetTypeAssignmentsByRelationTypeComponentId(relationTypeComponent.getRelationTypeComponentId());

    assertAll(
      () -> assertEquals(2, response.getRelation_type_component_allowed_asset_type().size()),
      () -> assertEquals(relationTypeComponent.getRelationTypeComponentName(), response.getRelation_type_component_name()),
      () -> assertEquals(1, response.getRelation_type_component_allowed_asset_type().stream().filter(a -> a.getAsset_type_id().equals(firstAssetType.getAssetTypeId())).toList().size()),
      () -> assertEquals(1, response.getRelation_type_component_allowed_asset_type().stream().filter(a -> a.getAsset_type_id().equals(secondAssetType.getAssetTypeId())).toList().size())
    );
  }

  @Test
  public void deleteRelationTypeComponentAssetTypeAssignmentAssignmentNotFoundIntegrationTest () {
    assertThrows(RelationTypeComponentAssetTypeAssignmentNotFound.class, () -> relationTypeComponentAssetTypesAssignmentsService.deleteRelationTypeComponentAssetTypeAssignment(UUID.randomUUID(), user));
  }

  @Test
  public void deleteRelationTypeComponentAssetTypeAssignmentAssignmentIsInheritedIntegrationTest () {
    RelationTypeComponentAssetTypeAssignment assignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, childAssetType, true, parentAssetType, user));

    assertThrows(RelationTypeComponentAssetTypeAssignmentIsInherited.class, () -> relationTypeComponentAssetTypesAssignmentsService.deleteRelationTypeComponentAssetTypeAssignment(assignment.getRelationTypeComponentAssetTypeAssignmentId(), user));
  }

  @Test
  public void deleteRelationTypeComponentAssetTypeAssignmentSuccessIntegrationTest () {
    RelationTypeComponentAssetTypeAssignment assignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, firstAssetType, false, null, user));

    relationTypeComponentAssetTypesAssignmentsService.deleteRelationTypeComponentAssetTypeAssignment(assignment.getRelationTypeComponentAssetTypeAssignmentId(), user);

    Optional<RelationTypeComponentAssetTypeAssignment> deletedAssignment = relationTypeComponentAssetTypeAssignmentRepository.findById(assignment.getRelationTypeComponentAssetTypeAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedBy()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn())
    );
  }

  @Test
  public void deleteRelationTypeComponentAssetTypeAssignmentDeleteAllChildrenIntegrationTest () {
    RelationTypeComponentAssetTypeAssignment parentAssignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, parentAssetType, false, null, user));
    RelationTypeComponentAssetTypeAssignment childAssignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, childAssetType, true, parentAssetType, user));

    relationTypeComponentAssetTypesAssignmentsService.deleteRelationTypeComponentAssetTypeAssignment(parentAssignment.getRelationTypeComponentAssetTypeAssignmentId(), user);

    Optional<RelationTypeComponentAssetTypeAssignment> deletedAssignment = relationTypeComponentAssetTypeAssignmentRepository.findById(childAssignment.getRelationTypeComponentAssetTypeAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedBy()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn())
    );
  }

  @Test
  public void deleteRelationTypeComponentAssetTypeAssignmentAssetTypeIsConnectedToRelationIntegrationTest () {
    Asset asset = assetRepository.save(new Asset("some name", firstAssetType, "adn", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("another name", secondAssetType, "adn", language, null, null, user));
    Relation relation = relationRepository.save(new Relation(relationType, user));
    RelationComponent childRelationComponent = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponent, asset, HierarchyRole.CHILD, null, user));
    RelationComponent parentRelationComponent = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponent, secondAsset, HierarchyRole.PARENT, null, user));

    RelationTypeComponentAssetTypeAssignment assignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, firstAssetType, false, null, user));

    assertThrows(AssetsTypeIsUsedInRelationsException.class, () -> relationTypeComponentAssetTypesAssignmentsService.deleteRelationTypeComponentAssetTypeAssignment(assignment.getRelationTypeComponentAssetTypeAssignmentId(), user));
  }

  @Test
  public void deleteRelationTypeComponentAssetTypeAssignmentChildAssetTypeIsConnectedToRelationIntegrationTest () {
    Asset asset = assetRepository.save(new Asset("some name", firstAssetType, "adn", language, null, null, user));
    Asset secondAsset = assetRepository.save(new Asset("child name", childAssetType, "adn", language, null, null, user));
    Relation relation = relationRepository.save(new Relation(relationType, user));
    RelationComponent childRelationComponent = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponent, asset, HierarchyRole.CHILD, null, user));
    RelationComponent parentRelationComponent = relationComponentRepository.save(new RelationComponent(relation, relationTypeComponent, secondAsset, HierarchyRole.PARENT, null, user));

    RelationTypeComponentAssetTypeAssignment parentAssignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, parentAssetType, false, null, user));
    RelationTypeComponentAssetTypeAssignment childAssignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, childAssetType, true, parentAssetType, user));

    assertThrows(AssetsTypeIsUsedInRelationsException.class, () -> relationTypeComponentAssetTypesAssignmentsService.deleteRelationTypeComponentAssetTypeAssignment(parentAssignment.getRelationTypeComponentAssetTypeAssignmentId(), user));
  }
}
