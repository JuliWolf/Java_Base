package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import static ru.leroymerlin.datamanagement.spring.data_catalog_backend.constants.PaginationConstants.MAX_PAGE_SIZE;

/**
 * @author juliwolf
 */

public class PageableUtils {
  public static Integer getPageNumber (Integer pageNumber) {
    return pageNumber == null ? 0 : pageNumber;
  }

  public static Integer getPageNumber (Integer pageNumber, Integer startFrom) {
    if (pageNumber == null) return 0;

    if (startFrom == 0) return pageNumber;

    int page = pageNumber - startFrom;

    return Math.max(page, 0);
  }

  public static Integer getPageSize (Integer pageSize) {

    return getPageSize(pageSize, MAX_PAGE_SIZE);
  }

  public static Integer getPageSize (Integer pageSize, Integer defaultPageSize) {
    if (pageSize == null || pageSize > defaultPageSize) {
      return defaultPageSize;
    }

    return pageSize;
  }
}
