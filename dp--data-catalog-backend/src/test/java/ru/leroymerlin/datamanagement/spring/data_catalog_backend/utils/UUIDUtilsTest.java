package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

@ExtendWith(MockitoExtension.class)
public class UUIDUtilsTest {
  @Test
  public void isValidUUIDTest () {
    assertAll(
      () -> assertFalse(UUIDUtils.isValidUUID("123")),
      () -> assertTrue(UUIDUtils.isValidUUID(UUID.randomUUID().toString()))
    );
  }
}
