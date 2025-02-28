package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ActionTypeName;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "action_type", schema = "public")
public class ActionType {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "action_type_id", updatable = false, nullable = false)
  private UUID actionTypeId;

  @Column(name = "action_type_name")
  @Enumerated(EnumType.STRING)
  private ActionTypeName actionTypeName;

  @Column(name = "action_type_description")
  private String actionTypeDescription;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_language", referencedColumnName = "language_id")
  @ToString.Exclude
  private Language language;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  public String getLanguageName () {
    return language != null ? language.getLanguage() : null;
  }

  public ActionType(ActionTypeName actionTypeName, String actionTypeDescription, Language language, User createdBy) {
    this.actionTypeName = actionTypeName;
    this.actionTypeDescription = actionTypeDescription;
    this.language = language;
  }
}
