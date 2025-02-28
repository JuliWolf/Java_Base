package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

/**
 * @author juliwolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "custom_view", schema = "public")
public class CustomView {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "custom_view_id", updatable = false, nullable = false)
  private UUID customViewId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "asset_type_id", referencedColumnName = "asset_type_id")
  @ToString.Exclude
  @OnDelete(action = OnDeleteAction.CASCADE)
  private AssetType assetType;

  @Column(name = "custom_view_name")
  private String customViewName;

  @Column(name="header_row_names", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String headerRowNames;

  @Column(name = "header_prepare_query", columnDefinition = "text")
  private String headerPrepareQuery;

  @Column(name = "header_select_query", columnDefinition = "text")
  private String headerSelectQuery;

  @Column(name = "header_clear_query", columnDefinition = "text")
  private String headerClearQuery;

  @Column(name="table_column_names", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String tableColumnNames;

  @Column(name = "table_prepare_query", columnDefinition = "text")
  private String tablePrepareQuery;

  @Column(name = "table_select_query", columnDefinition = "text")
  private String tableSelectQuery;

  @Column(name = "table_clear_query", columnDefinition = "text")
  private String tableClearQuery;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", referencedColumnName = "role_id")
  @ToString.Exclude
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Role role;

  @Column(name = "created_on")
  @CreationTimestamp
  private java.sql.Timestamp createdOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", referencedColumnName = "user_id")
  @ToString.Exclude
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User createdBy;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  @Column(name = "deleted_on")
  private java.sql.Timestamp deletedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by", referencedColumnName = "user_id")
  @ToString.Exclude
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User deletedBy;

  @Column(name = "last_modified_on")
  private java.sql.Timestamp lastModifiedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_modified_by", referencedColumnName = "user_id")
  @ToString.Exclude
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User lastModifiedBy;

  public UUID getRoleUUID () {
    return role != null ? role.getRoleId() : null;
  }

  public UUID getCreatedByUUID () {
    return createdBy != null ? createdBy.getUserId() : null;
  }

  public UUID getLastModifiedByUUID () {
    return lastModifiedBy != null ? lastModifiedBy.getUserId() : null;
  }

  public CustomView (AssetType assetType, String customViewName, String headerRowNames, String headerPrepareQuery, String headerSelectQuery, String headerClearQuery, String tableColumnNames, String tablePrepareQuery, String tableSelectQuery, String tableClearQuery, Role role, User createdBy) {
    this.assetType = assetType;
    this.customViewName = customViewName;
    this.headerRowNames = headerRowNames;
    this.headerPrepareQuery = headerPrepareQuery;
    this.headerSelectQuery = headerSelectQuery;
    this.headerClearQuery = headerClearQuery;
    this.tableColumnNames = tableColumnNames;
    this.tablePrepareQuery = tablePrepareQuery;
    this.tableSelectQuery = tableSelectQuery;
    this.tableClearQuery = tableClearQuery;
    this.role = role;
    this.createdBy = createdBy;
  }
}
