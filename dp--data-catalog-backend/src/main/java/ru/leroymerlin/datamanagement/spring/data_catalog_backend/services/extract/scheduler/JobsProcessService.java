package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.scheduler;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import logger.LoggerWrapper;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.StageAssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.StageAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.StageRelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.StageResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobObjectRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.ExtractJobRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJobObject;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.JobObjectStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.JobStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.JobsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post.PostExecuteUpdateRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.users.UsersDAO;

/**
 * @author juliwolf
 */

@Service
public class JobsProcessService {
  @Value("${extract.scheduler.user.id}")
  private String userId;

  public static final ZoneId UTC_ZONE_ID = ZoneId.of(ZoneOffset.UTC.getId());

  private final ExtractJobRepository extractJobRepository;
  private final ExtractJobObjectRepository extractJobObjectRepository;
  private final EntityManager entityManager;
  private final JobsService jobsService;
  private final UsersDAO usersDAO;

  private final StageAssetRepository stageAssetRepository;

  private final StageRelationRepository stageRelationRepository;

  private final StageAttributeRepository stageAttributeRepository;
  private final StageResponsibilityRepository stageResponsibilityRepository;

  public JobsProcessService (
    ExtractJobRepository extractJobRepository,
    ExtractJobObjectRepository extractJobObjectRepository,
    EntityManager entityManager,
    JobsService jobsService,
    UsersDAO usersDAO,

    StageAssetRepository stageAssetRepository,
    StageRelationRepository stageRelationRepository,
    StageAttributeRepository stageAttributeRepository,
    StageResponsibilityRepository stageResponsibilityRepository
  ) {
    this.extractJobRepository = extractJobRepository;
    this.extractJobObjectRepository = extractJobObjectRepository;
    this.entityManager = entityManager;
    this.jobsService = jobsService;
    this.usersDAO = usersDAO;

    this.stageAssetRepository = stageAssetRepository;
    this.stageRelationRepository = stageRelationRepository;
    this.stageAttributeRepository = stageAttributeRepository;
    this.stageResponsibilityRepository = stageResponsibilityRepository;
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void updateExpiredJobs (ZoneId UTC_ZONE_ID,  Integer maxTimeoutMinutes) {
    ZonedDateTime nowUTC = ZonedDateTime.now(UTC_ZONE_ID);

    ZonedDateTime timeoutZDT = nowUTC.minusMinutes(maxTimeoutMinutes);
    LoggerWrapper.info("nowUTC: " + nowUTC + " timeoutZDT " + timeoutZDT, this.getClass().getName());

    Timestamp minTimestamp = Timestamp.valueOf(timeoutZDT.toLocalDateTime());

    LoggerWrapper.info("Update expired jobs with min timeout " + minTimestamp, this.getClass().getName());

    extractJobRepository.updateExpiredJobs(minTimestamp);
  }

  public void computeCheckSums (Map<UUID, ExtractJob> jobsById) {
    LoggerWrapper.info("Starting computing checksum", this.getClass().getName());

    List<UUID> newJobsIds = jobsById.values().stream()
      .filter(extractJob -> extractJob.getJobStatus().equals(JobStatus.NEW))
      .map(ExtractJob::getJobId)
      .toList();

    List<ExtractJob> jobsToUpdate = new ArrayList<>();
    Map<UUID, Integer> totalReadObjectsByJobs = new HashMap<>();
    List<ExtractJobObject> extractJobObjectByJobs = extractJobObjectRepository.findExtractJobObjectByJobs(newJobsIds);

    List<ExtractJobObject> jobObjectsToUpdate = computeJobObjectCheckSums(extractJobObjectByJobs, totalReadObjectsByJobs);

    totalReadObjectsByJobs.forEach((key, value) -> {
      ExtractJob extractJob = jobsById.get(key);

      if (extractJob.getTablesCount().equals(value)) {
        LoggerWrapper.info("Job " + extractJob.getJobId() + " was checked", this.getClass().getName());

        extractJob.setJobStatus(JobStatus.DUMPED_DB);
        jobsToUpdate.add(extractJob);
      }
    });

    extractJobRepository.saveAll(jobsToUpdate);
    extractJobObjectRepository.saveAll(jobObjectsToUpdate);
  }

  public void checkOutdatedFunctions (Map<UUID, ExtractJob> jobsById) {
    LoggerWrapper.info("Start checking outdated function", this.getClass().getName());

    Map<String, List<ExtractJob>> dumpedDbJobsBySourceName = jobsById.values().stream()
      .filter(extractJob -> extractJob.getJobStatus().equals(JobStatus.DUMPED_DB) || extractJob.getJobStatus().equals(JobStatus.STAGE_COMPLETE))
      .collect(Collectors.groupingBy(ExtractJob::getSourceName));

    dumpedDbJobsBySourceName.forEach((key, values) -> {
      if (values.size() == 1) return;

      List<ExtractJob> orderedValues = values.stream()
        .sorted(Comparator.comparing(ExtractJob::getKeyTimestamp)).toList();

      for (int i = 0; i < orderedValues.size(); i++) {
        boolean isLast = i == orderedValues.size() - 1;
        if (isLast) return;

        ExtractJob extractJob = orderedValues.get(i);
        extractJob.setJobError("Job data is outdated");
        extractJob.setJobErrorFlag(true);
        extractJobRepository.save(extractJob);
      }
    });
  }

  public void executeFunctions (Map<UUID, ExtractJob> jobsById) {
    LoggerWrapper.info("Starting calling function", this.getClass().getName());

    List<ExtractJob> dumpedDbJobsIds = jobsById.values().stream()
      .filter(extractJob -> extractJob.getJobStatus().equals(JobStatus.DUMPED_DB))
      .toList();

    LoggerWrapper.info("Function will be executed for jobs " + dumpedDbJobsIds.stream().map(ExtractJob::getJobId).toList(), this.getClass().getName());

    String apiFunctionString = "Select extract_delta.fn_load_api_dpc_for_data_catalog(:rootAssetId, :jobId);";
    String relationalFunctionString = "Select extract_delta.fn_load_canon_object_meta_for_data_catalog(:rootAssetId, :jobId);";

    dumpedDbJobsIds.forEach(job -> {
      job.setJobStatus(JobStatus.DB_PROCESS);
      extractJobRepository.save(job);

      switch (job.getSourceKind()) {
        case API -> callFunction(apiFunctionString, job.getRootAsset().getAssetId(), job.getJobId());
        default -> callFunction(relationalFunctionString, job.getRootAsset().getAssetId(), job.getJobId());
      }
    });
  }

  private void callFunction (String queryString, UUID rootAssetId, UUID jobId) {
    Query functionQuery = entityManager.createNativeQuery(queryString);
    functionQuery.setParameter("rootAssetId", rootAssetId);
    functionQuery.setParameter("jobId", jobId);

    functionQuery.getResultList();
  }

  public void executeUpdate () {
    User schedulerUser = usersDAO.findUserById(UUID.fromString(userId));

    List<UUID> jobsIdsToUpdate = computedJobsForUpdate();

    LoggerWrapper.info("Starting execute update for jobs " + jobsIdsToUpdate, this.getClass().getName());

    jobsIdsToUpdate.forEach(jobId -> {
      jobsService.executeUpdate(new PostExecuteUpdateRequest(jobId), schedulerUser);
    });
  }

  public List<UUID> computedJobsForUpdate () {
    List<ExtractJob> jobsToUpdate = extractJobRepository.findJobsToUpdate();

    Map<String, List<ExtractJob>> jobsBySource = jobsToUpdate.stream().collect(Collectors.groupingBy(ExtractJob::getSourceName));
    return jobsBySource.values()
      .stream()
      .filter(extractJobs -> extractJobs.size() == 1 && extractJobs.get(0).getJobStatus().equals(JobStatus.STAGE_COMPLETE))
      .map(extractJobs -> extractJobs.get(0).getJobId())
      .toList();
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void clearDataOfFinishedJobs (ZoneId UTC_ZONE_ID) {
    ZonedDateTime nowUTC = ZonedDateTime.now(UTC_ZONE_ID);
    ZonedDateTime timeoutZDT = nowUTC.minusDays(5);
    Timestamp maxDateTime = Timestamp.valueOf(timeoutZDT.toLocalDateTime());

    List<ExtractJobObject> extractJobObjectsOfSuccessJobs = extractJobObjectRepository.findExtractJobObjectsOfFinishedJobs(maxDateTime);
    Set<UUID> successJobsSet = extractJobObjectsOfSuccessJobs
      .stream()
      .map(object -> object.getExtractJob().getJobId()).collect(Collectors.toSet());

    LoggerWrapper.info("Clear extract_delta for jobs " + successJobsSet, this.getClass().getName());

    stageAssetRepository.deleteStageAssetsByJobIds(successJobsSet);
    stageRelationRepository.deleteStageRelationsByJobIds(successJobsSet);
    stageAttributeRepository.deleteStageAttributesByJobIds(successJobsSet);
    stageResponsibilityRepository.deleteStageResponsibilitiesByJobIds(successJobsSet);

    clearExtractStageTables(extractJobObjectsOfSuccessJobs);
  }

  private List<ExtractJobObject> computeJobObjectCheckSums (List<ExtractJobObject> extractJobObjects, Map<UUID, Integer> totalReadObjectByJob) {
    String queryStringPattern = """
      Select count(*) as count
      From extract_stage.%tableStageName%
    """;

    String tableExistsStringPattern = """
        SELECT EXISTS (
          SELECT FROM information_schema.tables
          WHERE
            table_schema = 'extract_stage' AND
            table_name = '%tableStageName%'
        );
      """;


    List<ExtractJobObject> jobObjectsToUpdate = new ArrayList<>();

    extractJobObjects.forEach(extractJobObject -> {
      LoggerWrapper.info("Compute checksum for table " + extractJobObject.getTableStageName(), this.getClass().getName());

      if (extractJobObject.getJobObjectStatus().equals(JobObjectStatus.DUMPED_DB)) {
        totalReadObjectByJob.compute(
          extractJobObject.getExtractJob().getJobId(),
          (_key, value) -> value == null ? 1 : value + 1
        );

        return;
      }

      boolean isValidTableName = checkTableNameByPattern(extractJobObject.getTableStageName());

      if (!isValidTableName) return;

      String tableExistsQuery = tableExistsStringPattern.replace("%tableStageName%", extractJobObject.getTableStageName());
      boolean isExists = checkIfTableExists(tableExistsQuery);
      LoggerWrapper.info("Is table " + extractJobObject.getTableStageName() + " exists " + isExists, this.getClass().getName());

      if (!isExists) return;

      String updatedQueryString = queryStringPattern.replace("%tableStageName%", extractJobObject.getTableStageName());

      Query nativeQuery = entityManager.createNativeQuery(updatedQueryString);

      List resultList = nativeQuery.getResultList();
      Long rowsCount = (Long) resultList.get(0);

      if (
        extractJobObject.getControlSum() != null &&
        extractJobObject.getControlSum().equals(rowsCount)
      ) {
        totalReadObjectByJob.compute(
          extractJobObject.getExtractJob().getJobId(),
          (_key, value) -> value == null ? 1 : value + 1
        );

        LoggerWrapper.info("Extract job object with table stage name " + extractJobObject.getTableStageName() + " was checked", this.getClass().getName());

        extractJobObject.setJobObjectStatus(JobObjectStatus.DUMPED_DB);
        jobObjectsToUpdate.add(extractJobObject);
      }
    });

    return jobObjectsToUpdate;
  }

  private boolean checkTableNameByPattern (String tableName) {
    String regexp = "[a-zA-Z_0-9]+";

    Pattern pattern = Pattern.compile(regexp);

    Matcher matcher = pattern.matcher(tableName);

    if (!matcher.find()) return false;

    return true;
  }

  private boolean checkIfTableExists (String queryString) {
    Query isTableExistQuery = entityManager.createNativeQuery(queryString);

    List<Boolean> resultList = isTableExistQuery.getResultList();

    return resultList.get(0);
  }

  private void clearExtractStageTables (List<ExtractJobObject> jobObjects) {
    String queryStringPattern = "Drop table if exists extract_stage.%tableStageName%";

    jobObjects.forEach(extractJobObject -> {
      String dropTableQueryString = queryStringPattern.replace("%tableStageName%", extractJobObject.getTableStageName());

      Query nativeQuery = entityManager.createNativeQuery(dropTableQueryString);
      nativeQuery.executeUpdate();
    });
  }
}
