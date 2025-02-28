package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.models;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionDecision;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.StageAssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.DeleteAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.PatchAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.PostAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract.JobResultDTO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.AssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PatchAssetRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostAssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostOrPatchAssetRequest;

/**
 * @author juliwolf
 */

public class AssetsBulk {
  private final UUID jobId;

  private final User user;

  private final JobResultDTO.ProcessPostAsset processPostAsset;

  private final JobResultDTO.ProcessPatchAsset processPatchAsset;

  private final JobResultDTO.ProcessDeleteAsset processDeleteAsset;

  private final AssetsService assetsService;

  private final StageAssetRepository stageAssetRepository;

  @Getter
  private final PostAssets postAssets;
  @Getter
  private final PatchAssets patchAssets;
  @Getter
  private final DeleteAssets deleteAssets;

  public AssetsBulk (
    UUID jobId,
    User user,
    JobResultDTO.ProcessPostAsset processPostAsset,
    JobResultDTO.ProcessPatchAsset processPatchAsset,
    JobResultDTO.ProcessDeleteAsset processDeleteAsset,
    AssetsService assetsService,
    StageAssetRepository stageAssetRepository
  ) {
    this.jobId = jobId;
    this.user = user;
    this.processPostAsset = processPostAsset;
    this.processPatchAsset = processPatchAsset;
    this.processDeleteAsset = processDeleteAsset;
    this.assetsService = assetsService;
    this.stageAssetRepository = stageAssetRepository;

    this.postAssets = new PostAssets();
    this.patchAssets = new PatchAssets();
    this.deleteAssets = new DeleteAssets();
  }

  private class PostAssets implements BulkCallback <PostAssetResponse, PostAsset> {
    @Override
    public List<PostAsset> loadData (Integer pageNumber, Integer pageSize) {
      return stageAssetRepository.findAllPostStageAssetByJobIdPageable(jobId, ActionDecision.I, PageRequest.of(pageNumber, pageSize, Sort.by("stageAssetId").ascending()));
    }

    @Override
    public List<PostAssetResponse> callBatchRequest (List<PostAsset> request) {
      List<PostOrPatchAssetRequest> bulkRequest = request.stream().map(PostAsset::getRequest).toList();

      return assetsService.createAssetsBulk(bulkRequest, user);
    }

    @Override
    public void updateCount (RequestResult requestResult, Long count) {
      requestResult.updatePostCount(count);
    }

    @Override
    public void processSuccessResponse (List<PostAssetResponse> responseList, List<PostAsset> requests) {
      processPostAsset.processSuccessResult(responseList, requests);
    }

    @Override
    public void processErrorResponse (List<PostAsset> response) {
      processPostAsset.processErrorResult(response);
    }
  }

  private class PatchAssets implements BulkCallback <AssetResponse, PatchAsset> {

    @Override
    public List<PatchAsset> loadData (Integer pageNumber, Integer pageSize) {
      return stageAssetRepository.findAllPatchStageAssetByJobIdPageable(jobId, ActionDecision.U, PageRequest.of(pageNumber, pageSize, Sort.by("matchedAssetId").ascending()));
    }

    @Override
    public List<AssetResponse> callBatchRequest (List<PatchAsset> request) {
      List<PatchAssetRequest> bulkRequest = request.stream().map(PatchAsset::getRequest).toList();

      return assetsService.updateBulkAsset(bulkRequest, user);
    }

    @Override
    public void updateCount (RequestResult requestResult, Long count) {
      requestResult.updateUpdateCount(count);
    }

    @Override
    public void processSuccessResponse (List<AssetResponse> responseList, List<PatchAsset> requests) {
      processPatchAsset.processSuccessResult(responseList, requests);
    }

    @Override
    public void processErrorResponse (List<PatchAsset> response) {
      processPatchAsset.processErrorResult(response);
    }
  }

  private class DeleteAssets implements BulkCallback <UUID, DeleteAsset> {

    @Override
    public List<DeleteAsset> loadData (Integer pageNumber, Integer pageSize) {
      return stageAssetRepository.findAllDeleteStageAssetByJobIdPageable(jobId, ActionDecision.D, PageRequest.of(pageNumber, pageSize, Sort.by("matchedAssetId").ascending()));
    }

    @Override
    public List<UUID> callBatchRequest (List<DeleteAsset> request) {
      List<UUID> bulkRequest = request.stream().map(DeleteAsset::getRequest).toList();
      assetsService.deleteAssetsBulk(bulkRequest, user);
      return bulkRequest;
    }

    @Override
    public void updateCount (RequestResult requestResult, Long count) {
      requestResult.updateDeleteCount(count);
    }

    @Override
    public void processSuccessResponse (List<UUID> responseList, List<DeleteAsset> requests) {
      processDeleteAsset.processSuccessResult(responseList, requests);
    }

    @Override
    public void processErrorResponse (List<DeleteAsset> response) {
      processDeleteAsset.processErrorResult(response);
    }
  }
}
