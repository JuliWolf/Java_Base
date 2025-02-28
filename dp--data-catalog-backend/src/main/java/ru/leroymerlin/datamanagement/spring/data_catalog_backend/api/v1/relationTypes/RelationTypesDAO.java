package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationType.RelationTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationTypes.exceptions.RelationTypeNotFoundException;

@Service
public class RelationTypesDAO {
  @Autowired
  protected RelationTypeRepository relationTypeRepository;

  public RelationType findRelationTypeById (UUID relationTypeId, boolean isJoinFetch) {
    Optional<RelationType> foundRelationType;

    if (isJoinFetch) {
      foundRelationType = relationTypeRepository.findByIdWithJoinedTables(relationTypeId);
    } else {
      foundRelationType = relationTypeRepository.findById(relationTypeId);
    }

    if (foundRelationType.isEmpty()) {
      throw new RelationTypeNotFoundException();
    }

    if (foundRelationType.get().getIsDeleted()) {
      throw new RelationTypeNotFoundException();
    }

    return foundRelationType.get();
  }

  public List<RelationType> findAllByRelationTypeIds (List<UUID> relationTypeIds) {
    return relationTypeRepository.findAllByRelationTypeIds(relationTypeIds);
  }
}
