package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.dto;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get.AssetHistoryActionType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.get.AssetHistoryEntityType;

/**
 * @author juliwolf
 */

public class AssetChangeHistoryDTO {
  private final List<AssetHistoryActionType> actionTypes;
  private final List<AssetHistoryEntityType> entityTypes;

  private Map<AssetHistoryEntityType, EntityType> entityTypeMap = new HashMap<>();

  public AssetChangeHistoryDTO (
    List<AssetHistoryActionType> actionTypes,
    List<AssetHistoryEntityType> entityTypes
  ) {
    this.actionTypes = actionTypes != null ? actionTypes : new ArrayList<>();
    this.entityTypes = entityTypes != null ? entityTypes : new ArrayList<>();

    putEntityTypeInMap(AssetHistoryEntityType.ASSET, prepareAssetEntityType());
    putEntityTypeInMap(AssetHistoryEntityType.STATUS, prepareStatusEntityType());
    putEntityTypeInMap(AssetHistoryEntityType.RELATION, prepareRelationEntityType());
    putEntityTypeInMap(AssetHistoryEntityType.ATTRIBUTE, prepareAttributeEntityType());
    putEntityTypeInMap(AssetHistoryEntityType.RESPONSIBILITY, prepareResponsibilityEntityType());
    putEntityTypeInMap(AssetHistoryEntityType.RELATION_ATTRIBUTE, prepareRelationAttributeEntityType());
    putEntityTypeInMap(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE, prepareRelationComponentAttributeEntityType());
  }

  public AbstractMap.SimpleEntry<String, String> prepareRequestString () {
    StringBuilder countStringBuilder;
    StringBuilder selectStringBuilder;

    StringBuilder countStringBuilderQuery = new StringBuilder();
    StringBuilder selectStringBuilderQuery = new StringBuilder();

    AtomicInteger atomicInteger = new AtomicInteger();

    entityTypeMap.forEach((key, value) -> {
      Map<AssetHistoryActionType, ActionQueries> queryByActionsMap = value.getQueryByActionsMap();

      queryByActionsMap.forEach((queryKey, queryValues) -> {
        int index = atomicInteger.getAndIncrement();
        if (index != 0) {
          countStringBuilderQuery.append("UNION ");
          selectStringBuilderQuery.append("UNION ");
        }

        countStringBuilderQuery.append(queryValues.getCountQuery());
        selectStringBuilderQuery.append(queryValues.getSelectQuery());
      });
    });

    if (countStringBuilderQuery.isEmpty() || selectStringBuilderQuery.isEmpty()) {
      return new AbstractMap.SimpleEntry<>(null, null);
    }

    countStringBuilder = new StringBuilder("Select sum(allSel.count) From (");
    countStringBuilder.append(countStringBuilderQuery);
    countStringBuilder.append("""
    ) allSel
      WHERE
        allSel.count > 0 and
        (:actionTypesCount = 0 or allSel.actionType in (:actionTypes)) and
        (:entityTypesCount = 0 or allSel.entityType in (:entityTypes))
  """);

    selectStringBuilder = new StringBuilder("""
      Select
        s.entityTypeName as entity_type_name, s.entityTypeNameRu as entity_type_name_ru,
        s.actionTypeName as action_type_name, s.actionTypeNameRu as action_type_name_ru,
        s.assetId as asset_id, s.loggedOn as logged_on,
        s.userId as user_id, s.username as username, s.firstName as first_name, s.lastName as last_name,
        s.objectId as object_id, s.objectTypeId as object_type_id, s.objectTypeName as object_type_name, s.value
      from (
    """);
    selectStringBuilder.append(selectStringBuilderQuery);
    selectStringBuilder.append(") s");

    return new AbstractMap.SimpleEntry<>(countStringBuilder.toString(), selectStringBuilder.toString());
  }

  private void putEntityTypeInMap (AssetHistoryEntityType assetHistoryEntityType, EntityType entityType) {
    if (entityType == null) return;

    entityTypeMap.put(assetHistoryEntityType, entityType);
  }

