package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.structUnits;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.StructUnit;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.structUnits.StructUnitRepository;

/**
 * @author juliwolf
 */

@Service
public class StructUnitsDAO {
  protected final StructUnitRepository structUnitRepository;

  public StructUnitsDAO (StructUnitRepository structUnitRepository) {
    this.structUnitRepository = structUnitRepository;
  }

  public StructUnit findStructUnitByStructUnitId (UUID structUnitId) {
    return structUnitRepository.findStructUnitByStructUnitIdAndIsDeletedFalse(structUnitId);
  }

  public List<StructUnit> findAllStructUnitsByIds (Set<UUID> structUnitIds) {
    return structUnitRepository.findAllStructUnitsByIds(structUnitIds);
  }
}
