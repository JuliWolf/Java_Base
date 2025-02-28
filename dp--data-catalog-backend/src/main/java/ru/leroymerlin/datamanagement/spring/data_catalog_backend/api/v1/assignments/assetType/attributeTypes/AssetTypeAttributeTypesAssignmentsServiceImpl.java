package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import jakarta.transaction.Transactional;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models.AssetTypeAttributeTypeAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.models.AssetTypeAttributeTypeAssignmentWithAllowedValues;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AssetTypeAttributeTypeAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.SortUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentIsInheritedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AssetTypeAttributeTypeAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.exceprions.AttributeTypeIsUsedForAssetException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.GetAssetTypeAttributeTypeAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.GetAssetTypeAttributeTypeAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.GetAssetTypeAttributeTypesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.get.SortField;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributeAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributeTypesAssignmentsRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.models.post.PostAssetTypeAttributesAssignmentsResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributes.AttributesDAO;

/**
 * @author JuliWolf
 */
@Service
public class AssetTypeAttributeTypesAssignmentsServiceImpl extends AssetTypeAttributeTypesAssignmentsDAO implements AssetTypeAttributeTypesAssignmentsService {
  private final AssetTypesDAO assetTypesDAO;

  private final AttributeTypesDAO attributeTypesDAO;

  private final AttributesDAO attributesDAO;

  public AssetTypeAttributeTypesAssignmentsServiceImpl (
    AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository,
    AssetTypesDAO assetTypesDAO,
    AttributeTypesDAO attributeTypesDAO,
    AttributesDAO attributesDAO
  ) {
    super(assetTypeAttributeTypeAssignmentRepository);

    this.assetTypesDAO = assetTypesDAO;
    this.attributeTypesDAO = attributeTypesDAO;
    this.attributesDAO = attributesDAO;
  }

  @Override
  @Transactional
  public PostAssetTypeAttributesAssignmentsResponse createAssetTypeAttributeTypesAssignments (
    UUID assetTypeId,
    PostAssetTypeAttributeTypesAssignmentsRequest request,
    User user
  ) throws AssetTypeNotFoundException, AttributeTypeNotFoundException {
    AssetType assetType = assetTypesDAO.findAssetTypeById(assetTypeId);

    List<AssetType> assetTypes = assetTypesDAO.findAllAssetTypesByParentAssetTypeId(assetTypeId);

    List<PostAssetTypeAttributeAssignmentResponse> assignments = request.getAttribute_assignment()
      .stream()
      .map(attributeType -> {
        AttributeType foundAttributeType = attributeTypesDAO.findAttributeTypeById(attributeType.getAttribute_type_id(), false);

        AssetTypeAttributeTypeAssignment attributeTypeAssignment = assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(
          assetType,
          foundAttributeType,
          user
        ));

        assetTypes.forEach(_assetType -> {
          AssetTypeAttributeTypeAssignment assetTypeAttributeTypeAssignment = new AssetTypeAttributeTypeAssignment(
            _assetType,
            foundAttributeType,
            user
          );

          assetTypeAttributeTypeAssignment.setIsInherited(true);
          assetTypeAttributeTypeAssignment.setParentAssetType(assetType);

          assetTypeAttributeTypeAssignmentRepository.save(assetTypeAttributeTypeAssignment);
        });

        return new PostAssetTypeAttributeAssignmentResponse(
          attributeTypeAssignment.getAssetTypeAttributeTypeAssignmentId(),
          assetType.getAssetTypeId(),
          foundAttributeType.getAttributeTypeId(),
          new Timestamp(System.currentTimeMillis()),
          user.getUserId()
        );
      }).toList();

