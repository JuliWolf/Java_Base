package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.RelationTypesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.post.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.InvalidFieldLengthException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.ActionTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetHierarchy.AssetHierarchyRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationType.attributeTypes.RelationTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.attributeTypes.RelationTypeComponentAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roleActions.RoleActionRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.RelationTypeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.models.get.GetRelationTypeComponentWithAssignmentsResponse;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

public class RelationTypesServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private RelationTypesService relationTypesService;

  @Autowired
  private RelationTypeRepository relationTypeRepository;

  @Autowired
  private EntityRepository entityRepository;
  @Autowired
  private ActionTypeRepository actionTypeRepository;
  @Autowired
  private RoleActionRepository roleActionRepository;
  @Autowired
  private RoleRepository roleRepository;
  @Autowired
  private RelationTypeComponentRepository relationTypeComponentRepository;
  @Autowired
  private RelationTypeAttributeTypeAssignmentRepository relationTypeAttributeTypeAssignmentRepository;
  @Autowired
  private RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;
  @Autowired
  private AttributeTypeRepository attributeTypeRepository;
  @Autowired
  private AssetTypeRepository assetTypeRepository;
  @Autowired
  private RelationRepository relationRepository;
  @Autowired
  private RelationComponentRepository relationComponentRepository;
  @Autowired
  private ResponsibilityRepository responsibilityRepository;
  @Autowired
  private AssetRepository assetRepository;
  @Autowired
  private AssetHierarchyRepository assetHierarchyRepository;
  @Autowired
  private RelationTypeComponentAttributeTypeAssignmentRepository relationTypeComponentAttributeTypeAssignmentRepository;

  @AfterEach
  public void clearData () {
    assetHierarchyRepository.deleteAll();
    relationComponentRepository.deleteAll();
    responsibilityRepository.deleteAll();
    relationRepository.deleteAll();
    assetRepository.deleteAll();
    roleRepository.deleteAll();
    relationTypeComponentAttributeTypeAssignmentRepository.deleteAll();
    relationTypeAttributeTypeAssignmentRepository.deleteAll();
    relationTypeComponentAssetTypeAssignmentRepository.deleteAll();
    relationTypeComponentRepository.deleteAll();
    relationTypeRepository.deleteAll();
    assetTypeRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    roleActionRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @Test
  public void createRelationTypeComponentRequiredFieldsAreEmptyIntegrationTest () {
    try {
      PostRelationTypeComponentRequest sourceComponent = new PostRelationTypeComponentRequest(null, "desc", ResponsibilityInheritanceRole.SOURCE.name(), null, null);
      PostRelationTypeComponentRequest consumerComponent = new PostRelationTypeComponentRequest("some name", "desc", ResponsibilityInheritanceRole.CONSUMER.name(), null, null);
      List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
      componentsList.add(sourceComponent);
      componentsList.add(consumerComponent);

      PostRelationTypeRequest request = new PostRelationTypeRequest("some name", "desc", 2, true, false, true, false, componentsList);

      assertThrows(SomeRequiredFieldsAreEmptyException.class, () -> relationTypesService.createRelationType(request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createRelationTypeComponentHasNoResponsibilityRoleIntegrationTest () {
    try {
      PostRelationTypeComponentRequest sourceComponent = new PostRelationTypeComponentRequest("connected to", "desc", null, null, null);
      PostRelationTypeComponentRequest consumerComponent = new PostRelationTypeComponentRequest("some name", "desc", ResponsibilityInheritanceRole.CONSUMER.name(), null, null);
      List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
      componentsList.add(sourceComponent);
      componentsList.add(consumerComponent);

      PostRelationTypeRequest request = new PostRelationTypeRequest("some name", "desc", 2, true, false, true, false, componentsList);

      assertThrows(IncorrectRoleForResponsibilityInheritanceException.class, () -> relationTypesService.createRelationType(request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createRelationTypeComponentHas2ConsumerResponsibilityRoleIntegrationTest () {
    try {
      PostRelationTypeComponentRequest consumerComponent = new PostRelationTypeComponentRequest("connected to", "desc", ResponsibilityInheritanceRole.CONSUMER.name(), null, null);
      List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
      componentsList.add(consumerComponent);
      componentsList.add(consumerComponent);

      PostRelationTypeRequest request = new PostRelationTypeRequest("some name", "desc", 2, true, false, true, false, componentsList);

      assertThrows(IncorrectRoleForResponsibilityInheritanceException.class, () -> relationTypesService.createRelationType(request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createRelationTypeHasNoHierarchyRoleIntegrationTest () {
    try {
      PostRelationTypeComponentRequest childComponent = new PostRelationTypeComponentRequest("connected to", "desc", null, HierarchyRole.CHILD.name(), null);
      PostRelationTypeComponentRequest parentComponent = new PostRelationTypeComponentRequest("some name", "desc", null, null, null);
      List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
      componentsList.add(childComponent);
      componentsList.add(parentComponent);

      PostRelationTypeRequest request = new PostRelationTypeRequest("some name", "desc", 2, false, true, true, false, componentsList);

      assertThrows(IncorrectRoleInHierarchyException.class, () -> relationTypesService.createRelationType(request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createRelationTypeHas2ChildHierarchyRoleIntegrationTest () {
    try {
      PostRelationTypeComponentRequest childComponent = new PostRelationTypeComponentRequest("connected to", "desc", null, HierarchyRole.CHILD.name(), null);
      List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
      componentsList.add(childComponent);
      componentsList.add(childComponent);

      PostRelationTypeRequest request = new PostRelationTypeRequest("some name", "desc", 2, false, true, true, false, componentsList);

      assertThrows(IncorrectRoleInHierarchyException.class, () -> relationTypesService.createRelationType(request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createRelationTypeRelationTypeAlreadyExistsIntegrationTest () {
    RelationType relationType = relationTypeRepository.save(new RelationType("some name", "desc", 2, false, false, language, user));

    PostRelationTypeComponentRequest childComponent = new PostRelationTypeComponentRequest("connected to", "desc", null, HierarchyRole.CHILD.name(), null);
    PostRelationTypeComponentRequest parentComponent = new PostRelationTypeComponentRequest("some name", "desc", null, HierarchyRole.PARENT.name(), null);
    List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
    componentsList.add(childComponent);
    componentsList.add(parentComponent);

    PostRelationTypeRequest request = new PostRelationTypeRequest("some name", "desc", 2, false, true, true, false, componentsList);

    assertThrows(DataIntegrityViolationException.class, () -> relationTypesService.createRelationType(request, user));
  }

  @Test
  public void createRelationTypeLongRelationTypeComponentDescriptionIntegrationTest () {
    try {
      PostRelationTypeComponentRequest childComponent = new PostRelationTypeComponentRequest("connected to", StringUtils.repeat('f', 513), null, HierarchyRole.CHILD.name(), null);
      PostRelationTypeComponentRequest parentComponent = new PostRelationTypeComponentRequest("some name", "desc", null, HierarchyRole.PARENT.name(), null);
      List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
      componentsList.add(childComponent);
      componentsList.add(parentComponent);

      PostRelationTypeRequest request = new PostRelationTypeRequest("some name", "desc", 2, false, true, true, false, componentsList);

      assertThrows(InvalidFieldLengthException.class, () -> relationTypesService.createRelationType(request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createRelationTypeLongRelationTypeComponentNameIntegrationTest () {
    try {
      PostRelationTypeComponentRequest childComponent = new PostRelationTypeComponentRequest(StringUtils.repeat('f', 256), "desc", null, HierarchyRole.CHILD.name(), null);
      PostRelationTypeComponentRequest parentComponent = new PostRelationTypeComponentRequest("some name", "desc", null, HierarchyRole.PARENT.name(), null);
      List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
      componentsList.add(childComponent);
      componentsList.add(parentComponent);

      PostRelationTypeRequest request = new PostRelationTypeRequest("some name", "desc", 2, false, true, true, false, componentsList);

      assertThrows(InvalidFieldLengthException.class, () -> relationTypesService.createRelationType(request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void createRelationTypeSuccessIntegrationTest () {
    try {
      PostRelationTypeComponentRequest childComponent = new PostRelationTypeComponentRequest("connected to", "desc", null, HierarchyRole.CHILD.name(), null);
      PostRelationTypeComponentRequest parentComponent = new PostRelationTypeComponentRequest("some name", "desc", null, HierarchyRole.PARENT.name(), null);
      List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
      componentsList.add(childComponent);
      componentsList.add(parentComponent);

      PostRelationTypeRequest request = new PostRelationTypeRequest("some name", "desc", 2, false, true, true, false, componentsList);

      PostRelationTypeResponse response = relationTypesService.createRelationType(request, user);

      assertAll(
        () -> assertEquals(2, response.getRelation_type_component().size()),
        () -> assertEquals(request.getRelation_type_name(), response.getRelation_type_name()),
        () -> assertEquals(user.getUserId(), response.getCreated_by())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeRelationTypeNotFoundIntegrationTest () {
    try {
      PatchRelationTypeRequest request = new PatchRelationTypeRequest();
      assertThrows(RelationTypeNotFoundException.class, () -> relationTypesService.updateRelationType(UUID.randomUUID(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateRelationTypeNameAndDescriptionIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(Optional.of("new name"), Optional.of("new desc"), null, null, null, null, null);

      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      assertAll(
        () -> assertNotEquals(relationType.getRelationTypeName(), response.getRelation_type_name()),
        () -> assertNotEquals(relationType.getRelationTypeDescription(), response.getRelation_type_description())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeClearRelationTypeNameIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(Optional.empty(), Optional.of("new desc"), null, null, null, null, null);

      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      assertEquals("name", response.getRelation_type_name());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeDoNothingWithRelationTypeNameIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, null, null, null, null);

      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      assertEquals("name", response.getRelation_type_name());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeClearRelationTypeDescriptionIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(Optional.of("new name"), Optional.empty(), null, null, null, null, null);

      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      assertNull(response.getRelation_type_description());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeDoNothingWithRelationTypeDescriptionIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(Optional.of("new name"), null, null, null, null, null, null);

      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      assertEquals("description", response.getRelation_type_description());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateHierarchyFlagFromTrueToFalseIntegrationTest () {
    PostRelationTypeComponentRequest childComponent = new PostRelationTypeComponentRequest("connected to", "desc", null, HierarchyRole.CHILD.name(), null);
    PostRelationTypeComponentRequest parentComponent = new PostRelationTypeComponentRequest("some name", "desc", null, HierarchyRole.PARENT.name(), null);
    List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
    componentsList.add(childComponent);
    componentsList.add(parentComponent);

    PostRelationTypeResponse postResponse = relationTypesService.createRelationType(new PostRelationTypeRequest("name", "description", 2, false, true, true, false, componentsList), user);

    PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, false, null, null, null);

    RelationTypeResponse response = relationTypesService.updateRelationType(postResponse.getRelation_type_id(), request, user);

    List<RelationTypeComponent> allRelationTypeComponents = relationTypeComponentRepository.findAll();

    assertAll(
      () -> assertNotEquals(postResponse.getRelation_type_description(), response.getRelation_type_description()),
      () -> assertFalse(response.getHierarchy_flag()),
      () -> assertEquals(2, allRelationTypeComponents.stream().filter(c -> c.getHierarchyRole() == null).toList().size()),
      () -> assertEquals(0, response.getRelation_type_component().size())
    );
  }

  @Test
  public void updateRelationTypeLongRelationTypeComponentNameIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, true, language, user));
      RelationTypeComponent childComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, HierarchyRole.CHILD, null, language, relationType, user));
      RelationTypeComponent parentComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, HierarchyRole.PARENT, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedChildComponent = new PatchRelationTypeComponentRequest(childComponent.getRelationTypeComponentId().toString(), Optional.of(StringUtils.repeat('d', 256)), Optional.of("new desc"), null, HierarchyRole.CHILD.name(), null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedChildComponent);
      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, false, null, null, components);

      assertThrows(InvalidFieldLengthException.class, () -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateHierarchyFlagFromFalseToTrueWithMoreThan2ComponentsIntegrationTest () {
    try {
      PostRelationTypeComponentRequest childComponent = new PostRelationTypeComponentRequest("connected to", "desc", null, null, null);
      PostRelationTypeComponentRequest parentComponent = new PostRelationTypeComponentRequest("some name", "desc", null, null, null);
      PostRelationTypeComponentRequest someOtherComponent = new PostRelationTypeComponentRequest("some name", "desc", null, null, null);
      List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
      componentsList.add(childComponent);
      componentsList.add(parentComponent);
      componentsList.add(someOtherComponent);

      PostRelationTypeResponse postResponse = relationTypesService.createRelationType(new PostRelationTypeRequest("name", "description", 3, false, false, true, false, componentsList), user);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, true, null, null, null);

      assertThrows(InvalidNumberOfComponentsException.class, () -> relationTypesService.updateRelationType(postResponse.getRelation_type_id(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateHierarchyFlagFromFalseToTrueIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent childComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent parentComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedChildComponent = new PatchRelationTypeComponentRequest(childComponent.getRelationTypeComponentId().toString(), null, Optional.of("new desc"), null, HierarchyRole.CHILD.name(), null);
      PatchRelationTypeComponentRequest updatedParentComponent = new PatchRelationTypeComponentRequest(parentComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, null, HierarchyRole.PARENT.name(), null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedChildComponent);
      components.add(updatedParentComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, true, null, null, components);

      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);
      Optional<RelationTypeComponent> foundChildComponent = relationTypeComponentRepository.findById(childComponent.getRelationTypeComponentId());
      Optional<RelationTypeComponent> foundParentComponent = relationTypeComponentRepository.findById(parentComponent.getRelationTypeComponentId());

      assertAll(
        () -> assertEquals(1, response.getRelation_type_component().stream().filter(c -> updatedChildComponent.getRelation_type_component_description().get().equals(c.getRelation_type_component_description())).toList().size()),
        () -> assertEquals(1, response.getRelation_type_component().stream().filter(c -> updatedParentComponent.getRelation_type_component_name().get().equals(c.getRelation_type_component_name())).toList().size()),
        () -> assertTrue(request.getHierarchy_flag()),
        () -> assertEquals(HierarchyRole.CHILD, foundChildComponent.get().getHierarchyRole()),
        () -> assertEquals(HierarchyRole.PARENT, foundParentComponent.get().getHierarchyRole())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeLongRelationTypeDescriptionIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent childComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent parentComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedChildComponent = new PatchRelationTypeComponentRequest(childComponent.getRelationTypeComponentId().toString(), null, Optional.of(StringUtils.repeat('h', 513)), null, HierarchyRole.CHILD.name(), null);
      PatchRelationTypeComponentRequest updatedParentComponent = new PatchRelationTypeComponentRequest(parentComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, null, HierarchyRole.PARENT.name(), null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedChildComponent);
      components.add(updatedParentComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, true, null, null, components);

      assertThrows(InvalidFieldLengthException.class, () -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeClearRelationTypeComponentNameIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent childComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent parentComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedChildComponent = new PatchRelationTypeComponentRequest(childComponent.getRelationTypeComponentId().toString(), Optional.empty(), null, null, null, null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedChildComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, null, null, null, components);
      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      assertEquals("child component", response.getRelation_type_component().get(0).getRelation_type_component_name());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeDoNothingWithRelationTypeComponentNameIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent childComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent parentComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedChildComponent = new PatchRelationTypeComponentRequest(childComponent.getRelationTypeComponentId().toString(), null, Optional.of("new name"), null, null, null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedChildComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, null, null, null, components);
      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      assertEquals("child component", response.getRelation_type_component().get(0).getRelation_type_component_name());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeClearRelationTypeComponentDescriptionIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent childComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent parentComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedChildComponent = new PatchRelationTypeComponentRequest(childComponent.getRelationTypeComponentId().toString(), null, Optional.empty(), null, null, null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedChildComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, null, null, null, components);
      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      assertNull(response.getRelation_type_component().get(0).getRelation_type_component_description());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeDoNothingWithRelationTypeComponentDescriptionIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent childComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent parentComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedChildComponent = new PatchRelationTypeComponentRequest(childComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, null, null, null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedChildComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, null, null, null, components);
      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      assertEquals("desc", response.getRelation_type_component().get(0).getRelation_type_component_description());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateHierarchyFlagFromFalseToTrueComponentNotFoundIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent childComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent parentComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedChildComponent = new PatchRelationTypeComponentRequest(UUID.randomUUID().toString(), null, Optional.of("new desc"), null, HierarchyRole.CHILD.name(), null);
      PatchRelationTypeComponentRequest updatedParentComponent = new PatchRelationTypeComponentRequest(parentComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, null, HierarchyRole.PARENT.name(), null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedChildComponent);
      components.add(updatedParentComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, true, null, null, components);

      assertThrows(RelationTypeComponentNotFoundException.class, () -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateHierarchyFlagFromFalseToTrueIncorrectRoleInHierarchyExceptionIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent childComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent parentComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedChildComponent = new PatchRelationTypeComponentRequest(childComponent.getRelationTypeComponentId().toString(), null, Optional.of("new desc"), null, HierarchyRole.PARENT.name(), null);
      PatchRelationTypeComponentRequest updatedParentComponent = new PatchRelationTypeComponentRequest(parentComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, null, HierarchyRole.PARENT.name(), null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedChildComponent);
      components.add(updatedParentComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, true, null, null, components);

      assertThrows(IncorrectRoleInHierarchyException.class, () -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateHierarchyFlagFromFalseToTrueIncorrectRoleIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent childComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent parentComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedChildComponent = new PatchRelationTypeComponentRequest(childComponent.getRelationTypeComponentId().toString(), null, Optional.of("new desc"), null, "Some illegal role", null);
      PatchRelationTypeComponentRequest updatedParentComponent = new PatchRelationTypeComponentRequest(parentComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, null, HierarchyRole.PARENT.name(), null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedChildComponent);
      components.add(updatedParentComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, true, null, null, components);

      assertThrows(IncorrectRoleInHierarchyException.class, () -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateResponsibilityFlagFromFalseToTrueWithMoreThan2ComponentsIntegrationTest () {
    try {
      PostRelationTypeComponentRequest childComponent = new PostRelationTypeComponentRequest("connected to", "desc", null, null, null);
      PostRelationTypeComponentRequest parentComponent = new PostRelationTypeComponentRequest("some name", "desc", null, null, null);
      PostRelationTypeComponentRequest someOtherComponent = new PostRelationTypeComponentRequest("some name", "desc", null, null, null);
      List<PostRelationTypeComponentRequest> componentsList = new ArrayList<>();
      componentsList.add(childComponent);
      componentsList.add(parentComponent);
      componentsList.add(someOtherComponent);

      PostRelationTypeResponse postResponse = relationTypesService.createRelationType(new PostRelationTypeRequest("name", "description", 3, false, false, true, false, componentsList), user);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), true, false, null, null, null);

      assertThrows(InvalidNumberOfComponentsException.class, () -> relationTypesService.updateRelationType(postResponse.getRelation_type_id(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateResponsibilityFlagFromFalseToTrueIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(consumerComponent.getRelationTypeComponentId().toString(), null, Optional.of("new desc"), ResponsibilityInheritanceRole.CONSUMER.name(), null, null);
      PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, ResponsibilityInheritanceRole.SOURCE.name(), null, null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedConsumerComponent);
      components.add(updatedSourceComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), true, false, null, null, components);

      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);
      Optional<RelationTypeComponent> foundConsumerComponent = relationTypeComponentRepository.findById(consumerComponent.getRelationTypeComponentId());
      Optional<RelationTypeComponent> foundSourceComponent = relationTypeComponentRepository.findById(sourceComponent.getRelationTypeComponentId());

      assertAll(
        () -> assertTrue(request.getResponsibility_inheritance_flag()),
        () -> assertEquals(ResponsibilityInheritanceRole.CONSUMER, foundConsumerComponent.get().getResponsibilityInheritanceRole()),
        () -> assertEquals(ResponsibilityInheritanceRole.SOURCE, foundSourceComponent.get().getResponsibilityInheritanceRole())
      );
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateResponsibilityFlagFromFalseToTrueComponentNotFoundIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(UUID.randomUUID().toString(), null, Optional.of("new desc"), ResponsibilityInheritanceRole.CONSUMER.name(), null, null);
      PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, ResponsibilityInheritanceRole.SOURCE.name(), null, null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedConsumerComponent);
      components.add(updatedSourceComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), true, false, null, null, components);

      assertThrows(RelationTypeComponentNotFoundException.class, () -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateResponsibilityFlagFromFalseToTrueIncorrectRoleInHierarchyExceptionIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(UUID.randomUUID().toString(), null, Optional.of("new desc"), ResponsibilityInheritanceRole.SOURCE.name(), null, null);
      PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, ResponsibilityInheritanceRole.SOURCE.name(), null, null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedConsumerComponent);
      components.add(updatedSourceComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), true, false, null, null, components);

      assertThrows(IncorrectRoleForResponsibilityInheritanceException.class, () -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateResponsibilityFlagFromFalseToTrueIncorrectRoleIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(UUID.randomUUID().toString(), null, Optional.of("new desc"), "Some invalid role", null, null);
      PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, ResponsibilityInheritanceRole.SOURCE.name(), null, null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedConsumerComponent);
      components.add(updatedSourceComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), true, false, null, null, components);

      assertThrows(IncorrectRoleForResponsibilityInheritanceException.class, () -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateFlagFromFalseToTrueWithNoComponentsIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(UUID.randomUUID().toString(), null, Optional.of("new desc"), ResponsibilityInheritanceRole.CONSUMER.name(), null, null);
      PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, ResponsibilityInheritanceRole.SOURCE.name(), null, null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedConsumerComponent);
      components.add(updatedSourceComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), true, false, null, null, null);

      assertThrows(InvalidNumberOfComponentsException.class, () -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateUniquenessFlagFromFalseToTrueRelationsNotConnectedIntegrationTest () {
    RelationType relationType = new RelationType("name", "description", 2, false, false, language, user);
    relationType.setUniquenessFlag(false);
    relationTypeRepository.save(relationType);
    RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
    RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

    Asset firsdtAsset = assetRepository.save(new Asset("empty status", null, "stewardship name", language,  null, null, user));

    PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(UUID.randomUUID().toString(), null, Optional.of("new desc"), ResponsibilityInheritanceRole.CONSUMER.name(), null, null);
    PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, ResponsibilityInheritanceRole.SOURCE.name(), null, null);
    ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
    components.add(updatedConsumerComponent);
    components.add(updatedSourceComponent);

    PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, null, true, null, null);

    assertDoesNotThrow(() -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
  }

  @Test
  public void updateRelationTypeUpdateSelfRelatedFlagFromTrueToFalseIntegrationTest () {
    RelationType relationType = new RelationType("name", "description", 2, false, false, language, user);
    relationType.setSelfRelatedFlag(true);
    relationTypeRepository.save(relationType);
    RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
    RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

    Asset firsdtAsset = assetRepository.save(new Asset("empty status", null, "stewardship name", language,  null, null, user));

    Relation relation = relationRepository.save(new Relation(relationType, user));
    RelationComponent firstRelationComponent = relationComponentRepository.save(new RelationComponent(relation, consumerComponent, firsdtAsset, null, null, user));
    RelationComponent secondRelationComponent = relationComponentRepository.save(new RelationComponent(relation, sourceComponent, firsdtAsset, null, null, user));

    PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(UUID.randomUUID().toString(), null, Optional.of("new desc"), ResponsibilityInheritanceRole.CONSUMER.name(), null, null);
    PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, ResponsibilityInheritanceRole.SOURCE.name(), null, null);
    ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
    components.add(updatedConsumerComponent);
    components.add(updatedSourceComponent);

    PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, null, null, false, null);

    assertThrows(SelfRelatedAssetExistsException.class, () -> relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user));
  }

  @Test
  public void updateRelationTypeUpdateRelationComponentsWithNewRolesIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, false, language, user));
      RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, null, null, language, relationType, user));
      RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, null, null, language, relationType, user));

      Asset firsdtAsset = assetRepository.save(new Asset("empty status", null, "stewardship name", language,  null, null, user));
      Asset secondAsset = assetRepository.save(new Asset("some other asset", null, "stewardship name", language,  null, null, user));

      Relation relation = relationRepository.save(new Relation(relationType, user));
      RelationComponent firstRelationComponent = relationComponentRepository.save(new RelationComponent(relation, consumerComponent, firsdtAsset, null, null, user));
      RelationComponent secondRelationComponent = relationComponentRepository.save(new RelationComponent(relation, sourceComponent, secondAsset, null, null, user));

      PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(consumerComponent.getRelationTypeComponentId().toString(), null, Optional.of("new desc"), ResponsibilityInheritanceRole.CONSUMER.name(), HierarchyRole.CHILD.name(), null);
      PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, ResponsibilityInheritanceRole.SOURCE.name(), HierarchyRole.PARENT.name(), null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedConsumerComponent);
      components.add(updatedSourceComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), true, true, null, null, components);

      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      Optional<RelationComponent> updatedFirstRelationComponent = relationComponentRepository.findById(firstRelationComponent.getRelationComponentId());
      Optional<RelationComponent> updatedSecondRelationComponent = relationComponentRepository.findById(secondRelationComponent.getRelationComponentId());

      assertAll(
        () -> assertEquals(ResponsibilityInheritanceRole.CONSUMER, updatedFirstRelationComponent.get().getResponsibilityInheritanceRole()),
        () -> assertEquals(ResponsibilityInheritanceRole.SOURCE, updatedSecondRelationComponent.get().getResponsibilityInheritanceRole()),
        () -> assertEquals(HierarchyRole.CHILD, updatedFirstRelationComponent.get().getHierarchyRole()),
        () -> assertEquals(HierarchyRole.PARENT, updatedSecondRelationComponent.get().getHierarchyRole())
      );

    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeUpdateRelationComponentsClearRolesIntegrationTest () {
    try {
      RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, true, true, language, user));
      RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", ResponsibilityInheritanceRole.CONSUMER, HierarchyRole.CHILD, null, language, relationType, user));
      RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", ResponsibilityInheritanceRole.SOURCE, HierarchyRole.PARENT, null, language, relationType, user));

      Relation relation = relationRepository.save(new Relation(relationType, user));
      RelationComponent firstRelationComponent = relationComponentRepository.save(new RelationComponent(relation, consumerComponent, null, HierarchyRole.CHILD, ResponsibilityInheritanceRole.CONSUMER, user));
      RelationComponent secondRelationComponent = relationComponentRepository.save(new RelationComponent(relation, sourceComponent, null, HierarchyRole.PARENT, ResponsibilityInheritanceRole.SOURCE, user));

      PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(consumerComponent.getRelationTypeComponentId().toString(), null, Optional.of("new desc"), null, null, null);
      PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, null, null, null);
      ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
      components.add(updatedConsumerComponent);
      components.add(updatedSourceComponent);

      PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), false, false, null, null, components);

      RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

      Optional<RelationComponent> updatedFirstRelationComponent = relationComponentRepository.findById(firstRelationComponent.getRelationComponentId());
      Optional<RelationComponent> updatedSecondRelationComponent = relationComponentRepository.findById(secondRelationComponent.getRelationComponentId());

      assertAll(
        () -> assertNull(updatedFirstRelationComponent.get().getResponsibilityInheritanceRole()),
        () -> assertNull(updatedSecondRelationComponent.get().getResponsibilityInheritanceRole()),
        () -> assertNull(updatedFirstRelationComponent.get().getHierarchyRole()),
        () -> assertNull(updatedSecondRelationComponent.get().getHierarchyRole())
      );

    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void updateRelationTypeClearConnectedResponsibilitiesIntegrationTest () {
    RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, true, true, language, user));
    RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", ResponsibilityInheritanceRole.CONSUMER, HierarchyRole.CHILD, null, language, relationType, user));
    RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", ResponsibilityInheritanceRole.SOURCE, HierarchyRole.PARENT, null, language, relationType, user));

    Relation relation = relationRepository.save(new Relation(relationType, user));
    RelationComponent firstRelationComponent = relationComponentRepository.save(new RelationComponent(relation, consumerComponent, null, HierarchyRole.CHILD, ResponsibilityInheritanceRole.CONSUMER, user));
    RelationComponent secondRelationComponent = relationComponentRepository.save(new RelationComponent(relation, sourceComponent, null, HierarchyRole.PARENT, ResponsibilityInheritanceRole.SOURCE, user));

    Asset asset = assetRepository.save(new Asset("empty status", null, "stewardship name", language,  null, null, user));
    Role role = roleRepository.save(new Role( "role name", "some desc", null, user));
    Responsibility responsibility = new Responsibility(user, null, asset, role, ResponsibleType.USER, user);
    responsibility.setRelation(relation);
    responsibilityRepository.save(responsibility);

    PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(consumerComponent.getRelationTypeComponentId().toString(), null, Optional.of("new desc"), null, null, null);
    PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, null, null, null);
    ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
    components.add(updatedConsumerComponent);
    components.add(updatedSourceComponent);

    PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), false, false, null, null, components);

    RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

    Optional<Responsibility> deletedResponsibility = responsibilityRepository.findById(responsibility.getResponsibilityId());

    assertAll(
      () -> assertTrue(deletedResponsibility.get().getIsDeleted(), "is responsibility deleted"),
      () -> assertNotNull(deletedResponsibility.get().getDeletedOn(), "responsibility deleted on"),
      () -> assertEquals(user.getUserId(), deletedResponsibility.get().getDeletedBy().getUserId(), "responsibility deleted by")
    );
  }

  @Test
  public void updateRelationTypeCreateAssetHierarchyIntegrationTest () {
    RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, true, false, language, user));
    RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, relationType, user));
    RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, relationType, user));

    Asset parentAsset = assetRepository.save(new Asset("parent asset", null, "stewardship name", language,  null, null, user));
    Asset childAsset = assetRepository.save(new Asset("child asset", null, "stewardship name", language,  null, null, user));
    Relation relation = relationRepository.save(new Relation(relationType, user));
    RelationComponent firstRelationComponent = relationComponentRepository.save(new RelationComponent(relation, consumerComponent, childAsset, null, ResponsibilityInheritanceRole.CONSUMER, user));
    RelationComponent secondRelationComponent = relationComponentRepository.save(new RelationComponent(relation, sourceComponent, parentAsset, null, ResponsibilityInheritanceRole.SOURCE, user));

    PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(consumerComponent.getRelationTypeComponentId().toString(), null, Optional.of("new desc"), null, HierarchyRole.CHILD.name(), null);
    PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, null, HierarchyRole.PARENT.name(), null);
    ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
    components.add(updatedConsumerComponent);
    components.add(updatedSourceComponent);

    PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), false, true, null, null, components);

    RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

    List<AssetHierarchy> assetHierarchies = assetHierarchyRepository.findAll();

    assertAll(
      () -> assertEquals(1, assetHierarchies.size()),
      () -> assertEquals(parentAsset.getAssetId(), assetHierarchies.get(0).getParentAsset().getAssetId()),
      () -> assertEquals(childAsset.getAssetId(), assetHierarchies.get(0).getChildAsset().getAssetId())
    );
  }

  @Test
  public void updateRelationType_DeleteAssetHierarchy_Integration_Test () {
    RelationType relationType = relationTypeRepository.save(new RelationType("name", "description", 2, false, true, language, user));
    RelationTypeComponent consumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("child component", "desc", null, HierarchyRole.CHILD, null, language, relationType, user));
    RelationTypeComponent sourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("some name", "desc", null, HierarchyRole.PARENT, null, language, relationType, user));

    Asset parentAsset = assetRepository.save(new Asset("parent asset", null, "stewardship name", language,  null, null, user));
    Asset childAsset = assetRepository.save(new Asset("child asset", null, "stewardship name", language,  null, null, user));
    Relation relation = relationRepository.save(new Relation(relationType, user));
    RelationComponent firstRelationComponent = relationComponentRepository.save(new RelationComponent(relation, consumerComponent, childAsset, HierarchyRole.CHILD, null, user));
    RelationComponent secondRelationComponent = relationComponentRepository.save(new RelationComponent(relation, sourceComponent, parentAsset, HierarchyRole.PARENT, null, user));

    AssetHierarchy assetHierarchy = assetHierarchyRepository.save(new AssetHierarchy(parentAsset, childAsset, relation, user));
    PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(consumerComponent.getRelationTypeComponentId().toString(), null, Optional.of("new desc"), null, null, null);
    PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(sourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, null, null, null);
    ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
    components.add(updatedConsumerComponent);
    components.add(updatedSourceComponent);

    PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), null, false, null, null, components);

    RelationTypeResponse response = relationTypesService.updateRelationType(relationType.getRelationTypeId(), request, user);

    Optional<AssetHierarchy> deletedAssetHierarchy = assetHierarchyRepository.findById(assetHierarchy.getAssetHierarchyId());

    assertAll(
      () -> assertTrue(deletedAssetHierarchy.get().getIsDeleted()),
      () -> assertNotNull(deletedAssetHierarchy.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedAssetHierarchy.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void updateRelationType_CreateResponsibilities_Integration_Test () {
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("first", "description", 2, false, false, language, user));
    RelationTypeComponent firstConsumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first consumer component", "desc", null, null, null, language, firstRelationType, user));
    RelationTypeComponent firstSourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first source component", "desc", null, null, null, language, firstRelationType, user));

    Asset firstAssetSource = assetRepository.save(new Asset("first asset source", null, "stewardship prefix", language,  null, null, user));
    Asset firstAssetConsumer = assetRepository.save(new Asset("first asset consumer", null, "stewardship prefix", language,  null, null, user));
    Relation firstRelation = relationRepository.save(new Relation(firstRelationType, user));
    // source 1 -> consumer 1
    RelationComponent firstSourceRelationComponent = relationComponentRepository.save(new RelationComponent(firstRelation, firstSourceComponent, firstAssetSource, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent firstConsumerRelationComponent = relationComponentRepository.save(new RelationComponent(firstRelation, firstConsumerComponent, firstAssetConsumer, null, ResponsibilityInheritanceRole.CONSUMER, user));

    RelationType secondRelationType = relationTypeRepository.save(new RelationType("second", "description", 2, true, false, language, user));
    RelationTypeComponent secondConsumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second consumer component", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, secondRelationType, user));
    RelationTypeComponent secondSourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second source component", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, secondRelationType, user));

    Asset secondAssetSource = assetRepository.save(new Asset("second asset source", null, "stewardship prefix", language,  null, null, user));
    Asset secondAssetConsumer = assetRepository.save(new Asset("second asset consumer", null, "stewardship prefix", language,  null, null, user));
    Relation secondRelation = relationRepository.save(new Relation(secondRelationType, user));
    // consumer 1 -> source 2
    RelationComponent secondSourceRelationComponent = relationComponentRepository.save(new RelationComponent(secondRelation, secondSourceComponent, firstAssetConsumer, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent secondConsumerRelationComponent = relationComponentRepository.save(new RelationComponent(secondRelation, secondSourceComponent, secondAssetSource, null, ResponsibilityInheritanceRole.CONSUMER, user));

    RelationType thirdRelationType = relationTypeRepository.save(new RelationType("third", "description", 2, true, false, language, user));
    RelationTypeComponent thirdConsumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("third consumer component", "desc", ResponsibilityInheritanceRole.CONSUMER, null, null, language, secondRelationType, user));
    RelationTypeComponent thirdSourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("third source component", "desc", ResponsibilityInheritanceRole.SOURCE, null, null, language, secondRelationType, user));

    Relation thirdRelation = relationRepository.save(new Relation(secondRelationType, user));
    // source 2 -> consumer 2
    RelationComponent thirdSourceRelationComponent = relationComponentRepository.save(new RelationComponent(thirdRelation, thirdSourceComponent, secondAssetSource, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent thirdConsumerRelationComponent = relationComponentRepository.save(new RelationComponent(thirdRelation, thirdConsumerComponent, secondAssetConsumer, null, ResponsibilityInheritanceRole.CONSUMER, user));

    Role firstRole = roleRepository.save(new Role( "firstRole name", "some desc", null, user));
    Role secondRole = roleRepository.save(new Role( "second name", "some desc", null, user));
    Role thirdRole = roleRepository.save(new Role( "third name", "some desc", null, user));
    Role forthRole = roleRepository.save(new Role( "forth name", "some desc", null, user));

    // First source asset
    responsibilityRepository.save(new Responsibility(user, null, firstAssetSource, firstRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, firstAssetSource, secondRole, ResponsibleType.USER, user));

    // first consumer asset
    responsibilityRepository.save(new Responsibility(user, null, firstAssetConsumer, thirdRole, ResponsibleType.USER, user));
    responsibilityRepository.save(new Responsibility(user, null, firstAssetConsumer, forthRole, ResponsibleType.USER, user));

    PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(firstConsumerComponent.getRelationTypeComponentId().toString(), null, Optional.of("new desc"), ResponsibilityInheritanceRole.CONSUMER.name(), null, null);
    PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(firstSourceComponent.getRelationTypeComponentId().toString(), Optional.of("new name"), null, ResponsibilityInheritanceRole.SOURCE.name(), null, null);
    ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
    components.add(updatedConsumerComponent);
    components.add(updatedSourceComponent);

    PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, Optional.of("new desc"), true, null, null, null, components);

    relationTypesService.updateRelationType(firstRelationType.getRelationTypeId(), request, user);

    List<Responsibility> responsibilities = responsibilityRepository.findAll();

    assertAll(
      () -> assertEquals(14, responsibilities.size()),
      () -> assertEquals(4, responsibilities.stream().filter(responsibility -> responsibility.getAsset().getAssetId().equals(secondAssetSource.getAssetId())).toList().size()),
      () -> assertEquals(4, responsibilities.stream().filter(responsibility -> responsibility.getAsset().getAssetId().equals(secondAssetConsumer.getAssetId())).toList().size()),
      () -> assertEquals(4, responsibilities.stream().filter(responsibility -> responsibility.getAsset().getAssetId().equals(firstAssetConsumer.getAssetId())).toList().size()),
      () -> assertEquals(2, responsibilities.stream().filter(responsibility -> responsibility.getAsset().getAssetId().equals(firstAssetSource.getAssetId())).toList().size())
    );
  }

  @Test
  public void updateRelationType_UpdateUniquenessFlagFromFalseToTrueMultipleRelationsIsConnected_Integration_Test () {
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("first", "description", 2, false, false, false, false, language, user));
    RelationTypeComponent firstConsumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first consumer component", "desc", null, null, null, language, firstRelationType, user));
    RelationTypeComponent firstSourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first source component", "desc", null, null, null, language, firstRelationType, user));

    Asset firstAssetSource = assetRepository.save(new Asset("first asset source", null, "stewardship prefix", language,  null, null, user));
    Asset firstAssetConsumer = assetRepository.save(new Asset("first asset consumer", null, "stewardship prefix", language,  null, null, user));

    Relation firstRelation = relationRepository.save(new Relation(firstRelationType, user));
    RelationComponent firstSourceRelationComponent = relationComponentRepository.save(new RelationComponent(firstRelation, firstSourceComponent, firstAssetSource, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent firstConsumerRelationComponent = relationComponentRepository.save(new RelationComponent(firstRelation, firstConsumerComponent, firstAssetConsumer, null, ResponsibilityInheritanceRole.CONSUMER, user));

    Relation secondRelation = relationRepository.save(new Relation(firstRelationType, user));
    RelationComponent secondSourceRelationComponent = relationComponentRepository.save(new RelationComponent(firstRelation, firstSourceComponent, firstAssetConsumer, null, ResponsibilityInheritanceRole.CONSUMER, user));
    RelationComponent secondConsumerRelationComponent = relationComponentRepository.save(new RelationComponent(firstRelation, firstConsumerComponent, firstAssetSource, null, ResponsibilityInheritanceRole.SOURCE, user));

    PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(firstConsumerComponent.getRelationTypeComponentId().toString(), null, null, ResponsibilityInheritanceRole.CONSUMER.name(), null, null);
    PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(firstSourceComponent.getRelationTypeComponentId().toString(), null, null, ResponsibilityInheritanceRole.SOURCE.name(), null, null);
    ArrayList<PatchRelationTypeComponentRequest> components = new ArrayList<>();
    components.add(updatedConsumerComponent);
    components.add(updatedSourceComponent);

    PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, null, true, null, true, null, components);

    assertThrows(MultipleRelationExistsWithAssetException.class, () -> relationTypesService.updateRelationType(firstRelationType.getRelationTypeId(), request, user));
  }

  @Test
  public void updateRelationType_UpdateSameAssetFlagFromFalseToTrueMultipleRelationsComponentHasThisAsset_Integration_Test () {
    RelationType firstRelationType = relationTypeRepository.save(new RelationType("first", "description", 2, false, false, false, false, language, user));
    RelationTypeComponent firstConsumerComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first consumer component", "desc", null, null, null, language, firstRelationType, user));
    RelationTypeComponent firstSourceComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first source component", "desc", null, null, null, language, firstRelationType, user));

    Asset firstAssetSource = assetRepository.save(new Asset("first asset source", null, "stewardship prefix", language,  null, null, user));
    Asset firstAssetConsumer = assetRepository.save(new Asset("first asset consumer", null, "stewardship prefix", language,  null, null, user));

    Relation firstRelation = relationRepository.save(new Relation(firstRelationType, user));
    RelationComponent firstSourceRelationComponent = relationComponentRepository.save(new RelationComponent(firstRelation, firstSourceComponent, firstAssetSource, null, ResponsibilityInheritanceRole.SOURCE, user));
    RelationComponent firstConsumerRelationComponent = relationComponentRepository.save(new RelationComponent(firstRelation, firstConsumerComponent, firstAssetConsumer, null, ResponsibilityInheritanceRole.CONSUMER, user));

    Relation secondRelation = relationRepository.save(new Relation(firstRelationType, user));
    RelationComponent secondSourceRelationComponent = relationComponentRepository.save(new RelationComponent(secondRelation, firstSourceComponent, firstAssetSource, null, ResponsibilityInheritanceRole.CONSUMER, user));
    RelationComponent secondConsumerRelationComponent = relationComponentRepository.save(new RelationComponent(secondRelation, firstConsumerComponent, firstAssetConsumer, null, ResponsibilityInheritanceRole.SOURCE, user));

    PatchRelationTypeComponentRequest updatedConsumerComponent = new PatchRelationTypeComponentRequest(firstConsumerComponent.getRelationTypeComponentId().toString(), null, null, ResponsibilityInheritanceRole.CONSUMER.name(), null, true);
    PatchRelationTypeComponentRequest updatedSourceComponent = new PatchRelationTypeComponentRequest(firstSourceComponent.getRelationTypeComponentId().toString(), null, null, ResponsibilityInheritanceRole.SOURCE.name(), null, true);
    List<PatchRelationTypeComponentRequest> components = List.of(updatedConsumerComponent, updatedSourceComponent);

    PatchRelationTypeRequest request = new PatchRelationTypeRequest(null, null, true, null, null, null, components);

    assertThrows(MultipleRelationExistsWithSameAssetException.class, () -> relationTypesService.updateRelationType(firstRelationType.getRelationTypeId(), request, user));
  }

  @Test
  public void getRelationTypeByIdRelationTypeNotFoundIntegrationTest () {
    try {
      assertThrows(RelationTypeNotFoundException.class, () -> relationTypesService.getRelationTypeById(UUID.randomUUID()));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void getRelationTypeByIdSuccessIntegrationTest () {
    RelationType relationType = relationTypeRepository.save(new RelationType("some name", "desc", 2, false, false, language, user));
    AssetType assetType = assetTypeRepository.save(new AssetType("asset name", "desc", "an", "red", language, user));
    RelationTypeComponent firstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first", "desc", null, null, null, language, relationType, user));
    RelationTypeComponentAssetTypeAssignment firstAssignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstRelationTypeComponent, assetType, false, null, user));

    RelationTypeResponse relationTypeById = relationTypesService.getRelationTypeById(relationType.getRelationTypeId());

    GetRelationTypeComponentWithAssignmentsResponse relationTypeComponentResponse = (GetRelationTypeComponentWithAssignmentsResponse) relationTypeById.getRelation_type_component().get(0);
    assertAll(
      () -> assertEquals(relationType.getRelationTypeName(), relationTypeById.getRelation_type_name()),
      () -> assertEquals(relationType.getComponentNumber(), relationTypeById.getComponent_number()),
      () -> assertEquals(1, relationTypeById.getRelation_type_component().size()),
      () -> assertEquals(1, relationTypeComponentResponse.getRelation_type_component_asset_type_assignments().size()),
      () -> assertEquals(firstAssignment.getRelationTypeComponentAssetTypeAssignmentId(), relationTypeComponentResponse.getRelation_type_component_asset_type_assignments().get(0).getRelation_type_component_asset_type_assignment_id())
    );
  }

  @Test
  public void getRelationTypesByParamsPageableIntegrationTest () {
    generateRelationTypes(130);

    assertAll(
      () -> assertEquals(0, relationTypesService.getRelationTypesByParams("s", null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(50, relationTypesService.getRelationTypesByParams(null, null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(100, relationTypesService.getRelationTypesByParams(null, null, null, null, null, null, null, 0, 170).getResults().size()),
      () -> assertEquals(1, relationTypesService.getRelationTypesByParams("110", null, null, null, null, null, null, 0, 50).getResults().size()),
      () -> assertEquals(2, relationTypesService.getRelationTypesByParams(null, null, null, null, null, null, null, 0, 2).getResults().size()),
      () -> assertEquals(0, relationTypesService.getRelationTypesByParams("11", null, null, null, null, null, null, 10, 50).getResults().size()),
      () -> assertEquals(130, relationTypesService.getRelationTypesByParams(null, null, null, null, null, null, null, 0, 10).getTotal())
    );
  }

  @Test
  public void getRelationTypesByParamsIntegrationTest () {
    RelationType someRelationType = relationTypeRepository.save(new RelationType("some name", "desc", 6, false, false, language, user));
    RelationType secondRelationType = relationTypeRepository.save(new RelationType("second relation type", "desc", 9, false, false, language, user));
    RelationType has3ComponentNumberRelationType = relationTypeRepository.save(new RelationType("NuMber 3", "desc", 3, false, false, language, user));
    RelationType hasHierarchyFlagRelationType = relationTypeRepository.save(new RelationType("hierarchy FLAG", "desc", 2, false, true, language, user));
    RelationType hasResponsibilityRelationFlagRelationType = relationTypeRepository.save(new RelationType("relation", "desc", 2, true, false, language, user));
    RelationType hasAllFlagsRelationType = relationTypeRepository.save(new RelationType("all flags", "desc", 2, true, true, language, user));
    RelationType uniquenessFlagRelationType = relationTypeRepository.save(new RelationType("uniqueness flag", "desc", 3, null, null, false, false, language, user));
    RelationType selfRelatedFlagRelationType = relationTypeRepository.save(new RelationType("self related flag", "desc", 3, null, null, false, true, language, user));

    AssetType assetType = assetTypeRepository.save(new AssetType("asset name", "desc", "an", "red", language, user));
    RelationTypeComponent firstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first", "desc", null, null, null, language, someRelationType, user));
    RelationTypeComponentAssetTypeAssignment firstAssignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstRelationTypeComponent, assetType, false, null, user));

    assertAll(
      () -> assertEquals(5, relationTypesService.getRelationTypesByParams("s", null, null, null, null, null, null, 0, 50).getResults().size(), "'s' in relation type name"),
      () -> assertEquals(3, relationTypesService.getRelationTypesByParams(null, 2, null, null, null, null, null, 0, 50).getResults().size(), "2 component number"),
      () -> assertEquals(2, relationTypesService.getRelationTypesByParams(null, null, null, true, null, null, null, 0, 50).getResults().size(), "responsibility inheritance flag = true"),
      () -> assertEquals(1, relationTypesService.getRelationTypesByParams(null, null, false, true, null, null, null, 0, 50).getResults().size(), "hierarchy flag = false; responsibility inheritance flag = true"),
      () -> assertEquals(2, relationTypesService.getRelationTypesByParams(null, null, true, null, null, null, null, 0, 50).getResults().size(), "hierarchy flag = true"),
      () -> assertEquals(2, relationTypesService.getRelationTypesByParams("flag", null, true, null, null, null, null, 0, 50).getResults().size(), "'flag' in relation type name; hierarchy flag = true"),
      () -> assertEquals(1, relationTypesService.getRelationTypesByParams("flag", null, true, false, null, null, null, 0, 50).getResults().size(), "'flag' in relation type name; hierarchy flag = true; responsibility inheritance flag = false"),
      () -> assertEquals(0, relationTypesService.getRelationTypesByParams(null, 9, true, false, null, null, null, 0, 50).getResults().size(), "9 component number; hierarchy flag = true; responsibility inheritance flag = false"),
      () -> assertEquals(1, relationTypesService.getRelationTypesByParams(null, null, null, false, assetType.getAssetTypeId(), null, null, 0, 50).getResults().size(), "responsibility inheritance flag = false; find by asset type id"),
      () -> assertEquals(0, relationTypesService.getRelationTypesByParams(null, null, null, false, UUID.randomUUID(), null, null, 0, 50).getResults().size(), "responsibility inheritance flag = false; find by random asset type id"),
      () -> assertEquals(2, relationTypesService.getRelationTypesByParams(null, null, null, null, null, null, false, 0, 50).getResults().size(), "uniqueness flag = false"),
      () -> assertEquals(1, relationTypesService.getRelationTypesByParams(null, null, null, null, null, true, null, 0, 50).getResults().size(), "self related flag = true")
    );
  }

  @Test
  public void deleteRelationTypeByIdRelationTypeNotFoundIntegrationTest () {
    assertThrows(RelationTypeNotFoundException.class, () -> relationTypesService.deleteRelationTypeById(UUID.randomUUID(), user));
  }

  @Test
  public void deleteRelationTypeByIdRelationTypeAlreadyDeletedIntegrationTest () {
    RelationType someRelationType = new RelationType("some name", "desc", 6, false, false, language, user);
    someRelationType.setIsDeleted(true);
    relationTypeRepository.save(someRelationType);

    assertThrows(RelationTypeNotFoundException.class, () -> relationTypesService.deleteRelationTypeById(someRelationType.getRelationTypeId(), user));
  }

  @Test
  public void deleteRelationTypeByIdDeleteRelationTypeComponentsIntegrationTest () {
    RelationType someRelationType = relationTypeRepository.save(new RelationType("some name", "desc", 6, false, false, language, user));
    RelationTypeComponent firstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first", "desc", null, null, null, language, someRelationType, user));
    RelationTypeComponent secondRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second", "desc", null, null, null, language, someRelationType, user));

    relationTypesService.deleteRelationTypeById(someRelationType.getRelationTypeId(), user);

    Optional<RelationTypeComponent> deletedFirstRelationTypeComponent = relationTypeComponentRepository.findById(firstRelationTypeComponent.getRelationTypeComponentId());
    Optional<RelationTypeComponent> deletedSecondRelationTypeComponent = relationTypeComponentRepository.findById(secondRelationTypeComponent.getRelationTypeComponentId());

    assertAll(
      () -> assertTrue(deletedFirstRelationTypeComponent.get().getIsDeleted()),
      () -> assertNotNull(deletedFirstRelationTypeComponent.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedFirstRelationTypeComponent.get().getDeletedBy().getUserId()),
      () -> assertTrue(deletedSecondRelationTypeComponent.get().getIsDeleted()),
      () -> assertNotNull(deletedSecondRelationTypeComponent.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedSecondRelationTypeComponent.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRelationTypeByIdDeleteConnectedAssignmentsIntegrationTest () {
    AttributeType booleanAttributeType = attributeTypeRepository.save(new AttributeType("attribute name", "attribute desc", AttributeKindType.BOOLEAN, null, null, language, user));
    RelationType someRelationType = relationTypeRepository.save(new RelationType("some name", "desc", 6, false, false, language, user));

    RelationTypeAttributeTypeAssignment assignment = relationTypeAttributeTypeAssignmentRepository.save(new RelationTypeAttributeTypeAssignment(someRelationType, booleanAttributeType, user));

    relationTypesService.deleteRelationTypeById(someRelationType.getRelationTypeId(), user);

    Optional<RelationTypeAttributeTypeAssignment> deletedAssignment = relationTypeAttributeTypeAssignmentRepository.findById(assignment.getRelationTypeAttributeTypeAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedAssignment.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRelationTypeByIdDeleteConnectedAssetTypeAssignmentsIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("asset name", "desc", "an", "red", language, user));
    RelationType someRelationType = relationTypeRepository.save(new RelationType("some name", "desc", 6, false, false, language, user));
    RelationTypeComponent firstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first", "desc", null, null, null, language, someRelationType, user));
    RelationTypeComponent secondRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("second", "desc", null, null, null, language, someRelationType, user));

    RelationTypeComponentAssetTypeAssignment firstAssignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstRelationTypeComponent, assetType, false, null, user));
    RelationTypeComponentAssetTypeAssignment secondAssignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(secondRelationTypeComponent, assetType, false, null, user));

    relationTypesService.deleteRelationTypeById(someRelationType.getRelationTypeId(), user);

    Optional<RelationTypeComponentAssetTypeAssignment> firstDeletedAssignment = relationTypeComponentAssetTypeAssignmentRepository.findById(firstAssignment.getRelationTypeComponentAssetTypeAssignmentId());
    Optional<RelationTypeComponentAssetTypeAssignment> secondDeletedAssignment = relationTypeComponentAssetTypeAssignmentRepository.findById(secondAssignment.getRelationTypeComponentAssetTypeAssignmentId());


    assertAll(
      () -> assertTrue(firstDeletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(firstDeletedAssignment.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), firstDeletedAssignment.get().getDeletedBy().getUserId()),
      () -> assertTrue(secondDeletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(secondDeletedAssignment.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), secondDeletedAssignment.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRelationTypeByIdDeleteConnectedRelationTypeComponentAttributeTypeAssignmentsIntegrationTest () {
    AssetType assetType = assetTypeRepository.save(new AssetType("asset name", "desc", "an", "red", language, user));
    RelationType someRelationType = relationTypeRepository.save(new RelationType("some name", "desc", 6, false, false, language, user));
    RelationTypeComponent firstRelationTypeComponent = relationTypeComponentRepository.save(new RelationTypeComponent("first", "desc", null, null, null, language, someRelationType, user));

    RelationTypeComponentAssetTypeAssignment relationTypeComponentAssetTypeAssignment = relationTypeComponentAssetTypeAssignmentRepository.save(new RelationTypeComponentAssetTypeAssignment(firstRelationTypeComponent, assetType, false, null, user));

    relationTypesService.deleteRelationTypeById(someRelationType.getRelationTypeId(), user);

    Optional<RelationTypeComponentAssetTypeAssignment> deletedAssignment = relationTypeComponentAssetTypeAssignmentRepository.findById(relationTypeComponentAssetTypeAssignment.getRelationTypeComponentAssetTypeAssignmentId());

    assertAll(
      () -> assertTrue(deletedAssignment.get().getIsDeleted()),
      () -> assertNotNull(deletedAssignment.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedAssignment.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteRelationTypeByIdSuccessIntegrationTest () {
    RelationType someRelationType = relationTypeRepository.save(new RelationType("some name", "desc", 6, false, false, language, user));

    relationTypesService.deleteRelationTypeById(someRelationType.getRelationTypeId(), user);

    Optional<RelationType> relationType = relationTypeRepository.findById(someRelationType.getRelationTypeId());

    assertAll(
      () -> assertTrue(relationType.get().getIsDeleted()),
      () -> assertNotNull(relationType.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), relationType.get().getDeletedBy().getUserId())
    );
  }

  @Test
  public void deleteAttributeTypeByIdDeleteAllConnectedRoleActionsIntegrationTest () {
    RelationType someRelationType = relationTypeRepository.save(new RelationType("some name", "desc", 6, false, false, language, user));

    Optional<Entity> entity = entityRepository.findById(UUID.fromString("360ef840-5c19-424b-a86f-b3e24c2fcc2f"));
    Optional<ActionType> actionType = actionTypeRepository.findById(UUID.fromString("54d4330c-d08f-4fb2-b706-2ec3c022286d"));
    Role role = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    RoleAction roleAction = new RoleAction(role, actionType.get(), entity.get(), ActionScopeType.ONE_ID, PermissionType.ALLOW, user);

    roleAction.setRelationType(someRelationType);
    roleActionRepository.save(roleAction);

    relationTypesService.deleteRelationTypeById(someRelationType.getRelationTypeId(), user);

    Optional<RoleAction> deletedRoleAction = roleActionRepository.findById(roleAction.getRoleActionId());

    assertAll(
      () -> assertTrue(deletedRoleAction.get().getIsDeleted()),
      () -> assertNotNull(deletedRoleAction.get().getDeletedOn()),
      () -> assertEquals(user.getUserId(), deletedRoleAction.get().getDeletedBy().getUserId())
    );
  }

  private void generateRelationTypes (int count) {
    for (int i = 0; i < count; i++) {
      relationTypeRepository.save(new RelationType("relation type " + i, "desc_" + i, 2, false, false, language, user));
    }
  }
}
