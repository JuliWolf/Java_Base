package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
public class RelationTopicConsumerService {
  private final EntityManager entityManager;
  private final GeneralConsumerService generalConsumerService;

  public RelationTopicConsumerService (EntityManager entityManager) {
    this.entityManager = entityManager;
    this.generalConsumerService = new GeneralConsumerService(entityManager);
  }

  @Transactional
  public void consumeEvents (
    RelationalObjectName relationalObjectName,
    ConsumerRecords<String, GenericRecord> events
  ) {
    LoggerWrapper.info(
      "Start to consuming relational events for object " + relationalObjectName,
      RelationTopicConsumerService.class.getName()
    );

    Map<String, TableRequest> tableNameQueries = new HashMap<>();

    events.forEach(event -> {
      Map<String, String> headersList = generalConsumerService.mapHeaders(relationalObjectName.getValue(), event.headers());

      switch (relationalObjectName) {
        case CANON_VIEW: processRelationView(headersList, event, tableNameQueries); break;
        case CANON_TABLE: processRelationTable(headersList, event, tableNameQueries); break;
        case CANON_COLUMN: processRelationColumn(headersList, event, tableNameQueries); break;
        case CANON_SCHEMA: processRelationSchema(headersList, event, tableNameQueries); break;
      }
    });

    LoggerWrapper.info(
      "Prepared table names to insert values " + tableNameQueries.keySet(),
      RelationTopicConsumerService.class.getName()
    );

    generalConsumerService.executeBatchCreateTable(tableNameQueries.values().stream().toList());

    List<Query> insertList = tableNameQueries.values()
      .stream()
      .flatMap(tableRequest -> tableRequest.getRows().stream())
      .toList();

    LoggerWrapper.info(
      "Prepared insert values count " + events.count(),
      RelationTopicConsumerService.class.getName()
    );

    generalConsumerService.executeBatchInsert(insertList);
  }

  private void processRelationTable (
    Map<String, String> headers,
    ConsumerRecord<String, GenericRecord> event,
    Map<String, TableRequest> tableNameQueries
  ) {
    String createTableQuery = """
        create table IF NOT EXISTS extract_stage.%tableName% (
          schema_name   varchar(255) not null,
          table_name    varchar(255) not null,
          table_comment text,
          consumer      varchar(255),
          constraint %tableName%_pk
            primary key (schema_name, table_name)
        );
      """;

    String tableName = generalConsumerService.prepareTableQuery(SourceKind.RELATIONAL, createTableQuery, headers, tableNameQueries);

    String insertRow = """
        INSERT INTO extract_stage.%tableName% (schema_name, table_name, table_comment, consumer)
        VALUES (:schema_name, :table_name, :table_comment, :consumer)
      """;
    insertRow = insertRow.replace("%tableName%", tableName);

    GenericRecord value = event.value();

    Query prepareStatement = entityManager.createNativeQuery(insertRow);
    prepareStatement.setParameter("schema_name", value.get("schema_name").toString());
    prepareStatement.setParameter("table_name", value.get("table_name").toString());
    prepareStatement.setParameter("table_comment", generalConsumerService.getValue(value.get("table_comment")));
    prepareStatement.setParameter("consumer", generalConsumerService.generateConsumer(event));

    TableRequest tableRequest = tableNameQueries.get(tableName);
    tableRequest.getRows().add(prepareStatement);
  }

  private void processRelationView (
    Map<String, String> headers,
    ConsumerRecord<String, GenericRecord> event,
    Map<String, TableRequest> tableNameQueries
  ) {
    String createViewQuery = """
        create table IF NOT EXISTS extract_stage.%tableName% (
          schema_name       varchar(255) not null,
          view_name         varchar(255) not null,
          materialized_flag bool,
          view_sql          text,
          view_comment      text,
          consumer          varchar(255),
          constraint %tableName%_pk
            primary key (schema_name, view_name)
        );
      """;

    String tableName = generalConsumerService.prepareTableQuery(SourceKind.RELATIONAL, createViewQuery, headers, tableNameQueries);

    String insertRow = """
        INSERT INTO extract_stage.%tableName% (schema_name, view_name, materialized_flag, view_sql, view_comment, consumer)
        VALUES (:schema_name, :view_name, :materialized_flag, :view_sql, :view_comment, :consumer)
      """;
    insertRow = insertRow.replace("%tableName%", tableName);

    GenericRecord value = event.value();

    Query prepareStatement = entityManager.createNativeQuery(insertRow);
    prepareStatement.setParameter("schema_name", value.get("schema_name").toString());
    prepareStatement.setParameter("view_name", value.get("view_name").toString());
    prepareStatement.setParameter("materialized_flag", value.get("materialized_flag"));
    prepareStatement.setParameter("view_sql", generalConsumerService.getValue(value.get("view_sql")));
    prepareStatement.setParameter("view_comment", generalConsumerService.getValue(value.get("view_comment")));
    prepareStatement.setParameter("consumer", generalConsumerService.generateConsumer(event));

    TableRequest tableRequest = tableNameQueries.get(tableName);
    tableRequest.getRows().add(prepareStatement);
  }

