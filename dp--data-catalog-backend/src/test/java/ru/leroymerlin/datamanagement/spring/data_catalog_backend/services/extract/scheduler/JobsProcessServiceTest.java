package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.scheduler;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import logger.LoggerWrapper;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assets.AssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributes.AttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationComponentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relations.RelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.responsibilities.ResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.roles.RoleRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.users.UserRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageRelation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.StageResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.StageAssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.StageAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.StageRelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.StageResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobObjectRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJobObject;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.JobStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.SourceType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.JobsServiceUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.RelationTopicConsumerService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.RelationTopicUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.kafka.models.RelationalObjectName;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;

import static org.junit.jupiter.api.Assertions.*;
import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.scheduler.JobsProcessService.UTC_ZONE_ID;

/**
 * @author juliwolf
 */


public class JobsProcessServiceTest extends ServiceWithUserIntegrationTest {
  private final ExtractJobRepository extractJobRepository;

  private final ExtractJobObjectRepository extractJobObjectRepository;

  private final EntityManager entityManager;

  private final JobsProcessService jobsProcessService;

  private final RelationTopicConsumerService relationTopicConsumerService;

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final AssetRepository assetRepository;
  private final RelationRepository relationRepository;
  private final RelationComponentRepository relationComponentRepository;
  private final AssetTypeRepository assetTypeRepository;
  private final ResponsibilityRepository responsibilityRepository;
  private final AttributeRepository attributeRepository;
  private final AttributeTypeRepository attributeTypeRepository;
  private final RelationTypeRepository relationTypeRepository;
  private final RelationTypeComponentRepository relationTypeComponentRepository;
  private final AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;
  private final RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;

  private final StageAssetRepository stageAssetRepository;

  private final StageAttributeRepository stageAttributeRepository;

  private final StageRelationRepository stageRelationRepository;
  private final StageResponsibilityRepository stageResponsibilityRepository;

  private static final Integer maxTimeoutMinutes = 100000;
  private static final String CANON_SCHEMA_TOPIC = "canon_schema_topic";
  private static final String CANON_COLUMN_TOPIC = "canon_column_topic";
  private static final String CANON_TABLE_TOPIC = "canon_table_topic";
  private static final String CANON_VIEW_TOPIC = "canon_view_topic";

  RelationTopicUtils relationTopicUtils = new RelationTopicUtils();

  JobsServiceUtils jobsServiceUtils;

  @Autowired
  public JobsProcessServiceTest (
    ExtractJobRepository extractJobRepository,
    ExtractJobObjectRepository extractJobObjectRepository,
    EntityManager entityManager,
    JobsProcessService jobsProcessService,
    RelationTopicConsumerService relationTopicConsumerService,
    UserRepository userRepository, RoleRepository roleRepository, AssetRepository assetRepository,
    RelationRepository relationRepository,
    RelationComponentRepository relationComponentRepository,
    AssetTypeRepository assetTypeRepository,
    ResponsibilityRepository responsibilityRepository, AttributeRepository attributeRepository,
    AttributeTypeRepository attributeTypeRepository,
    RelationTypeRepository relationTypeRepository,
    RelationTypeComponentRepository relationTypeComponentRepository,
    AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository,
    RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository,
    StageAssetRepository stageAssetRepository,
    StageAttributeRepository stageAttributeRepository,
    StageRelationRepository stageRelationRepository,
    StageResponsibilityRepository stageResponsibilityRepository
  ) {
    this.extractJobRepository = extractJobRepository;
    this.extractJobObjectRepository = extractJobObjectRepository;

    this.entityManager = entityManager;
    this.jobsProcessService = jobsProcessService;

    this.relationTopicConsumerService = relationTopicConsumerService;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.assetRepository = assetRepository;
    this.relationRepository = relationRepository;
    this.relationComponentRepository = relationComponentRepository;
    this.assetTypeRepository = assetTypeRepository;
    this.responsibilityRepository = responsibilityRepository;
    this.attributeRepository = attributeRepository;
    this.attributeTypeRepository = attributeTypeRepository;
    this.relationTypeRepository = relationTypeRepository;
    this.relationTypeComponentRepository = relationTypeComponentRepository;
    this.assetTypeAttributeTypeAssignmentRepository = assetTypeAttributeTypeAssignmentRepository;
    this.relationTypeComponentAssetTypeAssignmentRepository = relationTypeComponentAssetTypeAssignmentRepository;

    this.stageAssetRepository = stageAssetRepository;
    this.stageAttributeRepository = stageAttributeRepository;
    this.stageRelationRepository = stageRelationRepository;
    this.stageResponsibilityRepository = stageResponsibilityRepository;

    User extractUser = this.userRepository.save(new User("extract user", "firstname", "last name", ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.SourceType.API, "someEmail@gcom.ru"));

    this.jobsServiceUtils = new JobsServiceUtils(
      stageAssetRepository,
      stageAttributeRepository,
      stageRelationRepository,
      stageResponsibilityRepository,
      assetTypeRepository,
      assetRepository,
      roleRepository,
      relationRepository,
      attributeRepository,
      relationTypeRepository,
      attributeTypeRepository,
      responsibilityRepository,
      relationComponentRepository,
      relationTypeComponentRepository,
      assetTypeAttributeTypeAssignmentRepository,
      relationTypeComponentAssetTypeAssignmentRepository,
      extractUser,
      language
    );
  }

