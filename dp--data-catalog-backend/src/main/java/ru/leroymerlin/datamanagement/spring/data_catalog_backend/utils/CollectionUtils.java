package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author juliwolf
 */

public class CollectionUtils {
  public static String generateStringOfAbsentUUIDs (Set<UUID> mainSet, Set<UUID> setToCompare) {
    return mainSet.stream()
      .filter(uuid -> !setToCompare.contains(uuid))
      .map(UUID::toString)
      .collect(Collectors.joining(","));
  }

  public static UUID findFirstNotFoundValue (Set<UUID> mainSet, Set<UUID> setToCompare) {
    return mainSet.stream()
      .filter(uuid -> !setToCompare.contains(uuid))
      .findFirst()
      .orElse(null);
  }

  public static <T> T findFirstDuplicate (List<T> uuidList) {
    Map<T, Long> idsCount = uuidList.stream()
      .collect(Collectors.groupingBy(uuid -> uuid, Collectors.counting()));

    Map.Entry<T, Long> firstEntry = idsCount.entrySet().stream()
      .filter(entry -> entry.getValue() > 1)
      .limit(1)
      .findFirst()
      .orElse(null);

    return firstEntry != null ? firstEntry.getKey() : null;
  }
}
