package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.StatusRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypeInheritance.AssetTypeInheritanceRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.cardHeader.AssetTypeCardHeaderAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.AssetTypeStatusAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.CustomViewRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetNameValidationMaskDoesNotMatchExampleException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetNameValidationMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeHasChildAssetTypesException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PatchAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PostAssetTypeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.models.post.PostAssetTypeResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JuliWolf
 */
public class AssetTypesServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private AssetTypesService assetTypesService;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

  @Autowired
  private EntityRepository entityRepository;
  @Autowired
  private ActionTypeRepository actionTypeRepository;
  @Autowired
  private RoleActionRepository roleActionRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private StatusRepository statusRepository;
  @Autowired
  private CustomViewRepository customViewRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private AssetTypeInheritanceRepository assetTypeInheritanceRepository;
  @Autowired
  private AssetTypeStatusAssignmentRepository assetTypeStatusAssignmentRepository;
  @Autowired
  private AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;
  @Autowired
  private AssetTypeCardHeaderAssignmentRepository assetTypeCardHeaderAssignmentRepository;
  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;

  private PostAssetTypeRequest postRequest = new PostAssetTypeRequest("BI REPORT POST", null, "BI REPORT description", "BI REPORT", "red", null, null);
  private PatchAssetTypeRequest patchRequest = new PatchAssetTypeRequest(Optional.of("BI REPORT PATCH"), Optional.of("BI REPORT description"), "BI REPORT", "red", null, null);

  @AfterEach
  public void clearTables() {
    postRequest = new PostAssetTypeRequest("BI REPORT POST", null, "BI REPORT description", "BI REPORT", "red", null, null);
    patchRequest = new PatchAssetTypeRequest(Optional.of("BI REPORT PATCH"), Optional.of("BI REPORT description"), "BI REPORT", "red", null, null);

    relationTypeComponentAssetTypeAssignmentRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    assetTypeInheritanceRepository.deleteAll();
    assetTypeStatusAssignmentRepository.deleteAll();
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
    assetTypeCardHeaderAssignmentRepository.deleteAll();
    statusRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    customViewRepository.deleteAll();
    assetTypeRepository.deleteAll();
    roleActionRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @Test
  public void createAssetTypeSuccessIntegrationTest () {
    try {
      PostAssetTypeResponse assetType = assetTypesService.createAssetType(postRequest, user);

      assertEquals(assetType.getCreated_by(), user.getUserId());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAssetTypeValidationMaskExampleEmptyIntegrationTest () {
    postRequest.setAsset_name_validation_mask("^Hello");

    assertThrows(AssetNameValidationMaskValidationException.class, () -> assetTypesService.createAssetType(postRequest, user));
  }

  @Test
  public void createAssetTypeValidationMaskExampleDoesNotMatchPatternIntegrationTest () {
    postRequest.setAsset_name_validation_mask("^Hello");
    postRequest.setAsset_name_validation_mask_example("fgdfgdfg");

    assertThrows(AssetNameValidationMaskDoesNotMatchExampleException.class, () -> assetTypesService.createAssetType(postRequest, user));
  }

  @Test
  public void createAssetTypeWithExistingNameIntegrationTest () {
    try {
      assetTypeRepository.save(new AssetType(
        postRequest.getAsset_type_name(),
        postRequest.getAsset_type_description(),
        postRequest.getAsset_type_acronym(),
        postRequest.getAsset_type_color(),
        null,
        null,
        language,
        user
      ));

      assertThrows(DataIntegrityViolationException.class, () ->
          assetTypesService.createAssetType(postRequest, user)
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAssetTypeCreateAssetWithParentIntegrationTest () {
    try {
      AssetType parentAssetType = assetTypeRepository.save(new AssetType(
        patchRequest.getAsset_type_name().get(),
        patchRequest.getAsset_type_description().get(),
        patchRequest.getAsset_type_acronym(),
        patchRequest.getAsset_type_color(),
        language,
        user
      ));

      postRequest.setParent_asset_type_id(parentAssetType.getAssetTypeId().toString());

      PostAssetTypeResponse response = assetTypesService.createAssetType(postRequest, user);
      List<AssetTypeInheritance> assetTypeInheritanceList = assetTypeInheritanceRepository.findAllAssetTypeInheritanceExistsByChildAssetTypeId(response.getAsset_type_id());

      assertAll(
        () -> assertEquals(1, assetTypeInheritanceList.size()),
        () -> assertEquals(parentAssetType.getAssetTypeId(), assetTypeInheritanceList.get(0).getParentAssetType().getAssetTypeId()),
        () -> assertEquals(response.getAsset_type_id(), assetTypeInheritanceList.get(0).getChildAssetType().getAssetTypeId())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAssetTypeCreateAssetWithWrongParentIdIntegrationTest () {
    try {
      AssetType parentAssetType = assetTypeRepository.save(new AssetType(
        patchRequest.getAsset_type_name().get(),
        patchRequest.getAsset_type_description().get(),
        patchRequest.getAsset_type_acronym(),
        patchRequest.getAsset_type_color(),
        language,
        user
      ));

      postRequest.setParent_asset_type_id("123");

      assertThrows(IllegalArgumentException.class, () -> assetTypesService.createAssetType(postRequest, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createAssetTypeInheritParentAssignmentsIntegrationTest () {
    try {
      AssetType parentAssetType = assetTypeRepository.save(new AssetType(
        "parent asset type",
        postRequest.getAsset_type_description(),
        postRequest.getAsset_type_acronym(),
        postRequest.getAsset_type_color(),
        language,
        user
      ));

      postRequest.setParent_asset_type_id(parentAssetType.getAssetTypeId().toString());

      AssetTypeStatusAssignment parentStatusAssignment = createAssetTypeStatusAssignments(parentAssetType);
      AssetTypeAttributeTypeAssignment parentAssetTypeAssignment = createAssetTypeAttributeTypeAssignments(parentAssetType);
      RelationTypeComponentAssetTypeAssignment parentRelationTypeComponentAssignment = createAssetTypeRelationTypeComponentAssignments(parentAssetType);

      PostAssetTypeResponse response = assetTypesService.createAssetType(postRequest, user);
      List<AssetTypeStatusAssignment> assetTypeStatusAssignments = assetTypeStatusAssignmentRepository.findAll();
      List<AssetTypeAttributeTypeAssignment> assetTypeAttributeTypeAssignments = assetTypeAttributeTypeAssignmentRepository.findAll();
      List<RelationTypeComponentAssetTypeAssignment> assetTypeRelationTypeComponentAssignments = relationTypeComponentAssetTypeAssignmentRepository.findAll();

      assertAll(
        () -> assertEquals(2, assetTypeAttributeTypeAssignments.size()),
        () -> assertEquals(2, assetTypeStatusAssignments.size()),
        () -> assertEquals(2, assetTypeRelationTypeComponentAssignments.size()),
        () -> assertEquals(1, assetTypeAttributeTypeAssignments.stream().filter(AssetTypeAttributeTypeAssignment::getIsInherited).toList().size()),
        () -> assertEquals(1, assetTypeStatusAssignments.stream().filter(AssetTypeStatusAssignment::getIsInherited).toList().size()),
        () -> assertEquals(1, assetTypeRelationTypeComponentAssignments.stream().filter(RelationTypeComponentAssetTypeAssignment::getIsInherited).toList().size())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getAssetTypeByNameAndDescriptionEmptyParamsIntegrationTest () {
    try {
      String name = null;
      String description = null;

      assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));
      assetTypeRepository.save(new AssetType("asset_type_2", "asset_description_2", "2", "AT2", language, user));

      assertEquals(2, assetTypesService.geAssetTypesByParams(null, name, description, null, null).getResults().size());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getAssetTypeByNameAndDescriptionWithParamsIntegrationTest () {
    try {
      AssetType parentAssetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));
      AssetType childAssetType = assetTypeRepository.save(new AssetType("asset_type_2", "asset_description_2", "2", "AT2", language, user));
      assetTypeInheritanceRepository.save(new AssetTypeInheritance(parentAssetType, childAssetType, user));

      assertAll(
        () -> assertEquals(1, assetTypesService.geAssetTypesByParams(null, null, "1", 0, 50).getResults().size()),
        () -> assertEquals(2, assetTypesService.geAssetTypesByParams(null, null, "description", 0, 50).getResults().size()),
        () -> assertEquals(0, assetTypesService.geAssetTypesByParams(null, "1", "something", 0, 50).getResults().size()),
        () -> assertEquals(1, assetTypesService.geAssetTypesByParams(true, "1", null, 0, 50).getResults().size()),
        () -> assertEquals(0, assetTypesService.geAssetTypesByParams(true, "2", null, 0, 50).getResults().size())
      );

      assetTypeRepository.save(new AssetType("type new", "52 D ScR", "2", "AT2", language, user));

      assertAll(
        () -> assertEquals(3, assetTypesService.geAssetTypesByParams(null, "type", null, 0, 50).getResults().size()),
        () -> assertEquals(3, assetTypesService.geAssetTypesByParams(null, null, "scr", 0, 50).getResults().size())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getAssetTypeByParamsPaginationIntegrationTest () {
    generateAssetTypes(130);

    assertAll(
      () -> assertEquals(1, assetTypesService.geAssetTypesByParams(null, null, "_110", 0, 50).getResults().size()),
      () -> assertEquals(50, assetTypesService.geAssetTypesByParams(null, null, "description", 0, 50).getResults().size()),
      () -> assertEquals(100, assetTypesService.geAssetTypesByParams(null, null, null, null, null).getResults().size()),
      () -> assertEquals(100, assetTypesService.geAssetTypesByParams(null, null, null, null, 130).getResults().size()),
      () -> assertEquals(0, assetTypesService.geAssetTypesByParams(null, "2", null, 2, 50).getResults().size()),
      () -> assertEquals(130, assetTypesService.geAssetTypesByParams(null, null, null, 2, 50).getTotal()),
      () -> assertEquals(11, assetTypesService.geAssetTypesByParams(null, "11", null, 2, 50).getTotal())
    );
  }

  @Test
  public void getAssetTypeByIdWithNotExistingAssetTypeIntegrationTest () {
    assertThrows(AssetTypeNotFoundException.class, () -> assetTypesService.getAssetTypeById(new UUID(123, 123)));
  }

  @Test
  public void getAssetTypeByIdSuccessIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    assertEquals(assetType.getAssetTypeId(), assetTypesService.getAssetTypeById(assetType.getAssetTypeId()).getAsset_type_id());
  }

  @Test
  public void getAssetTypeChildrenAssetTypeNotFoundIntegrationTest () {
    assertThrows(AssetTypeNotFoundException.class, () -> assetTypesService.getAssetTypeChildren(new UUID(123, 123), null, null));
  }

  @Test
  public void getAssetTypeChildrenSuccessIntegrationTest () {
    AssetType firstAssetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_2", "2", "AT2", language, user));
    AssetType secondAssetType = assetTypeRepository.save(new AssetType("asset_type_2", "asset_description_2", "2", "AT2", language, user));
    AssetType thirdAssetType = assetTypeRepository.save(new AssetType("asset_type_3", "asset_description_3", "3", "AT3", language, user));
    AssetType forthAssetType = assetTypeRepository.save(new AssetType("asset_type_4", "asset_description_4", "4", "AT4", language, user));

    assetTypeInheritanceRepository.save(new AssetTypeInheritance(firstAssetType, secondAssetType, user));
    assetTypeInheritanceRepository.save(new AssetTypeInheritance(firstAssetType, forthAssetType, user));
    assetTypeInheritanceRepository.save(new AssetTypeInheritance(secondAssetType, thirdAssetType, user));
    assetTypeInheritanceRepository.save(new AssetTypeInheritance(thirdAssetType, forthAssetType, user));

    assertAll(
      () -> assertEquals(2, assetTypesService.getAssetTypeChildren(firstAssetType.getAssetTypeId(), 0, 50).getResults().size()),
      () -> assertEquals(1, assetTypesService.getAssetTypeChildren(secondAssetType.getAssetTypeId(), 0, 50).getResults().size()),
      () -> assertEquals(1, assetTypesService.getAssetTypeChildren(thirdAssetType.getAssetTypeId(), 0, 50).getResults().size()),
      () -> assertEquals(0, assetTypesService.getAssetTypeChildren(forthAssetType.getAssetTypeId(), 0, 50).getResults().size())
    );
  }

  @Test
  public void deleteAssetTypeByIdResponsibilityAlreadyDeletedIntegrationTest () {
    AssetType assetType = new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user);

    assetType.setIsDeleted(true);
    AssetType savedAssetType = assetTypeRepository.save(assetType);

    assertThrows(AssetTypeNotFoundException.class, () -> assetTypesService.deleteAssetTypeById(savedAssetType.getAssetTypeId(), user));
  }

  @Test
  public void deleteAssetTypeByIdSuccessIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    assetTypesService.deleteAssetTypeById(assetType.getAssetTypeId(), user);

    Optional<AssetType> foundAssetType = assetTypeRepository.findById(assetType.getAssetTypeId());

    assertAll(
      () -> assertTrue(foundAssetType.get().getIsDeleted()),
      () -> assertEquals(foundAssetType.get().getDeletedBy().getUserId(), user.getUserId())
    );
  }

  @Test
  public void deleteAssetTypeByIdDeleteConnectedAssignmentsIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));
    AssetTypeAttributeTypeAssignment attributeTypeAssignment = createAssetTypeAttributeTypeAssignments(assetType);

    assetTypesService.deleteAssetTypeById(assetType.getAssetTypeId(), user);

    Optional<AssetTypeAttributeTypeAssignment> updatedAttributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.findById(attributeTypeAssignment.getAssetTypeAttributeTypeAssignmentId());

    assertAll(
      () -> assertTrue(updatedAttributeTypeAssignment.get().getIsDeleted()),
      () -> assertNotNull(updatedAttributeTypeAssignment.get().getDeletedOn())
    );
  }

  @Test
  public void deleteAssetTypeByIdHasChildAssetTypesIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));
    AssetType childAssetType = assetTypeRepository.save(new AssetType("child asset type", "child asset type desc", "cat", "blue", language, user));
    assetTypeInheritanceRepository.save(new AssetTypeInheritance(assetType, childAssetType, user));

    assertThrows(AssetTypeHasChildAssetTypesException.class, () -> assetTypesService.deleteAssetTypeById(assetType.getAssetTypeId(), user));
  }

  @Test
  public void deleteAssetTypeByIdDeleteAllInheritanceIntegrationTest () {
    AssetType parentAssetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));
    AssetType parentAssetType2 = assetTypeRepository.save(new AssetType("asset_type_2", "asset_description_2", "21", "AT2", language, user));
    AssetType childAssetType = assetTypeRepository.save(new AssetType("child asset type", "child asset type desc", "cat", "blue", language, user));
    assetTypeInheritanceRepository.save(new AssetTypeInheritance(parentAssetType, childAssetType, user));
    assetTypeInheritanceRepository.save(new AssetTypeInheritance(parentAssetType2, childAssetType, user));

    assetTypesService.deleteAssetTypeById(childAssetType.getAssetTypeId(), user);

    assertAll(
      () -> assertEquals(2, assetTypeInheritanceRepository.findAll().stream().filter(AssetTypeInheritance::getIsDeleted).toList().size()),
      () -> assertEquals(0, assetTypeInheritanceRepository.findAllAssetTypeInheritanceExistsByChildAssetTypeId(childAssetType.getAssetTypeId()).size())
    );
  }

  @Test
  public void deleteAssetTypeByIdDeleteAllConnectedRoleActionsIntegrationTest () {
    Optional<Entity> entity = entityRepository.findById(UUID.fromString("360ef840-5c19-424b-a86f-b3e24c2fcc2f"));
    Optional<ActionType> actionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));
    RoleAction roleAction = new RoleAction(role, actionType.get(), entity.get(), ActionScopeType.ONE_ID, PermissionType.ALLOW, user);

    roleAction.setAssetType(assetType);
    roleActionRepository.save(roleAction);

    assetTypesService.deleteAssetTypeById(assetType.getAssetTypeId(), user);

    Optional<RoleAction> deletedRoleAction = roleActionRepository.findById(roleAction.getRoleActionId());

    assertAll(
      () -> assertTrue(deletedRoleAction.get().getIsDeleted()),
      () -> assertNotNull(deletedRoleAction.get().getDeletedOn())
    );
  }

  @Test
  public void deleteAssetTypeByIdDeleteAllConnectedAssetTypeCardHeaderAssignmentIntegrationTest () {
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    AssetTypeCardHeaderAssignment assignment = assetTypeCardHeaderAssignmentRepository.save(new AssetTypeCardHeaderAssignment(assetType, null, role, user));

    assetTypesService.deleteAssetTypeById(assetType.getAssetTypeId(), user);

    Optional<AssetTypeCardHeaderAssignment> deletedAssignment = assetTypeCardHeaderAssignmentRepository.findById(assignment.getAssetTypeCardHeaderAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn())
    );
  }

  @Test
  public void deleteAssetTypeByIdDeleteAllConnectedCustomViewsIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));
    CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", "[]", null, "query", null, "[{\"column_kind\": \"RTF\", \"column_name\": \"name_1\"}, {\"column_kind\": \"TEXT\", \"column_name\": \"name_2\"}, {\"column_kind\": \"SINGLE_VALUE_LIST\", \"column_name\": \"name_3\"}]", null, "table query", null, null, user));

    assetTypesService.deleteAssetTypeById(assetType.getAssetTypeId(), user);

    Optional<CustomView> deletedCustomView = customViewRepository.findById(customView.getCustomViewId());

    assertAll(
      () -> assertTrue(deletedCustomView.get().getIsDeleted()),
      () -> assertNotNull(deletedCustomView.get().getDeletedOn())
    );
  }

  @Test
  public void deleteAssetTypeByIdDeleteAllConnectedRelationTypeComponentAssetTypeAssignmentIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    RelationTypeComponent relationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("new name", "desc", ResponsibilityInheritanceRole.CONSUMER, HierarchyRole.CHILD, null, language, null, user));
    RelationTypeComponentAssetTypeAssignment assignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, assetType, false, null, user));

    assetTypesService.deleteAssetTypeById(assetType.getAssetTypeId(), user);

    Optional<RelationTypeComponentAssetTypeAssignment> deletedAssignment = relationTypeComponentAssetTypeAssignmentRepository.findById(assignment.getRelationTypeComponentAssetTypeAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn())
    );
  }

  @Test
  public void updateAssetTypeSuccessIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(Optional.of("new asset type"), null, null, null, null, null);
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    PostAssetTypeResponse updatedAssetType = assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user);

    assertAll(
      () -> assertEquals(request.getAsset_type_name().get(), updatedAssetType.getAsset_type_name()),
      () -> assertNotEquals(assetType.getLastModifiedOn(), updatedAssetType.getLast_modified_on()),
      () -> assertNotNull(updatedAssetType.getLast_modified_by())
    );
  }

  @Test
  public void updateAssetTypeAssetNameValidationMaskPatternIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(Optional.of("new asset type"), null, null, null, Optional.of("new value"), null);
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", "Hello", "^Hello.*", language, user));

    assertThrows(AssetNameValidationMaskDoesNotMatchExampleException.class, () -> assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user));

    request.setAsset_name_validation_mask(null);
    request.setAsset_name_validation_mask_example(Optional.of("new value"));
    assertThrows(AssetNameValidationMaskDoesNotMatchExampleException.class, () -> assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user));


    request.setAsset_name_validation_mask(Optional.of("^new value.*"));
    request.setAsset_name_validation_mask_example(Optional.of("new value"));
    assertDoesNotThrow(() -> assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user));

    PatchAssetTypeRequest secondRequest = new PatchAssetTypeRequest(null, null, null, null, Optional.of("new value"), null);
    AssetType secondAssetType = assetTypeRepository.save(new AssetType("asset_type_2", "asset_description_1", "1", "AT1", null, null, language, user));

    assertThrows(AssetNameValidationMaskValidationException.class, () -> assetTypesService.updateAssetType(secondAssetType.getAssetTypeId(), secondRequest, user));

    secondRequest.setAsset_name_validation_mask_example(Optional.of("new value"));
    assertDoesNotThrow(() -> assetTypesService.updateAssetType(secondAssetType.getAssetTypeId(), secondRequest, user));
  }

  @Test
  public void updateAssetTypeClearAssetNameValidationMaskIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(Optional.of("new asset type"), null, null, null, Optional.empty(), Optional.empty());
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", "Hello", "^Hello.*", language, user));

    PostAssetTypeResponse postAssetTypeResponse = assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user);

    assertAll(
      () -> assertNull(postAssetTypeResponse.getAsset_name_validation_mask()),
      () -> assertNull(postAssetTypeResponse.getAsset_name_validation_mask_example())
    );
  }

  @Test
  public void updateAssetTypeClearNameIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(Optional.empty(), null, null, null, null, null);
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    PostAssetTypeResponse updatedAssetType = assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user);

    assertEquals(assetType.getAssetTypeName(), updatedAssetType.getAsset_type_name());
  }

  @Test
  public void updateRoleChangeRoleNameIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(Optional.of("some new name"), null, null, null, null, null);
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    PostAssetTypeResponse updatedAssetType = assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user);

    assertEquals("some new name", updatedAssetType.getAsset_type_name());
  }

  @Test
  public void updateRoleDoNothingWithRoleNameIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(null, Optional.empty(), null, null, null, null);
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    PostAssetTypeResponse updatedAssetType = assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user);

    assertEquals("asset_type_1", updatedAssetType.getAsset_type_name());
  }

  @Test
  public void updateRoleClearDescriptionIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(null, Optional.empty(), null, null, null, null);
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    PostAssetTypeResponse updatedAssetType = assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user);

    assertNull(updatedAssetType.getAsset_type_description());
  }

  @Test
  public void updateRoleChangeDescriptionIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(null, Optional.of("some new description"), null, null, null, null);
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    PostAssetTypeResponse updatedAssetType = assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user);

    assertEquals("some new description", updatedAssetType.getAsset_type_description());
  }

  @Test
  public void updateRoleRoleDoNothingWithDescriptionIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(Optional.of("some new name"), null, null, null, null, null);
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));

    PostAssetTypeResponse updatedAssetType = assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user);

    assertEquals("asset_description_1", updatedAssetType.getAsset_type_description());
  }

  @Test
  public void updateAssetTypeWithSameNameExistsIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(Optional.of("new asset type"), null, null, null, null, null);
    AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));
    assetTypeRepository.save(new AssetType("new asset type", "asset_description_1", "1", "AT1", language, user));

    assertThrows(DataIntegrityViolationException.class, () -> assetTypesService.updateAssetType(assetType.getAssetTypeId(), request, user));
  }

  @Test
  public void updateAssetTypeAssetTypeNotExistsIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(Optional.of("new asset type"), null, null, null, null, null);

    assertThrows(AssetTypeNotFoundException.class, () -> assetTypesService.updateAssetType(new UUID(123, 123), request, user));
  }

  @Test
  public void updateAssetTypeDeletedAssetErrorIntegrationTest () {
    PatchAssetTypeRequest request = new PatchAssetTypeRequest(Optional.of("new asset type"), null, null, null, null, null);
    AssetType assetType = new AssetType(request.getAsset_type_name().get(), "asset_description_1", "1", "AT1", language, user);
    assetType.setIsDeleted(true);
    AssetType updatedAssetType = assetTypeRepository.save(assetType);

    assertThrows(AssetTypeNotFoundException.class, () -> assetTypesService.updateAssetType(updatedAssetType.getAssetTypeId(), request, user));
  }

  public AssetTypeAttributeTypeAssignment createAssetTypeAttributeTypeAssignments (AssetType assetType) {
    AttributeType attributeType = attributeTypeRepository.save(new AttributeType("attribute type", "description", AttributeKindType.BOOLEAN, null, null, language, user));
    return assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(assetType, attributeType, user));
  }

  public AssetTypeStatusAssignment createAssetTypeStatusAssignments (AssetType assetType) {
    Status status = statusRepository.save(new Status("some name", "some description", language, user));
    return assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(assetType, AssignmentStatusType.STEWARDSHIP, status, user));
  }

  public RelationTypeComponentAssetTypeAssignment createAssetTypeRelationTypeComponentAssignments (AssetType assetType) {
    RelationTypeComponent relationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("new name", "desc", ResponsibilityInheritanceRole.CONSUMER, HierarchyRole.CHILD, null, language, null, user));
    return relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(relationTypeComponent, assetType, false, null, user));
  }

  private void generateAssetTypes (int count) {
    for (int i = 0; i < count; i++) {
      AssetType assetType = assetTypeRepository.save(new AssetType("asset_type_" + i, "asset_description" + i, "" + i, "AT"+i, language, user));
    }
  }
}
