package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.log;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Log;

/**
 * @author juliwolf
 */

public interface LogRepository extends JpaRepository<Log, UUID> {
}
