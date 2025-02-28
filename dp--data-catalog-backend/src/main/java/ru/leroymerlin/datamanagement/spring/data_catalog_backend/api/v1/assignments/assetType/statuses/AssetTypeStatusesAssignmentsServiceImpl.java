package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.models.AssetTypeStatusCount;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.statuses.models.AssetTypeStatusAssignmentWithConnectedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetTypeStatusAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.Status;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AssignmentStatusType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assets.AssetsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.AssetTypeStatusAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.exceptions.StatusTypeIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.get.GetAssetTypeStatusesResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.statuses.models.post.PostAssetTypeStatusesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.StatusesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.statuses.exceptions.StatusNotFoundException;

/**
 * @author JuliWolf
 */
@Service
public class AssetTypeStatusesAssignmentsServiceImpl extends AssetTypeStatusesAssignmentsDAO implements AssetTypeStatusesAssignmentsService {
  private final AssetTypesDAO assetTypesDAO;

  private final StatusesDAO statusesDAO;

  private final AssetsDAO assetsDAO;

  public AssetTypeStatusesAssignmentsServiceImpl (
    AssetTypesDAO assetTypesDAO,
    StatusesDAO statusesDAO,
    AssetsDAO assetsDAO
  ) {
    this.assetTypesDAO = assetTypesDAO;
    this.statusesDAO = statusesDAO;
    this.assetsDAO = assetsDAO;
  }

  @Override
  @Transactional
  public PostAssetTypeStatusesAssignmentsResponse createAssetTypeStatusesAssignments (
    UUID assetTypeId,
    PostAssetTypeStatusesAssignmentsRequest request,
    User user
  ) throws AssetTypeNotFoundException, StatusNotFoundException {
    AssetType assetType = assetTypesDAO.findAssetTypeById(assetTypeId);

    List<AssetType> assetTypes = assetTypesDAO.findAllAssetTypesByParentAssetTypeId(assetTypeId);

    List<PostAssetTypeStatusAssignmentResponse> assignments = request.getStatus_assignment()
      .stream()
      .map(status -> {
        Status foundStatus = statusesDAO.findStatusById(status.getStatus_id());

        AssetTypeStatusAssignment assetTypeStatusAssignment = assetTypeStatusAssignmentRepository.save(new AssetTypeStatusAssignment(
          assetType,
          status.getStatus_type(),
          foundStatus,
          user
        ));

        assetTypes.forEach(_assetType -> {
          AssetTypeStatusAssignment statusAssignment = new AssetTypeStatusAssignment(
            _assetType,
            status.getStatus_type(),
            foundStatus,
            user
          );

          statusAssignment.setIsInherited(true);
          statusAssignment.setParentAssetType(assetType);

          assetTypeStatusAssignmentRepository.save(statusAssignment);
        });

        return new PostAssetTypeStatusAssignmentResponse(
          assetTypeStatusAssignment.getAssetTypeStatusAssignmentId(),
          assetType.getAssetTypeId(),
          assetTypeStatusAssignment.getAssignmentStatusType(),
          foundStatus.getStatusId(),
          new Timestamp(System.currentTimeMillis()),
          user.getUserId()
        );
      }).toList();

    return new PostAssetTypeStatusesAssignmentsResponse(assignments);
  }

  @Override
  public GetAssetTypeStatusesAssignmentsResponse getAssetTypeStatusesAssignmentsByParams (UUID assetTypeId, AssignmentStatusType statusType) {
    if (!assetTypesDAO.isAssetTypeExists(assetTypeId)) {
      throw new AssetTypeNotFoundException();
    }

    List<AssetTypeStatusAssignmentWithConnectedValues> assignments = assetTypeStatusAssignmentRepository.findAllByAssignmentIdAndStatusTypePageable(
      assetTypeId,
      statusType
    );

    if (assignments.isEmpty()) {
      return new GetAssetTypeStatusesAssignmentsResponse();
    }

    AssetTypeStatusAssignmentWithConnectedValues assetType = assignments.get(0);

    return new GetAssetTypeStatusesAssignmentsResponse(
      assetType.getAssetTypeId(),
      assetType.getAssetTypeName(),
      assignments.stream()
        .map(assignment -> new GetAssetTypeStatusAssignmentResponse(
          assignment.getAssetTypeStatusAssignmentId(),
          assignment.getStatusId(),
          assignment.getStatusName(),
          assignment.getStatusDescription(),
          assignment.getIsInherited(),
          assignment.getParentAssetTypeId(),
          assignment.getParentAssetTypeName(),
          assignment.getAssignmentStatusType(),
          assignment.getCreatedOn(),
          assignment.getCreatedBy()
        )).toList()
    );
  }

