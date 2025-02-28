package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.RelationalObjectName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPostgreSQLContainer;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * @author juliwolf
 */

@Testable
public class RelationTopicConsumerServiceTest {
  @Autowired
  private RelationTopicConsumerService relationTopicConsumerService;

  @Autowired
  private EntityManager entityManager;

  @ClassRule
  public static PostgreSQLContainer<MockPostgreSQLContainer> postgreSQLContainer = MockPostgreSQLContainer.getInstance();

  RelationTopicUtils relationTopicUtils = new RelationTopicUtils();

  @Test
  public void consumeEventsCanonViewSuccessTest () {
    Date currentDate = relationTopicUtils.generateDate();

    Headers headers = relationTopicUtils.createKeyRecord("adb", RelationalObjectName.CANON_VIEW, currentDate, 24707L);

    List<ConsumerRecord<String, GenericRecord>> genericDataRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      1,
      headers,
      List.of(
        relationTopicUtils.createViewRecord("first_nav_navsql153_store153_ods", "first_v_whse__unit_line_hist", false, "Select * from table", null),
        relationTopicUtils.createViewRecord("second_nav_navsql153_store153_ods", "second_v_whse__unit_line_hist", true, "Select * from table", null),
        relationTopicUtils.createViewRecord("third_nav_navsql153_store153_ods", "third_v_whse__unit_line_hist", true, "Select * from table", null)
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);
    records.put(relationalTopic, genericDataRecords);

    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_VIEW, new ConsumerRecords<>(records));

    String tableName = relationTopicUtils.generateTableName(SourceKind.RELATIONAL, headers, currentDate);
    String selectQueryString = "Select * from extract_stage." + tableName;
    Query selectQuery = entityManager.createNativeQuery(selectQueryString);
    List resultList = selectQuery.getResultList();

