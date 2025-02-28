package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.dto;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.dto.AssetChangeHistoryDTO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get.AssetHistoryActionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get.AssetHistoryEntityType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

@ExtendWith(MockitoExtension.class)
public class AssetChangeHistoryDTOTest {

  @Test
  public void prepareRequestString_null_entity () {
    assertAll(
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
        null
        ).prepareRequestString().getKey().contains("Select 'STATUS' as entityType, 'EDIT' as actionType, sum(cnt.count)"),
        "Status edit count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
        null,
        null
        ).prepareRequestString().getValue().contains("'STATUS' as entityTypeName, 'Статус' as entityTypeNameRu,"),
        "Status edit select"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
        null,
        null
      ).prepareRequestString().getKey().contains("Select 'ASSET' as entityType, 'ADD' as actionType, count(*) as count"),
        "Asset add count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'ASSET' as entityType, 'EDIT' as actionType, sum(cnt.count)"),
        "Asset edit count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'ATTRIBUTE' as entityType, 'ADD' as actionType, count(*) as count"),
        "Attribute add count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'ATTRIBUTE' as entityType, 'EDIT' as actionType, count(*) as count"),
        "Attribute edit count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'ATTRIBUTE' as entityType, 'DELETE' as actionType, count(*) as count"),
        "Attribute delete count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'RELATION_ATTRIBUTE' as entityType, 'ADD' as actionType, count(*) as count"),
        "Relation Attribute add count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'RELATION_ATTRIBUTE' as entityType, 'EDIT' as actionType, count(*) as count"),
        "Relation Attribute edit count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'RELATION_ATTRIBUTE' as entityType, 'DELETE' as actionType, count(*) as count"),
        "Relation Attribute delete count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'RELATION_COMPONENT_ATTRIBUTE' as entityType, 'ADD' as actionType, count(*) as count"),
        "Relation Component Attribute add count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'RELATION_COMPONENT_ATTRIBUTE' as entityType, 'EDIT' as actionType, count(*) as count"),
        "Relation Component Attribute edit count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'RELATION_COMPONENT_ATTRIBUTE' as entityType, 'DELETE' as actionType, count(*) as count"),
        "Relation Component Attribute delete count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'RESPONSIBILITY' as entityType, 'ADD' as actionType, count(*) as count"),
        "Responsibility add count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'RESPONSIBILITY' as entityType, 'DELETE' as actionType, count(*) as count"),
        "Responsibility delete count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'RELATION' as entityType, 'ADD' as actionType, count(*) as count"),
        "Relation add count"
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          null,
          null
        ).prepareRequestString().getKey().contains("Select 'RELATION' as entityType, 'DELETE' as actionType, count(*) as count"),
        "Relation delete count"
      )
    );
  }

  @Test
  public void prepareRequestString_status_entity () {
    assertAll(
      () -> assertNull(
        new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.STATUS)
        ).prepareRequestString().getKey()
      ),
      () -> assertNull(
        new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.STATUS)
        ).prepareRequestString().getKey()
      ),
      () -> assertNotNull(
        new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.STATUS)
        ).prepareRequestString().getKey()
      ),
      () -> assertNotNull(
        new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.STATUS)
        ).prepareRequestString().getKey()
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.STATUS)
        ).prepareRequestString().getKey().contains("Select 'STATUS' as entityType, 'EDIT' as actionType, sum(cnt.count)")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.STATUS)
        ).prepareRequestString().getValue().contains("'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,")
      )
    );
  }

  @Test
  public void prepareRequestString_asset_entity () {
    assertAll(
      () -> assertNull(
        new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.ASSET)
        ).prepareRequestString().getKey()
      ),
      () -> assertNotNull(
        new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.ASSET)
        ).prepareRequestString().getKey()
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.ASSET)
        ).prepareRequestString().getKey().contains("Select 'ASSET' as entityType, 'EDIT' as actionType, sum(cnt.count)")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.ASSET)
        ).prepareRequestString().getValue().contains("'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.ASSET)
        ).prepareRequestString().getKey().contains("Select 'ASSET' as entityType, 'ADD' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.ASSET)
        ).prepareRequestString().getValue().contains("'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,")
      )
    );
  }

  @Test
  public void prepareRequestString_attribute_entity () {
    assertAll(
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.ATTRIBUTE)
        ).prepareRequestString().getKey().contains("Select 'ATTRIBUTE' as entityType, 'ADD' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.ATTRIBUTE)
        ).prepareRequestString().getValue().contains("'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.ATTRIBUTE)
        ).prepareRequestString().getKey().contains("Select 'ATTRIBUTE' as entityType, 'EDIT' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.ATTRIBUTE)
        ).prepareRequestString().getValue().contains("'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.ATTRIBUTE)
        ).prepareRequestString().getKey().contains("Select 'ATTRIBUTE' as entityType, 'DELETE' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.ATTRIBUTE)
        ).prepareRequestString().getValue().contains("'DELETE' as actionTypeName, 'Удаление' as actionTypeNameRu,")
      )
    );
  }

  @Test
  public void prepareRequestString_relation_attribute_entity () {
    assertAll(
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE)
        ).prepareRequestString().getKey().contains("Select 'RELATION_ATTRIBUTE' as entityType, 'ADD' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE)
        ).prepareRequestString().getValue().contains("'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE)
        ).prepareRequestString().getKey().contains("Select 'RELATION_ATTRIBUTE' as entityType, 'EDIT' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE)
        ).prepareRequestString().getValue().contains("'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE)
        ).prepareRequestString().getKey().contains("Select 'RELATION_ATTRIBUTE' as entityType, 'DELETE' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.RELATION_ATTRIBUTE)
        ).prepareRequestString().getValue().contains("'DELETE' as actionTypeName, 'Удаление' as actionTypeNameRu,")
      )
    );
  }

  @Test
  public void prepareRequestString_relation_component_attribute_entity () {
    assertAll(
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE)
        ).prepareRequestString().getKey().contains("Select 'RELATION_COMPONENT_ATTRIBUTE' as entityType, 'ADD' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE)
        ).prepareRequestString().getValue().contains("'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE)
        ).prepareRequestString().getKey().contains("Select 'RELATION_COMPONENT_ATTRIBUTE' as entityType, 'EDIT' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE)
        ).prepareRequestString().getValue().contains("'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE)
        ).prepareRequestString().getKey().contains("Select 'RELATION_COMPONENT_ATTRIBUTE' as entityType, 'DELETE' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE)
        ).prepareRequestString().getValue().contains("'DELETE' as actionTypeName, 'Удаление' as actionTypeNameRu,")
      )
    );
  }

  @Test
  public void prepareRequestString_relation_entity () {
    assertAll(
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.RELATION)
        ).prepareRequestString().getKey().contains("Select 'RELATION' as entityType, 'ADD' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.RELATION)
        ).prepareRequestString().getValue().contains("'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,")
      ),
      () -> assertNull(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.RELATION)
        ).prepareRequestString().getKey()
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.RELATION)
        ).prepareRequestString().getKey().contains("Select 'RELATION' as entityType, 'DELETE' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.RELATION)
        ).prepareRequestString().getValue().contains("'DELETE' as actionTypeName, 'Удаление' as actionTypeNameRu,")
      )
    );
  }

  @Test
  public void prepareRequestString_responsibility_entity () {
    assertAll(
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.RESPONSIBILITY)
        ).prepareRequestString().getKey().contains("Select 'RESPONSIBILITY' as entityType, 'ADD' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.ADD),
          List.of(AssetHistoryEntityType.RESPONSIBILITY)
        ).prepareRequestString().getValue().contains("'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,")
      ),
      () -> assertNull(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.EDIT),
          List.of(AssetHistoryEntityType.RESPONSIBILITY)
        ).prepareRequestString().getKey()
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.RESPONSIBILITY)
        ).prepareRequestString().getKey().contains("Select 'RESPONSIBILITY' as entityType, 'DELETE' as actionType, count(*) as count")
      ),
      () -> assertTrue(new AssetChangeHistoryDTO(
          List.of(AssetHistoryActionType.DELETE),
          List.of(AssetHistoryEntityType.RESPONSIBILITY)
        ).prepareRequestString().getValue().contains("'DELETE' as actionTypeName, 'Удаление' as actionTypeNameRu,")
      )
    );
  }
}
