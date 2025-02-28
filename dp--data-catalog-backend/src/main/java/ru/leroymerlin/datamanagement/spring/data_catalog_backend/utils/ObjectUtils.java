package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author juliwolf
 */

public class ObjectUtils {
  private static ObjectMapper mapper = new ObjectMapper();

  public static <T> Map<String, Object> convertObjectToMap (T request) {
    return mapper.convertValue(request, new TypeReference<Map<String, Object>>() {});
  }

  public static void putAndComputeKeys (Map<String, Integer> keysMap, String key) {
    if (key == null || StringUtils.isEmpty(key)) return;

    keysMap.compute(key, (_key, value) -> value == null ? 1 : value + 1);
  }

  public static void putAndComputeKeys (Map<AbstractMap.SimpleEntry<UUID, UUID>, Integer> keysMap, AbstractMap.SimpleEntry<UUID, UUID> key) {
    if (key == null) return;

    keysMap.compute(key, (_key, value) -> value == null ? 1 : value + 1);
  }
}
