package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.StageRelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models.DeleteRelation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models.PostRelation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.JobResultDTO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.RelationsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsResponse;

/**
 * @author juliwolf
 */

public class RelationsBulk {
  private final UUID jobId;

  private final User user;

  private final JobResultDTO.ProcessPostRelation processPostRelation;

  private final JobResultDTO.ProcessDeleteRelation processDeleteRelation;

  private final RelationsService relationsService;

  private final StageRelationRepository stageRelationRepository;

  @Getter
  private final PostRelations postRelations;
  @Getter
  private final DeleteRelations deleteRelations;

  public RelationsBulk (
    UUID jobId,
    User user,
    JobResultDTO.ProcessPostRelation processPostRelation,
    JobResultDTO.ProcessDeleteRelation processDeleteRelation,
    RelationsService relationsService,
    StageRelationRepository stageRelationRepository
  ) {
    this.jobId = jobId;
    this.user = user;
    this.processPostRelation = processPostRelation;
    this.processDeleteRelation = processDeleteRelation;
    this.relationsService = relationsService;
    this.stageRelationRepository = stageRelationRepository;

    this.postRelations = new PostRelations();
    this.deleteRelations = new DeleteRelations();
  }

  private class PostRelations implements BulkCallback <PostRelationsResponse, PostRelation> {

    @Override
    public List<PostRelation> loadData (Integer pageNumber, Integer pageSize) {
      return stageRelationRepository.findAllPostStageRelationByJobIdPageable(jobId, ActionDecision.I, PageRequest.of(pageNumber, pageSize, Sort.by("stageRelationId").ascending()));
    }

    @Override
    public List<PostRelationsResponse> callBatchRequest (List<PostRelation> request) {
      List<PostRelationsRequest> bulkRequest = request.stream().map(PostRelation::getRequest).toList();

      return relationsService.createRelationsBulk(bulkRequest, user);
    }

    @Override
    public void updateCount (RequestResult requestResult, Long count) {
      requestResult.updatePostCount(count);
    }

    @Override
    public void processSuccessResponse (List<PostRelationsResponse> responseList, List<PostRelation> requests) {
      processPostRelation.processSuccessResult(responseList, requests);
    }

    @Override
    public void processErrorResponse (List<PostRelation> response) {
      processPostRelation.processErrorResult(response);
    }
  }

  private class DeleteRelations implements BulkCallback <UUID, DeleteRelation> {

    @Override
    public List<DeleteRelation> loadData (Integer pageNumber, Integer pageSize) {
      return stageRelationRepository.findAllDeleteStageRelationByJobIdPageable(jobId, ActionDecision.D, PageRequest.of(pageNumber, pageSize, Sort.by("matchedRelationId").ascending()));
    }

    @Override
    public List<UUID> callBatchRequest (List<DeleteRelation> request) {
      List<UUID> bulkRequest = request.stream().map(DeleteRelation::getRequest).toList();

      relationsService.deleteRelationsBulk(bulkRequest, user);

      return bulkRequest;
    }

    @Override
    public void updateCount (RequestResult requestResult, Long count) {
      requestResult.updateDeleteCount(count);
    }

    @Override
    public void processSuccessResponse (List<UUID> responseList, List<DeleteRelation> requests) {
      processDeleteRelation.processSuccessResult(responseList, requests);
    }

    @Override
    public void processErrorResponse (List<DeleteRelation> response) {
      processDeleteRelation.processErrorResult(response);
    }
  }
}
