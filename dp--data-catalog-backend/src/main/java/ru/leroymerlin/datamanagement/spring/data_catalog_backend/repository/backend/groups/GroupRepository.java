package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.groups;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Group;

/**
 * @author JuliWolf
 */
public interface GroupRepository extends JpaRepository<Group, UUID> {
  @Query(value = """
    SELECT g
    FROM Group g
    WHERE
      (:groupName is null or lower(g.groupName) LIKE '%' || lower(:groupName) || '%') and
      (:groupDescription is null or lower(g.groupDescription) LIKE '%' || lower(:groupDescription) || '%') and
      g.isDeleted = false
  """, countQuery = """
    SELECT count(g.groupId) FROM Group g
    WHERE
      (:groupName is null or lower(g.groupName) LIKE '%' || lower(:groupName) || '%') and
      (:groupDescription is null or lower(g.groupDescription) LIKE '%' || lower(:groupDescription) || '%') and
      g.isDeleted = false
  """)
  Page<Group> findAllByGroupNameAndDescriptionPageable (
    @Param("groupName") String groupName,
    @Param("groupDescription") String groupDescription,
    Pageable pageable
  );

  @Query("""
    Select g
    FROM Group g
    where
      g.groupId in :groupIds and
      g.isDeleted = false
  """)
  List<Group> findGroupByGroupIds (
    @Param("groupIds") List<UUID> groupIds
  );
}
