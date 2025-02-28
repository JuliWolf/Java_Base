package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author juliwolf
 */

@ExtendWith(MockitoExtension.class)
public class CollectionUtilsTest {
  @Test
  public void generateStringOfAbsentUUIDsTest () {
    UUID firstUUID = UUID.randomUUID();
    UUID secondUUID = UUID.randomUUID();
    UUID thirdUUID = UUID.randomUUID();
    UUID forthUUID = UUID.randomUUID();

    LinkedHashSet<UUID> firstSet = new LinkedHashSet<>();
    firstSet.add(firstUUID);
    firstSet.add(secondUUID);
    firstSet.add(thirdUUID);
    firstSet.add(forthUUID);

    LinkedHashSet<UUID> secondSet = new LinkedHashSet<>();
    secondSet.add(thirdUUID);
    secondSet.add(forthUUID);


    assertAll(
      () -> assertEquals(firstUUID + "," + secondUUID,
        CollectionUtils.generateStringOfAbsentUUIDs(
          firstSet,
          secondSet
        )),
      () -> assertEquals("",
        CollectionUtils.generateStringOfAbsentUUIDs(
          firstSet,
          firstSet
        ))
    );
  }

  @Test
  public void findFirstNotFoundValueTest () {
    UUID firstUUID = UUID.randomUUID();
    UUID secondUUID = UUID.randomUUID();
    UUID thirdUUID = UUID.randomUUID();
    UUID forthUUID = UUID.randomUUID();

    LinkedHashSet<UUID> firstSet = new LinkedHashSet<>();
    firstSet.add(firstUUID);
    firstSet.add(secondUUID);
    firstSet.add(thirdUUID);
    firstSet.add(forthUUID);

    LinkedHashSet<UUID> secondSet = new LinkedHashSet<>();
    secondSet.add(thirdUUID);
    secondSet.add(forthUUID);

    assertAll(
      () -> assertEquals(firstUUID, CollectionUtils
        .findFirstNotFoundValue(
          firstSet,
          secondSet
        )),
      () -> assertEquals(null, CollectionUtils
        .findFirstNotFoundValue(
          firstSet,
          firstSet
        ))
    );
  }

  @Test
  public void ffindFirstDuplicateTest () {
    UUID firstUUID = UUID.randomUUID();
    UUID secondUUID = UUID.randomUUID();
    UUID forthUUID = UUID.randomUUID();

    List<UUID> firstList = new ArrayList<>();
    firstList.add(firstUUID);
    firstList.add(secondUUID);
    firstList.add(firstUUID);
    firstList.add(forthUUID);

    assertAll(
      () -> assertEquals(firstUUID, CollectionUtils
        .findFirstDuplicate(
          firstList
        ))
    );
  }
}
