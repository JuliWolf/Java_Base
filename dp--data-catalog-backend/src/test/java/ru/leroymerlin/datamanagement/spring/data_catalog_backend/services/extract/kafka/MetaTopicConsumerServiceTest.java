package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobObjectRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJobObject;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.MetaMessage;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.Testable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author juliwolf
 */

@Testable
public class MetaTopicConsumerServiceTest {
  @Autowired
  private MetaTopicConsumerService metaTopicConsumerService;

  @Autowired
  private ExtractJobRepository extractJobRepository;
  @Autowired
  private ExtractJobObjectRepository extractJobObjectRepository;

  @Autowired
  private AssetRepository assetRepository;

  @AfterEach
  public void clearJobsAndObjects () {
    extractJobObjectRepository.deleteAll();
    extractJobRepository.deleteAll();
  }

  @Test
  public void createJobAssetNotFoundExceptionTest () {
    Date date = new Date();
    MetaMessage metaMessage = new MetaMessage("import_tool", "import_kind", "import_ver", "test_topic", date, "NEW", SourceKind.RELATIONAL, SourceType.GREENPLUM, "source", UUID.randomUUID(), false, new ArrayList<>(), new ArrayList<>());

    metaTopicConsumerService.consumeEvent(new ConsumerRecord<String, MetaMessage>("test_topic", 1, 0, "event", metaMessage));

      List<ExtractJob> extractJobs = extractJobRepository.findAll();

      assertAll(
        () -> assertEquals("Asset not found", extractJobs.get(0).getJobError()),
        () -> assertTrue(extractJobs.get(0).getJobErrorFlag())
      );
  }

  @Test
  public void createJobSuccessExceptionTest () {
    Date date = new Date();
    MetaMessage.MetaObject tableMetaObject = new MetaMessage.MetaObject("table meta object", 20L);
    MetaMessage.MetaObject columnMetaObject = new MetaMessage.MetaObject("column meta object", 19L);
    Asset asset = assetRepository.save(new Asset("test", null, "some name", null, null, null, null));

    MetaMessage metaMessage = new MetaMessage("import_tool", "import_kind", "import_ver", "test_topic", date, "NEW", SourceKind.RELATIONAL, SourceType.GREENPLUM, "source", asset.getAssetId(), false, new ArrayList<>(), List.of(tableMetaObject, columnMetaObject));

    metaTopicConsumerService.consumeEvent(new ConsumerRecord<String, MetaMessage>("test_topic", 1, 0, "event", metaMessage));

    List<ExtractJob> extractJobs = extractJobRepository.findAll();
    List<ExtractJobObject> objects = extractJobObjectRepository.findAll();

    ExtractJobObject columnMetaObjectFromDB = objects.stream().filter(object -> object.getKafkaTopic().equals(columnMetaObject.getKafka_topic())).findFirst().get();
    ExtractJobObject tableMetaObjectFromDB = objects.stream().filter(object -> object.getKafkaTopic().equals(tableMetaObject.getKafka_topic())).findFirst().get();

    assertAll(
      () -> assertNull("Asset not found", extractJobs.get(0).getJobError()),
      () -> assertEquals(date, extractJobs.get(0).getKeyTimestamp()),
      () -> assertEquals(2, objects.size()),
      () -> assertEquals(columnMetaObject.getControl_sum(), columnMetaObjectFromDB.getControlSum()),
      () -> assertEquals(extractJobs.get(0).getSourceName() + "_" + extractJobs.get(0).getKeyTimestamp().getTime() + "_" + columnMetaObjectFromDB.getKafkaTopic(), columnMetaObjectFromDB.getTableStageName()),
      () -> assertEquals(tableMetaObject.getControl_sum(), tableMetaObjectFromDB.getControlSum()),
      () -> assertEquals(extractJobs.get(0).getSourceName() + "_" + extractJobs.get(0).getKeyTimestamp().getTime() + "_" + tableMetaObjectFromDB.getKafkaTopic(), tableMetaObjectFromDB.getTableStageName())
    );
  }
}