  @AfterEach
  public void clearData () {
    extractJobObjectRepository.deleteAll();
    extractJobRepository.deleteAll();
  }

  @Test
  public void updateExpiredJobsSuccessTest () {
    ZonedDateTime nowUTC = ZonedDateTime.now(UTC_ZONE_ID);

    ZonedDateTime timeoutZDT = nowUTC.minusMinutes(maxTimeoutMinutes);
    LoggerWrapper.info("nowUTC: " + nowUTC + " timeoutZDT " + timeoutZDT);

    Timestamp minTimestamp = Timestamp.valueOf(timeoutZDT.toLocalDateTime());

    ExtractJob expiredJob = new ExtractJob("first topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "new status", JobStatus.API_START, null, "{\"criteria\":\"value\"}", false);
    expiredJob.setLastStatusChangeDatetime(Timestamp.valueOf(nowUTC.minusMinutes(100000).toLocalDateTime()));
    extractJobRepository.save(expiredJob);

    ExtractJob activeJob = new ExtractJob("second topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "new status", JobStatus.NEW, null, "{\"criteria\":\"value\"}", false);
    activeJob.setLastStatusChangeDatetime(Timestamp.valueOf(nowUTC.minusMinutes(2).toLocalDateTime()));
    extractJobRepository.save(activeJob);

    ExtractJob secondExpiredJob = new ExtractJob("second topic", new Timestamp(System.currentTimeMillis()), 2, SourceType.GREENPLUM, "new status", JobStatus.NEW, null, "{\"criteria\":\"value\"}", false);
    secondExpiredJob.setLastStatusChangeDatetime(minTimestamp);
    extractJobRepository.save(secondExpiredJob);


    jobsProcessService.updateExpiredJobs(UTC_ZONE_ID, maxTimeoutMinutes);

    Optional<ExtractJob> updatedExpiredJob = extractJobRepository.findById(expiredJob.getJobId());
    Optional<ExtractJob> updatedActiveJob = extractJobRepository.findById(activeJob.getJobId());
    Optional<ExtractJob> updatedSecondExpiredJob = extractJobRepository.findById(secondExpiredJob.getJobId());

    assertAll(
      () -> assertTrue(updatedExpiredJob.get().getJobErrorFlag(), "job error flag true"),
      () -> assertNull(updatedActiveJob.get().getJobErrorFlag(), "job error flag null"),
      () -> assertTrue(updatedSecondExpiredJob.get().getJobErrorFlag(), "job error flag true")
    );
  }

  @Test
  public void computeCheckSumsSuccessTest () {
    ZonedDateTime nowUTC = ZonedDateTime.now(UTC_ZONE_ID);

    Date currentDate = relationTopicUtils.generateDate();

    ExtractJob activeJob = new ExtractJob("second topic", new Timestamp(currentDate.getTime()), 4, SourceType.GREENPLUM, "adb", JobStatus.NEW, null, "{\"criteria\":\"value\"}", false);
    activeJob.setLastStatusChangeDatetime(Timestamp.valueOf(nowUTC.minusMinutes(2).toLocalDateTime()));
    extractJobRepository.save(activeJob);

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonTableRecords = createCanonTableTopicData(activeJob.getSourceName(), currentDate);
    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonColumnRecords = createCanonColumnTopicData(activeJob.getSourceName(), currentDate);
    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonSchemaRecords = createCanonSchemaTopicData(activeJob.getSourceName(), currentDate);
    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonViewRecords = createCanonViewTopicData(activeJob.getSourceName(), currentDate);

    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_TABLE, new ConsumerRecords<>(canonTableRecords));
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_COLUMN, new ConsumerRecords<>(canonColumnRecords));
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_SCHEMA, new ConsumerRecords<>(canonSchemaRecords));
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_VIEW, new ConsumerRecords<>(canonViewRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 4L, CANON_SCHEMA_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_SCHEMA_TOPIC
    ));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 3L, CANON_COLUMN_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_COLUMN_TOPIC
    ));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 2L, CANON_TABLE_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_TABLE_TOPIC
    ));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 3L, CANON_VIEW_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_VIEW_TOPIC
    ));

    jobsProcessService.computeCheckSums(Map.of(activeJob.getJobId(), activeJob));

    Optional<ExtractJob> updatedActiveJob = extractJobRepository.findById(activeJob.getJobId());

    assertEquals(JobStatus.DUMPED_DB, updatedActiveJob.get().getJobStatus());
  }

  @Test
  public void computeCheckSumsNotFullDataTest () {
    ZonedDateTime nowUTC = ZonedDateTime.now(UTC_ZONE_ID);

    Date currentDate = relationTopicUtils.generateDate();

    ExtractJob activeJob = new ExtractJob("second topic", new Timestamp(currentDate.getTime()), 4, SourceType.GREENPLUM, "adb", JobStatus.NEW, null, "{\"criteria\":\"value\"}", false);
    activeJob.setLastStatusChangeDatetime(Timestamp.valueOf(nowUTC.minusMinutes(2).toLocalDateTime()));
    extractJobRepository.save(activeJob);

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonSchemaRecords = createCanonSchemaTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_SCHEMA, new ConsumerRecords<>(canonSchemaRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 2L, CANON_SCHEMA_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_SCHEMA_TOPIC
    ));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonColumnRecords = createCanonColumnTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_COLUMN, new ConsumerRecords<>(canonColumnRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 3L, CANON_COLUMN_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_COLUMN_TOPIC
    ));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonTableRecords = createCanonTableTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_TABLE, new ConsumerRecords<>(canonTableRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 1L, CANON_TABLE_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_TABLE_TOPIC
    ));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonViewRecords = createCanonViewTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_VIEW, new ConsumerRecords<>(canonViewRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 3L, CANON_VIEW_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_VIEW_TOPIC
    ));

    jobsProcessService.computeCheckSums(Map.of(activeJob.getJobId(), activeJob));

    Optional<ExtractJob> updatedActiveJob = extractJobRepository.findById(activeJob.getJobId());

    assertEquals(JobStatus.NEW, updatedActiveJob.get().getJobStatus());
  }

  @Test
  public void computeCheckSumsWrongTableNameTest () {
    ZonedDateTime nowUTC = ZonedDateTime.now(UTC_ZONE_ID);

    Date currentDate = relationTopicUtils.generateDate();

    ExtractJob activeJob = new ExtractJob("second topic", new Timestamp(currentDate.getTime()), 4, SourceType.GREENPLUM, "adb", JobStatus.NEW, null, "{\"criteria\":\"value\"}", false);
    activeJob.setLastStatusChangeDatetime(Timestamp.valueOf(nowUTC.minusMinutes(2).toLocalDateTime()));
    extractJobRepository.save(activeJob);

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonSchemaRecords = createCanonSchemaTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_SCHEMA, new ConsumerRecords<>(canonSchemaRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 4L, CANON_SCHEMA_TOPIC,
      activeJob.getKeyTimestamp().getTime() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_SCHEMA_TOPIC
    ));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonColumnRecords = createCanonColumnTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_COLUMN, new ConsumerRecords<>(canonColumnRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 3L, CANON_COLUMN_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_COLUMN_TOPIC
    ));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonTableRecords = createCanonTableTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_TABLE, new ConsumerRecords<>(canonTableRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 2L, CANON_TABLE_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_TABLE_TOPIC
    ));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonViewRecords = createCanonViewTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_VIEW, new ConsumerRecords<>(canonViewRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 3L, CANON_VIEW_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_VIEW_TOPIC
    ));

    jobsProcessService.computeCheckSums(Map.of(activeJob.getJobId(), activeJob));

    Optional<ExtractJob> updatedActiveJob = extractJobRepository.findById(activeJob.getJobId());

    assertEquals(updatedActiveJob.get().getJobStatus(), JobStatus.NEW);
  }

  @Test
  public void computeCheckSumsTableNotExistsTest () {
    ZonedDateTime nowUTC = ZonedDateTime.now(UTC_ZONE_ID);

    Date currentDate = relationTopicUtils.generateDate();

    ExtractJob activeJob = new ExtractJob("second topic", new Timestamp(currentDate.getTime()), 4, SourceType.GREENPLUM, "adb", JobStatus.NEW, null, "{\"criteria\":\"value\"}", false);
    activeJob.setLastStatusChangeDatetime(Timestamp.valueOf(nowUTC.minusMinutes(2).toLocalDateTime()));
    extractJobRepository.save(activeJob);

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 4L, CANON_SCHEMA_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_SCHEMA_TOPIC
    ));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonColumnRecords = createCanonColumnTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_COLUMN, new ConsumerRecords<>(canonColumnRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 3L, CANON_COLUMN_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_COLUMN_TOPIC
    ));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonTableRecords = createCanonTableTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_TABLE, new ConsumerRecords<>(canonTableRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 2L, CANON_TABLE_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_TABLE_TOPIC
    ));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> canonViewRecords = createCanonViewTopicData(activeJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_VIEW, new ConsumerRecords<>(canonViewRecords));

    extractJobObjectRepository.save(new ExtractJobObject(
      activeJob, new Timestamp(currentDate.getTime()), 3L, CANON_VIEW_TOPIC,
      activeJob.getSourceName() + "_" + activeJob.getKeyTimestamp().getTime() + "_" + CANON_VIEW_TOPIC
    ));

    jobsProcessService.computeCheckSums(Map.of(activeJob.getJobId(), activeJob));

    Optional<ExtractJob> updatedActiveJob = extractJobRepository.findById(activeJob.getJobId());

    assertEquals(updatedActiveJob.get().getJobStatus(), JobStatus.NEW);
  }

  @Test
  public void checkOutdatedFunctionsTest () {
    ExtractJob firstJob = extractJobRepository.save(new ExtractJob("first job", new Timestamp(System.currentTimeMillis() - 10), 2, SourceType.GREENPLUM, "new status", JobStatus.DUMPED_DB, null, "{\"criteria\":\"value\"}", true));
    ExtractJob secondJob = extractJobRepository.save(new ExtractJob("second job", new Timestamp(System.currentTimeMillis() - 20), 2, SourceType.GREENPLUM, "new status", JobStatus.DUMPED_DB, null, "{\"criteria\":\"value\"}", true));
    ExtractJob thirdJob = extractJobRepository.save(new ExtractJob("third job", new Timestamp(System.currentTimeMillis() - 30), 2, SourceType.GREENPLUM, "new status", JobStatus.DUMPED_DB, null, "{\"criteria\":\"value\"}", true));
    ExtractJob forthJob = extractJobRepository.save(new ExtractJob("third job", new Timestamp(System.currentTimeMillis() - 40), 2, SourceType.GREENPLUM, "new status", JobStatus.STAGE_COMPLETE, null, "{\"criteria\":\"value\"}", true));

    Map<UUID, ExtractJob> jobsById =  new HashMap<>();
    jobsById.put(firstJob.getJobId(), firstJob);
    jobsById.put(secondJob.getJobId(), secondJob);
    jobsById.put(thirdJob.getJobId(), thirdJob);
    jobsById.put(forthJob.getJobId(), forthJob);

    jobsProcessService.checkOutdatedFunctions(jobsById);
    Optional<ExtractJob> updatedFirstJob = extractJobRepository.findById(firstJob.getJobId());
    Optional<ExtractJob> updatedSecondJob = extractJobRepository.findById(secondJob.getJobId());
    Optional<ExtractJob> updatedThirdJob = extractJobRepository.findById(thirdJob.getJobId());
    Optional<ExtractJob> updatedForthJob = extractJobRepository.findById(forthJob.getJobId());

    assertAll(
      () -> assertNull(updatedFirstJob.get().getJobErrorFlag()),
      () -> assertTrue(updatedSecondJob.get().getJobErrorFlag()),
      () -> assertTrue(updatedThirdJob.get().getJobErrorFlag()),
      () -> assertTrue(updatedForthJob.get().getJobErrorFlag())
    );
  }

  @Test
  public void computedJobsForUpdateTest () {
    ExtractJob firstJob = extractJobRepository.save(new ExtractJob("first job", new Timestamp(System.currentTimeMillis() - 10), 2, SourceType.GREENPLUM, "new status", JobStatus.STAGE_COMPLETE, null, "{\"criteria\":\"value\"}", true));
    ExtractJob secondJob = extractJobRepository.save(new ExtractJob("second job", new Timestamp(System.currentTimeMillis() - 20), 2, SourceType.GREENPLUM, "new status", JobStatus.API_START, null, "{\"criteria\":\"value\"}", true));
    ExtractJob thirdJob = extractJobRepository.save(new ExtractJob("second job", new Timestamp(System.currentTimeMillis() - 20), 2, SourceType.GREENPLUM, "new source", JobStatus.STAGE_COMPLETE, null, "{\"criteria\":\"value\"}", true));

    List<UUID> jobsForUpdateUUIDs = jobsProcessService.computedJobsForUpdate();

    assertAll(
      () -> assertEquals(1, jobsForUpdateUUIDs.size()),
      () -> assertEquals(thirdJob.getJobId(), jobsForUpdateUUIDs.get(0))
    );
  }

  @Test
  public void clearDataBySuccessJobsTest () {
    Date currentDate = relationTopicUtils.generateDate();

    ExtractJob firstJob = extractJobRepository.save(new ExtractJob("first job", new Timestamp(currentDate.getTime()), 4, SourceType.GREENPLUM, "first_source", JobStatus.SUCCESS, null, "{\"criteria\":\"value\"}", true));
    ExtractJob secondJob = extractJobRepository.save(new ExtractJob("second job", new Timestamp(currentDate.getTime()), 4, SourceType.GREENPLUM, "second_source", JobStatus.SUCCESS, null, "{\"criteria\":\"value\"}", true));
    ExtractJob thirdJob = extractJobRepository.save(new ExtractJob("third job", new Timestamp(currentDate.getTime()), 4, SourceType.GREENPLUM, "third_source", JobStatus.API_START, null, "{\"criteria\":\"value\"}", true));

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -4);
    ExtractJob forthJob = new ExtractJob("forth job", new Timestamp(calendar.getTime().getTime()), 4, SourceType.GREENPLUM, "third_source", JobStatus.API_START, null, "{\"criteria\":\"value\"}", true);
    forthJob.setLastStatusChangeDatetime(new Timestamp(calendar.getTime().getTime()));
    forthJob.setJobErrorFlag(true);
    extractJobRepository.save(forthJob);

    calendar.add(Calendar.DAY_OF_MONTH, -2);
    ExtractJob fifthJob = new ExtractJob("fifth job", new Timestamp(calendar.getTime().getTime()), 4, SourceType.GREENPLUM, "third_source", JobStatus.API_START, null, "{\"criteria\":\"value\"}", true);
    fifthJob.setLastStatusChangeDatetime(new Timestamp(calendar.getTime().getTime()));
    fifthJob.setJobErrorFlag(true);
    extractJobRepository.save(fifthJob);

    String firstCanonSchema = firstJob.getSourceName() + "_" + firstJob.getKeyTimestamp().getTime() + "_" + CANON_SCHEMA_TOPIC;
    String firstCanonColumn = firstJob.getSourceName() + "_" + firstJob.getKeyTimestamp().getTime() + "_" + CANON_COLUMN_TOPIC;
    String firstCanonView = firstJob.getSourceName() + "_" + firstJob.getKeyTimestamp().getTime() + "_" + CANON_VIEW_TOPIC;
    String firstCanonTable = firstJob.getSourceName() + "_" + firstJob.getKeyTimestamp().getTime() + "_" + CANON_TABLE_TOPIC;
    extractJobObjectRepository.save(new ExtractJobObject(firstJob, firstJob.getKeyTimestamp(), 4L, CANON_SCHEMA_TOPIC, firstCanonSchema));
    extractJobObjectRepository.save(new ExtractJobObject(firstJob, firstJob.getKeyTimestamp(), 4L, CANON_COLUMN_TOPIC, firstCanonColumn));
    extractJobObjectRepository.save(new ExtractJobObject(firstJob, firstJob.getKeyTimestamp(), 4L, CANON_VIEW_TOPIC,firstCanonView));
    extractJobObjectRepository.save(new ExtractJobObject(firstJob, firstJob.getKeyTimestamp(), 4L, CANON_TABLE_TOPIC, firstCanonTable));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> firstCanonSchemaRecords = createCanonSchemaTopicData(firstJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_SCHEMA, new ConsumerRecords<>(firstCanonSchemaRecords));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> firstCanonColumnRecords = createCanonColumnTopicData(firstJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_COLUMN, new ConsumerRecords<>(firstCanonColumnRecords));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> firstCanonTableRecords = createCanonTableTopicData(firstJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_TABLE, new ConsumerRecords<>(firstCanonTableRecords));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> firstCanonViewRecords = createCanonViewTopicData(firstJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_VIEW, new ConsumerRecords<>(firstCanonViewRecords));

    String secondCanonSchema = secondJob.getSourceName() + "_" + secondJob.getKeyTimestamp().getTime() + "_" + CANON_SCHEMA_TOPIC;
    String secondCanonColumn = secondJob.getSourceName() + "_" + secondJob.getKeyTimestamp().getTime() + "_" + CANON_COLUMN_TOPIC;
    String secondCanonView = secondJob.getSourceName() + "_" + secondJob.getKeyTimestamp().getTime() + "_" + CANON_VIEW_TOPIC;
    String secondCanonTable = secondJob.getSourceName() + "_" + secondJob.getKeyTimestamp().getTime() + "_" + CANON_TABLE_TOPIC;
    extractJobObjectRepository.save(new ExtractJobObject(secondJob, secondJob.getKeyTimestamp(), 4L, CANON_SCHEMA_TOPIC, secondCanonSchema));
    extractJobObjectRepository.save(new ExtractJobObject(secondJob, secondJob.getKeyTimestamp(), 4L, CANON_COLUMN_TOPIC, secondCanonColumn));
    extractJobObjectRepository.save(new ExtractJobObject(secondJob, secondJob.getKeyTimestamp(), 4L, CANON_VIEW_TOPIC,secondCanonView));
    extractJobObjectRepository.save(new ExtractJobObject(secondJob, secondJob.getKeyTimestamp(), 4L, CANON_TABLE_TOPIC, secondCanonTable));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> secondCanonSchemaRecords = createCanonSchemaTopicData(secondJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_SCHEMA, new ConsumerRecords<>(secondCanonSchemaRecords));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> secondCanonColumnRecords = createCanonColumnTopicData(secondJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_COLUMN, new ConsumerRecords<>(secondCanonColumnRecords));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> secondCanonTableRecords = createCanonTableTopicData(secondJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_TABLE, new ConsumerRecords<>(secondCanonTableRecords));

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> secondCanonViewRecords = createCanonViewTopicData(secondJob.getSourceName(), currentDate);
    relationTopicConsumerService.consumeEvents(RelationalObjectName.CANON_VIEW, new ConsumerRecords<>(secondCanonViewRecords));

    String forthCanonSchema = forthJob.getSourceName() + "_" + forthJob.getKeyTimestamp().getTime() + "_" + CANON_SCHEMA_TOPIC;
    String forthCanonColumn = forthJob.getSourceName() + "_" + forthJob.getKeyTimestamp().getTime() + "_" + CANON_COLUMN_TOPIC;
    String forthCanonView = forthJob.getSourceName() + "_" + forthJob.getKeyTimestamp().getTime() + "_" + CANON_VIEW_TOPIC;
    String forthCanonTable = forthJob.getSourceName() + "_" + forthJob.getKeyTimestamp().getTime() + "_" + CANON_TABLE_TOPIC;
    extractJobObjectRepository.save(new ExtractJobObject(forthJob, forthJob.getKeyTimestamp(), 4L, CANON_SCHEMA_TOPIC, forthCanonSchema));
    extractJobObjectRepository.save(new ExtractJobObject(forthJob, forthJob.getKeyTimestamp(), 4L, CANON_COLUMN_TOPIC, forthCanonColumn));
    extractJobObjectRepository.save(new ExtractJobObject(forthJob, forthJob.getKeyTimestamp(), 4L, CANON_VIEW_TOPIC, forthCanonView));
    extractJobObjectRepository.save(new ExtractJobObject(forthJob, forthJob.getKeyTimestamp(), 4L, CANON_TABLE_TOPIC, forthCanonTable));

    String fifthCanonSchema = fifthJob.getSourceName() + "_" + fifthJob.getKeyTimestamp().getTime() + "_" + CANON_SCHEMA_TOPIC;
    String fifthCanonColumn = fifthJob.getSourceName() + "_" + fifthJob.getKeyTimestamp().getTime() + "_" + CANON_COLUMN_TOPIC;
    String fifthCanonView = fifthJob.getSourceName() + "_" + fifthJob.getKeyTimestamp().getTime() + "_" + CANON_VIEW_TOPIC;
    String fifthCanonTable = fifthJob.getSourceName() + "_" + fifthJob.getKeyTimestamp().getTime() + "_" + CANON_TABLE_TOPIC;
    extractJobObjectRepository.save(new ExtractJobObject(fifthJob, fifthJob.getKeyTimestamp(), 4L, CANON_SCHEMA_TOPIC, fifthCanonSchema));
    extractJobObjectRepository.save(new ExtractJobObject(fifthJob, fifthJob.getKeyTimestamp(), 4L, CANON_COLUMN_TOPIC, fifthCanonColumn));
    extractJobObjectRepository.save(new ExtractJobObject(fifthJob, fifthJob.getKeyTimestamp(), 4L, CANON_VIEW_TOPIC, fifthCanonView));
    extractJobObjectRepository.save(new ExtractJobObject(fifthJob, fifthJob.getKeyTimestamp(), 4L, CANON_TABLE_TOPIC, fifthCanonTable));

    this.jobsServiceUtils.prepareStageAssets(firstJob, 10, ActionDecision.I);
    this.jobsServiceUtils.prepareStageAttributes(firstJob, 10, ActionDecision.U);
    this.jobsServiceUtils.prepareStageRelations(firstJob, 10, ActionDecision.D);

    this.jobsServiceUtils.prepareStageAttributes(secondJob, 10, ActionDecision.U);
    this.jobsServiceUtils.prepareStageAssets(secondJob, 10, ActionDecision.I);
    this.jobsServiceUtils.prepareStageRelations(secondJob, 10, ActionDecision.D);
    this.jobsServiceUtils.prepareStageResponsibilities(secondJob, 12, ActionDecision.D);

    this.jobsServiceUtils.prepareStageAssets(thirdJob, 10, ActionDecision.I);
    this.jobsServiceUtils.prepareStageRelations(thirdJob, 10, ActionDecision.D);
    this.jobsServiceUtils.prepareStageResponsibilities(thirdJob, 8, ActionDecision.I);

    this.jobsServiceUtils.prepareStageAssets(forthJob, 10, ActionDecision.I);

    this.jobsServiceUtils.prepareStageRelations(fifthJob, 5, ActionDecision.D);

    jobsProcessService.clearDataOfFinishedJobs(UTC_ZONE_ID);

