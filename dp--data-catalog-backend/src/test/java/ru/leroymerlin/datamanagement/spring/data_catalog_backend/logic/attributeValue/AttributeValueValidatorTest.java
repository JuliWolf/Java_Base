package ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue;

import java.sql.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeInvalidDataTypeException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueMaskValidationException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.logic.attributeValue.exceptions.AttributeValueNotAllowedException;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assetTypes.AssetTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.AttributeTypeAllowedValueRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.assignments.assetType.attributeTypes.AssetTypeAttributeTypeAssignmentRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.*;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

public class AttributeValueValidatorTest extends ServiceWithUserIntegrationTest {
  @Autowired
  private AttributeValueValidator attributeValueValidator;

  @Autowired
  private AssetTypeRepository assetTypeRepository;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;

  @Autowired
  private AssetTypeAttributeTypeAssignmentRepository assetTypeAttributeTypeAssignmentRepository;
  @Autowired
  private AttributeTypeAllowedValueRepository attributeTypeAllowedValueRepository;

  AssetType simpleAssetType;
  AssetType differentAssetType;
  AttributeType booleanAttributeType;
  AttributeType textAttributeType;
  AttributeType decimalAttributeType;
  AttributeType integerAttributeType;
  AttributeType dateAttributeType;
  AttributeType dateTimeAttributeType;
  AttributeType multipleListAttributeType;
  AttributeType singleValueListAttributeType;

