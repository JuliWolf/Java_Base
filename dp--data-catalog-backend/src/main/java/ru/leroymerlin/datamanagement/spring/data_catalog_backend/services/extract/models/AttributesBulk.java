package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.StageAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.DeleteAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.PatchAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.PostAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PatchAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PatchBulkAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.JobResultDTO;

/**
 * @author juliwolf
 */

public class AttributesBulk {
  private final UUID jobId;

  private final User user;

  private final JobResultDTO.ProcessPostAttribute processPostAttribute;
  private final JobResultDTO.ProcessPatchAttribute processPatchAttribute;

  private final JobResultDTO.ProcessDeleteAttribute processDeleteAttribute;

  private final AttributesService attributesService;

  private final StageAttributeRepository stageAttributeRepository;

  @Getter
  private final PostAttributes postAttributes;
  @Getter
  private final PatchAttributes patchAttributes;
  @Getter
  private final DeleteAttributes deleteAttributes;

  public AttributesBulk (
    UUID jobId,
    User user,
    JobResultDTO.ProcessPostAttribute processPostAttribute,
    JobResultDTO.ProcessPatchAttribute processPatchAttribute,
    JobResultDTO.ProcessDeleteAttribute processDeleteAttribute,
    AttributesService attributesService,
    StageAttributeRepository stageAttributeRepository
  ) {
    this.jobId = jobId;
    this.user = user;
    this.processPostAttribute = processPostAttribute;
    this.processPatchAttribute = processPatchAttribute;
    this.processDeleteAttribute = processDeleteAttribute;
    this.attributesService = attributesService;
    this.stageAttributeRepository = stageAttributeRepository;

    this.postAttributes = new PostAttributes();
    this.patchAttributes = new PatchAttributes();
    this.deleteAttributes = new DeleteAttributes();
  }

  private class PostAttributes implements BulkCallback <PostAttributeResponse, PostAttribute> {

    @Override
    public List<PostAttribute> loadData (Integer pageNumber, Integer pageSize) {
      return stageAttributeRepository.findAllPostStageAttributeByJobIdPageable(jobId, ActionDecision.I, PageRequest.of(pageNumber, pageSize, Sort.by("stageAttributeId").ascending()));
    }

    @Override
    public List<PostAttributeResponse> callBatchRequest (List<PostAttribute> request) {
      List<PostAttributeRequest> bulkRequest = request.stream().map(PostAttribute::getRequest).toList();

      return attributesService.createAttributesBulk(bulkRequest, user);
    }

    @Override
    public void updateCount (RequestResult requestResult, Long count) {
      requestResult.updatePostCount(count);
    }

    @Override
    public void processSuccessResponse (List<PostAttributeResponse> responseList, List<PostAttribute> requests) {
      processPostAttribute.processSuccessResult(responseList, requests);
    }

    @Override
    public void processErrorResponse (List<PostAttribute> response) {
      processPostAttribute.processErrorResult(response);
    }
  }

  private class PatchAttributes implements BulkCallback <PatchAttributeResponse, PatchAttribute> {

    @Override
    public List<PatchAttribute> loadData (Integer pageNumber, Integer pageSize) {
      return stageAttributeRepository.findAllPatchStageAttributeByJobIdPageable(jobId, ActionDecision.U, PageRequest.of(pageNumber, pageSize, Sort.by("stageAttributeId").ascending()));
    }

    @Override
    public List<PatchAttributeResponse> callBatchRequest (List<PatchAttribute> request) {
      List<PatchBulkAttributeRequest> bulkRequest = request.stream().map(PatchAttribute::getRequest).toList();

      return attributesService.updateAttributesBulk(bulkRequest, user);
    }

    @Override
    public void updateCount (RequestResult requestResult, Long count) {
      requestResult.updateUpdateCount(count);
    }

    @Override
    public void processSuccessResponse (List<PatchAttributeResponse> responseList, List<PatchAttribute> requests) {
      processPatchAttribute.processSuccessResult(responseList, requests);
    }

    @Override
    public void processErrorResponse (List<PatchAttribute> response) {
      processPatchAttribute.processErrorResult(response);
    }
  }

  private class DeleteAttributes implements BulkCallback <UUID, DeleteAttribute> {

    @Override
    public List<DeleteAttribute> loadData (Integer pageNumber, Integer pageSize) {
      return stageAttributeRepository.findAllDeleteStageAttributeByJobIdPageable(jobId, ActionDecision.D, PageRequest.of(pageNumber, pageSize, Sort.by("matchedAttributeId").ascending()));
    }

    @Override
    public List<UUID> callBatchRequest (List<DeleteAttribute> request) {
      List<UUID> bulkRequest = request.stream().map(DeleteAttribute::getRequest).toList();

      attributesService.deleteAttributesBulk(bulkRequest, user);

      return bulkRequest;
    }

    @Override
    public void updateCount (RequestResult requestResult, Long count) {
      requestResult.updateDeleteCount(count);
    }

    @Override
    public void processSuccessResponse (List<UUID> responseList, List<DeleteAttribute> requests) {
      processDeleteAttribute.processSuccessResult(responseList, requests);
    }

    @Override
    public void processErrorResponse (List<DeleteAttribute> response) {
      processDeleteAttribute.processErrorResult(response);
    }
  }
}