  private EntityType prepareStatusEntityType () {
    if (!entityTypes.isEmpty() && !entityTypes.contains(AssetHistoryEntityType.STATUS)) return null;

    if (!actionTypes.isEmpty() && !actionTypes.contains(AssetHistoryActionType.EDIT)) return null;

    return new EntityType(
      null,
      new AbstractMap.SimpleEntry<>(AssetHistoryActionType.EDIT, new ActionQueries(
        """
          Select 'STATUS' as entityType, 'EDIT' as actionType, sum(cnt.count) as count
          From (
            SELECT
              (CASE WHEN lifecycle_status IS DISTINCT FROM prev_lifecycle_status THEN 1 ELSE 0 END) +
              (CASE WHEN stewardship_status IS DISTINCT FROM prev_stewardship_status THEN 1 ELSE 0 END) as count
            FROM (
              SELECT
                ah.lifecycle_status,
                ah.stewardship_status,
                LAG(lifecycle_status, 1, NULL) OVER (PARTITION BY asset_id ORDER BY last_modified_on) as prev_lifecycle_status,
                LAG(stewardship_status, 1, NULL) OVER (PARTITION BY asset_id ORDER BY last_modified_on) as prev_stewardship_status
              FROM asset_history ah
              Where
                ah.asset_id = :assetId and
                (cast(:minTime as date) is null or ah.last_modified_on >= :minTime) and
                (cast(:maxTime as date) is null or :maxTime >= ah.last_modified_on) and
                (:userIdsCount = 0 or ah.last_modified_by in (:userIds))
            ) AS logged_data
            Where
              logged_data.lifecycle_status IS DISTINCT FROM prev_lifecycle_status or
              logged_data.stewardship_status IS DISTINCT FROM prev_stewardship_status
          ) cnt
        """,
        """
          select
            'STATUS' as entityTypeName, 'Статус' as entityTypeNameRu,
            'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,
            alsc.asset_id as assetId, alsc.last_modified_on as loggedOn,
            u.user_id as userId, u.username as username,
            u.first_name as firstName, u.last_name as lastName,
            alsc.asset_id as objectId, cast(null as uuid) as objectTypeId, 'Lifecycle-статус' as objectTypeName,
            s.status_name as value
          from (
            SELECT
              ah.*,
              LAG(lifecycle_status, 1, NULL) OVER (PARTITION BY asset_id ORDER BY last_modified_on) as prev_lifecycle_status
            FROM asset_history ah
            Where
              ah.asset_id = :assetId and
              (cast(:minTime as date) is null or ah.last_modified_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= ah.last_modified_on) and
              (:userIdsCount = 0 or ah.last_modified_by in (:userIds))
          ) alsc
          inner join "user" u on u.user_id = alsc.last_modified_by
          inner join status s on s.status_id = alsc.lifecycle_status and s.deleted_flag = false
          Where
            alsc.lifecycle_status IS DISTINCT FROM prev_lifecycle_status
          union
          select
            'STATUS' as entityTypeName, 'Статус' as entityTypeNameRu,
            'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,
            astsc.asset_id as assetId, astsc.last_modified_on as loggedOn,
            u.user_id as userId, u.username as username,
            u.first_name as firstName, u.last_name as lastName,
            astsc.asset_id as objectId, cast(null as uuid) as objectTypeId, 'Stewardship-статус' as objectTypeName,
            s.status_name as value
          FROM (
             SELECT
               ah.*,
               LAG(stewardship_status, 1, NULL) OVER (PARTITION BY asset_id ORDER BY last_modified_on) as prev_stewardship_status
             FROM asset_history ah
             Where
               ah.asset_id = :assetId and
               (cast(:minTime as date) is null or ah.last_modified_on >= :minTime) and
               (cast(:maxTime as date) is null or :maxTime >= ah.last_modified_on) and
               (:userIdsCount = 0 or ah.last_modified_by in (:userIds))
          ) AS astsc
          inner join "user" u on u.user_id = astsc.last_modified_by
          inner join status s on s.status_id = astsc.stewardship_status and s.deleted_flag = false
          Where
            astsc.stewardship_status IS DISTINCT FROM prev_stewardship_status
        """)),
      null
    );
  }