  private void processRelationColumn (
    Map<String, String> headers,
    ConsumerRecord<String, GenericRecord> event,
    Map<String, TableRequest> tableNameQueries
  ) {
    String createColumnQuery = """
        create table IF NOT EXISTS extract_stage.%tableName% (
          schema_name      varchar(255) not null,
          object_name      varchar(255) not null,
          column_name      varchar(255) not null,
          column_position  integer,
          is_nullable      varchar(3),
          column_type      varchar(255),
          column_comment   text,
          pk_flag          bool,
          consumer         varchar(255),
          constraint %tableName%_pk
            primary key (schema_name, object_name, column_name)
        );
      """;

    String tableName = generalConsumerService.prepareTableQuery(SourceKind.RELATIONAL, createColumnQuery, headers, tableNameQueries);

    String insertRow = """
        INSERT INTO extract_stage.%tableName% (schema_name, object_name, column_name, column_position, is_nullable, column_type, column_comment, pk_flag, consumer)
        VALUES (:schema_name, :object_name, :column_name, :column_position, :is_nullable, :column_type, :column_comment, :pk_flag, :consumer)
      """;
    insertRow = insertRow.replace("%tableName%", tableName);

    GenericRecord value = event.value();

    Query prepareStatement = entityManager.createNativeQuery(insertRow);
    prepareStatement.setParameter("schema_name", value.get("schema_name").toString());
    prepareStatement.setParameter("object_name", value.get("object_name").toString());
    prepareStatement.setParameter("column_name", value.get("column_name").toString());
    prepareStatement.setParameter("column_position", value.get("column_position"));
    prepareStatement.setParameter("is_nullable", generalConsumerService.getValue(value.get("is_nullable")));
    prepareStatement.setParameter("column_type", generalConsumerService.getValue(value.get("column_type")));
    prepareStatement.setParameter("column_comment", generalConsumerService.getValue(value.get("column_comment")));
    prepareStatement.setParameter("pk_flag", value.get("pk_flag"));
    prepareStatement.setParameter("consumer", generalConsumerService.generateConsumer(event));

    TableRequest tableRequest = tableNameQueries.get(tableName);
    tableRequest.getRows().add(prepareStatement);
  }

  private void processRelationSchema (
    Map<String, String> headers,
    ConsumerRecord<String, GenericRecord> event,
    Map<String, TableRequest> tableNameQueries
  ) {
    String createSchemaQuery = """
      create table IF NOT EXISTS extract_stage.%tableName% (
          schema_name    varchar(255) not null
            constraint %tableName%_pk
              primary key,
          schema_comment text,
          consumer       varchar(255)
        );
      """;

    String tableName = generalConsumerService.prepareTableQuery(SourceKind.RELATIONAL, createSchemaQuery, headers, tableNameQueries);

    String insertRow = """
        INSERT INTO extract_stage.%tableName% (schema_name, schema_comment, consumer)
        VALUES (:schema_name, :schema_comment, :consumer)
      """;
    insertRow = insertRow.replace("%tableName%", tableName);

    GenericRecord value = event.value();

    Query prepareStatement = entityManager.createNativeQuery(insertRow);
    prepareStatement.setParameter("schema_name", value.get("schema_name").toString());
    prepareStatement.setParameter("schema_comment", generalConsumerService.getValue(value.get("schema_comment")));
    prepareStatement.setParameter("consumer", generalConsumerService.generateConsumer(event));

    TableRequest tableRequest = tableNameQueries.get(tableName);
    tableRequest.getRows().add(prepareStatement);
  }
}
