package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.entities;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Entity;

/**
 * @author JuliWolf
 */
@Service
public class EntitiesDAO {

  @Autowired
  private EntityRepository entityRepository;

  public Entity findEntityById (UUID entityId) throws EntityNotFoundException {
    Optional<Entity> entity = entityRepository.findById(entityId);

    if (entity.isEmpty()) {
      throw new EntityNotFoundException();
    }

    if (entity.get().getIsDeleted()) {
      throw new EntityNotFoundException();
    }

    return entity.get();
  }
}
