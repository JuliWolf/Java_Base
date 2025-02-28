package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * @author juliwolf
 */

public class TestUtils {
  public static final ObjectMapper MAPPER = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

  /**
   * Reads all content from a file and deserializes it into object of type T.<br/>
   * <b>Note</b>: doesn't work if T is a composite type like T = List&lt;S&gt;
   *
   * @param path  relative path to the file in the folder {@code src/test/resources}
   * @param clazz the type of object to deserialize into
   * @return object of type T
   */
  @SneakyThrows
  public static <T> T readJson(String path, Class<T> clazz) {
    return MAPPER.readValue(readString(path), clazz);
  }

  /**
   * Reads all content from a file into a string.
   *
   * @param path relative path to the file in the folder {@code src/test/resources}
   * @return String representation of file
   */
  @SneakyThrows
  public static String readString(String path) {
    final var classLoader = TestUtils.class.getClassLoader();
    final var resource = Objects.requireNonNull(classLoader.getResource(path)).toURI();
    return Files.readString(Paths.get(resource));
  }
}
