package ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.api.v1.attributeTypes.allowedValues.AttributeTypesAllowedValuesDAO;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypeAllowedValues.AttributeTypeAllowedValueRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.attributeTypes.AttributeTypeRepository;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.AttributeTypeAllowedValue;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.enums.AttributeKindType;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.stub.ServiceWithUserIntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author JuliWolf
 */
public class AttributeTypesAllowedValuesDAOTest extends ServiceWithUserIntegrationTest {

  @Autowired
  private AttributeTypeAllowedValueRepository attributeTypeAllowedValueRepository;

  @Autowired
  private AttributeTypesAllowedValuesDAO attributeTypesAllowedValuesDAO;

  @Autowired
  private AttributeTypeRepository attributeTypeRepository;

  private AttributeType attributeType;

  @BeforeAll
  public void prepareAttributeType () {
    attributeType = attributeTypeRepository.save(new AttributeType(
      "test name",
      "test description",
      AttributeKindType.DECIMAL,
      null,
      null,
      language,
      user
    ));
  }

  @AfterAll
  public void clearPreparedValues () {
    attributeTypeRepository.deleteAll();
  }

  @AfterEach
  public void clearTables() {
    attributeTypeAllowedValueRepository.deleteAll();
  }

  @Test
  public void countAttributeTypeAllowedValueByAttributeTypeIdIntegrationTest () {
    assertEquals(0, attributeTypesAllowedValuesDAO.countAttributeTypeAllowedValueByAttributeTypeId(attributeType.getAttributeTypeId()));

    attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(attributeType, "some value", language, user));
    attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(attributeType, "some other value", language, user));
    attributeTypeAllowedValueRepository.save(new AttributeTypeAllowedValue(attributeType, "some last value", language, user));

    assertEquals(3, attributeTypesAllowedValuesDAO.countAttributeTypeAllowedValueByAttributeTypeId(attributeType.getAttributeTypeId()));
  }
}
