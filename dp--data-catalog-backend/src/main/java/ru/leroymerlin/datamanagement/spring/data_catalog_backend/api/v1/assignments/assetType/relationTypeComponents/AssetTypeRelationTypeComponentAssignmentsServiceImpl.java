package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.models.AssetTypeRelationTypeComponentAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.HierarchyRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.ResponsibilityInheritanceRole;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.PageableUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.AssetTypesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assetTypes.exceptions.AssetTypeNotFoundException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents.models.get.GetAssetTypeRelationTypeComponentAssignment;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents.models.get.GetAssetTypeRelationTypeComponentAssignments;

/**
 * @author juliwolf
 */

@Service
public class AssetTypeRelationTypeComponentAssignmentsServiceImpl extends AssetTypeRelationTypeComponentAssignmentsDAO implements AssetTypeRelationTypeComponentAssignmentsService {
  @Autowired
  private AssetTypesDAO assetTypesDAO;

  @Autowired
  public AssetTypeRelationTypeComponentAssignmentsServiceImpl (
    RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository
  ) {
    super(relationTypeComponentAssetTypeAssignmentRepository);
  }

  @Override
  public GetAssetTypeRelationTypeComponentAssignments getAssetTypeRelationTypeComponentAssignments (
    UUID assetTypeId,
    HierarchyRole hierarchyRole,
    ResponsibilityInheritanceRole responsibilityInheritanceRole,
    String relationTypeComponentName,
    Integer pageNumber,
    Integer pageSize
  ) throws AssetTypeNotFoundException {
    assetTypesDAO.findAssetTypeById(assetTypeId);

    pageSize = PageableUtils.getPageSize(pageSize, 50);
    pageNumber = PageableUtils.getPageNumber(pageNumber);

    Page<AssetTypeRelationTypeComponentAssignment> response = relationTypeComponentAssetTypeAssignmentRepository.findAllByParamsPageable(
      assetTypeId,
      hierarchyRole,
      responsibilityInheritanceRole,
      relationTypeComponentName,
      PageRequest.of(pageNumber, pageSize, Sort.by("relationTypeComponentAssetTypeAssignmentId").ascending())
    );

    return new GetAssetTypeRelationTypeComponentAssignments(
      response.getTotalElements(),
      pageSize,
      pageNumber,
      response.stream().map(GetAssetTypeRelationTypeComponentAssignment::new).toList()
    );
  }
}
