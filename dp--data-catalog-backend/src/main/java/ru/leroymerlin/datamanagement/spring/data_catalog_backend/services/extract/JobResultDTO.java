package ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.extract;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import lombok.Getter;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.model.enums.ActionProcessStatus;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.StageAssetRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.DeleteAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.PatchAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAssets.models.PostAsset;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.StageAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.DeleteAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.PatchAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageAttributes.models.PostAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.StageRelationRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models.DeleteRelation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageRelations.models.PostRelation;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.StageResponsibilityRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.models.DeleteResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.extract_delta.stageResponsibilities.models.PostResponsibility;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.services.TransactionalService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.AssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.models.post.PostAssetResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PatchAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.models.post.PostAttributeResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relations.models.post.PostRelationsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.responsibilities.models.post.PostResponsibilityResponse;

/**
 * @author juliwolf
 */

@Component
public class JobResultDTO {
  private final StageAssetRepository stageAssetRepository;

  private final StageRelationRepository stageRelationRepository;

  private final StageAttributeRepository stageAttributeRepository;

  private final StageResponsibilityRepository stageResponsibilityRepository;

  private final TransactionalService transactionalService;

  @Getter
  private final ProcessPostAsset processPostAsset;
  @Getter
  private final ProcessPatchAsset processPatchAsset;
  @Getter
  private final ProcessDeleteAsset processDeleteAsset;

  @Getter
  private final ProcessPostAttribute processPostAttribute;
  @Getter
  private final ProcessPatchAttribute processPatchAttribute;
  @Getter
  private final ProcessDeleteAttribute processDeleteAttribute;

  @Getter
  private final ProcessPostRelation processPostRelation;
  @Getter
  private final ProcessDeleteRelation processDeleteRelation;

  @Getter
  private final ProcessPostResponsibility processPostResponsibility;
  @Getter
  private final ProcessDeleteResponsibility processDeleteResponsibility;

  public JobResultDTO (
    StageAssetRepository stageAssetRepository,
    StageRelationRepository stageRelationRepository,
    StageAttributeRepository stageAttributeRepository,
    StageResponsibilityRepository stageResponsibilityRepository,
    TransactionalService transactionalService
  ) {
    this.stageAssetRepository = stageAssetRepository;
    this.stageRelationRepository = stageRelationRepository;
    this.stageAttributeRepository = stageAttributeRepository;
    this.stageResponsibilityRepository = stageResponsibilityRepository;

    this.transactionalService = transactionalService;

    this.processPostAsset = new ProcessPostAsset();
    this.processPatchAsset = new ProcessPatchAsset();
    this.processDeleteAsset = new ProcessDeleteAsset();

    this.processPostAttribute = new ProcessPostAttribute();
    this.processPatchAttribute = new ProcessPatchAttribute();
    this.processDeleteAttribute = new ProcessDeleteAttribute();

    this.processPostRelation = new ProcessPostRelation();
    this.processDeleteRelation = new ProcessDeleteRelation();

    this.processPostResponsibility = new ProcessPostResponsibility();
    this.processDeleteResponsibility = new ProcessDeleteResponsibility();
  }

  public class ProcessPostAsset implements JobProcessResultService<PostAssetResponse, PostAsset> {

    public void processSuccessResult (List<PostAssetResponse> responseList, List<PostAsset> postAssetRequests) {
      transactionalService.processTransaction(() -> {
        Map<String, PostAssetResponse> assetNameResponseMap = responseList
          .stream()
          .collect(Collectors.toMap(PostAssetResponse::getAsset_name, item -> item));

        postAssetRequests.forEach(request -> {
          PostAssetResponse assetResponse = assetNameResponseMap.get(request.getAssetName());

          stageAssetRepository.setStageAssetsProcessStatus(assetResponse.getAsset_id(), ActionProcessStatus.SUCCESS.toString(), request.getStageAssetId());
          stageAttributeRepository.setStageAssetId(request.getJobId(), assetResponse.getAsset_id(), request.getAssetName());
          stageResponsibilityRepository.setStageAssetId(request.getJobId(), assetResponse.getAsset_id(), request.getAssetName());
          stageRelationRepository.setStageAssetIdForAsset1(request.getJobId(), assetResponse.getAsset_id(), request.getAssetName());
          stageRelationRepository.setStageAssetIdForAsset2(request.getJobId(), assetResponse.getAsset_id(), request.getAssetName());
        });
      });
    }

    public void processErrorResult (List<PostAsset> assetsPostList) {
      transactionalService.processTransaction(() -> {
        List<Long> stageAssetIds = assetsPostList.stream().map(PostAsset::getStageAssetId).toList();

        stageAssetRepository.setStageAssetsProcessStatus(ActionProcessStatus.ERROR.toString(), stageAssetIds);
      });
    }
  }

