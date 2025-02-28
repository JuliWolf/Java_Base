package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import logger.LoggerWrapper;
import lombok.Setter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.exceptions.RuntimeExceptionWithDetails;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.filters.BulkRequestFilter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.models.StageCountByDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.StageAssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.StageAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.StageRelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.StageResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.extractJobs.models.ActiveExtractJob;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.ExtractJob;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_meta.model.enums.JobStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.exceptions.ExtractJobNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.exceptions.WrongJobStatusException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.get.GetJobsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post.PostExecuteUpdateRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models.post.PostExecuteUpdateResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesService;

/**
 * @author juliwolf
 */

@Service
public class JobsServiceImpl extends JobsDAO implements JobsService {

  @Setter
  @Value("${extract.request-timeout.minutes}")
  private Integer TIMEOUT_MINUTES;

  private final StageAssetRepository stageAssetRepository;

  private final StageAttributeRepository stageAttributeRepository;

  private final StageRelationRepository stageRelationRepository;

  private final StageResponsibilityRepository stageResponsibilityRepository;

  private final AssetsService assetsService;

  private final AttributesService attributesService;

  private final RelationsService relationsService;

  private final ResponsibilitiesService responsibilitiesService;

  private final JobResultDTO jobResultDTO;


  @Autowired
  public JobsServiceImpl (
    AssetsService assetsService,
    RelationsService relationsService,
    AttributesService attributesService,
    StageAssetRepository stageAssetRepository,
    StageAttributeRepository stageAttributeRepository,
    StageRelationRepository stageRelationRepository,
    StageResponsibilityRepository stageResponsibilityRepository,
    ResponsibilitiesService responsibilitiesService,
    JobResultDTO jobResultDTO
  ) {
    this.assetsService = assetsService;
    this.relationsService = relationsService;
    this.attributesService = attributesService;
    this.responsibilitiesService = responsibilitiesService;

    this.stageAssetRepository = stageAssetRepository;
    this.stageAttributeRepository = stageAttributeRepository;
    this.stageRelationRepository = stageRelationRepository;
    this.stageResponsibilityRepository = stageResponsibilityRepository;

    this.jobResultDTO = jobResultDTO;
  }

  @Override
  public GetJobsResponse extractJobs () {
    Page<ActiveExtractJob> response = extractJobRepository.findAllActiveJobsPageable(
      PageRequest.of(0, 500, Sort.by("jobId").ascending())
    );

    return new GetJobsResponse(
      response
    );
  }

