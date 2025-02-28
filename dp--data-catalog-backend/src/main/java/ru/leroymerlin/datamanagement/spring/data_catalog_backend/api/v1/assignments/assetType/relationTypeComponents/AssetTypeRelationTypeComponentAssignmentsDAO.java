package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.assignments.assetType.relationTypeComponents;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.relationTypeComponent.assetTypes.RelationTypeComponentAssetTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationTypeComponentAssetTypeAssignment;

/**
 * @author juliwolf
 */

@Service
public class AssetTypeRelationTypeComponentAssignmentsDAO {
  protected final RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository;

  @Autowired
  public AssetTypeRelationTypeComponentAssignmentsDAO (
    RelationTypeComponentAssetTypeAssignmentRepository relationTypeComponentAssetTypeAssignmentRepository
  ) {
    this.relationTypeComponentAssetTypeAssignmentRepository = relationTypeComponentAssetTypeAssignmentRepository;
  }

  public List<RelationTypeComponentAssetTypeAssignment> findAllByAssetTypeId (UUID assetTypeId) {
    return relationTypeComponentAssetTypeAssignmentRepository.findAllByAssetTypeId(assetTypeId);
  }

  public RelationTypeComponentAssetTypeAssignment saveRelationTypeComponentAssetTypeAssignment (RelationTypeComponentAssetTypeAssignment relationTypeComponentAssetTypeAssignment) {
    return relationTypeComponentAssetTypeAssignmentRepository.save(relationTypeComponentAssetTypeAssignment);
  }
}
