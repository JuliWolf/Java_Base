package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author juliwolf
 */

public class OptionalUtils {
  public static <T> Optional<T> getOptionalFromField (Optional<T> value) {
    return Optional.ofNullable(value).orElse(Optional.empty());
  }

  public static <T> boolean isEmpty (Optional<T> value) {
    return value != null && value.isEmpty();
  }

  public static <T, Y> void doActionIfPresent (Optional<T> value, Consumer<? super Optional<T>> action) {
    Optional.ofNullable(value).ifPresent(action);
  }
}
