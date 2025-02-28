package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.RelationalObjectName;

/**
 * @author juliwolf
 */

public class RelationTopicUtils {

  String canonViewSchemaString = """
      {
        "type": "record",
        "name": "canon_view",
        "fields": [
          {"name" : "schema_name", "type":"string"},
          {"name" : "view_name",  "type":"string"},
          {"name" : "materialized_flag", "type":"boolean"},
          {"name" : "view_sql", "type":["null","string"]},
          {"name" : "view_comment", "type":["null","string"]}
        ]
      }
    """;

  String canonTableString = """
      {
         "type": "record",
         "name": "canon_table",
         "fields": [
           {"name" : "schema_name", "type":"string"},
           {"name" : "table_name",  "type":"string"},
           {"name" : "table_comment", "type":["null","string"]}
         ]
       }
    """;

  String canonSchemaString = """
      {
          "type": "record",
          "name": "canon_schema",
          "fields": [
            {"name" : "schema_name","type":"string"},
            {"name" : "schema_comment","type":["null","string"]}
          ]
        }
    """;

  String canonColumnString = """
      {
         "type":"record",
         "name":"canon_column",
         "fields":[
           {"name":"schema_name", "type":"string"},
           {"name":"object_name", "type":"string"},
           {"name":"column_name", "type":"string"},
           {"name":"column_position", "type":"int"},
           {"name":"is_nullable", "type":"string"},
           {"name":"column_type", "type":"string"},
           {"name":"column_comment","type":["null","string"]},
           {"name":"pk_flag","type":"boolean"}
         ]
       }
    """;

  Schema canonViewSchema = new Schema.Parser().parse(canonViewSchemaString);
  Schema canonTableSchema = new Schema.Parser().parse(canonTableString);
  Schema canonSchemaSchema = new Schema.Parser().parse(canonSchemaString);
  Schema canonColumnSchema = new Schema.Parser().parse(canonColumnString);

  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

  public GenericData.Record createViewRecord (
    String schema_name,
    String view_name,
    Boolean materialized_flag,
    String view_sql,
    String view_comment
  ) {
    GenericRecordBuilder firstCanonViewBuilder = new GenericRecordBuilder(canonViewSchema);
    firstCanonViewBuilder.set("schema_name", schema_name);
    firstCanonViewBuilder.set("view_name", view_name);
    firstCanonViewBuilder.set("materialized_flag", materialized_flag);
    firstCanonViewBuilder.set("view_sql", view_sql);
    firstCanonViewBuilder.set("view_comment", view_comment);
    return firstCanonViewBuilder.build();
  }

  public List<ConsumerRecord<String, GenericRecord>> mapRecords (
    String topicName,
    Integer partition,
    Headers headers,
    List<GenericRecord> records
  ) {
    return records.stream().map(record ->  new ConsumerRecord<String, GenericRecord>(topicName, partition, 0, -1L, TimestampType.NO_TIMESTAMP_TYPE, -1, -1, null, record, headers, Optional.empty())).toList();
  }

  public GenericData.Record createColumnRecord (
    String schema_name,
    String object_name,
    String column_name,
    Integer column_position,
    String is_nullable,
    String column_type,
    String column_comment,
    Boolean pk_flag
  ) {
    GenericRecordBuilder firstCanonColumnBuilder = new GenericRecordBuilder(canonColumnSchema);
    firstCanonColumnBuilder.set("schema_name", schema_name);
    firstCanonColumnBuilder.set("object_name", object_name);
    firstCanonColumnBuilder.set("column_name", column_name);
    firstCanonColumnBuilder.set("column_position", column_position);
    firstCanonColumnBuilder.set("is_nullable", is_nullable);
    firstCanonColumnBuilder.set("column_type", column_type);
    firstCanonColumnBuilder.set("column_comment", column_comment);
    firstCanonColumnBuilder.set("pk_flag", pk_flag);
    return firstCanonColumnBuilder.build();
  }

  public GenericData.Record createTableRecord (
    String schema_name,
    String table_name,
    String table_comment
  ) {
    GenericRecordBuilder firstCanonTableBuilder = new GenericRecordBuilder(canonTableSchema);
    firstCanonTableBuilder.set("schema_name", schema_name);
    firstCanonTableBuilder.set("table_name", table_name);
    firstCanonTableBuilder.set("table_comment", table_comment);
    return firstCanonTableBuilder.build();
  }

  public GenericData.Record createTableSchema (
    String schema_name,
    String schema_comment
  ) {
    GenericRecordBuilder builder = new GenericRecordBuilder(canonSchemaSchema);
    builder.set("schema_name", schema_name);
    builder.set("schema_comment", schema_comment);
    return builder.build();
  }

  public Date generateDate () {
    Calendar calendar = Calendar.getInstance();
    return calendar.getTime();
  }

  public Headers createKeyRecord (
    String source,
    RelationalObjectName relationalKafkaTopic,
    Date currentDate,
    Long controlSum
  ) {
    RecordHeaders headers = new RecordHeaders();
    headers.add(new RecordHeader("source", source.getBytes()));
    headers.add(new RecordHeader("kafka_topic", relationalKafkaTopic.getValue().getBytes()));
    headers.add(new RecordHeader("key_timestamp", simpleDateFormat.format(currentDate.getTime()).getBytes()));
    headers.add(new RecordHeader("control_sum", controlSum.toString().getBytes()));
    return headers;
  }

  public Headers createKeyRecord (
    String source,
    String kafkaTopic,
    Date currentDate,
    Long controlSum
  ) {
    RecordHeaders headers = new RecordHeaders();
    headers.add(new RecordHeader("source", source.getBytes()));
    headers.add(new RecordHeader("kafka_topic", kafkaTopic.getBytes()));
    headers.add(new RecordHeader("key_timestamp", simpleDateFormat.format(currentDate.getTime()).getBytes()));
    headers.add(new RecordHeader("control_sum", controlSum.toString().getBytes()));
    return headers;
  }

  public String generateTableName (SourceKind sourceKind, Headers headers, Date currentDate) {
    Map<String, Object> mappedHeaders = mapHeaders(headers);

    if (sourceKind.equals(SourceKind.API)) {
      return mappedHeaders.get("kafka_topic") + "_" + currentDate.getTime();
    }

    return mappedHeaders.get("source") + "_" + currentDate.getTime() + "_" + mappedHeaders.get("kafka_topic");
  }

  private Map<String, Object> mapHeaders (Headers headers) {
    Map<String, Object> headersMap = new HashMap<>();

    headers.forEach(header -> {
      headersMap.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
    });

    return headersMap;
  }
}
