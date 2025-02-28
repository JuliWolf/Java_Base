package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceType;

/**
 * @author juliwolf
 */

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaMessage {
  private String import_tool;

  private String import_kind;

  private String import_ver;

  private String kafka_topic;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
  private Date key_timestamp;

  private String status;

  private SourceKind source_kind;

  private SourceType source_type;

  private String source;

  private UUID root_asset_id;

  private Boolean full_meta_flag;

  private List<FilterCriteria> filter_criteria;

  private List<MetaObject> objects;

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class FilterCriteria {
    private String type;

    private String object_type;

    private String condition_type;

    private String value;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class MetaObject {
    private String kafka_topic;

    private Long control_sum;
  }
}
