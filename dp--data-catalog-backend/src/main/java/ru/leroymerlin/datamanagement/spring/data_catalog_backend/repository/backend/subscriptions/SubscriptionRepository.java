package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Subscription;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.models.SubscriptionWithConnectedValues;

/**
 * @author juliwolf
 */

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
  @Query("""
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.models.SubscriptionWithConnectedValues(
      s.subscriptionId, a.assetId, a.assetName, a.assetDisplayName,
      at.assetTypeId, at.assetTypeName,
      stws.statusId, stws.statusName,
      lfs.statusId, lfs.statusName,
      u.userId, u.username, u.firstName, u.lastName,
      s.notificationSchedule, s.createdOn, s.createdBy.userId, s.lastModifiedOn, s.modifiedBy.userId
    )
    From Subscription s
    Inner Join Asset a on a.assetId = s.asset.assetId
    Inner Join User u on u.userId = s.ownerUser.userId
    Left Join AssetType at on at.assetTypeId = a.assetType.assetTypeId
    Left Join Status stws on stws.statusId = a.stewardshipStatus.statusId
    Left Join Status lfs on stws.statusId = a.lifecycleStatus.statusId
    Where
      s.subscriptionId = :subscriptionId and
      s.isDeleted = false
  """)
  Optional<SubscriptionWithConnectedValues> getSubscriptionWithConnectedValuesById(
    @Param(value = "subscriptionId") UUID subscriptionId
  );

  @Query(value = """
    Select new ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.subscriptions.models.SubscriptionWithConnectedValues(
      s.subscriptionId, a.assetId, a.assetName, a.assetDisplayName,
      at.assetTypeId, at.assetTypeName,
      stws.statusId, stws.statusName,
      lfs.statusId, lfs.statusName,
      u.userId, u.username, u.firstName, u.lastName,
      s.notificationSchedule, s.createdOn, s.createdBy.userId, s.lastModifiedOn, s.modifiedBy.userId
    )
    From Subscription s
    Inner Join Asset a on a.assetId = s.asset.assetId
    Inner Join User u on u.userId = s.ownerUser.userId
    Left Join AssetType at on at.assetTypeId = a.assetType.assetTypeId
    Left Join Status stws on stws.statusId = a.stewardshipStatus.statusId
    Left Join Status lfs on lfs.statusId = a.lifecycleStatus.statusId
    Where
      (cast(:assetId as org.hibernate.type.PostgresUUIDType) is null or a.assetId = :assetId) and
      (cast(:userId as org.hibernate.type.PostgresUUIDType) is null or u.userId = :userId) and
      (:assetTypeIdsCount = 0 or assetTypeId in :assetTypeIds) and
      (:lifecycleStatusIdsCount = 0 or lfs.statusId in :lifecycleStatusIds) and
      (:stewardshipStatusIdsCount = 0 or stws.statusId in :stewardshipStatusIds) and
      s.isDeleted = false
  """, countQuery = """
    Select count(s.subscriptionId)
    From Subscription s
    Inner Join Asset a on a.assetId = s.asset.assetId
    Inner Join User u on u.userId = s.ownerUser.userId
    Left Join AssetType at on at.assetTypeId = a.assetType.assetTypeId
    Left Join Status stws on stws.statusId = a.stewardshipStatus.statusId
    Left Join Status lfs on lfs.statusId = a.lifecycleStatus.statusId
    Where
      (cast(:assetId as org.hibernate.type.PostgresUUIDType) is null or a.assetId = :assetId) and
      (cast(:userId as org.hibernate.type.PostgresUUIDType) is null or u.userId = :userId) and
      (:assetTypeIdsCount = 0 or assetTypeId in :assetTypeIds) and
      (:lifecycleStatusIdsCount = 0 or lfs.statusId in :lifecycleStatusIds) and
      (:stewardshipStatusIdsCount = 0 or stws.statusId in :stewardshipStatusIds) and
      s.isDeleted = false
  """)
  Page<SubscriptionWithConnectedValues> findAllSubscriptionWithConnectedValuesByParamsPageable(
    @Param(value = "assetId") UUID assetId,
    @Param(value = "userId") UUID userId,
    @Param(value = "assetTypeIdsCount") Integer assetTypeIdsCount,
    @Param(value = "assetTypeIds") List<UUID> assetTypeIds,
    @Param(value = "lifecycleStatusIdsCount") Integer lifecycleStatusIdsCount,
    @Param(value = "lifecycleStatusIds") List<UUID> lifecycleStatusIds,
    @Param(value = "stewardshipStatusIdsCount") Integer stewardshipStatusIdsCount,
    @Param(value = "stewardshipStatusIds") List<UUID> stewardshipStatusIds,
    Pageable pageable
  );

  @Modifying
  @Query(value = """
    UPDATE subscrption
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      asset_id in :assetIds
  """, nativeQuery = true)
  void deleteAllByAssetIds(
    @Param("assetIds") List<UUID> assetIds,
    @Param("userId") UUID userId
  );

  @Modifying
  @Query(value = """
    UPDATE subscrption
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      owner_user_id = :ownerUserId
  """, nativeQuery = true)
  void deleteAllByOwnerUserId(
    @Param("ownerUserId") UUID ownerUserId,
    @Param("userId") UUID userId
  );
}
