package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.StageResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.models.DeleteResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.models.PostResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.JobResultDTO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.ResponsibilitiesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityResponse;

/**
 * @author juliwolf
 */

public class ResponsibilitiesBulk {
  private final UUID jobId;

  private final User user;

  private final JobResultDTO.ProcessPostResponsibility processPostResponsibility;
  private final JobResultDTO.ProcessDeleteResponsibility processDeleteResponsibility;

  private final ResponsibilitiesService responsibilitiesService;

  private final StageResponsibilityRepository stageResponsibilityRepository;

  @Getter
  private final PostResponsibilities postResponsibilities;
  @Getter
  private final DeleteResponsibilities deleteResponsibilities;

  public ResponsibilitiesBulk (
    UUID jobId,
    User user,
    JobResultDTO.ProcessPostResponsibility processPostResponsibility,
    JobResultDTO.ProcessDeleteResponsibility processDeleteResponsibility,
    ResponsibilitiesService responsibilitiesService,
    StageResponsibilityRepository stageResponsibilityRepository
  ) {
    this.jobId = jobId;
    this.user = user;
    this.processPostResponsibility = processPostResponsibility;
    this.processDeleteResponsibility = processDeleteResponsibility;
    this.responsibilitiesService = responsibilitiesService;
    this.stageResponsibilityRepository = stageResponsibilityRepository;

    this.postResponsibilities = new PostResponsibilities();
    this.deleteResponsibilities = new DeleteResponsibilities();
  }

  private class PostResponsibilities implements BulkCallback <PostResponsibilityResponse, PostResponsibility> {

    @Override
    public List<PostResponsibility> loadData (Integer pageNumber, Integer pageSize) {
      return stageResponsibilityRepository.findAllPostStageResponsibilityByJobIdPageable(jobId, ActionDecision.I, PageRequest.of(pageNumber, pageSize, Sort.by("stageResponsibilityId").ascending()));
    }

    @Override
    public List<PostResponsibilityResponse> callBatchRequest (List<PostResponsibility> request) {
      List<PostResponsibilityRequest> bulkRequest = request.stream().map(PostResponsibility::getRequest).toList();

      return responsibilitiesService.createResponsibilitiesBulk(bulkRequest, user);
    }

    @Override
    public void updateCount (RequestResult requestResult, Long count) {
      requestResult.updatePostCount(count);
    }

    @Override
    public void processSuccessResponse (List<PostResponsibilityResponse> responseList, List<PostResponsibility> requests) {
      processPostResponsibility.processSuccessResult(responseList, requests);
    }

    @Override
    public void processErrorResponse (List<PostResponsibility> response) {
      processPostResponsibility.processErrorResult(response);
    }
  }

  private class DeleteResponsibilities implements BulkCallback <UUID, DeleteResponsibility> {

    @Override
    public List<DeleteResponsibility> loadData (Integer pageNumber, Integer pageSize) {
      return stageResponsibilityRepository.findAllDeleteStageResponsibilityByJobIdPageable(jobId, ActionDecision.D, PageRequest.of(pageNumber, pageSize, Sort.by("matchedResponsibilityId").ascending()));
    }

    @Override
    public List<UUID> callBatchRequest (List<DeleteResponsibility> request) {
      List<UUID> bulkRequest = request.stream().map(DeleteResponsibility::getRequest).toList();

      responsibilitiesService.deleteResponsibilitiesBulk(bulkRequest, user);

      return bulkRequest;
    }

    @Override
    public void updateCount (RequestResult requestResult, Long count) {
      requestResult.updateDeleteCount(count);
    }

    @Override
    public void processSuccessResponse (List<UUID> responseList, List<DeleteResponsibility> requests) {
      processDeleteResponsibility.processSuccessResult(responseList, requests);
    }

    @Override
    public void processErrorResponse (List<DeleteResponsibility> response) {
      processDeleteResponsibility.processErrorResult(response);
    }
  }
}
