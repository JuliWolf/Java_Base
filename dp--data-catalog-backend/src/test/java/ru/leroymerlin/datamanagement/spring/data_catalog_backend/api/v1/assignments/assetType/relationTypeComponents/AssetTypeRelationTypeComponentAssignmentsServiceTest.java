package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents.AssetTypeRelationTypeComponentAssignmentsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponent;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponentAssetTypeAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

public class AssetTypeRelationTypeComponentAssignmentsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private AssetTypeRelationTypeComponentAssignmentsService assetTypeRelationTypeComponentAssignmentsService;

  @Autowired
  private RelationTypeRepository relationTypeRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;

  @AfterEach
  public void clearData () {
    relationTypeComponentAssetTypeAssignmentRepository.deleteAll();
    assetTypeRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
  }

  @Test
  public void getAssetTypeRelationTypeComponentAssignmentsAssetTypeNotFoundIntegrationTest () {
    assertThrows(AssetTypeNotFoundException.class, () -> assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(UUID.randomUUID(), null, null, null, 0, 50));
  }

  @Test
  public void getAssetTypeRelationTypeComponentAssignmentsSuccessIntegrationTest () {
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("first relation type", "des", 2, false, false, language, user));
    RelationTypeComponent firstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first", "desc", null, null, null, language, firstRelationType, user));

    AssetType assetType = assetTypeRepository.save(new AssetType("first asset type", "desc", "acr", "color", language, user));

    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstRelationTypeComponent, assetType, false, null, user));

    RelationType secondRelationType = relationTypeRepository.save(new RelationType("second relation type", "des", 2, false, false, language, user));
    RelationTypeComponent secondFirstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second first", "desc", null, HierarchyRole.CHILD, null, language, secondRelationType, user));
    RelationTypeComponent secondSecondRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second second ", "desc", null, HierarchyRole.PARENT, null, language, secondRelationType, user));

    AssetType secondAssetType = assetTypeRepository.save(new AssetType("second asset type", "desc", "acr", "color", language, user));
    AssetType thirsAssetType = assetTypeRepository.save(new AssetType("third asset type", "desc", "acr", "color", language, user));

    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondFirstRelationTypeComponent, secondAssetType, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondSecondRelationTypeComponent, thirsAssetType, false, null, user));

    RelationType thirdRelationType = relationTypeRepository.save(new RelationType("third relation type", "des", 2, false, false, language, user));
    RelationTypeComponent thirdFirstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("third first", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, thirdRelationType, user));
    RelationTypeComponent thirdSecondRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("third second", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, thirdRelationType, user));

    AssetType forthAssetType = assetTypeRepository.save(new AssetType("forth asset type", "desc", "acr", "color", language, user));
    AssetType fifthAssetType = assetTypeRepository.save(new AssetType("fifth asset type", "desc", "acr", "color", language, user));

    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(thirdFirstRelationTypeComponent, forthAssetType, false, null, user));
    relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(thirdSecondRelationTypeComponent, fifthAssetType, false, null, user));

    assertAll(
      () -> assertEquals(1, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(assetType.getAssetTypeId(), null, null, null, 0, 50).getTotal(), "first asset type"),
      () -> assertEquals(0, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(assetType.getAssetTypeId(), HierarchyRole.PARENT, null, null, 0, 50).getTotal(), "first asset type; HierarchyRole.PARENT"),
      () -> assertEquals(0, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(assetType.getAssetTypeId(), null, ResponsibilityInheritanceRole.SOURCE, null, 0, 50).getTotal(), "first asset type; ResponsibilityInheritanceRole.SOURCE"),
      () -> assertEquals(1, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(assetType.getAssetTypeId(), null, null, "first", 0, 50).getTotal(), "first asset type; name - `first`"),
      () -> assertEquals(0, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(assetType.getAssetTypeId(), null, null, "second", 0, 50).getTotal(), "first asset type; name - `second`"),
      () -> assertEquals(1, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(secondAssetType.getAssetTypeId(), null, null, "second", 0, 50).getTotal(), "second asset type; name - `second`"),
      () -> assertEquals(1, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(secondAssetType.getAssetTypeId(), HierarchyRole.CHILD, null, null, 0, 50).getTotal(), "second asset type; HierarchyRole.CHILD"),
      () -> assertEquals(0, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(secondAssetType.getAssetTypeId(), HierarchyRole.PARENT, null, null, 0, 50).getTotal(), "second asset type; HierarchyRole.PARENT"),
      () -> assertEquals(1, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(thirsAssetType.getAssetTypeId(), HierarchyRole.PARENT, null, null, 0, 50).getTotal(), "third asset type; HierarchyRole.PARENT"),
      () -> assertEquals(0, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(thirsAssetType.getAssetTypeId(), null, ResponsibilityInheritanceRole.SOURCE, null, 0, 50).getTotal(), "third asset type; ResponsibilityInheritanceRole.SOURCE"),
      () -> assertEquals(1, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(forthAssetType.getAssetTypeId(), null, ResponsibilityInheritanceRole.SOURCE, null, 0, 50).getTotal(), "forth asset type; ResponsibilityInheritanceRole.SOURCE"),
      () -> assertEquals(0, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(forthAssetType.getAssetTypeId(), null, ResponsibilityInheritanceRole.CONSUMER, null, 0, 50).getTotal(), "forth asset type; ResponsibilityInheritanceRole.CONSUMER"),
      () -> assertEquals(1, assetTypeRelationTypeComponentAssignmentsService.getAssetTypeRelationTypeComponentAssignments(fifthAssetType.getAssetTypeId(), null, ResponsibilityInheritanceRole.CONSUMER, null, 0, 50).getTotal(), "fifth asset type; ResponsibilityInheritanceRole.CONSUMER")
    );
  }
}
