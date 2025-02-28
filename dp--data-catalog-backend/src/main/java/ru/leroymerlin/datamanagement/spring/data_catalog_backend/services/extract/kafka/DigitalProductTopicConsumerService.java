package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import logger.LoggerWrapper;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Headers;
import org.hibernate.JDBCException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.RelationalObjectName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.TableRequest;

/**
 * @author juliwolf
 */

@Service
public class DigitalProductTopicConsumerService {
  private final EntityManager entityManager;
  private final GeneralConsumerService generalConsumerService;

  public DigitalProductTopicConsumerService (EntityManager entityManager) {
    this.entityManager = entityManager;

    this.generalConsumerService = new GeneralConsumerService(entityManager);
  }

  @Transactional
  public void consumeEvents (
    String kafkaTopic,
    ConsumerRecords<String, GenericRecord> events
  ) {
    LoggerWrapper.info(
      "Start to consuming digital product events for object",
      DigitalProductTopicConsumerService.class.getName()
    );

    Map<String, TableRequest> tableNameQueries = new HashMap<>();

    events.forEach(event -> {
      Map<String, String> headersList = generalConsumerService.mapHeaders(kafkaTopic, event.headers());

      processDigitalProduct(headersList, event, tableNameQueries);
    });

    LoggerWrapper.info(
      "Prepared table names to insert values " + tableNameQueries.keySet(),
      DigitalProductTopicConsumerService.class.getName()
    );

    generalConsumerService.executeBatchCreateTable(tableNameQueries.values().stream().toList());

    List<Query> insertList = tableNameQueries.values()
      .stream()
      .flatMap(tableRequest -> tableRequest.getRows().stream())
      .toList();

    LoggerWrapper.info(
      "Prepared insert values count " + events.count(),
      DigitalProductTopicConsumerService.class.getName()
    );

    generalConsumerService.executeBatchInsert(insertList);
  }

  private void processDigitalProduct (
    Map<String, String> headers,
    ConsumerRecord<String, GenericRecord> event,
    Map<String, TableRequest> tableNameQueries
  ) {
    String createTableQuery = """
        create table IF NOT EXISTS extract_stage.%tableName% (
          digital_product_code        varchar(10)
                                      primary key,
          digital_product_name        text not null,
          digital_product_description text,
          digital_product_owner       varchar(10),
          digital_product_manager     varchar(10),
          digital_product_status      varchar(30) not null,
          consumer                    varchar(255)
        );
      """;

    String tableName = generalConsumerService.prepareTableQuery(SourceKind.API, createTableQuery, headers, tableNameQueries);

    String insertRow = """
        INSERT INTO extract_stage.%tableName% (digital_product_code, digital_product_name, digital_product_description, digital_product_owner, digital_product_manager, digital_product_status, consumer)
        VALUES (:digital_product_code, :digital_product_name, :digital_product_description, :digital_product_owner, :digital_product_manager, :digital_product_status, :consumer)
      """;
    insertRow = insertRow.replace("%tableName%", tableName);

    GenericRecord value = event.value();

    Query prepareStatement = entityManager.createNativeQuery(insertRow);
    prepareStatement.setParameter("digital_product_code", value.get("digital_product_code").toString());
    prepareStatement.setParameter("digital_product_name", value.get("digital_product_name").toString());
    prepareStatement.setParameter("digital_product_description", generalConsumerService.getValue(value.get("digital_product_description")));
    prepareStatement.setParameter("digital_product_owner", generalConsumerService.getValue(value.get("digital_product_owner")));
    prepareStatement.setParameter("digital_product_manager", generalConsumerService.getValue(value.get("digital_product_manager")));
    prepareStatement.setParameter("digital_product_status", value.get("digital_product_status").toString());
    prepareStatement.setParameter("consumer", generalConsumerService.generateConsumer(event));

    TableRequest tableRequest = tableNameQueries.get(tableName);
    tableRequest.getRows().add(prepareStatement);
  }
}
