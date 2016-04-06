package org.apache.calcite.runtime;

import org.apache.calcite.avatica.util.DateTimeUtils;

/**
 * Created by amoghm on 4/6/16.
 */
public class HiveQlFunctions {
  /**
   * Converts timestamp string to unix date string
   * implements to_date builtin function of Hive.
   * @param timestamp String depicting timestamp
   * @return string containing unix date
   */
  public static String toDate(String timestamp) {
    long ts =
        DateTimeUtils.timestampStringToUnixDate(timestamp);
    int date = (int)(ts / 86400000L);
    int time = (int)(ts % 86400000L);
    if(time < 0) {
      --date;
    }
    return DateTimeUtils.unixDateToString(date);
  }
}
