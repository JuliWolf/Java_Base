package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Status;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.models.StatusUsageCount;

/**
 * @author JuliWolf
 */
public interface StatusRepository extends JpaRepository<Status, UUID>  {

  @Query(value = """
    SELECT s
    FROM Status s
    WHERE
      (:statusName is null or lower(s.statusName) LIKE '%' || lower(:statusName) || '%') and
      (:statusDescription is null or lower(s.statusDescription) LIKE '%' || lower(:statusDescription) || '%') and
      s.isDeleted = false
  """, countQuery = """
    SELECT count(s.statusId) FROM Status s
    WHERE
      (:statusName is null or lower(s.statusName) LIKE '%' || lower(:statusName) || '%') and
      (:statusDescription is null or lower(s.statusDescription) LIKE '%' || lower(:statusDescription) || '%') and
      s.isDeleted = false
  """)
  Page<Status> findAllByStatusNameAndDescriptionPageable (
    @Param("statusName") String statusName,
    @Param("statusDescription") String statusDescription,
    Pageable pageable
  );

  @Query("""
    SELECT s
    FROM Status s
    Where
      s.statusId in :statusIds and
      s.isDeleted = false
  """)
  List<Status> findAllByStatusIds (
    @Param("statusIds") List<UUID> statusIds
  );

  @Query(value = """
      select
            cast(statusId as text) as statusIdText,
            sum(cnt) as statusUsageCount
      from (
        select
            lifecycle_status as statusId,
            count(*) as cnt
        from asset a
        where
            not deleted_flag and
            a.lifecycle_status in :statusIds
        group by lifecycle_status

        union

        select
            stewardship_status as statusId,
            count(*) as cnt
        from asset a
        where
            not deleted_flag and
            a.stewardship_status in :statusIds
        group by stewardship_status
      ) as s
      group by statusId
  """, nativeQuery = true)
  List<StatusUsageCount> countStatusesUsageByStatusIds (
    @Param("statusIds") List<UUID> statusIds
  );
}