//    Query extractStage = entityManager.createNativeQuery("""
//        SELECT EXISTS (
//          SELECT FROM information_schema.tables
//          WHERE
//            table_schema = 'extract_stage'
//        );
//      """);
//    List<Boolean> resultList = extractStage.getResultList();

    List<StageAsset> stageAssets = stageAssetRepository.findAll();
    List<StageRelation> stageRelations = stageRelationRepository.findAll();
    List<StageAttribute> stageAttributes = stageAttributeRepository.findAll();
    List<StageResponsibility> stageResponsibilities = stageResponsibilityRepository.findAll();

    assertAll(
//      () -> assertFalse(resultList.get(0),  "extract stage tables exists"),
      () -> assertEquals(20, stageAssets.size(),  "stageAssets size"),
      () -> assertEquals(10, stageRelations.size(),  "stageRelations size"),
      () -> assertEquals(8, stageResponsibilities.size(),  "stageResponsibilities size"),
      () -> assertEquals(0, stageAttributes.size(),  "stageRelations size")
    );
  }

  private Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> createCanonSchemaTopicData (String source, Date currentDate) {
    Headers canonSchemaHeaders = relationTopicUtils.createKeyRecord(source, RelationalObjectName.CANON_SCHEMA, currentDate, 2160106L);
    List<ConsumerRecord<String, GenericRecord>> canonSchemaRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      4,
      canonSchemaHeaders,
      List.of(
        relationTopicUtils.createTableSchema("first_b2b_segmentation_ods_" + Math.random() * 100, "first_DP2.0"),
        relationTopicUtils.createTableSchema("second_b2b_segmentation_ods_" + Math.random() * 100, "second_DP2.0"),
        relationTopicUtils.createTableSchema("third_b2b_segmentation_ods_" + Math.random() * 100, "third_DP2.0"),
        relationTopicUtils.createTableSchema("forth_b2b_segmentation_ods_" + Math.random() * 100, "forth_DP2.0")
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);

    ArrayList<ConsumerRecord<String, GenericRecord>> allRecords = new ArrayList<>();
    allRecords.addAll(canonSchemaRecords);

    records.put(relationalTopic, allRecords);

    return records;
  }

  private Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> createCanonColumnTopicData (String source, Date currentDate) {
    Headers canonColumnHeaders = relationTopicUtils.createKeyRecord(source, RelationalObjectName.CANON_COLUMN, currentDate, 2160106L);
    List<ConsumerRecord<String, GenericRecord>> canonColumnRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      3,
      canonColumnHeaders,
      List.of(
        relationTopicUtils.createColumnRecord("first_nav_navsql114_store114_ods_" + Math.random() * 100, "first_v_wms_invent__line_det__hist", "first_md5_hash", 1, "YES", "character varying(4096)", "comment", false),
        relationTopicUtils.createColumnRecord("second_nav_navsql114_store114_ods_" + Math.random() * 100, "second_v_wms_invent__line_det__hist", "second_md5_hash", 3, "NO", "character varying(255)", null, true),
        relationTopicUtils.createColumnRecord("third_nav_navsql114_store114_ods_" + Math.random() * 100, "third_v_wms_invent__line_det__hist", "third_md5_hash", 4, "NO", "bool", null, true)
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);

    ArrayList<ConsumerRecord<String, GenericRecord>> allRecords = new ArrayList<>();
    allRecords.addAll(canonColumnRecords);

    records.put(relationalTopic, allRecords);

    return records;
  }

  private Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> createCanonTableTopicData (String source, Date currentDate) {
    Headers canonTableHeaders = relationTopicUtils.createKeyRecord(source, RelationalObjectName.CANON_TABLE, currentDate, 46243L);
    List<ConsumerRecord<String, GenericRecord>> canonTableRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      2,
      canonTableHeaders,
      List.of(
        relationTopicUtils.createTableRecord("first_nav_navsql052_store052_ods_" + Math.random() * 100, "first_whse__doc__status_log_hist", null),
        relationTopicUtils.createTableRecord("second_nav_navsql052_store052_ods_" + Math.random() * 100, "second_whse__doc__status_log_hist", null)
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);

    ArrayList<ConsumerRecord<String, GenericRecord>> allRecords = new ArrayList<>();
    allRecords.addAll(canonTableRecords);

    records.put(relationalTopic, allRecords);

    return records;
  }

  private Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> createCanonViewTopicData (String source, Date currentDate) {
    Headers canonViewHeaders = relationTopicUtils.createKeyRecord(source, RelationalObjectName.CANON_VIEW, currentDate, 24707L);
    List<ConsumerRecord<String, GenericRecord>> canonViewRecords = relationTopicUtils.mapRecords(
      "relational_topic",
      1,
      canonViewHeaders,
      List.of(
        relationTopicUtils.createViewRecord("first_nav_navsql153_store153_ods_" + Math.random() * 100, "first_v_whse__unit_line_hist", false, "Select * from table", null),
        relationTopicUtils.createViewRecord("second_nav_navsql153_store153_ods_" + Math.random() * 100, "second_v_whse__unit_line_hist", true, "Select * from table", null),
        relationTopicUtils.createViewRecord("third_nav_navsql153_store153_ods_" + Math.random() * 100, "third_v_whse__unit_line_hist", true, "Select * from table", null)
      )
    );

    Map<TopicPartition, List<ConsumerRecord<String, GenericRecord>>> records = new HashMap<>();
    TopicPartition relationalTopic = new TopicPartition("relational_topic", 1);

    ArrayList<ConsumerRecord<String, GenericRecord>> allRecords = new ArrayList<>();
    allRecords.addAll(canonViewRecords);

    records.put(relationalTopic, allRecords);

    return records;
  }
}
