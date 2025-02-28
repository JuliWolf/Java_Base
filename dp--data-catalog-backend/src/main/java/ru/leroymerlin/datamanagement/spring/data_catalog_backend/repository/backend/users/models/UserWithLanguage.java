package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType;

/**
 * @author juliwolf
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserWithLanguage {
  private UUID userId;

  private String username;

  private String email;

  private String firstName;

  private String lastName;

  private SourceType source;

  private String language;

  private java.sql.Timestamp createdOn;

  private UUID createdBy;

  private java.sql.Timestamp lastModifiedOn;

  private UUID lastModifiedBy;

  private java.sql.Timestamp lastAuthTime;
}
