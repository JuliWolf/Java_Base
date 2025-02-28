package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.LanguageRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author JuliWolf
 */
@Testable
public class LanguageServiceTest {

  @Autowired
  private LanguageRepository languageRepository;

  @Autowired
  private LanguageService languageService;

  @AfterEach
  public void clearTables() {
    languageRepository.deleteAll();
  }

  @Test
  public void getLanguageCreateNewTest() {
    String lang = "eng";
    Optional<Language> eng = languageRepository.getLanguageByLanguage(lang);

    assertTrue(eng.isEmpty());

    languageService.getLanguage(lang);

    Optional<Language> createdLang = languageRepository.getLanguageByLanguage(lang);

    assertTrue(createdLang.isPresent());
  }

  @Test
  public void getLanguageUseExistingTest() {
    String lang = "ru";
    // В миграциях у нас уже добавляется ru язык
    Language language = languageService.getLanguage(lang);

    Optional<Language> createdLang = languageRepository.getLanguageByLanguage(lang);

    assertEquals(language.getLanguageId(), createdLang.get().getLanguageId());
  }
}
