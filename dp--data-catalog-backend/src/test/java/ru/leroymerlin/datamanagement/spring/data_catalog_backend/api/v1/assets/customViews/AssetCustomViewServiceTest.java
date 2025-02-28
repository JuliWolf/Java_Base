package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.exceptions.CustomViewHeaderQueryIsEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.customViews.exceptions.CustomViewTableQueryIsEmptyException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.exceptions.CustomViewNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewHeaderRowName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.customViews.models.CustomViewTableColumnName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.customView.CustomViewRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.CustomView;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author juliwolf
 */

public class AssetCustomViewServiceTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private CustomViewRepository customViewRepository;

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

  @Autowired
  private AssetCustomViewService assetCustomViewService;

  Asset asset;
  Asset secondAsset;
  AssetType assetType;
  AssetType secondAssetType;
  CustomView customViewWithHeaderRows;
  CustomView customViewWithTableColumns;

  ObjectMapper objectMapper = new ObjectMapper();

  @SneakyThrows
  @BeforeAll
  public void prepareData() {
    assetType = assetTypeRepository.save(new AssetType("asset type name", "desc", "atn", "red", language, user));
    asset = assetRepository.save(new Asset("some name", assetType, "disp", language, null, null, user));

    secondAssetType = assetTypeRepository.save(new AssetType("second asset type name", "desc", "atn", "red", language, user));
    secondAsset = assetRepository.save(new Asset("second asset name", secondAssetType, "disp", language, null, null, user));

    List<CustomViewHeaderRowName> headerRowNames = new ArrayList<>();
    headerRowNames.add(new CustomViewHeaderRowName("tech_name", AttributeKindType.TEXT));
    headerRowNames.add(new CustomViewHeaderRowName("synonyms", AttributeKindType.TEXT));
    customViewWithHeaderRows = customViewRepository.save(new CustomView(assetType, "some name", objectMapper.writeValueAsString(headerRowNames), null, "select a.value as tech_name, a2.value as synonyms from asset ast left join attribute a on ast.asset_id = a.asset_id and not a.deleted_flag and a.attribute_type_id = 'fb4e656f-6c3d-4c73-b09a-00baa40d44b6' left join attribute a2 on ast.asset_id = a2.asset_id and not a2.deleted_flag and a2.attribute_type_id = '29942fad-13e0-4770-9eec-55e78896f4ac'\nwhere ast.asset_id = :assetId", null, null, null, null, null, null, user));

    List<CustomViewTableColumnName> tableColumnNames = new ArrayList<>();
    tableColumnNames.add(new CustomViewTableColumnName("tech_name", AttributeKindType.TEXT));
    tableColumnNames.add(new CustomViewTableColumnName("asset_displayname", AttributeKindType.TEXT));
    customViewWithTableColumns = customViewRepository.save(new CustomView(secondAssetType, "second name", null, null, null, null, objectMapper.writeValueAsString(tableColumnNames), null, "select a.value as tech_name, ast.asset_displayname as asset_displayname from asset ast left join attribute a on ast.asset_id = a.asset_id and not a.deleted_flag\nwhere ast.asset_id = :assetId ORDER BY ast.asset_id", null, null, user));
  }

  @AfterAll
  public void clearData () {
    customViewRepository.deleteAll();
    assetRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }
  @Test
  public void getAssetCustomViewHeaderRowsAssetNotFoundIntegrationTest () {
    assertThrows(AssetNotFoundException.class, () -> assetCustomViewService.getAssetCustomViewHeaderRows(UUID.randomUUID(), customViewWithHeaderRows.getCustomViewId()));
  }

  @Test
  public void getAssetCustomViewHeaderRowsCustomViewFoundIntegrationTest () {
    assertThrows(CustomViewNotFoundException.class, () -> assetCustomViewService.getAssetCustomViewHeaderRows(asset.getAssetId(), UUID.randomUUID()));
  }

  @Test
  public void getAssetCustomViewHeaderRowsCustomViewHeaderIsEmptyIntegrationTest () {
    assertThrows(CustomViewHeaderQueryIsEmptyException.class, () -> assetCustomViewService.getAssetCustomViewHeaderRows(asset.getAssetId(), customViewWithTableColumns.getCustomViewId()));
  }

  @Test
  public void getAssetCustomViewHeaderRowsSuccessIntegrationTest () {
    assertDoesNotThrow(() -> assetCustomViewService.getAssetCustomViewHeaderRows(asset.getAssetId(), customViewWithHeaderRows.getCustomViewId()));
  }

  @Test
  public void getAssetCustomViewTableRowsAssetNotFoundIntegrationTest () {
    assertThrows(AssetNotFoundException.class, () -> assetCustomViewService.getAssetCustomViewTableRows(UUID.randomUUID(), customViewWithTableColumns.getCustomViewId(), 0, 50));
  }

  @Test
  public void getAssetCustomViewTableRowsCustomViewFoundIntegrationTest () {
    assertThrows(CustomViewNotFoundException.class, () -> assetCustomViewService.getAssetCustomViewTableRows(secondAsset.getAssetId(), UUID.randomUUID(), 0, 50));
  }

  @Test
  public void getAssetCustomViewTableRowsCustomViewHeaderIsEmptyIntegrationTest () {
    assertThrows(CustomViewTableQueryIsEmptyException.class, () -> assetCustomViewService.getAssetCustomViewTableRows(secondAsset.getAssetId(), customViewWithHeaderRows.getCustomViewId(), 0, 50));
  }

  @Test
  public void getAssetCustomViewTableRows_Success_IntegrationTest () {
    assertDoesNotThrow(() -> assetCustomViewService.getAssetCustomViewTableRows(secondAsset.getAssetId(), customViewWithTableColumns.getCustomViewId(), 0, 50));
  }
}