  public class ProcessPatchAsset implements JobProcessResultService<AssetResponse, PatchAsset> {

    public void processSuccessResult (List<AssetResponse> responseList, List<PatchAsset> patchAssetRequests) {
      transactionalService.processTransaction(() -> {
        List<Long> stageAssetIds = patchAssetRequests.stream().map(PatchAsset::getStageAssetId).toList();

        stageAssetRepository.setStageAssetsProcessStatus(ActionProcessStatus.SUCCESS.toString(), stageAssetIds);
      });
    }

    public void processErrorResult (List<PatchAsset> assetsPatchList) {
      transactionalService.processTransaction(() -> {
        List<Long> stageAssetIds = assetsPatchList.stream().map(PatchAsset::getStageAssetId).toList();

        stageAssetRepository.setStageAssetsProcessStatus(ActionProcessStatus.ERROR.toString(), stageAssetIds);
      });
    }
  }

  public class ProcessDeleteAsset implements JobProcessResultService<UUID, DeleteAsset> {

    public void processSuccessResult (List<UUID> assetsList, List<DeleteAsset> requests) {
      transactionalService.processTransaction(() -> {
        List<Long> stageAssetIds = requests.stream().map(DeleteAsset::getStageAssetId).toList();

        stageAssetRepository.setStageAssetsProcessStatus(ActionProcessStatus.SUCCESS.toString(), stageAssetIds);
      });
    }

    public void processErrorResult (List<DeleteAsset> assetsList) {
      transactionalService.processTransaction(() -> {
        List<Long> stageAssetIds = assetsList.stream().map(DeleteAsset::getStageAssetId).toList();

        stageAssetRepository.setStageAssetsProcessStatus(ActionProcessStatus.ERROR.toString(), stageAssetIds);
      });
    }
  }

  public class ProcessPostAttribute implements JobProcessResultService<PostAttributeResponse, PostAttribute> {

    public void processErrorResult (List<PostAttribute> attributesPostList) {
      transactionalService.processTransaction(() -> {
        List<Long> stageAttributesIds = attributesPostList.stream().map(PostAttribute::getStageAttributeId).toList();

        stageAttributeRepository.setStageAttributesProcessStatus(ActionProcessStatus.ERROR.toString(), stageAttributesIds);
      });
    }

    public void processSuccessResult (List<PostAttributeResponse> responseList, List<PostAttribute> postAttributeRequests) {
      Map<AbstractMap.SimpleEntry<UUID, UUID>, PostAttributeResponse> assetTypeIdAssetIdResponseMap = responseList
        .stream()
        .collect(Collectors.toMap(item -> new AbstractMap.SimpleEntry<>(item.getAttribute_type_id(), item.getAsset_id()), item -> item));

      transactionalService.processTransaction(() -> {
        postAttributeRequests.forEach(request -> {
          PostAttributeResponse attributeResponse = assetTypeIdAssetIdResponseMap.get(new AbstractMap.SimpleEntry<>(request.getAttributeTypeId(), request.getAssetId()));
          stageAttributeRepository.setStageAttributesProcessStatus(attributeResponse.getAttribute_id(), ActionProcessStatus.SUCCESS.toString(), request.getStageAttributeId());
        });
      });
    }
  }

  public class ProcessPatchAttribute implements JobProcessResultService<PatchAttributeResponse, PatchAttribute> {

    public void processErrorResult (List<PatchAttribute> attributesPostList) {
      transactionalService.processTransaction(() -> {
        List<Long> stageAttributesIds = attributesPostList.stream().map(PatchAttribute::getStageAttributeId).toList();

        stageAttributeRepository.setStageAttributesProcessStatus(ActionProcessStatus.ERROR.toString(), stageAttributesIds);
      });
    }

    public void processSuccessResult (List<PatchAttributeResponse> responseList, List<PatchAttribute> patchAttributeRequests) {
      transactionalService.processTransaction(() -> {
        List<Long> stageAttributesIds = patchAttributeRequests.stream().map(PatchAttribute::getStageAttributeId).toList();

        stageAttributeRepository.setStageAttributesProcessStatus(ActionProcessStatus.SUCCESS.toString(), stageAttributesIds);
      });
    }
  }

  public class ProcessDeleteAttribute implements JobProcessResultService<UUID, DeleteAttribute> {

    public void processSuccessResult (List<UUID> attributesList, List<DeleteAttribute> requests) {
      transactionalService.processTransaction(() -> {
        List<Long> stageAttributesIds = requests.stream().map(DeleteAttribute::getStageAttributeId).toList();

        stageAttributeRepository.setStageAttributesProcessStatus(ActionProcessStatus.SUCCESS.toString(), stageAttributesIds);
      });
    }

