package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name= "language", schema = "public")
public class Language {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name="language_id", updatable = false, nullable = false)
  private UUID languageId;

  @Column(name="language", unique = true, nullable = false)
  private String language;

  public Language(String language) {
    this.language = language;
  }
}
