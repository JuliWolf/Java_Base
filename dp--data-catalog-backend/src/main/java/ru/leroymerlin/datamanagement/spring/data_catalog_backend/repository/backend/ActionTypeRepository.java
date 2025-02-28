package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.ActionType;

/**
 * @author JuliWolf
 */
public interface ActionTypeRepository extends JpaRepository<ActionType, UUID> {
}
