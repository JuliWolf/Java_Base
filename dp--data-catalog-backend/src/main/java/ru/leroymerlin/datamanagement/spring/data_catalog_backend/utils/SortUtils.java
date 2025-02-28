package ru.leroymerlin.datamanagement.spring.data_catalog_backend.utils;

import org.springframework.data.domain.Sort;
import org.apache.commons.lang3.StringUtils;
import ru.leroymerlin.datamanagement.spring.data_catalog_backend.models.SortOrder;

/**
 * @author juliwolf
 */

public class SortUtils {
  public static SortOrder getSortType (SortOrder sortOrder) {
    if (sortOrder == null) return SortOrder.ASC;

    return sortOrder;
  }

  public static String getSortField (String sortField, String defaultSortField) {
    if (StringUtils.isEmpty(sortField)) return defaultSortField;

    return sortField;
  }

  public static Sort getSort (SortOrder _sortOrder, String _sortField, String defaultSortField) {
    SortOrder sortOrder = getSortType(_sortOrder);
    String sortField = getSortField(_sortField, defaultSortField);

    Sort sortBy = Sort.by(sortField);

    return sortOrder.equals(SortOrder.DESC) ? sortBy.descending() : sortBy.ascending();
  }

  public static Sort.Order getSortOrder (SortOrder _sortOrder, String _sortField, String defaultSortField) {
    SortOrder sortOrder = getSortType(_sortOrder);
    String sortField = getSortField(_sortField, defaultSortField);

    return sortOrder.equals(SortOrder.DESC) ? Sort.Order.desc(sortField) : Sort.Order.asc(sortField);
  }
}
