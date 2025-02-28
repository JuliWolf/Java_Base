package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.LanguageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;

/**
 * @author JuliWolf
 */
@Service
public class LanguageService {
  @Autowired
  private LanguageRepository languageRepository;

  public Language getLanguage (String language) {
    Optional<Language> ruLanguage = languageRepository.getLanguageByLanguage(language);

    return ruLanguage.orElseGet(() -> languageRepository.save(new Language(language)));
  }
}
