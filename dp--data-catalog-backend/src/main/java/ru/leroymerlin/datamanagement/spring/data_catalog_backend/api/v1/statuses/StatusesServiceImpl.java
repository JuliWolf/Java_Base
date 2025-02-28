package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Language;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Status;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.statuses.models.StatusUsageCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.OptionalUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.language.LanguageService;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.StatusResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.get.GetStatusResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.get.GetStatusesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PatchStatusRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PostStatusRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.models.post.PostStatusResponse;

/**
 * @author JuliWolf
 */
@Service
public class StatusesServiceImpl extends StatusesDAO implements StatusesService {
  @Autowired
  private LanguageService languageService;

  @Autowired
  private AssetsDAO assetsDAO;

  @Override
  public PostStatusResponse createStatus (PostStatusRequest statusRequest, User user) {
    Language ru = languageService.getLanguage("ru");

    Status status = statusRepository.save(new Status(
      statusRequest.getStatus_name(),
      statusRequest.getStatus_description(),
      ru,
      user
    ));

    return new PostStatusResponse(
      status.getStatusId(),
      status.getStatusName(),
      status.getStatusDescription(),
      ru.getLanguage(),
      status.getCreatedOn(),
      status.getCreatedByUUID()
    );
  }

  @Override
  public StatusResponse updateStatus (UUID statusId, PatchStatusRequest statusRequest, User user) throws StatusNotFoundException {
    Status foundStatus = findStatusById(statusId);

    OptionalUtils.doActionIfPresent(statusRequest.getStatus_name(), statusName -> foundStatus.setStatusName(statusName.orElse(foundStatus.getStatusName())));
    OptionalUtils.doActionIfPresent(statusRequest.getStatus_description(), statusDescription -> foundStatus.setStatusDescription(statusDescription.orElse(null)));

    foundStatus.setLastModifiedOn(new Timestamp(System.currentTimeMillis()));
    foundStatus.setModifiedBy(user);

    Status status = statusRepository.save(foundStatus);

    return new StatusResponse(
      status.getStatusId(),
      status.getStatusName(),
      status.getStatusDescription(),
      status.getLanguageName(),
      status.getCreatedOn(),
      status.getCreatedByUUID(),
      status.getLastModifiedOn(),
      status.getModifiedBy().getUserId()
    );
  }

  @Override
  public StatusResponse getStatusById (UUID statusId) throws StatusNotFoundException {
    Status status = findStatusById(statusId);

    UUID lastModifiedBy = status.getModifiedBy() != null ? status.getModifiedBy().getUserId() : null;

    return new StatusResponse(
      status.getStatusId(),
      status.getStatusName(),
      status.getStatusDescription(),
      status.getLanguageName(),
      status.getCreatedOn(),
      status.getCreatedByUUID(),
      status.getLastModifiedOn(),
      lastModifiedBy
    );
  }

  @Override
  public GetStatusesResponse getStatusesByParams (
    String statusName,
    String statusDescription,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<Status> statuses = statusRepository.findAllByStatusNameAndDescriptionPageable(
      statusName,
      statusDescription,
      PageRequest.of(pageNumber, pageSize, Sort.by("statusId").ascending())
    );

    List<UUID> statusesIds = statuses.stream().map(Status::getStatusId).toList();
    List<StatusUsageCount> statusUsageCounts = statusRepository.countStatusesUsageByStatusIds(statusesIds);
    Map<UUID, Long> countByStatusIdMap = statusUsageCounts.stream().collect(Collectors.toMap(
      StatusUsageCount::getStatusId,
      StatusUsageCount::getStatusUsageCount
    ));

    List<GetStatusResponse> statusesCollection = statuses.stream().map(status -> {
      UUID lastModifiedBy = status.getModifiedBy() != null ? status.getModifiedBy().getUserId() : null;

      return new GetStatusResponse(
        status.getStatusId(),
        status.getStatusName(),
        status.getStatusDescription(),
        countByStatusIdMap.getOrDefault(status.getStatusId(), 0L),
        status.getLanguageName(),
        status.getCreatedOn(),
        status.getCreatedByUUID(),
        status.getLastModifiedOn(),
        lastModifiedBy
      );
    }).toList();

    return new GetStatusesResponse(
      statuses.getTotalElements(),
      pageSize,
      pageNumber,
      statusesCollection
    );
  }

  @Override
  public void deleteStatusById (UUID statusId, User user) throws StatusNotFoundException, StatusIsUsedForAssetException {
    Status status = findStatusById(statusId);

    UUID assetId = assetsDAO.findFirstAssetByAssetTypeAndStatusId(statusId, null);
    if (assetId != null) {
      throw new StatusIsUsedForAssetException(assetId);
    }

    status.setIsDeleted(true);
    status.setDeletedBy(user);
    status.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    statusRepository.save(status);
  }
}