    assertEquals(3, resultList.size());
  }

  @Test
  public void consumeEventsCanonViewDuplicateTest () {
    Date currentDate = relationTopicUtils.generateDate();

    Headers headers = relationTopicUtils.createKeyRecord("adb", RelationalObjectName.CANON_VIEW, currentDate, 24707L);

    List<ConsumerRecord<String, GenericRecord>> genericDataRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      1,
      headers,
      List.of(
        relationTopicUtils.createViewRecord("first_nav_navsql153_store153_ods", "first_v_whse__unit_line_hist", false, "Select * from table", null),
        relationTopicUtils.createViewRecord("first_nav_navsql153_store153_ods", "first_v_whse__unit_line_hist", true, "Select * from table", null),
        relationTopicUtils.createViewRecord("third_nav_navsql153_store153_ods", "third_v_whse__unit_line_hist", true, "Select * from table", null)
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);
    records.put(relationalTopic, genericDataRecords);

    assertThrows(Exception.class, () -> relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_VIEW, new ConsumerRecords<>(records)));
  }

  @Test
  public void consumeEventsCanonTableSuccessTest () {
    Date currentDate = relationTopicUtils.generateDate();

    Headers headers = relationTopicUtils.createKeyRecord("adb", RelationalObjectName.CANON_TABLE, currentDate, 46243L);

    List<ConsumerRecord<String, GenericRecord>> genericDataRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      2,
      headers,
      List.of(
        relationTopicUtils.createTableRecord("first_nav_navsql052_store052_ods", "first_whse__doc__status_log_hist", null),
        relationTopicUtils.createTableRecord("second_nav_navsql052_store052_ods", "second_whse__doc__status_log_hist", null)
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);
    records.put(relationalTopic, genericDataRecords);

    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_TABLE, new ConsumerRecords<>(records));

    String tableName = relationTopicUtils.generateTableName(SourceKind.RELATIONAL, headers, currentDate);

    String selectQueryString = "Select * from extract_stage." + tableName;
    Query selectQuery = entityManager.createNativeQuery(selectQueryString);
    List resultList = selectQuery.getResultList();

    assertEquals(2, resultList.size());
  }

  @Test
  public void consumeEventsCanonTableDuplicateTest () {
    Date currentDate = relationTopicUtils.generateDate();

    Headers headers = relationTopicUtils.createKeyRecord("adb", RelationalObjectName.CANON_TABLE, currentDate, 46243L);

    List<ConsumerRecord<String, GenericRecord>> genericDataRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      2,
      headers,
      List.of(
        relationTopicUtils.createTableRecord("first_nav_navsql052_store052_ods", "first_whse__doc__status_log_hist", null),
        relationTopicUtils.createTableRecord("first_nav_navsql052_store052_ods", "first_whse__doc__status_log_hist", null)
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);
    records.put(relationalTopic, genericDataRecords);

    assertThrows(Exception.class, () -> relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_TABLE, new ConsumerRecords<>(records)));
  }

  @Test
  public void consumeEventsCanonColumnSuccessTest () {
    Date currentDate = relationTopicUtils.generateDate();

    Headers headers = relationTopicUtils.createKeyRecord("adb", RelationalObjectName.CANON_COLUMN, currentDate, 2160106L);

    List<ConsumerRecord<String, GenericRecord>> genericDataRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      3,
      headers,
      List.of(
        relationTopicUtils.createColumnRecord("first_nav_navsql114_store114_ods", "first_v_wms_invent__line_det__hist", "first_md5_hash", 1, "YES", "character varying(4096)", "comment", false),
        relationTopicUtils.createColumnRecord("second_nav_navsql114_store114_ods", "second_v_wms_invent__line_det__hist", "second_md5_hash", 3, "NO", "character varying(255)", null, true),
        relationTopicUtils.createColumnRecord("third_nav_navsql114_store114_ods", "third_v_wms_invent__line_det__hist", "third_md5_hash", 4, "NO", "bool", null, true)
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);
    records.put(relationalTopic, genericDataRecords);

    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_COLUMN,new ConsumerRecords<>(records));

    String tableName = relationTopicUtils.generateTableName(SourceKind.RELATIONAL, headers, currentDate);

    String selectQueryString = "Select * from extract_stage." + tableName;
    Query selectQuery = entityManager.createNativeQuery(selectQueryString);
    List resultList = selectQuery.getResultList();

    assertEquals(3, resultList.size());
  }

  @Test
  public void consumeEventsCanonColumnDuplicateTest () {
    Date currentDate = relationTopicUtils.generateDate();

    Headers headers = relationTopicUtils.createKeyRecord("adb", RelationalObjectName.CANON_COLUMN, currentDate, 2160106L);

    List<ConsumerRecord<String, GenericRecord>> genericDataRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      3,
      headers,
      List.of(
        relationTopicUtils.createColumnRecord("first_nav_navsql114_store114_ods", "first_v_wms_invent__line_det__hist", "first_md5_hash", 1, "YES", "character varying(4096)", "comment", false),
        relationTopicUtils.createColumnRecord("first_nav_navsql114_store114_ods", "first_v_wms_invent__line_det__hist", "first_md5_hash", 3, "NO", "character varying(255)", null, true),
        relationTopicUtils.createColumnRecord("third_nav_navsql114_store114_ods", "third_v_wms_invent__line_det__hist", "third_md5_hash", 4, "NO", "bool", null, true)
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);
    records.put(relationalTopic, genericDataRecords);

    assertThrows(Exception.class, () -> relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_COLUMN, new ConsumerRecords<>(records)));
  }


  @Test
  public void consumeEventsCanonSchemaSuccessTest () {
    Date currentDate = relationTopicUtils.generateDate();

    Headers headers = relationTopicUtils.createKeyRecord("adb", RelationalObjectName.CANON_SCHEMA, currentDate, 2160106L);

    List<ConsumerRecord<String, GenericRecord>> genericDataRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      4,
      headers,
      List.of(
        relationTopicUtils.createTableSchema("first_b2b_segmentation_ods", "first_DP2.0"),
        relationTopicUtils.createTableSchema("second_b2b_segmentation_ods", "second_DP2.0"),
        relationTopicUtils.createTableSchema("third_b2b_segmentation_ods", "third_DP2.0"),
        relationTopicUtils.createTableSchema("forth_b2b_segmentation_ods", "forth_DP2.0")
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);
    records.put(relationalTopic, genericDataRecords);

    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_SCHEMA, new ConsumerRecords<>(records));

    String tableName = relationTopicUtils.generateTableName(SourceKind.RELATIONAL, headers, currentDate);

    String selectQueryString = "Select * from extract_stage." + tableName;
    Query selectQuery = entityManager.createNativeQuery(selectQueryString);
    List resultList = selectQuery.getResultList();

    assertEquals(4, resultList.size());
  }

  @Test
  public void consumeEventsCanonSchemaDuplicateTest () {
    Date currentDate = relationTopicUtils.generateDate();

    Headers headers = relationTopicUtils.createKeyRecord("adb", RelationalObjectName.CANON_SCHEMA, currentDate, 2160106L);

    List<ConsumerRecord<String, GenericRecord>> genericDataRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      4,
      headers,
      List.of(
        relationTopicUtils.createTableSchema("first_b2b_segmentation_ods", "first_DP2.0"),
        relationTopicUtils.createTableSchema("first_b2b_segmentation_ods", "first_DP2.0"),
        relationTopicUtils.createTableSchema("third_b2b_segmentation_ods", "third_DP2.0"),
        relationTopicUtils.createTableSchema("forth_b2b_segmentation_ods", "forth_DP2.0")
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);
    records.put(relationalTopic, genericDataRecords);

    assertThrows(Exception.class, () -> relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_SCHEMA, new ConsumerRecords<>(records)));
  }
}
