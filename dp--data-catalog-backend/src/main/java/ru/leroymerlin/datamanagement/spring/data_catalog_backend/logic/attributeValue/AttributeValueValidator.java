package ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.models.AttributeTypeKind;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.AttributeTypesAllowedValuesDAO;

/**
 * @author juliwolf
 */

@Component
public class AttributeValueValidator {
  private final AttributeTypesAllowedValuesDAO attributeTypesAllowedValuesDAO;

  public AttributeValueValidator (AttributeTypesAllowedValuesDAO attributeTypesAllowedValuesDAO) {
    this.attributeTypesAllowedValuesDAO = attributeTypesAllowedValuesDAO;
  }

  public void validateValueType (
    String value,
    UUID attributeTypeId,
    String validationMask,
    AttributeKindType attributeKindType
  ) throws
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    AttributeValueMaskValidationException
  {
    validateValueType(value, attributeTypeId, validationMask, attributeKindType, null);
  }

  public void validateValueType (
    String value,
    UUID attributeTypeId,
    String validationMask,
    AttributeKindType attributeKindType,
    Map<String, Object> details
  ) throws
    AttributeInvalidDataTypeException,
    AttributeValueNotAllowedException,
    AttributeValueMaskValidationException
  {
    switch (attributeKindType) {
      case INTEGER, DATE, DATE_TIME -> {
        if (!Pattern.matches("^\\d+$", value)) {
          throw new AttributeInvalidDataTypeException(details);
        }
      }
      case BOOLEAN -> {
        if (!value.equals("false") && !value.equals("true")) {
          throw new AttributeInvalidDataTypeException(details);
        }
      }
      case DECIMAL -> {
        if (!Pattern.matches("^([0-9]*[.])?[0-9]+$", value)) {
          throw new AttributeInvalidDataTypeException(details);
        }
      }
      case TEXT -> {
        if (validationMask == null) return;

        if (!Pattern.matches(validationMask, value)) {
          throw new AttributeValueMaskValidationException(details);
        }
      }
      case SINGLE_VALUE_LIST -> {
        validateAttributeTypeAllowedValue(attributeTypeId, value, details);
      }
      case MULTIPLE_VALUE_LIST -> {
        String[] values = value.split(";");

        for (String str : values) {
          validateAttributeTypeAllowedValue(attributeTypeId, str, details);
        }
      }
    };
  }

  private void validateAttributeTypeAllowedValue (UUID attributeTypeId, String value, Map<String, Object> details) throws AttributeValueNotAllowedException {
    Boolean isExists = attributeTypesAllowedValuesDAO.isAllowedValueExistsByAttributeTypeIdAndValue(attributeTypeId, value);

    if (!isExists) {
      throw new AttributeValueNotAllowedException(details);
    }
  }

  public void setAttributeValueByType (AttributeTypeKind attribute, String value, AttributeKindType attributeKindType) {
    attribute.setValue(value);

    switch (attributeKindType) {
      case INTEGER -> {
        attribute.setIsInteger(true);
        attribute.setValueNumeric(Double.valueOf(value));
      }
      case DECIMAL -> {
        attribute.setIsInteger(false);
        attribute.setValueNumeric(Double.valueOf(value));
      }
      case BOOLEAN -> {
        attribute.setValueBoolean(value.equals("true"));
      }
      case DATE, DATE_TIME -> {
        long milliseconds = Long.parseLong(value);
        attribute.setValueDatetime(new Timestamp(milliseconds));
      }
    }
  }
}
