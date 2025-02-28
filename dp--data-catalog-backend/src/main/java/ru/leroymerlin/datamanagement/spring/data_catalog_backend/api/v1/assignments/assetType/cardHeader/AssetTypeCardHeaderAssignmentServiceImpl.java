package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader;

import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeCardHeaderAssignmentNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.attributeTypes.AssetTypeAttributeTypesAssignmentsDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.exceptions.AttributeTypeNotAssignedForAssetTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post.PostAssetTypeCardHeaderAssignmentRequest;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.cardHeader.post.PostAssetTypeCardHeaderAssignmentResponse;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.AttributeTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RoleNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.roles.RolesDAO;

/**
 * @author juliwolf
 */

@Service
public class AssetTypeCardHeaderAssignmentServiceImpl extends AssetTypeCardHeaderAssignmentDAO implements AssetTypeCardHeaderAssignmentService {
  @Autowired
  private AttributeTypesDAO attributeTypesDAO;

  @Autowired
  private AssetTypesDAO assetTypesDAO;

  @Autowired
  private RolesDAO rolesDAO;

  @Autowired
  private AssetTypeAttributeTypesAssignmentsDAO assetTypeAttributeTypesAssignmentsDAO;

  @Override
  public PostAssetTypeCardHeaderAssignmentResponse createAssetTypeCardHeaderAssignment (
    UUID assetTypeId,
    PostAssetTypeCardHeaderAssignmentRequest assetTypeRequest,
    User user
  ) throws
    RoleNotFoundException,
    AssetTypeNotFoundException,
    AttributeTypeNotFoundException
  {
    AssetType assetType = assetTypesDAO.findAssetTypeById(assetTypeId);

    AttributeType attributeType = null;
    if (StringUtils.isNotEmpty(assetTypeRequest.getDescription_field_attribute_type_id())) {
      attributeType = attributeTypesDAO.findAttributeTypeById(UUID.fromString(assetTypeRequest.getDescription_field_attribute_type_id()), false);
    }
    Role role = null;
    if (StringUtils.isNotEmpty(assetTypeRequest.getOwner_field_role_id())) {
      role = rolesDAO.findRoleById(UUID.fromString(assetTypeRequest.getOwner_field_role_id()));
    }

    if (attributeType != null) {
      Boolean isAssignmentExists = assetTypeAttributeTypesAssignmentsDAO.isAssignmentsExistingByAssetTypeIdAndAttributeTypeId(assetType.getAssetTypeId(), attributeType.getAttributeTypeId());
      if (!isAssignmentExists) {
        throw new AttributeTypeNotAssignedForAssetTypeException();
      }
    }

    AssetTypeCardHeaderAssignment assetTypeCardHeaderAssignment = assetTypeCardHeaderAssignmentRepository.save(new AssetTypeCardHeaderAssignment(
      assetType,
      attributeType,
      role,
      user
    ));

    return new PostAssetTypeCardHeaderAssignmentResponse(
      assetTypeCardHeaderAssignment.getAssetTypeCardHeaderAssignmentId(),
      assetTypeCardHeaderAssignment.getAssetType().getAssetTypeId(),
      assetTypeCardHeaderAssignment.getDescriptionFieldAttributeTypeUUID(),
      assetTypeCardHeaderAssignment.getOwnerFieldRoleUUID(),
      assetTypeCardHeaderAssignment.getCreatedOn(),
      assetTypeCardHeaderAssignment.getCreatedByUUID()
    );
  }

  @Override
  public void deleteAssetTypeCardHeaderAssignment (UUID assetTypeCardHeaderAssignmentId, User user) throws AssetTypeCardHeaderAssignmentNotFoundException {
    AssetTypeCardHeaderAssignment assetTypeCardHeaderAssignment = findAssetTypeCardHeaderAssignmentById(assetTypeCardHeaderAssignmentId);

    assetTypeCardHeaderAssignment.setIsDeleted(true);
    assetTypeCardHeaderAssignment.setDeletedBy(user);
    assetTypeCardHeaderAssignment.setDeletedOn(new Timestamp(System.currentTimeMillis()));

    assetTypeCardHeaderAssignmentRepository.save(assetTypeCardHeaderAssignment);
  }
}
