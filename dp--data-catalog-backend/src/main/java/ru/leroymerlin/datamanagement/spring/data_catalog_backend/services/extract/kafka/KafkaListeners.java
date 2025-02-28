package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import logger.LoggerWrapper;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.MetaMessage;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.RelationalObjectName;

/**
 * @author juliwolf
 */

@Service
public class KafkaListeners {
  private static final String META_GROUP_ID = "meta_group";
  private static final String CANON_COLUMN_GROUP_ID = "canon_column_group";
  private static final String CANON_VIEW_GROUP_ID = "canon_view_group";
  private static final String CANON_TABLE_GROUP_ID = "canon_table_group";
  private static final String CANON_SCHEMA_GROUP_ID = "canon_schema_group";
  private static final String DIGITAL_PRODUCT_GROUP_ID = "digital_product_group";

  private final MetaTopicConsumerService metaTopicConsumerService;
  private final RelationTopicConsumerService relationTopicConsumerService;
  private final DigitalProductTopicConsumerService digitalProductTopicConsumerService;

  public KafkaListeners (
    MetaTopicConsumerService metaTopicConsumerService,
    RelationTopicConsumerService relationTopicConsumerService,
    DigitalProductTopicConsumerService digitalProductTopicConsumerService
  ) {
    this.metaTopicConsumerService = metaTopicConsumerService;
    this.relationTopicConsumerService = relationTopicConsumerService;
    this.digitalProductTopicConsumerService = digitalProductTopicConsumerService;
  }

  @org.springframework.kafka.annotation.KafkaListener(
    id = "CANON_COLUMN_TOPIC",
    groupId = CANON_COLUMN_GROUP_ID,
    batch = "true",
    autoStartup = "true",
    topics="canon_column_topic",
    containerFactory = "avroListenerContainerFactory"
  )
  public void listenCanonColumnMessage(
    @Payload ConsumerRecords<String, GenericRecord> events
  ) {
    try {
      relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_COLUMN, events);
    } catch (Exception exception) {
      LoggerWrapper.error("Error while parsing events: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        KafkaListeners.class.getName()
      );
    }
  }

  @org.springframework.kafka.annotation.KafkaListener(
    id = "CANON_VIEW_TOPIC",
    groupId = CANON_VIEW_GROUP_ID,
    batch = "true",
    autoStartup = "true",
    topics="canon_view_topic",
    containerFactory = "avroListenerContainerFactory"
  )
  public void listenCanonViewMessage(
    @Payload ConsumerRecords<String, GenericRecord> events
  ) {
    try {
      relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_VIEW, events);
    } catch (Exception exception) {
      LoggerWrapper.error("Error while parsing events: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        KafkaListeners.class.getName()
      );
    }
  }

  @org.springframework.kafka.annotation.KafkaListener(
    id = "CANON_TABLE_TOPIC",
    groupId = CANON_TABLE_GROUP_ID,
    batch = "true",
    autoStartup = "true",
    topics="canon_table_topic",
    containerFactory = "avroListenerContainerFactory"
  )
  public void listenCanonTableMessage(
    @Payload ConsumerRecords<String, GenericRecord> events
  ) {
    try {
      relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_TABLE, events);
    } catch (Exception exception) {
      LoggerWrapper.error("Error while parsing events: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        KafkaListeners.class.getName()
      );
    }
  }

  @org.springframework.kafka.annotation.KafkaListener(
    id = "CANON_SCHEMA_TOPIC",
    groupId = CANON_SCHEMA_GROUP_ID,
    batch = "true",
    autoStartup = "true",
    topics="canon_schema_topic",
    containerFactory = "avroListenerContainerFactory"
  )
  public void listenCanonSchemaMessage(
    @Payload ConsumerRecords<String, GenericRecord> events
  ) {
    try {
      relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_SCHEMA, events);
    } catch (Exception exception) {
      LoggerWrapper.error("Error while parsing events: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        KafkaListeners.class.getName()
      );
    }
  }

  @org.springframework.kafka.annotation.KafkaListener(
    id = "DIGITAL_PRODUCT_TOPIC",
    groupId = DIGITAL_PRODUCT_GROUP_ID,
    batch = "true",
    autoStartup = "true",
    topics="dpc_digital_product_topic",
    containerFactory = "avroListenerContainerFactory"
  )
  public void listenDigitalProductMessage(
    @Payload ConsumerRecords<String, GenericRecord> events
  ) {
    try {
      digitalProductTopicConsumerService.consumeEvents("dpc_digital_product_topic", events);
    } catch (Exception exception) {
      LoggerWrapper.error("Error while parsing events: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        KafkaListeners.class.getName()
      );
    }
  }

  @Transactional
  @org.springframework.kafka.annotation.KafkaListener(
    groupId = META_GROUP_ID,
    topics = "${spring.kafka.topics.meta}",
    containerFactory = "metaTopicListenerContainerFactory"
  )
  public void listenMetaMessage(
    @Payload ConsumerRecord<String, MetaMessage> messageEvent
  ) {
    metaTopicConsumerService.consumeEvent(messageEvent);
  }
}
