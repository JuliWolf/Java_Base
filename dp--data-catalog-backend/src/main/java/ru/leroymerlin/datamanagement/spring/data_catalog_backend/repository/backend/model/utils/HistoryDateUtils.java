package ru.leroymerlin.datamanagement.spring.data_catalog_backend.repository.backend.model.utils;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * @author juliwolf
 */

public class HistoryDateUtils {
  public static java.sql.Timestamp getValidToDefaultTime () {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 3000);
    calendar.set(Calendar.DAY_OF_YEAR, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    return new Timestamp(calendar.getTime().getTime());
  }
}
