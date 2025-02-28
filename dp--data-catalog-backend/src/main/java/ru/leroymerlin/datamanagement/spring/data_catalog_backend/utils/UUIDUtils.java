package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.util.UUID;

/**
 * @author juliwolf
 */

public class UUIDUtils {
  public static boolean isValidUUID (String value) {
    try {
      UUID.fromString(value);

      return true;
    } catch (IllegalArgumentException illegalArgumentException) {
      return false;
    }
  }

  public static String convertUUIDToString (UUID value) {
    return value != null ? value.toString() : null;
  }
}
