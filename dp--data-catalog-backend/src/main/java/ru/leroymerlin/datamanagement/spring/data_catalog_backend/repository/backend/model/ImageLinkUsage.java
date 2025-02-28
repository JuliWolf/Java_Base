package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model;

import java.sql.Timestamp;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * @author juliwolf
 */

@jakarta.persistence.Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "image_link_usage", schema = "public")
public class ImageLinkUsage {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "image_link_usage_id", updatable = false, nullable = false)
  private UUID assetLinkUsageId;

  @Column(name = "image_name")
  private String imageName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attribute_id", referencedColumnName = "attribute_id")
  @ToString.Exclude
  private Attribute attribute;

  @Column(name = "created_on")
  @CreationTimestamp
  private Timestamp createdOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User createdBy;

  @Column(name="deleted_flag", columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  @Column(name = "deleted_on")
  private Timestamp deletedOn;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by", referencedColumnName = "user_id")
  @ToString.Exclude
  private User deletedBy;

  public UUID getCreatedByUUID () {
    return createdBy != null ? createdBy.getUserId() : null;
  }

  public ImageLinkUsage (String imageName, Attribute attribute, User createdBy) {
    this.imageName = imageName;
    this.attribute = attribute;
    this.createdBy = createdBy;
    this.createdOn = new Timestamp(System.currentTimeMillis());
  }
}
