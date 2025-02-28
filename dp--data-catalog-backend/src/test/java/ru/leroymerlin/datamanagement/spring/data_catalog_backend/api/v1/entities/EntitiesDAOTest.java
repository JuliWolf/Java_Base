package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.entities;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.entities.EntitiesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.entities.EntityNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.EntityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Entity;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author JuliWolf
 */
@Testable
public class EntitiesDAOTest {
  @Autowired
  private EntitiesDAO entitiesDAO;

  @Autowired
  private EntityRepository entityRepository;

  @Test
  public void findEntityByIdNotFoundExceptionIntegrationTest () {
    try {
      assertThrows(EntityNotFoundException.class, () -> entitiesDAO.findEntityById(new UUID(123,123)));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  @Test
  public void findEntityByIdSuccessIntegrationTest () {
    try {
      List<Entity> entities = entityRepository.findAll();
      Entity firstEntity = entities.get(0);

      assertEquals(firstEntity.getId().toString(), entitiesDAO.findEntityById(firstEntity.getId()).getId().toString());
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }
}
