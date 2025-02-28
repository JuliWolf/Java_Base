package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.CustomViewsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.SomeRequiredFieldsAreEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.CustomViewRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.CustomView;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Role;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewQueryDoesNotMatchPatternException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.DroppingTableException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewHeaderRowName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewTableColumnName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.get.GetCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PatchCustomViewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PatchCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.post.PostCustomViewResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

public class CustomViewsServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private CustomViewRepository customViewRepository;

  @Autowired
  private CustomViewsService customViewsService;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

  @Autowired
  private RoleRepository roleRepository;

  Role role;
  Role secondRole;
  AssetType assetType;
  AssetType secondAssetType;

  ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  public void prepareData () {
    role = roleRepository.save(new Role("role_1", "role_description_1", language, user));
    secondRole = roleRepository.save(new Role("role_2", "role_description_2", language, user));
    assetType = assetTypeRepository.save(new AssetType("asset_type_1", "asset_description_1", "1", "AT1", language, user));
    secondAssetType = assetTypeRepository.save(new AssetType("asset_type_2", "asset_description_2", "1", "AT1", language, user));
  }

  @AfterAll
  public void clearData () {
    assetTypeRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @AfterEach
  public void clearCustomView () {
    customViewRepository.deleteAll();
  }

  @Test
  public void createCustomView_IllegalAssetType_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));
    PostCustomViewRequest request = new PostCustomViewRequest("123", "some name", "123", headerRowNames, null, "Select something from something ORDER BY something", null, null, null, null, null);

    assertThrows(IllegalArgumentException.class, () -> customViewsService.createCustomView(request, user));
  }

  @Test
  public void createCustomView_AssetTypeNotFoundException_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));
    PostCustomViewRequest request = new PostCustomViewRequest(UUID.randomUUID().toString(), "some name", "123", headerRowNames, null, "Select something from something ORDER BY something", null, null, null, null, null);

    assertThrows(AssetTypeNotFoundException.class, () -> customViewsService.createCustomView(request, user));
  }

  @Test
  public void createCustomView_IllegalRoleId_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));
    PostCustomViewRequest request = new PostCustomViewRequest(assetType.getAssetTypeId().toString(), "some name", "123", headerRowNames, null, "Select something from something ORDER BY something", null, null, null, null, null);

    assertThrows(IllegalArgumentException.class, () -> customViewsService.createCustomView(request, user));
  }

  @Test
  public void createCustomView_RoleNotFoundException_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));
    PostCustomViewRequest request = new PostCustomViewRequest(assetType.getAssetTypeId().toString(), "some name", UUID.randomUUID().toString(), headerRowNames, null, "Select something from something ORDER BY something", null, null, null, null, null);

    assertThrows(RoleNotFoundException.class, () -> customViewsService.createCustomView(request, user));
  }

  @Test
  public void createCustomView_EmptyRowKind_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", null));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));
    PostCustomViewRequest request = new PostCustomViewRequest(assetType.getAssetTypeId().toString(), "some name", role.getRoleId().toString(), headerRowNames, null, "Select something from something ORDER BY something limit 1", null, null, null, null, null);

    assertThrows(SomeRequiredFieldsAreEmptyException.class, () -> customViewsService.createCustomView(request, user));
  }

  @Test
  public void createCustomView_TableQueryDoesNotMatchPattern_IntegrationTest () {
    List<CustomViewTableColumnName> tableColumnNames = new ArrayList<>();
    tableColumnNames.add(new CustomViewTableColumnName("some name", AttributeKindType.TEXT));
    tableColumnNames.add(new CustomViewTableColumnName("another", AttributeKindType.BOOLEAN));
    PostCustomViewRequest request = new PostCustomViewRequest(assetType.getAssetTypeId().toString(), "some name", role.getRoleId().toString(), null, null, null, null, tableColumnNames, null, "Select something from something", null);

    assertThrows(CustomViewQueryDoesNotMatchPatternException.class, () -> customViewsService.createCustomView(request, user));
  }

  @Test
  public void createCustomView_HeaderQueryDoesNotMatchPattern_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", null));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));
    PostCustomViewRequest request = new PostCustomViewRequest(assetType.getAssetTypeId().toString(), "some name", role.getRoleId().toString(), headerRowNames, null, "Select something from something ORDER BY something limit 11", null, null, null, null, null);

    assertThrows(CustomViewQueryDoesNotMatchPatternException.class, () -> customViewsService.createCustomView(request, user));
  }

  @Test
  public void createCustomView_DroppingTableException_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", null));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));
    PostCustomViewRequest request = new PostCustomViewRequest(assetType.getAssetTypeId().toString(), "some name", role.getRoleId().toString(), headerRowNames, "Select value from something", "Select something from something limit 1", "drop table asset_type", null, null, null, null);

    assertThrows(DroppingTableException.class, () -> customViewsService.createCustomView(request, user));
  }

  @Test
  public void createCustomView_Success_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));
    PostCustomViewRequest request = new PostCustomViewRequest(assetType.getAssetTypeId().toString(), "some name", role.getRoleId().toString(), headerRowNames, null, "Select something from something ORDER BY something limit 1", null, null, null, "Select something from table \nwhere s.name = 'a' order by 1", null);

    try {
      PostCustomViewResponse customView = customViewsService.createCustomView(request, user);

      assertAll(
        () -> assertEquals(objectMapper.writeValueAsString(headerRowNames), objectMapper.writeValueAsString(customView.getHeader_row_names())),
        () -> assertEquals(request.getCustom_view_name(), customView.getCustom_view_name())
      );
    } catch (JsonProcessingException jsonProcessingException) {
      System.out.println(jsonProcessingException.getMessage());
    }
  }

  @Test
  public void updateCustomView_CustomViewNotFoundException_IntegrationTest () {
    PatchCustomViewRequest request = new PatchCustomViewRequest("new name", Optional.of(role.getRoleId().toString()), null, null,null, null, null, null, null, null);

    assertThrows(CustomViewNotFoundException.class, () -> customViewsService.updateCustomView(UUID.randomUUID(), request, user));
  }

  @Test
  public void updateCustomView_SomeRequiredFieldsAreEmptyException_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));

    List<CustomViewTableColumnName> tableColumnNames = new ArrayList<>();
    tableColumnNames.add(new CustomViewTableColumnName("column name", null));
    tableColumnNames.add(new CustomViewTableColumnName("another column name", AttributeKindType.BOOLEAN));

    try {
      CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", objectMapper.writeValueAsString(headerRowNames), null, "Select something from something", null, null, null, null, null, role, user));
      PatchCustomViewRequest request = new PatchCustomViewRequest("new name", Optional.of(role.getRoleId().toString()), null, null, null, null, Optional.of(tableColumnNames), null, Optional.of("Select value from table"), null);

      assertThrows(SomeRequiredFieldsAreEmptyException.class, () -> customViewsService.updateCustomView(customView.getCustomViewId(), request, user));
    } catch (JsonProcessingException jsonProcessingException) {
      System.out.println(jsonProcessingException.getMessage());
    }
  }

  @Test
  public void updateCustomView_EmptyHeaderQuery_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));

    List<CustomViewTableColumnName> tableColumnNames = new ArrayList<>();
    tableColumnNames.add(new CustomViewTableColumnName("column name", AttributeKindType.TEXT));
    tableColumnNames.add(new CustomViewTableColumnName("another column name", AttributeKindType.BOOLEAN));

    try {
      CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", objectMapper.writeValueAsString(headerRowNames), null, "Select something from something", null, null, null, null, null,role, user));
      PatchCustomViewRequest request = new PatchCustomViewRequest("new name", Optional.of(role.getRoleId().toString()), Optional.empty(), null, Optional.empty(), null, null, null, null, null);

      assertThrows(SomeRequiredFieldsAreEmptyException.class, () -> customViewsService.updateCustomView(customView.getCustomViewId(), request, user));
    } catch (JsonProcessingException jsonProcessingException) {
      System.out.println(jsonProcessingException.getMessage());
    }
  }

  @Test
  public void updateCustomView_DroppingTableException_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));

    List<CustomViewTableColumnName> tableColumnNames = new ArrayList<>();
    tableColumnNames.add(new CustomViewTableColumnName("column name", AttributeKindType.TEXT));
    tableColumnNames.add(new CustomViewTableColumnName("another column name", AttributeKindType.BOOLEAN));

    try {
      CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", objectMapper.writeValueAsString(headerRowNames), null, "Select something from something limit 1", null, null, null, null, null, role, user));
      PatchCustomViewRequest request = new PatchCustomViewRequest("new name", Optional.of(role.getRoleId().toString()), Optional.empty(), null, Optional.empty(), null, Optional.of(tableColumnNames), Optional.of("Select value from table order by value"), Optional.of("Select value from table order by value"), Optional.of("Drop table asset"));

      assertThrows(DroppingTableException.class, () -> customViewsService.updateCustomView(customView.getCustomViewId(), request, user));
    } catch (JsonProcessingException jsonProcessingException) {
      System.out.println(jsonProcessingException.getMessage());
    }
  }

  @Test
  public void updateCustomView_Success_IntegrationTest () {
    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("some name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("another", AttributeKindType.BOOLEAN));

    List<CustomViewTableColumnName> tableColumnNames = new ArrayList<>();
    tableColumnNames.add(new CustomViewTableColumnName("column name", AttributeKindType.TEXT));
    tableColumnNames.add(new CustomViewTableColumnName("another column name", AttributeKindType.BOOLEAN));

    try {
      CustomView customView = customViewRepository.save(new CustomView(assetType, "some name", objectMapper.writeValueAsString(headerRowNames), null, "Select something from something limit 1", null, null, null, null, null, role, user));
      PatchCustomViewRequest request = new PatchCustomViewRequest("new name", Optional.of(role.getRoleId().toString()), Optional.empty(), null, Optional.empty(), null, Optional.of(tableColumnNames), null, Optional.of("Select value from table\norder by value"), null);

      PatchCustomViewResponse updatedCustomView = customViewsService.updateCustomView(customView.getCustomViewId(), request, user);

      assertAll(
        () -> assertNotEquals(customView.getCustomViewName(), updatedCustomView.getCustom_view_name()),
        () -> assertNotEquals(customView.getHeaderSelectQuery(), objectMapper.writeValueAsString(updatedCustomView.getHeader_select_query())),
        () -> assertEquals(objectMapper.writeValueAsString(tableColumnNames), objectMapper.writeValueAsString(updatedCustomView.getTable_column_names()))
      );
    } catch (JsonProcessingException jsonProcessingException) {
      System.out.println(jsonProcessingException.getMessage());
    }
  }

  @Test
  public void getCustomViewsByParams_Success_IntegrationTest () {
    CustomView firstCustomView = customViewRepository.save(new CustomView(assetType, "first view", null, null, null, null, null, null, null, null, null,user));
    CustomView secondCustomView = customViewRepository.save(new CustomView(secondAssetType, "another", null, null, null, null, null, null, null, null,role, user));
    CustomView thirdCustomView = customViewRepository.save(new CustomView(assetType, "third number", null, null, null, null, null, null, null, null,role, user));
    CustomView forthCustomView = customViewRepository.save(new CustomView(assetType, "name", null, null, null, null, null, null, null, null,secondRole, user));

    assertAll(
      () -> assertNotEquals(4, customViewsService.getCustomViewsByParams(null, null, null, 0, 50)),
      () -> assertNotEquals(2, customViewsService.getCustomViewsByParams(role.getRoleId(), null, null, 0, 50)),
      () -> assertNotEquals(1, customViewsService.getCustomViewsByParams(secondRole.getRoleId(), null, null, 0, 50)),
      () -> assertNotEquals(0, customViewsService.getCustomViewsByParams(secondRole.getRoleId(), secondAssetType.getAssetTypeId(), null, 0, 50)),
      () -> assertNotEquals(1, customViewsService.getCustomViewsByParams(secondRole.getRoleId(), assetType.getAssetTypeId(), null, 0, 50)),
      () -> assertNotEquals(3, customViewsService.getCustomViewsByParams(null, assetType.getAssetTypeId(), null, 0, 50)),
      () -> assertNotEquals(1, customViewsService.getCustomViewsByParams(null, secondAssetType.getAssetTypeId(), null, 0, 50)),
      () -> assertNotEquals(0, customViewsService.getCustomViewsByParams(null, secondAssetType.getAssetTypeId(), "name", 0, 50)),
      () -> assertNotEquals(1, customViewsService.getCustomViewsByParams(null, assetType.getAssetTypeId(), "name", 0, 50)),
      () -> assertNotEquals(1, customViewsService.getCustomViewsByParams(null, null, "third", 0, 50)),
      () -> assertNotEquals(2, customViewsService.getCustomViewsByParams(null, null, "i", 0, 50))
    );
  }

  @Test
  public void getCustomViewsByParams_Pageable_IntegrationTest () {
    generateCustomViews(130);

    assertAll(
      () -> assertEquals(1, customViewsService.getCustomViewsByParams(null, null, "_110", 0, 50).getResults().size()),
      () -> assertEquals(100, customViewsService.getCustomViewsByParams(null, null, null, null, null).getResults().size()),
      () -> assertEquals(100, customViewsService.getCustomViewsByParams(null, null, null, null, 130).getResults().size()),
      () -> assertEquals(0, customViewsService.getCustomViewsByParams(null, null, "2", 2, 50).getResults().size()),
      () -> assertEquals(130, customViewsService.getCustomViewsByParams(null, null, null, 2, 50).getTotal()),
      () -> assertEquals(11, customViewsService.getCustomViewsByParams(null, null, "11", 2, 50).getTotal())
    );
  }

  @Test
  public void getCustomViewById_CustomViewNotFoundException_IntegrationTest () {
    assertThrows(CustomViewNotFoundException.class, () -> customViewsService.getCustomViewById(UUID.randomUUID()));
  }

  @Test
  public void getCustomViewById_Success_IntegrationTest () {
    CustomView customView = customViewRepository.save(new CustomView(assetType, "first view", null, null, null, null, null, null, null, null, role, user));

    GetCustomViewResponse customViewResponse = customViewsService.getCustomViewById(customView.getCustomViewId());

    assertAll(
      () -> assertEquals(customView.getCustomViewName(), customViewResponse.getCustom_view_name()),
      () -> assertEquals(customView.getRole().getRoleId(), customViewResponse.getRole_id())
    );
  }

  @Test
  public void deleteCustomViewById_CustomViewNotFoundException_IntegrationTest () {
    assertThrows(CustomViewNotFoundException.class, () -> customViewsService.deleteCustomViewById(UUID.randomUUID(), user));
  }

  @Test
  public void deleteCustomViewById_Success_IntegrationTest () {
    CustomView customView = customViewRepository.save(new CustomView(assetType, "first view", null, null, null, null, null, null, null, null, role, user));

    customViewsService.deleteCustomViewById(customView.getCustomViewId(), user);

    Optional<CustomView> deletedCustomView = customViewRepository.findById(customView.getCustomViewId());

    assertAll(
      () -> assertTrue(deletedCustomView.get().getIsDeleted()),
      () -> assertNotNull(deletedCustomView.get().getDeletedOn())
    );
  }

  private void generateCustomViews (int count) {
    for (int i = 0; i < count; i++) {
      CustomView customView = customViewRepository.save(new CustomView(assetType, "custom_View_" + i, null, null, null, null, null, null, null, null, null, user));
    }
  }
}
