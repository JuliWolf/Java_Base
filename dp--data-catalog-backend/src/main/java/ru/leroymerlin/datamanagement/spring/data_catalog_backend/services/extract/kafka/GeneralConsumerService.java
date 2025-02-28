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
import org.springframework.util.StopWatch;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import logger.LoggerWrapper;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.hibernate.JDBCException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.TableRequest;

/**
 * @author juliwolf
 */

public class GeneralConsumerService {
  private final EntityManager entityManager;

  public GeneralConsumerService (EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Map<String, String> mapHeaders (String kafkaTopic, Headers headers) {
    Map<String, String> headersMap = new HashMap<>();

    headers.forEach(header -> {
      headersMap.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
    });

    headersMap.put("kafka_topic", kafkaTopic);

    return headersMap;
  }

  private Query createCheckTableQuery (String tableName) {
    String tableExistsQuery = """
        SELECT EXISTS (
          SELECT FROM information_schema.tables
          WHERE
            table_schema = 'extract_stage' AND
            table_name = '%tableName%'
        );
      """;

    tableExistsQuery = tableExistsQuery.replace("%tableName%", tableName);
    return entityManager.createNativeQuery(tableExistsQuery);
  }

  public String prepareTableQuery (
    SourceKind sourceKind,
    String query,
    Map<String, String> headers,
    Map<String, TableRequest> tableNameQueries
  ) {
    try {
      String tableName = generateTaleName(sourceKind, headers);

      if (tableNameQueries.containsKey(tableName)) {
        return tableName;
      }

      query = query.replaceAll("%tableName%", tableName);

      Query prepareStatement = entityManager.createNativeQuery(query);
      Query checkTableQuery = createCheckTableQuery(tableName);

      TableRequest tableRequest =  new TableRequest();
      tableRequest.setCheckTableQuery(checkTableQuery);
      tableRequest.setCreateTableQuery(prepareStatement);

      tableNameQueries.put(tableName, tableRequest);

      return tableName;
    } catch (ParseException exception) {
      LoggerWrapper.error("Error while parsing key_timestamp in key: " + headers + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GeneralConsumerService.class.getName()
      );

      return null;
    }
  }

  public String getValue (Object value) {
    if (value != null) {
      return value.toString();
    }

    return null;
  }

  public void executeBatchInsert (List<Query> insertStatements) {
    StopWatch watch = new StopWatch();

    try {
      LoggerWrapper.info(
        "Executing insert queries",
        GeneralConsumerService.class.getName()
      );

      watch.start();
      insertStatements.forEach(Query::executeUpdate);

      entityManager.flush();
    } catch (JDBCException exception) {
      String messageRegExp = ".*(\\[ERROR:[.\\s\\S]*already exists\\.\\]).*";
      Pattern pattern = Pattern.compile(messageRegExp);

      Matcher matcher = pattern.matcher(exception.getMessage());

      if (!matcher.find()) throw exception;

      LoggerWrapper.error("Error while inserting row: " + matcher.group(1),
        exception.getStackTrace(),
        null,
        GeneralConsumerService.class.getName()
      );
    } catch (Exception exception) {
      LoggerWrapper.error("Error while inserting row: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GeneralConsumerService.class.getName()
      );
    } finally {
      watch.stop();

      LoggerWrapper.info(
        "Executing insert queries time took: " + watch.getLastTaskTimeMillis() + " mills",
        GeneralConsumerService.class.getName()
      );
    }
  }

  public void executeBatchCreateTable (List<TableRequest> createTableStatements) {
    try {
      LoggerWrapper.info(
        "Executing create table queries",
        GeneralConsumerService.class.getName()
      );

      createTableStatements.forEach(tableRequest -> {
        boolean isTableExists = isTableExists(tableRequest.getCheckTableQuery());
        if (isTableExists) return;

        tableRequest.getCreateTableQuery().executeUpdate();
      });

      entityManager.flush();
    } catch (Exception exception) {
      LoggerWrapper.error("Error while creating table: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        GeneralConsumerService.class.getName()
      );
    }
  }

  public boolean isTableExists (Query isTableExistQuery) {
    List<Boolean> resultList = isTableExistQuery.getResultList();

    return resultList.get(0);
  }

  public String generateTaleName (
    SourceKind sourceKind,
    Map<String, String> headers
  ) throws ParseException {
    String source = headers.get("source");
    String keyTimestamp = headers.get("key_timestamp");
    String kafkaTopic = headers.get("kafka_topic");

    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").parse(keyTimestamp);

    if (sourceKind.equals(SourceKind.API)) {
      return kafkaTopic + "_" + date.getTime();
    }

    return source + "_" + date.getTime() + "_" + kafkaTopic;
  }

  public String generateConsumer (ConsumerRecord<String, GenericRecord> event) {
    //consumer-canon_column_group-3
    return "consumer-" + event.topic() + "_group-" + event.partition();
  }
}