    public void processErrorResult (List<DeleteAttribute> attributesList) {
      transactionalService.processTransaction(() -> {
        List<Long> stageAttributesIds = attributesList.stream().map(DeleteAttribute::getStageAttributeId).toList();

        stageAttributeRepository.setStageAttributesProcessStatus(ActionProcessStatus.ERROR.toString(), stageAttributesIds);
      });
    }
  }

  public class ProcessPostRelation implements JobProcessResultService<PostRelationsResponse, PostRelation> {

    public void processSuccessResult (List<PostRelationsResponse> responseList, List<PostRelation> postRelationRequests) {
      Map<PostRelationsResponse, PostRelationsResponse> relationsResponseMap = responseList
        .stream()
        .collect(Collectors.toMap(item -> item, item -> item));

      transactionalService.processTransaction(() -> {
        postRelationRequests.forEach(request -> {
          PostRelationsResponse relationResponse = relationsResponseMap.get(request);
          stageRelationRepository.setStageRelationProcessStatus(relationResponse.getRelation_id(), ActionProcessStatus.SUCCESS.toString(), request.getStageRelationId());
        });
      });
    }

    public void processErrorResult (List<PostRelation> relationsPostList) {
      transactionalService.processTransaction(() -> {
        List<Long> stageRelationIds = relationsPostList.stream().map(PostRelation::getStageRelationId).toList();

        stageRelationRepository.setStageRelationsProcessStatus(ActionProcessStatus.ERROR.toString(), stageRelationIds);
      });
    }
  }

  public class ProcessDeleteRelation implements JobProcessResultService<UUID, DeleteRelation> {

    public void processSuccessResult (List<UUID> relationsList, List<DeleteRelation> requests) {
      transactionalService.processTransaction(() -> {
        List<Long> stageRelationIds = requests.stream().map(DeleteRelation::getStageRelationId).toList();

        stageRelationRepository.setStageRelationsProcessStatus(ActionProcessStatus.SUCCESS.toString(), stageRelationIds);
      });
    }

    public void processErrorResult (List<DeleteRelation> relationsList) {
      transactionalService.processTransaction(() -> {
        List<Long> stageRelationIds = relationsList.stream().map(DeleteRelation::getStageRelationId).toList();

        stageRelationRepository.setStageRelationsProcessStatus(ActionProcessStatus.ERROR.toString(), stageRelationIds);
      });
    }
  }

  public class ProcessPostResponsibility implements JobProcessResultService<PostResponsibilityResponse, PostResponsibility> {

    public void processErrorResult (List<PostResponsibility> attributesPostList) {
      transactionalService.processTransaction(() -> {
        List<Long> stageResponsibilitiesIds = attributesPostList.stream().map(PostResponsibility::getStageResponsibilityId).toList();

        stageResponsibilityRepository.setStageResponsibilitiesProcessStatus(ActionProcessStatus.ERROR.toString(), stageResponsibilitiesIds);
      });
    }

    public void processSuccessResult (List<PostResponsibilityResponse> responseList, List<PostResponsibility> postResponsibilityRequests) {
      transactionalService.processTransaction(() -> {
        List<Long> stageResponsibilitiesIds = postResponsibilityRequests.stream().map(PostResponsibility::getStageResponsibilityId).toList();

        stageResponsibilityRepository.setStageResponsibilitiesProcessStatus(ActionProcessStatus.SUCCESS.toString(), stageResponsibilitiesIds);
      });
    }
  }

  public class ProcessDeleteResponsibility implements JobProcessResultService<UUID, DeleteResponsibility> {

    public void processSuccessResult (List<UUID> relationsList, List<DeleteResponsibility> requests) {
      transactionalService.processTransaction(() -> {
        List<Long> stageResponsibilitiesIds = requests.stream().map(DeleteResponsibility::getStageResponsibilityId).toList();

        stageResponsibilityRepository.setStageResponsibilitiesProcessStatus(ActionProcessStatus.SUCCESS.toString(), stageResponsibilitiesIds);
      });
    }

    public void processErrorResult (List<DeleteResponsibility> responsibilitiesList) {
      transactionalService.processTransaction(() -> {
        List<Long> stageResponsibilitiesIds = responsibilitiesList.stream().map(DeleteResponsibility::getStageResponsibilityId).toList();

        stageResponsibilityRepository.setStageResponsibilitiesProcessStatus(ActionProcessStatus.ERROR.toString(), stageResponsibilitiesIds);
      });
    }
  }
}
