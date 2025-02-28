package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.imageLinkUsage;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.ImageLinkUsage;

/**
 * @author JuliWolf
 */
public interface ImageLinkUsageRepository extends JpaRepository<ImageLinkUsage, UUID> {
  @Query("""
    Select ilu
    From ImageLinkUsage ilu
    Where
      ilu.attribute.attributeId = :attributeId and
      ilu.isDeleted = false
  """)
  List<ImageLinkUsage> findAllByAttributeId(
    @Param("attributeId") UUID attributeId
  );

  @Modifying
  @Query(value = """
    UPDATE image_link_usage
    Set
      deleted_flag = true,
      deleted_on = current_timestamp,
      deleted_by = :userId
    Where
      attribute_id in :attributeIds
  """, nativeQuery = true)
  void deleteAllByAttributeId (
    @Param("attributeIds") List<UUID> attributeIds,
    @Param("userId") UUID userId
  );
}
