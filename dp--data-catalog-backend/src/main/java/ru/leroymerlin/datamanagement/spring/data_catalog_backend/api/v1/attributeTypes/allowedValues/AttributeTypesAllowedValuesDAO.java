package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.AttributeTypeAllowedValueRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.models.AttributeTypeAllowedValueWithAttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeTypeAllowedValue;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.User;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.exceptions.AttributeTypeAllowedValueNotFoundException;

/**
 * @author JuliWolf
 */
@Service
public class AttributeTypesAllowedValuesDAO {
  @Autowired
  private AttributeTypeAllowedValueRepository attributeTypeAllowedValueRepository;

  public AttributeTypeAllowedValue findAttributeTypeAllowedValueById (UUID attributeTypeAllowedValueId) throws AttributeTypeAllowedValueNotFoundException {
    Optional<AttributeTypeAllowedValue> attributeTypeAllowedValue = attributeTypeAllowedValueRepository.findById(attributeTypeAllowedValueId);

    if (attributeTypeAllowedValue.isEmpty()) {
      throw new AttributeTypeAllowedValueNotFoundException();
    }

    if (attributeTypeAllowedValue.get().getIsDeleted()) {
      throw new AttributeTypeAllowedValueNotFoundException();
    }

    return attributeTypeAllowedValue.get();
  }

  public List<AttributeTypeAllowedValueWithAttributeType> findAllByAttributeTypeId (List<UUID> attributeTypeIds) {
    return attributeTypeAllowedValueRepository.findAllByAttributeTypeId(attributeTypeIds);
  }

  public AttributeTypeAllowedValue saveAttributeTypeAllowedValue (AttributeTypeAllowedValue attributeTypeAllowedValue) {
    return attributeTypeAllowedValueRepository.save(attributeTypeAllowedValue);
  }

  public Integer countAttributeTypeAllowedValueByAttributeTypeId (UUID attributeTypeId) {
    return attributeTypeAllowedValueRepository.countAttributeTypeAllowedValuesByAttributeTypeId(attributeTypeId);
  }

  public Boolean isAllowedValueExistsByAttributeTypeIdAndValue (UUID attributeTypeId, String value) {
    return attributeTypeAllowedValueRepository.isExistsAttributeTypeAllowedValueByAttributeTypeIdAndValue(attributeTypeId, value);
  }

  public void deleteAllByAttributeTypeId (UUID attributeTypeId, User user) {
    attributeTypeAllowedValueRepository.deleteAllByAttributeTypeId(attributeTypeId, user.getUserId());
  }
}
