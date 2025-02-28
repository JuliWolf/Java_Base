package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils.models.TestObject;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author juliwolf
 */

@ExtendWith(MockitoExtension.class)
public class OptionalUtilsTest {
  @Test
  public void getOptionalFromFieldTest () {
    TestObject testObj = new TestObject();
    testObj.setAge(60);

    assertAll(
      () -> assertEquals(Optional.of(60), OptionalUtils.getOptionalFromField(Optional.ofNullable(testObj.getAge()))),
      () -> assertEquals(Optional.empty(), OptionalUtils.getOptionalFromField(Optional.ofNullable(testObj.getIsYoung())))
    );
  }

  @Test
  public void isEmptyTest () {
    Optional<Integer> ageOptional = Optional.of(60);

    assertAll(
      () -> assertEquals(false, OptionalUtils.isEmpty(ageOptional)),
      () -> assertEquals(true, OptionalUtils.isEmpty(Optional.empty()))
    );
  }

  @Test
  public void doActionIfPresentTest () {
    TestObject testObj = new TestObject();
    testObj.setAge(60);

    OptionalUtils.doActionIfPresent(Optional.of(testObj.getAge()), count -> {
      testObj.setAge(1 + count.orElse(1));
    });
    assertEquals(61, testObj.getAge());

    testObj.setAge(null);
    OptionalUtils.doActionIfPresent(Optional.ofNullable(testObj.getAge()), count -> {
      testObj.setAge(1 + count.orElse(1));
    });
    assertEquals(2, testObj.getAge());
  }
}
