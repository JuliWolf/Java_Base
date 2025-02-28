package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.EntityNameType;

/**
 * @author JuliWolf
 */
@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "entity", schema = "public")
public class Entity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "entity_id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "entity_name")
  @Enumerated(EnumType.STRING)
  private EntityNameType entityName;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  public Entity(EntityNameType entityName, User createdBy) {
    this.entityName = entityName;
  }
}
