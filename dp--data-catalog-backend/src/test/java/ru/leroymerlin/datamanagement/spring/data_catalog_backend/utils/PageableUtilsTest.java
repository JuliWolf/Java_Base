package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.PaginationConstants;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author juliwolf
 */

@ExtendWith(MockitoExtension.class)
public class PageableUtilsTest {
  @Test
  public void getPageNumberTest () {
    assertAll(
      () -> assertEquals(0, PageableUtils.getPageNumber(null)),
      () -> assertEquals(10, PageableUtils.getPageNumber(10)),
      () -> assertEquals(0, PageableUtils.getPageNumber(1, 1)),
      () -> assertEquals(0, PageableUtils.getPageNumber(1, 2)),
      () -> assertEquals(2, PageableUtils.getPageNumber(3, 1))
    );
  }

  @Test
  public void getPageSizeTest () {
    assertAll(
      () -> assertEquals(PaginationConstants.MAX_PAGE_SIZE, PageableUtils.getPageSize(null)),
      () -> assertEquals(10, PageableUtils.getPageSize(10)),
      () -> assertEquals(PaginationConstants.MAX_PAGE_SIZE, PageableUtils.getPageSize(PaginationConstants.MAX_PAGE_SIZE + 1)),
      () -> assertEquals(PaginationConstants.MAX_PAGE_SIZE + 1, PageableUtils.getPageSize(PaginationConstants.MAX_PAGE_SIZE + 1), PaginationConstants.MAX_PAGE_SIZE + 1)
    );
  }
}
