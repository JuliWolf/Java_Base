package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import org.springframework.data.domain.Sort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author juliwolf
 */

@ExtendWith(MockitoExtension.class)
public class SortUtilsTest {
  @Test
  public void getSortTypeTest() {
    assertAll(
      () -> assertEquals(SortOrder.ASC, SortUtils.getSortType(null)),
      () -> assertEquals(SortOrder.DESC, SortUtils.getSortType(SortOrder.DESC))
    );
  }

  @Test
  public void getSortFieldTest() {
    assertAll(
      () -> assertEquals("default", SortUtils.getSortField(null, "default")),
      () -> assertEquals("Test", SortUtils.getSortField("Test", "default"))
    );
  }

  @Test
  public void getSortTest() {
    Sort defaultSort = Sort.by("default");
    Sort ascendingSort = Sort.by("test").ascending();
    Sort descendingSort = Sort.by("descending").descending();

    assertAll(
      () -> assertEquals(defaultSort, SortUtils.getSort(null, null, "default")),
      () -> assertEquals(ascendingSort, SortUtils.getSort(null, "test", "default")),
      () -> assertEquals(descendingSort, SortUtils.getSort(SortOrder.DESC, "descending", "default"))
    );
  }
}
