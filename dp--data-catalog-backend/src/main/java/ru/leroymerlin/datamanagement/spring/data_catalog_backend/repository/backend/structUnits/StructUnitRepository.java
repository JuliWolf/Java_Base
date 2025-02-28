package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.structUnits;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.StructUnit;

/**
 * @author JuliWolf
 */
public interface StructUnitRepository extends JpaRepository<StructUnit, UUID> {
  StructUnit findStructUnitByStructUnitIdAndIsDeletedFalse(UUID structUnitId);

  @Query("""
    Select s
    From StructUnit s
    Where
      s.isDeleted = false and
      s.structUnitId in :structUnitIds
  """)
  List<StructUnit> findAllStructUnitsByIds (Set<UUID> structUnitIds);
}
