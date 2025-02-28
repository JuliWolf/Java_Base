package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.exceptions.AttributeTypeNotFoundException;

@Service
public class AttributeTypesDAO {

  protected final AttributeTypeRepository attributeTypeRepository;

  @Autowired
  public AttributeTypesDAO (AttributeTypeRepository attributeTypeRepository) {
    this.attributeTypeRepository = attributeTypeRepository;
  }

  public AttributeType findAttributeTypeById (UUID attributeTypeId, boolean isJoinFetch) throws AttributeTypeNotFoundException {
    Optional<AttributeType> attributeType;

    if (isJoinFetch) {
      attributeType = attributeTypeRepository.findAttributeTypeByAttributeTypeIdWithJoinedTables(attributeTypeId);
    } else {
      attributeType = attributeTypeRepository.findById(attributeTypeId);
    }

    if (attributeType.isEmpty()) {
      throw new AttributeTypeNotFoundException();
    }

    if (attributeType.get().getIsDeleted()) {
      throw new AttributeTypeNotFoundException();
    }

    return attributeType.get();
  }

  public List<AttributeType> findAttributeTypesByIds (List<UUID> attributeTypeIds) {
    return attributeTypeRepository.findAttributeTypesByIds(attributeTypeIds);
  }

  public boolean isAttributeTypeExists (UUID attributeTypeId) {
    return attributeTypeRepository.existsByAttributeTypeIdAndIsDeletedFalse(attributeTypeId);
  }

  public List<AttributeType> findAttributeTypesByAttributeIds (List<UUID> attributeIds) {
    return attributeTypeRepository.findAttributeTypesByAttributeIds(attributeIds);
  }
}