  @Override
  public PostExecuteUpdateResponse executeUpdate (
    PostExecuteUpdateRequest request,
    User user
  ) throws
    WrongJobStatusException,
    ExtractJobNotFoundException
  {
    LoggerWrapper.info("Start execute update for jobId " + request.getJobId());

    ExtractJob extractJob = findExtractJobById(request.getJobId());

    if (!extractJob.getJobStatus().equals(JobStatus.STAGE_COMPLETE)) {
      LoggerWrapper.info("Job " + request.getJobId() + " has wrong status to execute");

      throw new WrongJobStatusException("Expected status - 'STAGE_COMPLETE' to run update");
    }

    extractJob.setJobStatus(JobStatus.API_START);
    extractJobRepository.save(extractJob);

    StopWatch watch = new StopWatch();
    LoggerWrapper.info("Start counting actions by decision" + request.getJobId(),
      JobsServiceImpl.class.getName()
    );
    watch.start();
    List<StageCountByDecision> stageAssetCountByDecision = stageAssetRepository.countActionDecisionsByJobId(extractJob.getJobId());
    List<StageCountByDecision> stageAttributeCountByDecision = stageAttributeRepository.countActionDecisionsByJobId(extractJob.getJobId());
    List<StageCountByDecision> stageRelationCountByDecision = stageRelationRepository.countActionDecisionsByJobId(extractJob.getJobId());
    List<StageCountByDecision> stageResponsibilityCountByDecision = stageResponsibilityRepository.countActionDecisionsByJobId(extractJob.getJobId());
    watch.stop();

    LoggerWrapper.info(
      "Counting actions by decisions time took: " + watch.getLastTaskTimeMillis() + " mills",
      JobsServiceImpl.class.getName()
    );

    Map<ActionDecision, AbstractMap.SimpleEntry<Integer, Long>> stageAssetMapCountByDecision = countMaxCyclesByStageCount(stageAssetCountByDecision);
    Map<ActionDecision, AbstractMap.SimpleEntry<Integer, Long>> stageAttributeMapCountByDecision = countMaxCyclesByStageCount(stageAttributeCountByDecision);
    Map<ActionDecision, AbstractMap.SimpleEntry<Integer, Long>> stageRelationMapCountByDecision = countMaxCyclesByStageCount(stageRelationCountByDecision);
    Map<ActionDecision, AbstractMap.SimpleEntry<Integer, Long>> stageResponsibilityMapCountByDecision = countMaxCyclesByStageCount(stageResponsibilityCountByDecision);

    ExecutorService requestExecutor = Executors.newFixedThreadPool(5);

    RequestResult assetsRequestResult = computeAssets(
      request.getJobId(),
      user,
      jobResultDTO,
      requestExecutor,
      stageAssetMapCountByDecision
    );

    RequestResult attributesRequestResult = computeAttributes(
      request.getJobId(),
      user,
      jobResultDTO,
      requestExecutor,
      stageAttributeMapCountByDecision
    );

    RequestResult relationsRequestResult = computeRelations(
      request.getJobId(),
      user,
      jobResultDTO,
      requestExecutor,
      stageRelationMapCountByDecision
    );

    RequestResult responsibilitiesRequestResult = computeResponsibilities(
      request.getJobId(),
      user,
      jobResultDTO,
      requestExecutor,
      stageResponsibilityMapCountByDecision
    );

    PostExecuteUpdateResponse response = new PostExecuteUpdateResponse(
      extractJob.getJobId(),
      new PostExecuteUpdateResponse.TotalItems(
        stageAssetCountByDecision,
        stageAttributeCountByDecision,
        stageRelationCountByDecision,
        stageResponsibilityCountByDecision
      ),
      new PostExecuteUpdateResponse.UpdatedItems(
        assetsRequestResult.getUpdateCount().get(),
        attributesRequestResult.getUpdateCount().get()
      ),
      new PostExecuteUpdateResponse.InsertedItems(
        assetsRequestResult.getPostCount().get(),
        attributesRequestResult.getPostCount().get(),
        relationsRequestResult.getPostCount().get(),
        responsibilitiesRequestResult.getPostCount().get()
      ),
      new PostExecuteUpdateResponse.DeletedItems(
        assetsRequestResult.getDeleteCount().get(),
        attributesRequestResult.getDeleteCount().get(),
        relationsRequestResult.getDeleteCount().get(),
        responsibilitiesRequestResult.getDeleteCount().get()
      ),
      new PostExecuteUpdateResponse.ItemsErrors(
        assetsRequestResult.getErrorCount().get(),
        attributesRequestResult.getErrorCount().get(),
        relationsRequestResult.getErrorCount().get(),
        responsibilitiesRequestResult.getErrorCount().get()
      )
    );

    if (response.hasErrors()) {
      extractJob.setJobErrorFlag(true);
      extractJob.setJobError(response.toString());
    }

    extractJob.setJobStatus(JobStatus.SUCCESS);
    extractJobRepository.save(extractJob);

    return response;
  }

  private Map<ActionDecision, AbstractMap.SimpleEntry<Integer, Long>> countMaxCyclesByStageCount (List<StageCountByDecision> stageCountByDecisionList) {
    Map<ActionDecision, AbstractMap.SimpleEntry<Integer, Long>> maxOperatioMap = new HashMap<>();

    stageCountByDecisionList.forEach(action -> {
      Integer maxCount = BulkRequestFilter.MAX_REQUEST_COUNT_BY_METHOD.get(action.getActionDecision().getValue());
      double maxOperationCount = Math.ceil((double) action.getCount() / maxCount);

      maxOperatioMap.put(action.getActionDecision(), new AbstractMap.SimpleEntry<>((int) maxOperationCount, action.getCount()));
    });

    return maxOperatioMap;
  }

  private RequestResult computeAssets (
    UUID jobId,
    User user,
    JobResultDTO jobResultDTO,
    ExecutorService requestExecutor,
    Map<ActionDecision, AbstractMap.SimpleEntry<Integer, Long>> stageAssetMapCountByDecision
  ) {
    LoggerWrapper.info("Start computing assets");

    RequestResult requestResult = new RequestResult();

    AssetsBulk assetsBulk = new AssetsBulk(
      jobId,
      user,
      jobResultDTO.getProcessPostAsset(),
      jobResultDTO.getProcessPatchAsset(),
      jobResultDTO.getProcessDeleteAsset(),
      assetsService,
      stageAssetRepository
    );

    // POST
    processCompletableFutureRequest(
      "Insert assets",
      requestResult,
      ActionDecision.I,
      requestExecutor,
      assetsBulk.getPostAssets(),
      stageAssetMapCountByDecision.get(ActionDecision.I)
    );

    // DELETE
    processCompletableFutureRequest(
      "Delete assets",
      requestResult,
      ActionDecision.D,
      requestExecutor,
      assetsBulk.getDeleteAssets(),
      stageAssetMapCountByDecision.get(ActionDecision.D)
    );

    // UPDATE
    processCompletableFutureRequest(
      "Update assets",
      requestResult,
      ActionDecision.U,
      requestExecutor,
      assetsBulk.getPatchAssets(),
      stageAssetMapCountByDecision.get(ActionDecision.U)
    );

    return requestResult;
  }