    return new PostAssetTypeAttributesAssignmentsResponse(assignments);
  }

  @Override
  public GetAssetTypeAttributeTypesAssignmentsResponse getAssetTypeAttributeTypesAssignmentsByAssetTypeId (UUID assetTypeId) {
    if (!assetTypesDAO.isAssetTypeExists(assetTypeId)) {
      throw new AssetTypeNotFoundException();
    }

    List<AssetTypeAttributeTypeAssignmentWithAllowedValues> responses = assetTypeAttributeTypeAssignmentRepository.findAllByAssetTypeIdWithJoinedTables(
      assetTypeId
    );

    if (responses.isEmpty()) {
      return new GetAssetTypeAttributeTypesAssignmentsResponse();
    }

    AssetTypeAttributeTypeAssignmentWithAllowedValues firstAssignment = responses.get(0);

    return new GetAssetTypeAttributeTypesAssignmentsResponse(
      firstAssignment.getAssetTypeId(),
      firstAssignment.getAssetTypeName(),
      responses.stream()
        .map(response -> new GetAssetTypeAttributeTypeAssignmentResponse(
          response.getAssetTypeAttributeTypeAssignmentId(),
          response.getAttributeTypeId(),
          response.getAttributeTypeName(),
          response.getAttributeTypeDescription(),
          response.getAttributeKindType(),
          response.getValidationMask(),
          response.getAllowedValues(),
          response.getIsInherited(),
          response.getParentAssetTypeId(),
          response.getParentAssetTypeName(),
          response.getCreatedOn(),
          response.getCreatedBy()
        )).toList()
    );
  }

  @Override
  public GetAssetTypeAttributeTypeAssignmentsResponse getAssetTypeAttributeTypesAssignmentsByParams (
    String assetTypeId,
    String attributeTypeId,
    SortField sortField,
    SortOrder sortOrder,
    Integer pageNumber,
    Integer pageSize
  ) throws
    IllegalArgumentException,
    AssetTypeNotFoundException,
    AttributeTypeNotFoundException
  {
    UUID assetTypeUUID = parseAssetTypeId(assetTypeId);
    UUID attributeTypeUUID = parseAttributeTypeId(attributeTypeId);

    pageSize = PageableUtils.getPageSize(pageSize);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<AssetTypeAttributeTypeAssignmentResponse> response = assetTypeAttributeTypeAssignmentRepository.findAllByAssetTypeIdWithJoinedTablesPageable(
      assetTypeUUID,
      attributeTypeUUID,
      PageRequest.of(pageNumber, pageSize, getSorting(sortField == null ? SortField.ASSET_TYPE_ATTRIBUTE_TYPE_USAGE_COUNT : sortField, sortOrder))
    );

    List<GetAssetTypeAttributeTypeAssignmentsResponse.GetAssetTypeAttributeTypeAssignmentResponse> list = response.stream()
      .map(item -> new GetAssetTypeAttributeTypeAssignmentsResponse.GetAssetTypeAttributeTypeAssignmentResponse(
        item.getAssetTypeAttributeTypeAssignmentId(),
        item.getAssetTypeId(),
        item.getAssetTypeName(),
        item.getAttributeTypeId(),
        item.getAttributeTypeName(),
        item.getCount() == null ? 0L : item.getCount(),
        item.getIsInherited(),
        item.getParentAssetTypeId(),
        item.getParentAssetTypeName(),
        item.getCreatedOn(),
        item.getCreatedBy()
      )).toList();

    return new GetAssetTypeAttributeTypeAssignmentsResponse(
      response.getTotalElements(),
      pageSize,
      pageNumber,
      list
    );
  }

  @Override
  @Transactional
  public void deleteAssetTypeAttributeTypeAssignmentById (
    UUID assetTypeAttributeTypeAssignmentId,
    User user
  ) throws
    AttributeTypeIsUsedForAssetException,
    AssetTypeAttributeTypeAssignmentNotFoundException,
    AssetTypeAttributeTypeAssignmentIsInheritedException
  {
    AssetTypeAttributeTypeAssignment foundAssignment = findAssetTypeAttributeTypeAssignmentById(assetTypeAttributeTypeAssignmentId, false);

    if (foundAssignment.getIsInherited()) {
      throw new AssetTypeAttributeTypeAssignmentIsInheritedException();
    }

    checkIfAttributeTypeIsUsedForAsset(foundAssignment.getAssetType().getAssetTypeId(), foundAssignment.getAttributeType().getAttributeTypeId());

    List<AssetTypeAttributeTypeAssignment> childAssignments = assetTypeAttributeTypeAssignmentRepository.findAllChildAssignmentsByAssetTypeAttributeTypeId(foundAssignment.getAssetTypeAttributeTypeAssignmentId());
    childAssignments.forEach(assignment -> {
      checkIfAttributeTypeIsUsedForAsset(assignment.getAssetType().getAssetTypeId(), assignment.getAttributeType().getAttributeTypeId());

      assignment.setIsDeleted(true);
      assignment.setDeletedBy(user);
      assignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));

      assetTypeAttributeTypeAssignmentRepository.save(assignment);
    });

    foundAssignment.setIsDeleted(true);
    foundAssignment.setDeletedBy(user);
    foundAssignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    assetTypeAttributeTypeAssignmentRepository.save(foundAssignment);
  }

  private void checkIfAttributeTypeIsUsedForAsset (UUID assetTypeId, UUID attributeTypeId) throws AttributeTypeIsUsedForAssetException {
    UUID assetId = attributesDAO.findFirstAssetIdByAssetTypeIdAndAttributeTypeId(assetTypeId, attributeTypeId);

    if (assetId != null) {
      throw new AttributeTypeIsUsedForAssetException(assetId);
    }
  }

  private UUID parseAssetTypeId (String assetTypeId) throws IllegalArgumentException, AssetTypeNotFoundException{
    UUID assetTypeUUID = null;

    if (StringUtils.isEmpty(assetTypeId)) return assetTypeUUID;

    assetTypeUUID = UUID.fromString(assetTypeId);

    boolean isAssetTypeExists = assetTypesDAO.isAssetTypeExists(assetTypeUUID);
    if (!isAssetTypeExists) {
      throw new AssetTypeNotFoundException();
    }

    return assetTypeUUID;
  }

  private UUID parseAttributeTypeId (String attributeTypeId) throws IllegalArgumentException, AttributeTypeNotFoundException {
    UUID attributeTypeUUID = null;

    if (StringUtils.isEmpty(attributeTypeId)) return attributeTypeUUID;

    attributeTypeUUID = UUID.fromString(attributeTypeId);

    boolean isAttributeTypeExists = attributeTypesDAO.isAttributeTypeExists(attributeTypeUUID);
    if (!isAttributeTypeExists) {
      throw new AttributeTypeNotFoundException();
    }

    return attributeTypeUUID;
  }

  private Sort getSorting (SortField sortField, SortOrder sortOrder) {
    return SortUtils.getSort(sortOrder, sortField.getValue(), SortField.ASSET_TYPE_ATTRIBUTE_TYPE_USAGE_COUNT.getValue());
  }
}
