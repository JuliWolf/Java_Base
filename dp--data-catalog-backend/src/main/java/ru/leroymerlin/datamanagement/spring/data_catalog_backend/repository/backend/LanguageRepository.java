package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;

/**
 * @author JuliWolf
 */
public interface LanguageRepository extends JpaRepository<Language, UUID> {
  Optional<Language> getLanguageByLanguage(String language);
}
