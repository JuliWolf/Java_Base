package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.models.TestObject;

import static org.junit.Assert.assertEquals;

/**
 * @author juliwolf
 */

@ExtendWith(MockitoExtension.class)
public class ObjectUtilsTest {
  @Test
  public void convertObjectToMapTest () {
    TestObject testObj = new TestObject("test name", 12, true);
    Map<String, Object> firstMap = new HashMap<>();
    firstMap.put("name", testObj.getName());
    firstMap.put("age", testObj.getAge());
    firstMap.put("isYoung", testObj.getIsYoung());

    assertEquals(
      firstMap,
      ObjectUtils.convertObjectToMap(new TestObject("test name", 12, true))
    );
  }

  @Test
  public void putAndComputeKeysTest () {
    Map<String, Integer> testMap = new HashMap<>();

    ObjectUtils.putAndComputeKeys(testMap, "firstKey");
    assertEquals("1", testMap.get("firstKey").toString());

    ObjectUtils.putAndComputeKeys(testMap, "firstKey");
    assertEquals("2", testMap.get("firstKey").toString());

    ObjectUtils.putAndComputeKeys(testMap, "secondKey");
    assertEquals("1", testMap.get("secondKey").toString());
  }
}