  private RequestResult computeAttributes (
    UUID jobId,
    User user,
    JobResultDTO jobResultDTO,
    ExecutorService requestExecutor,
    Map<ActionDecision, AbstractMap.SimpleEntry<Integer, Long>> stageAttributeMapCountByDecision
  ) {
    LoggerWrapper.info("Start computing attributes");

    RequestResult requestResult = new RequestResult();

    AttributesBulk attributesBulk = new AttributesBulk(
      jobId,
      user,
      jobResultDTO.getProcessPostAttribute(),
      jobResultDTO.getProcessPatchAttribute(),
      jobResultDTO.getProcessDeleteAttribute(),
      attributesService,
      stageAttributeRepository
    );

    // POST
    processCompletableFutureRequest(
      "Insert attributes",
      requestResult,
      ActionDecision.I,
      requestExecutor,
      attributesBulk.getPostAttributes(),
      stageAttributeMapCountByDecision.get(ActionDecision.I)
    );

    // DELETE
    processCompletableFutureRequest(
      "Delete attributes",
      requestResult,
      ActionDecision.D,
      requestExecutor,
      attributesBulk.getDeleteAttributes(),
      stageAttributeMapCountByDecision.get(ActionDecision.D)
    );

    // UPDATE
    processCompletableFutureRequest(
      "Update attributes",
      requestResult,
      ActionDecision.U,
      requestExecutor,
      attributesBulk.getPatchAttributes(),
      stageAttributeMapCountByDecision.get(ActionDecision.U)
    );

    return requestResult;
  }

  private RequestResult computeRelations (
    UUID jobId,
    User user,
    JobResultDTO jobResultDTO,
    ExecutorService requestExecutor,
    Map<ActionDecision, AbstractMap.SimpleEntry<Integer, Long>> stageRelationMapCountByDecision
  ) {
    LoggerWrapper.info("Start computing relations");

    RequestResult requestResult = new RequestResult();

    RelationsBulk relationsBulk = new RelationsBulk(
      jobId,
      user,
      jobResultDTO.getProcessPostRelation(),
      jobResultDTO.getProcessDeleteRelation(),
      relationsService,
      stageRelationRepository
    );

    // POST
    processCompletableFutureRequest(
      "Insert relations",
      requestResult,
      ActionDecision.I,
      requestExecutor,
      relationsBulk.getPostRelations(),
      stageRelationMapCountByDecision.get(ActionDecision.I)
    );

    // DELETE
    processCompletableFutureRequest(
      "Delete relations",
      requestResult,
      ActionDecision.D,
      requestExecutor,
      relationsBulk.getDeleteRelations(),
      stageRelationMapCountByDecision.get(ActionDecision.D)
    );

    return requestResult;
  }

  private RequestResult computeResponsibilities (
    UUID jobId,
    User user,
    JobResultDTO jobResultDTO,
    ExecutorService requestExecutor,
    Map<ActionDecision, AbstractMap.SimpleEntry<Integer, Long>> stageRelationMapCountByDecision
  ) {
    LoggerWrapper.info("Start computing responsibilities");

    RequestResult requestResult = new RequestResult();

    ResponsibilitiesBulk responsibilitiesBulk = new ResponsibilitiesBulk(
      jobId,
      user,
      jobResultDTO.getProcessPostResponsibility(),
      jobResultDTO.getProcessDeleteResponsibility(),
      responsibilitiesService,
      stageResponsibilityRepository
    );

    // POST
    processCompletableFutureRequest(
      "Insert responsibilities",
      requestResult,
      ActionDecision.I,
      requestExecutor,
      responsibilitiesBulk.getPostResponsibilities(),
      stageRelationMapCountByDecision.get(ActionDecision.I)
    );

    // DELETE
    processCompletableFutureRequest(
      "Delete responsibilities",
      requestResult,
      ActionDecision.D,
      requestExecutor,
      responsibilitiesBulk.getDeleteResponsibilities(),
      stageRelationMapCountByDecision.get(ActionDecision.D)
    );

    return requestResult;
  }

