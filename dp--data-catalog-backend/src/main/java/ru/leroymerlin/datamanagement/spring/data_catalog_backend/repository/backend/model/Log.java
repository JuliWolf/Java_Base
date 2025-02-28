package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.RequestType;

/**
 * @author juliwolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "log", schema = "public")
public class Log {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "log_id", updatable = false, nullable = false)
  private UUID logId;

  @Column(name = "request_type", columnDefinition = "varchar(10)")
  @Enumerated(EnumType.STRING)
  private RequestType requestType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id")
  @ToString.Exclude
  private User user;

  @Column(name="controller_name", columnDefinition = "varchar(100)")
  @Size(max = 100)
  private String controllerName;

  @Column(name="path_variables", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String pathVariables;

  @Column(name="request_json", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String requestJSON;

  @Column(name="response_json", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String responseJSON;

  @Column(name="response_code", columnDefinition = "smallint")
  private int responseCode;

  @CreationTimestamp
  @Column(name="logged_on")
  private java.sql.Timestamp loggedOn;

  public Log (RequestType requestType, User user, String controllerName, String pathVariables, String requestJSON, String responseJSON, int responseCode) {
    this.requestType = requestType;
    this.user = user;
    this.controllerName = controllerName;
    this.pathVariables = pathVariables;
    this.requestJSON = requestJSON;
    this.responseJSON = responseJSON;
    this.responseCode = responseCode;
  }
}
