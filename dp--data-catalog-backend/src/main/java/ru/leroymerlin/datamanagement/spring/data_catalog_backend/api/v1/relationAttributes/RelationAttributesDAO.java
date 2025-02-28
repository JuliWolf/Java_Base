package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationAttributes.RelationAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationAttributes.exceptions.RelationAttributeNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class RelationAttributesDAO {
  @Autowired
  protected RelationAttributeRepository relationAttributeRepository;

  public RelationAttribute findRelationAttributeById (UUID relationAttributeId, Boolean isJoinFetch) throws RelationAttributeNotFoundException {
    Optional<RelationAttribute> relationAttribute;

    if (isJoinFetch) {
      relationAttribute = relationAttributeRepository.findRelationAttributeByIdWithJoinedTables(relationAttributeId);
    } else {
      relationAttribute = relationAttributeRepository.findById(relationAttributeId);
    }

    if (relationAttribute.isEmpty()) {
      throw new RelationAttributeNotFoundException(relationAttributeId);
    }

    if (relationAttribute.get().getIsDeleted()) {
      throw new RelationAttributeNotFoundException(relationAttributeId);
    }

    return relationAttribute.get();
  }

  public Boolean isRelationAttributesExistsByAttributeType (UUID attributeTypeId) {
    return relationAttributeRepository.isRelationAttributesExistsByAttributeType(attributeTypeId);
  }

  public void deleteAllByRelationIds (List<UUID> relationIds, User user) {
    relationAttributeRepository.deleteAllByRelationIds(relationIds, user.getUserId());
  }

  public UUID findFirstRelationAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue (UUID attributeTypeId, String value) {
    return relationAttributeRepository.findFirstRelationAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue(attributeTypeId, value);
  }

  public Boolean isRelationAttributesExistsByRelationTypeId (UUID relationTypeId) {
    return relationAttributeRepository.isRelationAttributesExistsByRelationTypeId(relationTypeId);
  }
}