  private <T, Y> void processCompletableFutureRequest (
    String action,
    RequestResult requestResult,
    ActionDecision actionDecision,
    ExecutorService requestExecutor,
    BulkCallback<T, Y> bulkCallback,
    AbstractMap.SimpleEntry<Integer, Long> decisionCount
  ) {
    if (decisionCount == null) return;

    LoggerWrapper.info("Process action decision "  + actionDecision);

    RequestResult processRequestResult;
    try {
      StopWatch processRequestWatch = new StopWatch();
      processRequestWatch.start();

      CompletableFuture<RequestResult> future = processRequest(
        action,
        decisionCount.getKey(),
        actionDecision,
        requestExecutor,
        bulkCallback
      );

      processRequestResult = future.get();

      processRequestWatch.stop();
      LoggerWrapper.info(
        "Process request for " + action + " took time " + processRequestWatch.getLastTaskTimeMillis() + " mills",
        JobsServiceImpl.class.getName()
      );

      joinRequestResponse(processRequestResult, requestResult);
    } catch (
      InterruptedException |
      ExecutionException exception
    ) {
      LoggerWrapper.error("Error while execute requests: " + exception.getMessage(),
        exception.getStackTrace(),
        null,
        JobsServiceImpl.class.getName()
      );

      requestResult.updateErrorCount(decisionCount.getValue());
    }
  }

  private <T, Y> CompletableFuture<RequestResult> processRequest (
    String action,
    Integer stageCount,
    ActionDecision actionDecision,
    ExecutorService requestExecutor,
    BulkCallback<T, Y> bulkCallback
  ) {
    LoggerWrapper.info("Process request for " + action + ": action decision - " + actionDecision + " stageCount - " + stageCount, JobsServiceImpl.class.getName());

    RequestResult requestResult = new RequestResult();
    AtomicReference<RequestResult> atomicReference = new AtomicReference<>();
    atomicReference.set(requestResult);

    AtomicInteger iterator = new AtomicInteger(0);

    CompletableFuture[] tasks = new CompletableFuture[stageCount];
    Integer pageSize = BulkRequestFilter.MAX_REQUEST_COUNT_BY_METHOD.get(actionDecision.getValue());

    for (int i = 0; i < stageCount; i++) {
      tasks[i] = CompletableFuture.supplyAsync(() -> {
        int pageNumber = iterator.getAndIncrement();

        List<Y> requests = bulkCallback.loadData(pageNumber, pageSize);

        try {
          // Make request
          LoggerWrapper.info("Make request for " + action, JobsServiceImpl.class.getName());

          StopWatch bulkRequestWatch = new StopWatch();
          bulkRequestWatch.start();
          List<T> response = bulkCallback.callBatchRequest(requests);
          bulkRequestWatch.stop();

          LoggerWrapper.info(
            "Making request for " + action + " took time " + bulkRequestWatch.getLastTaskTimeMillis() + " mills",
            JobsServiceImpl.class.getName()
          );

          // Update count
          bulkCallback.updateCount(atomicReference.get(), (long) requests.size());

          // Process success response
          bulkCallback.processSuccessResponse(response, requests);
        } catch (RuntimeExceptionWithDetails exceptionWithDetails) {
          LoggerWrapper.error("Error while proceed requests for " + action + ": " + exceptionWithDetails.getMessage() + " details: " + exceptionWithDetails.getDetails(),
            exceptionWithDetails.getStackTrace(),
            null,
            JobsServiceImpl.class.getName()
          );

          processError(atomicReference.get(), requests, bulkCallback);
        } catch (Exception exception) {
          LoggerWrapper.error("Error while proceed requests for " + action + ": " + exception.getMessage(),
            exception.getStackTrace(),
            null,
            JobsServiceImpl.class.getName()
          );

          processError(atomicReference.get(), requests, bulkCallback);
        }

        return true;
      }, requestExecutor)
        .orTimeout(TIMEOUT_MINUTES, TimeUnit.MINUTES);;
    }

    return CompletableFuture.allOf(tasks).thenApply(result -> atomicReference.get());
  }

  private <T, Y> void processError (
    RequestResult requestResult,
    List<Y> requests,
    BulkCallback<T, Y> bulkCallback
  ) {
    // Process error count
    requestResult.updateErrorCount((long) requests.size());

    // Process error response
    bulkCallback.processErrorResponse(requests);
  }

  private void joinRequestResponse (RequestResult fromRequestResult, RequestResult toRequestResult) {
    toRequestResult.updateUpdateCount(fromRequestResult.getUpdateCount().get());
    toRequestResult.updateErrorCount(fromRequestResult.getErrorCount().get());
    toRequestResult.updatePostCount(fromRequestResult.getPostCount().get());
    toRequestResult.updateDeleteCount(fromRequestResult.getDeleteCount().get());
  }
}
