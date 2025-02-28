package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import logger.LoggerWrapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Asset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobObjectRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJobObject;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.JobStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.MetaMessage;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.exceptions.AssetNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class MetaTopicConsumerService {
  private final AssetsDAO assetsDAO;

  private final ExtractJobRepository extractJobRepository;

  private final ExtractJobObjectRepository extractJobObjectRepository;

  private final ObjectMapper objectMapper;

  @Value("${spring.kafka.topics.meta}")
  private String metaTopic;

  @Autowired
  public MetaTopicConsumerService (
    AssetsDAO assetsDAO,
    ExtractJobRepository extractJobRepository,
    ExtractJobObjectRepository extractJobObjectRepository,
    ObjectMapper objectMapper
  ) {
    this.assetsDAO = assetsDAO;
    this.extractJobRepository = extractJobRepository;
    this.extractJobObjectRepository = extractJobObjectRepository;
    this.objectMapper = objectMapper;
  }

  public void consumeEvent (
    ConsumerRecord<String, MetaMessage> messageEvent
  ) {
    MetaMessage message = messageEvent.value();

    LoggerWrapper.info("Got meta message " + message, MetaTopicConsumerService.class.getName());

    ExtractJob extractJob = createJob(message);

    createJoObjects(extractJob, message);
  }

  private ExtractJob createJob (MetaMessage message) {
    Asset asset = findAsset(message.getRoot_asset_id());

    ExtractJob extractJob = new ExtractJob(
      metaTopic,
      new java.sql.Timestamp(message.getKey_timestamp().getTime()),
      message.getObjects().size(),
      message.getSource(),
      message.getSource_kind(),
      message.getSource_type(),
      JobStatus.NEW,
      message.getFull_meta_flag(),
      parseFilterCriteriaToString(message.getFilter_criteria())
    );

    if (asset == null) {
      extractJob.setJobError("Asset not found");
      extractJob.setJobErrorFlag(true);
    } else {
      extractJob.setSourceName(asset.getAssetName());
      extractJob.setRootAsset(asset);
    }

    return extractJobRepository.save(extractJob);
  }

  private void createJoObjects (ExtractJob extractJob, MetaMessage message) {
    message.getObjects().forEach(object -> {
      extractJobObjectRepository.save(new ExtractJobObject(
        extractJob,
        extractJob.getKeyTimestamp(),
        object.getControl_sum(),
        object.getKafka_topic(),
        prepareTableNameBySourceKind(message.getSource_kind(), object, extractJob)
      ));
    });
  }

  private Asset findAsset (UUID assetId) {
    try {
      return assetsDAO.findAssetById(assetId);
    } catch (AssetNotFoundException assetNotFoundException) {
      return null;
    }
  }

  private String parseFilterCriteriaToString (List<MetaMessage.FilterCriteria> filterCriteria) {
    if (filterCriteria == null) return null;

    try {
      return objectMapper.writeValueAsString(filterCriteria);
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  private String prepareTableNameBySourceKind (
    SourceKind sourceKind,
    MetaMessage.MetaObject object,
    ExtractJob extractJob
  ) {
    return switch (sourceKind) {
      case API -> object.getKafka_topic() + "_" + extractJob.getKeyTimestamp().getTime();
      default -> extractJob.getSourceName() + "_" + extractJob.getKeyTimestamp().getTime() + "_" + object.getKafka_topic();
    };
  }
}
