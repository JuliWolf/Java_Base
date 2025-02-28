package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.RelationComponentAttribute;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.relationComponentAttributes.RelationComponentAttributeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.relationComponentAttributes.exceptions.RelationComponentAttributeNotFoundException;

/**
 * @author juliwolf
 */

@Service
public class RelationComponentAttributesDAO {
  @Autowired
  protected RelationComponentAttributeRepository relationComponentAttributeRepository;

  public RelationComponentAttribute findRelationComponentAttributeById (UUID relationComponentAttributeId,  Boolean isJoinFetch) throws RelationComponentAttributeNotFoundException {
    Optional<RelationComponentAttribute> relationComponentAttribute;

    if (isJoinFetch) {
      relationComponentAttribute = relationComponentAttributeRepository.findRelationComponentAttributeByIdWithJoinedTables(relationComponentAttributeId);
    } else {
      relationComponentAttribute = relationComponentAttributeRepository.findById(relationComponentAttributeId);
    }

    if (relationComponentAttribute.isEmpty()) {
      throw new RelationComponentAttributeNotFoundException(relationComponentAttributeId);
    }

    if (relationComponentAttribute.get().getIsDeleted()) {
      throw new RelationComponentAttributeNotFoundException(relationComponentAttributeId);
    }

    return relationComponentAttribute.get();
  }

  public Boolean isRelationComponentAttributesExistsByAttributeType (UUID attributeTypeId) {
    return relationComponentAttributeRepository.isRelationComponentAttributesExistsByAttributeType(attributeTypeId);
  }

  public Boolean isRelationComponentAttributesExistsByRelationTypeComponentId (UUID relationTypeComponentId) {
    return relationComponentAttributeRepository.isRelationComponentAttributesExistsByRelationTypeComponentId(relationTypeComponentId);
  }

  public void deleteAllByRelationIds (List<UUID> relationIds, User user) {
    relationComponentAttributeRepository.deleteAllByRelationIds(relationIds, user.getUserId());
  }

  public UUID findFirstRelationComponentAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue (UUID attributeTypeId, String value) {
    return relationComponentAttributeRepository.findFirstRelationComponentAttributeByAttributeTypeAndAttributeKindIsSingleOrMultipleContainsValue(attributeTypeId, value);
  }
}