  private EntityType prepareAssetEntityType () {
    if (!entityTypes.isEmpty() && !entityTypes.contains(AssetHistoryEntityType.ASSET)) return null;

    boolean isActionTypesEmpty = actionTypes.isEmpty();
    boolean hasAddAction = actionTypes.contains(AssetHistoryActionType.ADD);
    boolean hasEditAction = actionTypes.contains(AssetHistoryActionType.EDIT);
    if (!actionTypes.isEmpty() && !hasEditAction && !hasAddAction) return null;

    return new EntityType(
      !hasAddAction && !isActionTypesEmpty
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.ADD, new ActionQueries(
        """
            Select 'ASSET' as entityType, 'ADD' as actionType, count(*) as count
            From asset_history ah
            inner join "user" u on ah.created_by = u.user_id
            inner join asset_type ast on ah.asset_type_id = ast.asset_type_id
            Where
              ah.deleted_flag = false and
              ah.last_modified_on is null and
              ah.asset_id = :assetId and
              (cast(:minTime as date) is null or ah.created_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= ah.created_on) and
              (:userIdsCount = 0 or ah.created_by in (:userIds))
          """,
        """
          Select
            'ASSET' as entityTypeName, 'Актив' as entityTypeNameRu,
            'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,
            ah.asset_id as assetId, ah.created_on as loggedOn,
            u.user_id as userId, u.username as username,
            u.first_name as firstName, u.last_name as lastName,
            ah.asset_id as objectId, ah.asset_type_id as objectTypeId, ast.asset_type_name objectTypeName,
            ah.asset_displayname as value
          from asset_history ah
          inner join "user" u on ah.created_by = u.user_id
          inner join asset_type ast on ah.asset_type_id = ast.asset_type_id
          Where
            ah.deleted_flag = false and
            ah.last_modified_on is null and
            ah.asset_id = :assetId and
            (cast(:minTime as date) is null or ah.created_on >= :minTime) and
            (cast(:maxTime as date) is null or :maxTime >= ah.created_on) and
            (:userIdsCount = 0 or ah.created_by in (:userIds))
          """
      )),
      !hasEditAction && !isActionTypesEmpty
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.EDIT, new ActionQueries(
        """
            Select 'ASSET' as entityType, 'EDIT' as actionType, sum(cnt.count) as count
            From (
              SELECT
                (CASE WHEN asset_name IS DISTINCT FROM prev_asset_name THEN 1 ELSE 0 END) +
                (CASE WHEN asset_displayname IS DISTINCT FROM prev_asset_displayname THEN 1 ELSE 0 END) as count
              FROM (
                SELECT
                  ah.asset_name,
                  ah.asset_displayname,
                  ah.last_modified_on,
                  ah.last_modified_by,
                  LAG(asset_name, 1, NULL) OVER (PARTITION BY asset_id ORDER BY valid_from) as prev_asset_name,
                  LAG(asset_displayname, 1, NULL) OVER (PARTITION BY asset_id ORDER BY valid_from) as prev_asset_displayname
                FROM asset_history ah
                Where
                  ah.asset_id = :assetId and
                  ah.deleted_flag = false
              ) AS logged_data
              Where
                (logged_data.asset_name IS DISTINCT FROM prev_asset_name and prev_asset_name is not null) or
                (logged_data.asset_displayname IS DISTINCT FROM prev_asset_displayname and prev_asset_displayname is not null) and
                (cast(:minTime as date) is null or logged_data.last_modified_on >= :minTime) and
                (cast(:maxTime as date) is null or :maxTime >= logged_data.last_modified_on) and
                (:userIdsCount = 0 or logged_data.last_modified_by in (:userIds))
            ) cnt
          """,
        """
            select
              'ASSET' as entityTypeName, 'Актив' as entityTypeNameRu,
              'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,
              anc.asset_id as assetId, anc.last_modified_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              anc.asset_id as objectId, cast(null as uuid) as objectTypeId, 'Полное имя' as objectTypeName,
              anc.asset_name as value
            FROM (
              SELECT
                ah.*,
                LAG(asset_name, 1, NULL) OVER (PARTITION BY asset_id ORDER BY valid_from) as prev_asset_name
              FROM asset_history ah
              Where
                ah.asset_id = :assetId and
                ah.deleted_flag = false
            ) AS anc
            inner join "user" u on u.user_id = anc.last_modified_by
            Where
              prev_asset_name is not null and
              anc.asset_name IS DISTINCT FROM prev_asset_name and
              (cast(:minTime as date) is null or anc.last_modified_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= anc.last_modified_on) and
              (:userIdsCount = 0 or anc.last_modified_by in (:userIds))
            union
            select
              'ASSET' as entityTypeName, 'Актив' as entityTypeNameRu,
              'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,
              anc.asset_id as assetId, anc.last_modified_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              anc.asset_id as objectId, cast(null as uuid) as objectTypeId, 'Отображаемое имя' as objectTypeName,
              anc.asset_displayname as value
            FROM (
              SELECT
                ah.*,
                LAG(asset_displayname, 1, NULL) OVER (PARTITION BY asset_id ORDER BY valid_from) as prev_asset_displayname
              FROM asset_history ah
              Where
                ah.asset_id = :assetId and
                ah.deleted_flag = false
            ) AS anc
            inner join "user" u on u.user_id = anc.last_modified_by
            Where
              anc.prev_asset_displayname is not null and
              anc.asset_displayname IS DISTINCT FROM prev_asset_displayname and
              (cast(:minTime as date) is null or anc.last_modified_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= anc.last_modified_on) and
              (:userIdsCount = 0 or anc.last_modified_by in (:userIds))
          """
      )),
      null
    );
  }

  private EntityType prepareAttributeEntityType () {
    if (!entityTypes.isEmpty() && !entityTypes.contains(AssetHistoryEntityType.ATTRIBUTE)) return null;

    boolean hasAddAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.ADD);
    boolean hasEditAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.EDIT);
    boolean hasDeleteAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.DELETE);

    return new EntityType(
      !hasAddAction
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.ADD, new ActionQueries(
        """
            Select 'ATTRIBUTE' as entityType, 'ADD' as actionType, count(*) as count
            From attribute_history attH
            inner join attribute_type at on at.attribute_type_id = attH.attribute_type_id
            inner join "user" u on attH.created_by = u.user_id
            Where
                attH.deleted_flag = false and
                attH.last_modified_on is null and
                attH.asset_id = :assetId and
                (cast(:minTime as date) is null or attH.created_on >= :minTime) and
                (cast(:maxTime as date) is null or :maxTime >= attH.created_on) and
                (:userIdsCount = 0 or attH.created_by in (:userIds))
            group by attH.asset_id
          """,
        """
            select
              'ATTRIBUTE' as entityTypeName, 'Свойство' as entityTypeNameRu,
              'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,
              attH.asset_id as assetId, attH.created_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              attH.attribute_id as objectId, attH.attribute_type_id as objectTypeId, at.attribute_type_name as objectTypeName,
              attH.value as value
            from attribute_history attH
            inner join attribute_type at on at.attribute_type_id = attH.attribute_type_id
            inner join "user" u on attH.created_by = u.user_id
            WHERE
              attH.asset_id = :assetId and
              attH.last_modified_on is null and
              attH.deleted_flag = false and
              (cast(:minTime as date) is null or attH.created_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= attH.created_on) and
              (:userIdsCount = 0 or attH.created_by in (:userIds))
          """
      )),
      !hasEditAction
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.EDIT, new ActionQueries(
        """
            Select 'ATTRIBUTE' as entityType, 'EDIT' as actionType, count(*) as count
            From attribute_history attH
            inner join attribute_type at on at.attribute_type_id = attH.attribute_type_id
            inner join "user" u on attH.last_modified_by = u.user_id
            Where
              attH.deleted_flag = false and
              attH.last_modified_on is not null and
              attH.asset_id = :assetId and
              (cast(:minTime as date) is null or attH.last_modified_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= attH.last_modified_on) and
              (:userIdsCount = 0 or attH.last_modified_by in (:userIds))
            group by attH.asset_id
          """,
        """
            select
              'ATTRIBUTE' as entityTypeName, 'Свойство' as entityTypeNameRu,
              'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,
              attH.asset_id as assetId, attH.last_modified_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              attH.attribute_id as objectId, attH.attribute_type_id as objectTypeId, at.attribute_type_name as objectTypeName,
              attH.value as value
            from attribute_history attH
            inner join attribute_type at on at.attribute_type_id = attH.attribute_type_id
            inner join "user" u on attH.last_modified_by = u.user_id
            WHERE
              attH.asset_id = :assetId and
              attH.last_modified_on is not null and
              attH.deleted_flag = false and
              (cast(:minTime as date) is null or attH.last_modified_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= attH.last_modified_on) and
              (:userIdsCount = 0 or attH.last_modified_by in (:userIds))
          """
      )),
      !hasDeleteAction
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.DELETE, new ActionQueries(
        """
            Select 'ATTRIBUTE' as entityType, 'DELETE' as actionType, count(*) as count
            From attribute att
            inner join attribute_type at on at.attribute_type_id = att.attribute_type_id
            inner join "user" u on att.deleted_by = u.user_id
            Where
              att.asset_id = :assetId and
              att.deleted_flag = true and
              (cast(:minTime as date) is null or att.deleted_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= att.deleted_on) and
              (:userIdsCount = 0 or att.deleted_by in (:userIds))
            group by att.asset_id
          """,
        """
            select
              'ATTRIBUTE' as entityTypeName, 'Свойство' as entityTypeNameRu,
              'DELETE' as actionTypeName, 'Удаление' as actionTypeNameRu,
              att.asset_id as assetId, att.deleted_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              att.attribute_id as objectId, att.attribute_type_id as objectTypeId, at.attribute_type_name as objectTypeName,
              att.value as value
            from attribute att
            inner join attribute_type at on at.attribute_type_id = att.attribute_type_id
            inner join "user" u on att.deleted_by = u.user_id
            WHERE
              att.asset_id = :assetId and
              att.deleted_flag = true and
              (cast(:minTime as date) is null or att.deleted_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= att.deleted_on) and
              (:userIdsCount = 0 or att.deleted_by in (:userIds))
          """
      ))
    );
  }

  private EntityType prepareRelationAttributeEntityType () {
    if (!entityTypes.isEmpty() && !entityTypes.contains(AssetHistoryEntityType.RELATION_ATTRIBUTE)) return null;

    boolean hasAddAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.ADD);
    boolean hasEditAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.EDIT);
    boolean hasDeleteAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.DELETE);

    return new EntityType(
      !hasAddAction
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.ADD, new ActionQueries(
        """
            Select 'RELATION_ATTRIBUTE' as entityType, 'ADD' as actionType, count(*) as count
            From relation_component rc
            inner join relation_attribute_history rah on rc.relation_id = rah.relation_id
            inner join attribute_type att on att.attribute_type_id = rah.attribute_type_id
            inner join "user" u on rah.created_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            Where
              rc.deleted_flag = false and
              rah.deleted_flag = false and
              rah.last_modified_on is null and
              rc.asset_id = :assetId and
              (cast(:minTime as date) is null or rah.created_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rah.created_on) and
              (:userIdsCount = 0 or rah.created_by in (:userIds))
          """,
        """
            select
              'RELATION_ATTRIBUTE' as entityTypeName, 'Свойство связи' as entityTypeNameRu,
              'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,
              rc.asset_id as assetId, rah.created_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              rah.relation_attribute_id as objectId, rah.attribute_type_id as objectTypeId, concat(att.attribute_type_name, ' - связь с ', connectedA.asset_displayname) as objectTypeName,
              rah.value as value
            from relation_component rc
            inner join relation_attribute_history rah on rc.relation_id = rah.relation_id
            inner join attribute_type att on att.attribute_type_id = rah.attribute_type_id
            inner join "user" u on rah.created_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            where
              rc.deleted_flag = false and
              rah.deleted_flag = false and
              rah.last_modified_on is null and
              rc.asset_id = :assetId and
              (cast(:minTime as date) is null or rah.created_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rah.created_on) and
              (:userIdsCount = 0 or rah.created_by in (:userIds))
          """
      )),
      !hasEditAction
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.EDIT, new ActionQueries(
        """
            Select 'RELATION_ATTRIBUTE' as entityType, 'EDIT' as actionType, count(*) as count
            From relation_component rc
            inner join relation_attribute_history rah on rc.relation_id = rah.relation_id
            inner join attribute_type att on att.attribute_type_id = rah.attribute_type_id
            inner join "user" u on rah.last_modified_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            Where
              rc.deleted_flag = false and
              rah.deleted_flag = false and
              rah.last_modified_on is not null and
              rc.asset_id = :assetId and
              (cast(:minTime as date) is null or rah.last_modified_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rah.last_modified_on) and
              (:userIdsCount = 0 or rah.last_modified_by in (:userIds))
            group by rc.asset_id
          """,
        """
            select
              'RELATION_ATTRIBUTE' as entityTypeName, 'Свойство связи' as entityTypeNameRu,
              'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,
              rc.asset_id as assetId, rah.last_modified_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              rah.relation_attribute_id as objectId, rah.attribute_type_id as objectTypeId, concat(att.attribute_type_name, ' - связь с ', connectedA.asset_displayname) as objectTypeName,
              rah.value as value
            from relation_component rc
            inner join relation_attribute_history rah on rc.relation_id = rah.relation_id
            inner join attribute_type att on att.attribute_type_id = rah.attribute_type_id
            inner join "user" u on rah.last_modified_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            WHERE
              rc.deleted_flag = false and
              rah.deleted_flag = false and
              rah.last_modified_on is not null and
              rc.asset_id = :assetId and
              (cast(:minTime as date) is null or rah.last_modified_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rah.last_modified_on) and
              (:userIdsCount = 0 or rah.last_modified_by in (:userIds))
          """
      )),
      !hasDeleteAction
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.DELETE, new ActionQueries(
        """
            Select 'RELATION_ATTRIBUTE' as entityType, 'DELETE' as actionType, count(*) as count
            From relation_component rc
            inner join relation_attribute ra on rc.relation_id = ra.relation_id
            inner join attribute_type att on att.attribute_type_id = ra.attribute_type_id
            inner join "user" u on ra.deleted_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            Where
              rc.asset_id = :assetId and
              ra.deleted_flag = true and
              (cast(:minTime as date) is null or ra.deleted_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= ra.deleted_on) and
              (:userIdsCount = 0 or ra.deleted_by in (:userIds))
            group by rc.asset_id
          """,
        """
            select
              'RELATION_ATTRIBUTE' as entityTypeName, 'Свойство связи' as entityTypeNameRu,
              'DELETE' as actionTypeName, 'Удаление' as actionTypeNameRu,
              rc.asset_id as assetId, ra.deleted_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              ra.relation_attribute_id as objectId, ra.attribute_type_id as objectTypeId, concat(att.attribute_type_name, ' - связь с ', connectedA.asset_displayname) as objectTypeName,
              ra.value as value
            from relation_component rc
            inner join relation_attribute ra on rc.relation_id = ra.relation_id
            inner join attribute_type att on att.attribute_type_id = ra.attribute_type_id
            inner join "user" u on ra.deleted_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            WHERE
              rc.asset_id = :assetId and
              ra.deleted_flag = true and
              (cast(:minTime as date) is null or ra.deleted_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= ra.deleted_on) and
              (:userIdsCount = 0 or ra.deleted_by in (:userIds))
          """
      ))
    );
  }

  private EntityType prepareRelationComponentAttributeEntityType () {
    if (!entityTypes.isEmpty() && !entityTypes.contains(AssetHistoryEntityType.RELATION_COMPONENT_ATTRIBUTE)) return null;

    boolean hasAddAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.ADD);
    boolean hasEditAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.EDIT);
    boolean hasDeleteAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.DELETE);

    return new EntityType(
      !hasAddAction
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.ADD, new ActionQueries(
        """
            Select 'RELATION_COMPONENT_ATTRIBUTE' as entityType, 'ADD' as actionType, count(*) as count
            From relation_component rc
            inner join relation_component_attribute_history rcah on rc.relation_component_id = rcah.relation_component_id
            inner join attribute_type att on att.attribute_type_id = rcah.attribute_type_id
            inner join "user" u on rcah.created_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            Where
              rc.deleted_flag = false and
              rcah.deleted_flag = false and
              rcah.last_modified_on is null and
              rc.asset_id = :assetId and
              (cast(:minTime as date) is null or rcah.created_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rcah.created_on) and
              (:userIdsCount = 0 or rcah.created_by in (:userIds))
            group by rc.asset_id
          """,
        """
            select
              'RELATION_COMPONENT_ATTRIBUTE' as entityTypeName, 'Свойство компоненты связи' as entityTypeNameRu,
              'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,
              rc.asset_id as assetId, rcah.created_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              rcah.relation_component_attribute_id as objectId, rcah.attribute_type_id as objectTypeId, concat(att.attribute_type_name, ' - связь с ', connectedA.asset_displayname) as objectTypeName,
              rcah.value as value
            from relation_component rc
            inner join relation_component_attribute_history rcah on rc.relation_component_id = rcah.relation_component_id
            inner join attribute_type att on att.attribute_type_id = rcah.attribute_type_id
            inner join "user" u on rcah.created_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            where
              rc.deleted_flag = false and
              rcah.deleted_flag = false and
              rcah.last_modified_on is null and
              rc.asset_id = :assetId and
              (cast(:minTime as date) is null or rcah.created_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rcah.created_on) and
              (:userIdsCount = 0 or rcah.created_by in (:userIds))
          """
      )),
      !hasEditAction
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.EDIT, new ActionQueries(
        """
            Select 'RELATION_COMPONENT_ATTRIBUTE' as entityType, 'EDIT' as actionType, count(*) as count
            From relation_component rc
            inner join relation_component_attribute rcah on rc.relation_component_id = rcah.relation_component_id
            inner join attribute_type att on att.attribute_type_id = rcah.attribute_type_id
            inner join "user" u on rcah.last_modified_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            Where
              rc.deleted_flag = false and
              rc.asset_id = :assetId and
              rcah.last_modified_on is not null and
              rcah.deleted_flag = false and
              (cast(:minTime as date) is null or rcah.last_modified_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rcah.last_modified_on) and
              (:userIdsCount = 0 or rcah.last_modified_by in (:userIds))
            group by rc.asset_id
          """,
        """
            select
              'RELATION_COMPONENT_ATTRIBUTE' as entityTypeName, 'Свойство компоненты связи' as entityTypeNameRu,
              'EDIT' as actionTypeName, 'Редактирование' as actionTypeNameRu,
              rc.asset_id as assetId, rcah.last_modified_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              rcah.relation_component_attribute_id as objectId, rcah.attribute_type_id as objectTypeId, concat(att.attribute_type_name, ' - связь с ', connectedA.asset_displayname) as objectTypeName,
              rcah.value as value
            from relation_component rc
            inner join relation_component_attribute rcah on rc.relation_component_id = rcah.relation_component_id
            inner join attribute_type att on att.attribute_type_id = rcah.attribute_type_id
            inner join "user" u on rcah.last_modified_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            WHERE
              rc.deleted_flag = false and
              rc.asset_id = :assetId and
              rcah.last_modified_on is not null and
              rcah.deleted_flag = false and
              (cast(:minTime as date) is null or rcah.last_modified_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rcah.last_modified_on) and
              (:userIdsCount = 0 or rcah.last_modified_by in (:userIds))
          """
      )),
      !hasDeleteAction
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.DELETE, new ActionQueries(
        """
            Select 'RELATION_COMPONENT_ATTRIBUTE' as entityType, 'DELETE' as actionType, count(*) as count
            From relation_component rc
            inner join relation_component_attribute rca on rc.relation_component_id = rca.relation_component_id
            inner join attribute_type att on att.attribute_type_id = rca.attribute_type_id
            inner join "user" u on rca.deleted_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            Where
              rc.asset_id = :assetId and
              rca.deleted_flag = true and
              (cast(:minTime as date) is null or rca.deleted_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rca.deleted_on) and
              (:userIdsCount = 0 or rca.deleted_by in (:userIds))
            group by rc.asset_id
          """,
        """
            select
              'RELATION_COMPONENT_ATTRIBUTE' as entityTypeName, 'Свойство компоненты связи' as entityTypeNameRu,
              'DELETE' as actionTypeName, 'Удаление' as actionTypeNameRu,
              rc.asset_id as assetId, rca.deleted_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              rca.relation_component_attribute_id as objectId, rca.attribute_type_id as objectTypeId, concat(att.attribute_type_name, ' - связь с ', connectedA.asset_displayname) as objectTypeName,
              rca.value as value
            from relation_component rc
            inner join relation_component_attribute rca on rc.relation_component_id = rca.relation_component_id
            inner join attribute_type att on att.attribute_type_id = rca.attribute_type_id
            inner join "user" u on rca.deleted_by = u.user_id
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join asset connectedA on connectedA.asset_id = connectedRc.asset_id
            WHERE
              rc.asset_id = :assetId and
              rca.deleted_flag = true and
              (cast(:minTime as date) is null or rca.deleted_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rca.deleted_on) and
              (:userIdsCount = 0 or rca.deleted_by in (:userIds))
          """
      ))
    );
  }

  private EntityType prepareResponsibilityEntityType () {
    if (!entityTypes.isEmpty() && !entityTypes.contains(AssetHistoryEntityType.RESPONSIBILITY)) return null;

    boolean isActionTypesEmpty = actionTypes.isEmpty();
    boolean hasAddAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.ADD);
    boolean hasDeleteAction = actionTypes.isEmpty() || actionTypes.contains(AssetHistoryActionType.DELETE);
    if (!actionTypes.isEmpty() && !hasAddAction && !hasDeleteAction) return null;


    return new EntityType(
      !hasAddAction && !isActionTypesEmpty
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.ADD, new ActionQueries(
        """
            Select 'RESPONSIBILITY' as entityType, 'ADD' as actionType, count(*) as count
            From responsibility resp
            inner join role r on r.role_id = resp.role_id
            inner join "user" respnsibleU on respnsibleU.user_id = resp.user_id
            inner join "user" u on resp.created_by = u.user_id
            Where
              resp.asset_id = :assetId and
              (cast(:minTime as date) is null or resp.created_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= resp.created_on) and
              (:userIdsCount = 0 or resp.created_by in (:userIds))
            group by resp.asset_id
          """,
        """
            select
              'RESPONSIBILITY' as entityTypeName, 'Ответственность' as entityTypeNameRu,
              'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,
              resp.asset_id as assetId, resp.created_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              resp.responsibility_id as objectId, r.role_id as objectTypeId, r.role_name as objectTypeName,
              concat(respnsibleU.first_name,' ', respnsibleU.last_name, ' (', respnsibleU.username, ')') as value
            from responsibility resp
            inner join role r on r.role_id = resp.role_id
            inner join "user" respnsibleU on respnsibleU.user_id = resp.user_id
            inner join "user" u on resp.created_by = u.user_id
            where
              resp.asset_id = :assetId and
              (cast(:minTime as date) is null or resp.created_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= resp.created_on) and
              (:userIdsCount = 0 or resp.created_by in (:userIds))
          """
      )),
      null,
      !hasDeleteAction && !isActionTypesEmpty
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.DELETE, new ActionQueries(
        """
            Select 'RESPONSIBILITY' as entityType, 'DELETE' as actionType, count(*) as count
            From responsibility resp
            inner join role r on r.role_id = resp.role_id
            inner join "user" respnsibleU on respnsibleU.user_id = resp.user_id
            inner join "user" u on resp.deleted_by = u.user_id
            Where
              resp.asset_id = :assetId and
              resp.deleted_flag = true and
              (cast(:minTime as date) is null or resp.deleted_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= resp.deleted_on) and
              (:userIdsCount = 0 or resp.deleted_by in (:userIds))
            group by resp.asset_id
          """,
        """
            select
              'RESPONSIBILITY' as entityTypeName, 'Ответственность' as entityTypeNameRu,
              'DELETE' as actionTypeName, 'Удаление' as actionTypeNameRu,
              resp.asset_id as assetId, resp.deleted_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              resp.responsibility_id as objectId, r.role_id as objectTypeId, r.role_name as objectTypeName,
              concat(respnsibleU.first_name,' ', respnsibleU.last_name, ' (', respnsibleU.username, ')') as value
            from responsibility resp
            inner join role r on r.role_id = resp.role_id
            inner join "user" respnsibleU on respnsibleU.user_id = resp.user_id
            inner join "user" u on resp.deleted_by = u.user_id
            WHERE
              resp.asset_id = :assetId and
              resp.deleted_flag = true and
              (cast(:minTime as date) is null or resp.deleted_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= resp.deleted_on) and
              (:userIdsCount = 0 or resp.deleted_by in (:userIds))
          """
      ))
    );
  }

  private EntityType prepareRelationEntityType () {
    if (!entityTypes.isEmpty() && !entityTypes.contains(AssetHistoryEntityType.RELATION)) return null;

    boolean isActionTypesEmpty = actionTypes.isEmpty();
    boolean hasAddAction = actionTypes.contains(AssetHistoryActionType.ADD);
    boolean hasDeleteAction = actionTypes.contains(AssetHistoryActionType.DELETE);
    if (!actionTypes.isEmpty() && !hasAddAction && !hasDeleteAction) return null;

    return new EntityType(
      !hasAddAction && !isActionTypesEmpty
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.ADD, new ActionQueries(
        """
            Select 'RELATION' as entityType, 'ADD' as actionType, count(*) as count
            From relation_component rc
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join relation r on r.relation_id = rc.relation_id
            inner join relation_type rt on rt.relation_type_id = r.relation_type_id
            inner join "user" u on u.user_id = r.created_by
            inner join asset a on a.asset_id = connectedRc.asset_id
            inner join relation_type_component rtc on rtc.relation_type_component_id = connectedRc.relation_type_component_id
            Where
              rc.asset_id = :assetId and
              (cast(:minTime as date) is null or rc.created_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rc.created_on) and
              (:userIdsCount = 0 or rc.created_by in (:userIds))
            group by rc.asset_id
          """,
        """
            select
              'RELATION' as entityTypeName, 'Связь' as entityTypeNameRu,
              'ADD' as actionTypeName, 'Добавление' as actionTypeNameRu,
              rc.asset_id as assetId, r.created_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              r.relation_id as objectId, rt.relation_type_id as objectTypeId, rt.relation_type_name as objectTypeName,
              string_agg(concat(rtc.relation_type_component_name, ' - ',a.asset_displayname), ',') as value
            from relation_component rc
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join relation r on r.relation_id = rc.relation_id
            inner join relation_type rt on rt.relation_type_id = r.relation_type_id
            inner join "user" u on u.user_id = r.created_by
            inner join asset a on a.asset_id = connectedRc.asset_id
            inner join relation_type_component rtc on rtc.relation_type_component_id = connectedRc.relation_type_component_id
            where
              rc.asset_id = :assetId and
              (cast(:minTime as date) is null or rc.created_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rc.created_on) and
              (:userIdsCount = 0 or rc.created_by in (:userIds))
            group by rc.asset_id, r.created_on, u.user_id, u.username, u.first_name, u.last_name, r.relation_id, rt.relation_type_id, rt.relation_type_name
          """
      )),
      null,
      !hasDeleteAction && !isActionTypesEmpty
        ? null
        : new AbstractMap.SimpleEntry<>(AssetHistoryActionType.DELETE, new ActionQueries(
        """
            Select 'RELATION' as entityType, 'DELETE' as actionType, count(*) as count
            From relation_component rc
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join relation r on r.relation_id = rc.relation_id
            inner join relation_type rt on rt.relation_type_id = r.relation_type_id
            inner join "user" u on u.user_id = r.deleted_by
            inner join asset a on a.asset_id = connectedRc.asset_id
            inner join relation_type_component rtc on rtc.relation_type_component_id = connectedRc.relation_type_component_id
            Where
              rc.asset_id = :assetId and
              rc.deleted_flag = true and
              (cast(:minTime as date) is null or rc.deleted_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rc.deleted_on) and
              (:userIdsCount = 0 or rc.deleted_by in (:userIds))
            group by rc.asset_id
          """,
        """
            select
              'RELATION' as entityTypeName, 'Связь' as entityTypeNameRu,
              'DELETE' as actionTypeName, 'Удаление' as actionTypeNameRu,
              rc.asset_id as assetId, r.deleted_on as loggedOn,
              u.user_id as userId, u.username as username,
              u.first_name as firstName, u.last_name as lastName,
              r.relation_id as objectId, rt.relation_type_id as objectTypeId, rt.relation_type_name as objectTypeName,
              string_agg(concat(rtc.relation_type_component_name, ' - ',a.asset_displayname), ',') as value
            from relation_component rc
            inner join relation_component connectedRc on rc.relation_id = connectedRc.relation_id and rc.asset_id != connectedRc.asset_id
            inner join relation r on r.relation_id = rc.relation_id
            inner join relation_type rt on rt.relation_type_id = r.relation_type_id
            inner join "user" u on u.user_id = r.deleted_by
            inner join asset a on a.asset_id = connectedRc.asset_id
            inner join relation_type_component rtc on rtc.relation_type_component_id = connectedRc.relation_type_component_id
            WHERE
              rc.asset_id = :assetId and
              rc.deleted_flag = true and
              (cast(:minTime as date) is null or rc.deleted_on >= :minTime) and
              (cast(:maxTime as date) is null or :maxTime >= rc.deleted_on) and
              (:userIdsCount = 0 or rc.deleted_by in (:userIds))
            group by rc.asset_id, r.created_on, u.user_id, u.username, u.first_name, u.last_name, r.relation_id, rt.relation_type_id, rt.relation_type_name
          """
      ))
    );
  }

  @Getter
  private class EntityType {
    private Map<AssetHistoryActionType, ActionQueries> queryByActionsMap = new HashMap<>();

    public EntityType (
      AbstractMap.SimpleEntry<AssetHistoryActionType, ActionQueries> addEntry,
      AbstractMap.SimpleEntry<AssetHistoryActionType, ActionQueries> editEntry,
      AbstractMap.SimpleEntry<AssetHistoryActionType, ActionQueries> deleteEntry
    ) {
      putEntry(addEntry);
      putEntry(editEntry);
      putEntry(deleteEntry);
    }

    private void putEntry (AbstractMap.SimpleEntry<AssetHistoryActionType, ActionQueries> entry) {
      if (entry == null) return;

      this.queryByActionsMap.put(entry.getKey(), entry.getValue());
    }
  }

  @Getter
  @AllArgsConstructor
  private class ActionQueries {
    private String countQuery;

    private String selectQuery;
  }
}