  @BeforeAll
  public void prepareData () {
    simpleAssetType = assetTypeRepository.save(new AssetType("simple asset type", "some simple description", "sa", "blue", language, user));
    differentAssetType = assetTypeRepository.save(new AssetType("different asset type", "different desc", "da", "purple", language, user));

    booleanAttributeType = attributeTypeRepository.save(new AttributeType("boolean attribute type", "attribute with boolean type", AttributeKindType.BOOLEAN, null, null, language, user));
    decimalAttributeType = attributeTypeRepository.save(new AttributeType("decimal attribute type", "attribute with decimal type", AttributeKindType.DECIMAL, null, null, language, user));
    integerAttributeType = attributeTypeRepository.save(new AttributeType("integer attribute type", "attribute with integer type", AttributeKindType.INTEGER, null, null, language, user));
    dateAttributeType = attributeTypeRepository.save(new AttributeType("date attribute type", "attribute with date type", AttributeKindType.DATE, null, null,language, user));
    dateTimeAttributeType = attributeTypeRepository.save(new AttributeType("date time attribute type", "attribute with date type", AttributeKindType.DATE_TIME, null, null,language, user));
    textAttributeType = attributeTypeRepository.save(new AttributeType("text attribute type", "attribute with text type", AttributeKindType.TEXT, null, null, language, user));
    multipleListAttributeType = attributeTypeRepository.save(new AttributeType("multiple list attribute type", "attribute with multiple list type", AttributeKindType.MULTIPLE_VALUE_LIST, null, null,language, user));
    singleValueListAttributeType = attributeTypeRepository.save(new AttributeType("lonely attribute type", "attribute with empty assignments", AttributeKindType.SINGLE_VALUE_LIST, null, null,language, user));

    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(simpleAssetType, booleanAttributeType, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(simpleAssetType, decimalAttributeType, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(simpleAssetType, integerAttributeType, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(simpleAssetType, dateAttributeType, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(simpleAssetType, dateTimeAttributeType, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(simpleAssetType, textAttributeType, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(differentAssetType, multipleListAttributeType, user));
    assetTypeAttributeTypeAssignmentRepository.save(new AssetTypeAttributeTypeAssignment(differentAssetType, singleValueListAttributeType, user));

    attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(multipleListAttributeType, "1", language, user));
    attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(multipleListAttributeType, "2", language, user));
    attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(singleValueListAttributeType, "3", language, user));
    attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(singleValueListAttributeType, "4", language, user));
  }

  @AfterAll
  public void clearData () {
    attributeTypeAllowedValueRepository.deleteAll();
    assetTypeAttributeTypeAssignmentRepository.deleteAll();
    attributeTypeRepository.deleteAll();
    assetTypeRepository.deleteAll();
  }

  @Test
  public void validateValueTypeValidateBooleanTest () {
    assertAll(
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("true", booleanAttributeType.getAttributeTypeId(), null, AttributeKindType.BOOLEAN)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("false", booleanAttributeType.getAttributeTypeId(), null, AttributeKindType.BOOLEAN)),
      () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributeValueValidator.validateValueType("123", booleanAttributeType.getAttributeTypeId(), null, AttributeKindType.BOOLEAN))
    );
  }

  @Test
  public void validateValueTypeValidateDecimalTest () {
    assertAll(
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("10", decimalAttributeType.getAttributeTypeId(), null, AttributeKindType.DECIMAL)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("10.3", decimalAttributeType.getAttributeTypeId(), null, AttributeKindType.DECIMAL)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("0.3", decimalAttributeType.getAttributeTypeId(), null, AttributeKindType.DECIMAL)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("1000.34", decimalAttributeType.getAttributeTypeId(), null, AttributeKindType.DECIMAL)),
      () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributeValueValidator.validateValueType("dfgsdfg", decimalAttributeType.getAttributeTypeId(), null, AttributeKindType.DECIMAL)),
      () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributeValueValidator.validateValueType("23.fg", decimalAttributeType.getAttributeTypeId(), null, AttributeKindType.DECIMAL))
    );
  }

  @Test
  public void validateValueTypeValidateDateTest () {
    assertAll(
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("10", dateAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("103", dateAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("5673", dateAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("100034", dateAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE)),
      () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributeValueValidator.validateValueType("dfgsdfg", dateAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE)),
      () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributeValueValidator.validateValueType("23.fg", dateAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE))
    );
  }

  @Test
  public void validateValueTypeValidateDateTimeTest () {
    assertAll(
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("10", dateTimeAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE_TIME)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("103", dateTimeAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE_TIME)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("5673", dateTimeAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE_TIME)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("100034", dateTimeAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE_TIME)),
      () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributeValueValidator.validateValueType("dfgsdfg", dateTimeAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE_TIME)),
      () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributeValueValidator.validateValueType("23.fg", dateTimeAttributeType.getAttributeTypeId(), null, AttributeKindType.DATE_TIME))
    );
  }

  @Test
  public void validateValueTypeValidateIntegerTest () {
    assertAll(
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("10", integerAttributeType.getAttributeTypeId(), null, AttributeKindType.INTEGER)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("103", integerAttributeType.getAttributeTypeId(), null, AttributeKindType.INTEGER)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("5673", integerAttributeType.getAttributeTypeId(), null, AttributeKindType.INTEGER)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("100034", integerAttributeType.getAttributeTypeId(), null, AttributeKindType.INTEGER)),
      () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributeValueValidator.validateValueType("dfgsdfg", integerAttributeType.getAttributeTypeId(), null, AttributeKindType.INTEGER)),
      () -> assertThrows(AttributeInvalidDataTypeException.class, () -> attributeValueValidator.validateValueType("23.53", integerAttributeType.getAttributeTypeId(), null, AttributeKindType.INTEGER))
    );
  }

  @Test
  public void validateValueTypeValidateTextTest () {
    assertAll(
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("10", textAttributeType.getAttributeTypeId(), null, AttributeKindType.TEXT)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("103", textAttributeType.getAttributeTypeId(), "\\d+", AttributeKindType.TEXT)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("dfgsdfg", textAttributeType.getAttributeTypeId(), "\\w+", AttributeKindType.TEXT)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("100.34", textAttributeType.getAttributeTypeId(), "^([0-9]*[.])?[0-9]+$", AttributeKindType.TEXT)),
      () -> assertThrows(AttributeValueMaskValidationException.class, () -> attributeValueValidator.validateValueType("dfgsdfg", textAttributeType.getAttributeTypeId(), "^([0-9]*[.])?[0-9]+$", AttributeKindType.TEXT)),
      () -> assertThrows(AttributeValueMaskValidationException.class, () -> attributeValueValidator.validateValueType("23.53", textAttributeType.getAttributeTypeId(), "\\w+", AttributeKindType.TEXT))
    );
  }

  @Test
  public void validateValueTypeValidateSingleValueListTest () {
    assertAll(
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("3", singleValueListAttributeType.getAttributeTypeId(), null, AttributeKindType.SINGLE_VALUE_LIST)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("4", singleValueListAttributeType.getAttributeTypeId(), null, AttributeKindType.SINGLE_VALUE_LIST)),
      () -> assertThrows(AttributeValueNotAllowedException.class, () -> attributeValueValidator.validateValueType("5", singleValueListAttributeType.getAttributeTypeId(), null, AttributeKindType.SINGLE_VALUE_LIST)),
      () -> assertThrows(AttributeValueNotAllowedException.class, () -> attributeValueValidator.validateValueType("23.53", singleValueListAttributeType.getAttributeTypeId(), null, AttributeKindType.SINGLE_VALUE_LIST))
    );
  }

  @Test
  public void validateValueTypeValidateMultipleValueListTest () {
    assertAll(
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("1;2;", multipleListAttributeType.getAttributeTypeId(), null, AttributeKindType.MULTIPLE_VALUE_LIST)),
      () -> assertDoesNotThrow(() -> attributeValueValidator.validateValueType("2;", multipleListAttributeType.getAttributeTypeId(), null, AttributeKindType.MULTIPLE_VALUE_LIST)),
      () -> assertThrows(AttributeValueNotAllowedException.class, () -> attributeValueValidator.validateValueType("5", multipleListAttributeType.getAttributeTypeId(), null, AttributeKindType.MULTIPLE_VALUE_LIST)),
      () -> assertThrows(AttributeValueNotAllowedException.class, () -> attributeValueValidator.validateValueType("23.53", multipleListAttributeType.getAttributeTypeId(), null, AttributeKindType.MULTIPLE_VALUE_LIST))
    );
  }

  @Test
  public void setAttributeValueByTypeBooleanTest () {
    Attribute attribute = new Attribute(booleanAttributeType, null, language, user);

    attributeValueValidator.setAttributeValueByType(attribute, "true", AttributeKindType.BOOLEAN);

    assertAll(
      () -> assertTrue(attribute.getValueBoolean()),
      () -> assertEquals("true", attribute.getValue())
    );

    attributeValueValidator.setAttributeValueByType(attribute, "false", AttributeKindType.BOOLEAN);

    assertFalse(attribute.getValueBoolean());
  }

  @Test
  public void setAttributeValueByTypeDecimalTest () {
    Attribute attribute = new Attribute(decimalAttributeType, null, language, user);

    attributeValueValidator.setAttributeValueByType(attribute, "123.4", AttributeKindType.DECIMAL);

    assertAll(
      () -> assertEquals(123.4, attribute.getValueNumeric()),
      () -> assertEquals("123.4", attribute.getValue())
    );

    attributeValueValidator.setAttributeValueByType(attribute, "0.45", AttributeKindType.DECIMAL);

    assertEquals(0.45, attribute.getValueNumeric());
  }

  @Test
  public void setAttributeValueByTypeIntegerTest () {
    Attribute attribute = new Attribute(integerAttributeType, null, language, user);

    attributeValueValidator.setAttributeValueByType(attribute, "123", AttributeKindType.INTEGER);

    assertAll(
      () -> assertEquals(123, attribute.getValueNumeric()),
      () -> assertEquals("123", attribute.getValue())
    );

    attributeValueValidator.setAttributeValueByType(attribute, "435", AttributeKindType.INTEGER);

    assertEquals(435, attribute.getValueNumeric());
  }

  @Test
  public void setAttributeValueByTypeDateTest () {
    Attribute attribute = new Attribute(dateAttributeType, null, language, user);

    long mills = System.currentTimeMillis();

    attributeValueValidator.setAttributeValueByType(attribute, mills + "", AttributeKindType.DATE);

    assertAll(
      () -> assertEquals(new Timestamp(mills), attribute.getValueDatetime()),
      () -> assertEquals(new Timestamp(mills).getTime() + "", attribute.getValue())
    );

    long newMills = System.currentTimeMillis();

    attributeValueValidator.setAttributeValueByType(attribute, newMills + "", AttributeKindType.DATE);

    assertEquals(new Timestamp(newMills), attribute.getValueDatetime());
  }

  @Test
  public void setAttributeValueByTypeDateTimeTest () {
    Attribute attribute = new Attribute(dateTimeAttributeType, null, language, user);

    long mills = System.currentTimeMillis();

    attributeValueValidator.setAttributeValueByType(attribute, mills + "", AttributeKindType.DATE_TIME);

    assertAll(
      () -> assertEquals(new Timestamp(mills), attribute.getValueDatetime()),
      () -> assertEquals(new Timestamp(mills).getTime() + "", attribute.getValue())
    );

    long newMills = System.currentTimeMillis();

    attributeValueValidator.setAttributeValueByType(attribute, newMills + "", AttributeKindType.DATE_TIME);

    assertEquals(new Timestamp(newMills), attribute.getValueDatetime());
  }
}
