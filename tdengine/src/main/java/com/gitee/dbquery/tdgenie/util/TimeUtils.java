package com.gitee.dbquery.tdgenie.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * @author 风一样的码农
 * @since 2024/2/21
 **/
public class TimeUtils {
    public static Long LocalDateToLong(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
//        long stamp = instant.toEpochMilli() / 1000;
        return instant.toEpochMilli();
    }
}
