package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.MockPostgreSQLContainer;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

@Testable
public class DigitalProductTopicConsumerServiceTest {
  @Autowired
  private DigitalProductTopicConsumerService digitalProductTopicConsumerService;

  @Autowired
  private EntityManager entityManager;

  @ClassRule
  public static PostgreSQLContainer<MockPostgreSQLContainer> postgreSQLContainer = MockPostgreSQLContainer.getInstance();

  RelationTopicUtils relationTopicUtils = new RelationTopicUtils();

  String digitalProductSchemaString = """
      {
        "type": "record",
        "name": "digital_product",
        "fields": [
          {"name" : "digital_product_code", "type":"string"},
          {"name" : "digital_product_name",  "type":"string"},
          {"name" : "digital_product_description","type":["null","string"]},
          {"name" : "digital_product_owner","type":["null","string"]},
          {"name" : "digital_product_manager","type":["null","string"]},
          {"name" : "digital_product_status", "type":"string"}
        ]
      }
    """;
  Schema digitalProductSchema = new Schema.Parser().parse(digitalProductSchemaString);

  @Test
  public void consumeEventsDigitalProductSuccessTest () {
    Date currentDate = relationTopicUtils.generateDate();

    Headers headers = relationTopicUtils.createKeyRecord("Digital Product", "dpc_digital_product_topic", currentDate, 24707L);

    List<ConsumerRecord<String, GenericRecord>> genericDataRecords = relationTopicUtils.mapRecords(
      "dpc_digital_product_topic",
      1,
      headers,
      List.of(
        createDigitalProductRecord("1P1194", "first_Comprehensive marketing analytics", "Комплексная маркетинговая аналитика для команды онлайн маркетинга.", "60236678", "60236673", "new"),
        createDigitalProductRecord("2P1194", "second_Comprehensive marketing analytics", "Комплексная маркетинговая аналитика для команды онлайн маркетинга.", "60236679", "60236679", "old"),
        createDigitalProductRecord("3P1194", "third_Comprehensive marketing analytics", "Комплексная маркетинговая аналитика для команды онлайн маркетинга.", "60236676", "60236676", "new")
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("dpc_digital_product_topic", 1);
    records.put(relationalTopic, genericDataRecords);

    digitalProductTopicConsumerService.consumeEvents("dpc_digital_product_topic", new ConsumerRecords<>(records));

    String tableName = relationTopicUtils.generateTableName(SourceKind.API, headers, currentDate);
    String selectQueryString = "Select * from extract_stage." + tableName;
    Query selectQuery = entityManager.createNativeQuery(selectQueryString);
    List resultList = selectQuery.getResultList();

    assertEquals(3, resultList.size());
  }

  public GenericData.Record createDigitalProductRecord (
    String digital_product_code,
    String digital_product_name,
    String digital_product_description,
    String digital_product_owner,
    String digital_product_manager,
    String digital_product_status
  ) {
    GenericRecordBuilder digitalProductBuilder = new GenericRecordBuilder(digitalProductSchema);
    digitalProductBuilder.set("digital_product_code", digital_product_code);
    digitalProductBuilder.set("digital_product_name", digital_product_name);
    digitalProductBuilder.set("digital_product_description", digital_product_description);
    digitalProductBuilder.set("digital_product_owner", digital_product_owner);
    digitalProductBuilder.set("digital_product_manager", digital_product_manager);
    digitalProductBuilder.set("digital_product_status", digital_product_status);
    return digitalProductBuilder.build();
  }
}