  @Override
  public void deleteAssetTypeStatusAssignmentById (
    UUID assetTypeId,
    User user
  ) throws
    StatusTypeIsUsedForAssetException,
    AssetTypeStatusAssignmentNotFoundException,
    AssetTypeStatusAssignmentIsInheritedException {
    AssetTypeStatusAssignment foundAssignment = findAssetTypeStatusAssignmentsById(assetTypeId);

    if (foundAssignment.getIsInherited()) {
      throw new AssetTypeStatusAssignmentIsInheritedException();
    }

    UUID statusId = foundAssignment.getStatus() != null ? foundAssignment.getStatus().getStatusId() : null;
    checkIfAssetTypeIsUsedForAsset(statusId, foundAssignment.getAssetType().getAssetTypeId());

    List<AssetTypeStatusAssignment> childAssignments = assetTypeStatusAssignmentRepository.findAllChildAssignmentsByAssetTypeStatusAssignmentId(foundAssignment.getAssetTypeStatusAssignmentId());
    childAssignments.forEach(assignment -> {
      checkIfAssetTypeIsUsedForAsset(assignment.getStatus().getStatusId(), assignment.getAssetType().getAssetTypeId());

      assignment.setIsDeleted(true);
      assignment.setDeletedBy(user);
      assignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));

      assetTypeStatusAssignmentRepository.save(assignment);
    });

    foundAssignment.setIsDeleted(true);
    foundAssignment.setDeletedBy(user);
    foundAssignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    assetTypeStatusAssignmentRepository.save(foundAssignment);
  }

  @Override
  public GetAssetTypeStatusesResponse getAssetTypeStatusesByParams (
    UUID statusId,
    AssignmentStatusType statusType,
    UUID assetTypeId,
    Integer pageNumber,
    Integer pageSize
  ) {
    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<AssetTypeStatusAssignment> statusAssignments = assetTypeStatusAssignmentRepository.findAllByParamsWithJoinedTablesPageable(statusId, statusType, assetTypeId, PageRequest.of(pageNumber, pageSize));
    Set<UUID> assetTypeStatusAssignmentIds = statusAssignments.stream().map(AssetTypeStatusAssignment::getAssetTypeStatusAssignmentId).collect(Collectors.toSet());

    List<AssetTypeStatusCount> assetTypeStatusCounts = assetTypeStatusAssignmentRepository.countAssetTypeStatusUsage(assetTypeStatusAssignmentIds.stream().toList());
    Map<UUID, Long> countByAssetTypeStatusAssignment = assetTypeStatusCounts.stream().collect(Collectors.toMap(AssetTypeStatusCount::getAssetTypeStatusAssignmentId, AssetTypeStatusCount::getCount));

    List<GetAssetTypeStatusResponse> statuses = statusAssignments.stream().map(assignment ->  new GetAssetTypeStatusResponse(
        assignment.getAssetTypeStatusAssignmentId(),
        assignment.getAssetType().getAssetTypeId(),
        assignment.getAssetType().getAssetTypeName(),
        assignment.getStatus().getStatusId(),
        assignment.getStatus().getStatusName(),
        assignment.getAssignmentStatusType(),
        countByAssetTypeStatusAssignment.getOrDefault(assignment.getAssetTypeStatusAssignmentId(), 0L),
        assignment.getIsInherited(),
        assignment.getParentAssetType() != null ? assignment.getParentAssetType().getAssetTypeId() : null,
        assignment.getParentAssetType() != null ? assignment.getParentAssetType().getAssetTypeName() : null,
        assignment.getCreatedOn(),
        assignment.getCreatedByUUID()
      )
    ).toList();

    return new GetAssetTypeStatusesResponse(
      statusAssignments.getTotalElements(),
      pageSize,
      pageNumber,
      statuses
    );
  }

  private void checkIfAssetTypeIsUsedForAsset (UUID statusId, UUID assetTypeId) throws StatusTypeIsUsedForAssetException {
    UUID assetId = assetsDAO.findFirstAssetByAssetTypeAndStatusId(statusId, assetTypeId);
    if (assetId != null) {
      throw new StatusTypeIsUsedForAssetException(assetId);
    }
  }
}
