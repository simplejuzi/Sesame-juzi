package tkaxv7s.xposed.sesame.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Constanline
 * @since 2023/07/17
 */
public class TimeUtil {

    public static final DateFormat DATE_FORMAT = DateFormat.getDateInstance();
    public static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance();

    public static Boolean checkNowInTimeRange(String timeRange) {
        return checkInTimeRange(System.currentTimeMillis(), timeRange);
    }

    public static Boolean checkInTimeRange(Long timeMillis, String timeRange) {
        try {
            String[] timeRangeArray = timeRange.split("-");
            if (timeRangeArray.length == 2) {
                String min = timeRangeArray[0];
                String max = timeRangeArray[1];
                return isAfterOrCompareTimeStr(timeMillis, min) && isBeforeOrCompareTimeStr(timeMillis, max);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        return false;
    }

    public static Boolean isNowBeforeTimeStr(String beforeTimeStr) {
        return isBeforeTimeStr(System.currentTimeMillis(), beforeTimeStr);
    }

    public static Boolean isNowAfterTimeStr(String afterTimeStr) {
        return isAfterTimeStr(System.currentTimeMillis(), afterTimeStr);
    }

    public static Boolean isNowBeforeOrCompareTimeStr(String beforeTimeStr) {
        return isBeforeOrCompareTimeStr(System.currentTimeMillis(), beforeTimeStr);
    }

    public static Boolean isNowAfterOrCompareTimeStr(String afterTimeStr) {
        return isAfterOrCompareTimeStr(System.currentTimeMillis(), afterTimeStr);
    }

    public static Boolean isBeforeTimeStr(Long timeMillis, String beforeTimeStr) {
        Integer compared = isCompareTimeStr(timeMillis, beforeTimeStr);
        if (compared != null) {
            return compared < 0;
        }
        return false;
    }

    public static Boolean isAfterTimeStr(Long timeMillis, String afterTimeStr) {
        Integer compared = isCompareTimeStr(timeMillis, afterTimeStr);
        if (compared != null) {
            return compared > 0;
        }
        return false;
    }

    public static Boolean isBeforeOrCompareTimeStr(Long timeMillis, String beforeTimeStr) {
        Integer compared = isCompareTimeStr(timeMillis, beforeTimeStr);
        if (compared != null) {
            return compared <= 0;
        }
        return false;
    }

    public static Boolean isAfterOrCompareTimeStr(Long timeMillis, String afterTimeStr) {
        Integer compared = isCompareTimeStr(timeMillis, afterTimeStr);
        if (compared != null) {
            return compared >= 0;
        }
        return false;
    }

    public static Integer isCompareTimeStr(Long timeMillis, String compareTimeStr) {
        try {
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTimeInMillis(timeMillis);
            Calendar compareCalendar = getTodayCalendarByTimeStr(compareTimeStr);
            if (compareCalendar != null) {
                return timeCalendar.compareTo(compareCalendar);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        return null;
    }

    public static Calendar getTodayCalendarByTimeStr(String timeStr) {
        return getCalendarByTimeStr(null, timeStr);
    }

    public static Calendar getCalendarByTimeStr(Long timeMillis, String timeStr) {
        try {
            Calendar timeCalendar = getCalendarByTimeMillis(timeMillis);
            timeCalendar.set(Calendar.MILLISECOND, 0);
            int length = timeStr.length();
            switch (length) {
                case 6:
                    timeCalendar.set(Calendar.SECOND, Integer.parseInt(timeStr.substring(4)));
                    timeCalendar.set(Calendar.MINUTE, Integer.parseInt(timeStr.substring(2, 4)));
                    timeCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr.substring(0, 2)));
                    return timeCalendar;
                case 4:
                    timeCalendar.set(Calendar.SECOND, 0);
                    timeCalendar.set(Calendar.MINUTE, Integer.parseInt(timeStr.substring(2, 4)));
                    timeCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr.substring(0, 2)));
                    return timeCalendar;
                case 2:
                    timeCalendar.set(Calendar.SECOND, 0);
                    timeCalendar.set(Calendar.MINUTE, 0);
                    timeCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeStr.substring(0, 2)));
                    return timeCalendar;
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
        return null;
    }

    public static Calendar getCalendarByTimeMillis(Long timeMillis) {
        Calendar timeCalendar = Calendar.getInstance();
        if (timeMillis != null) {
            timeCalendar.setTimeInMillis(timeMillis);
        }
        return timeCalendar;
    }

    public static String getTimeStr(long ts) {
        return TIME_FORMAT.format(new java.util.Date(ts));
    }

    public static String getDateStr() {
        return getDateStr(0);
    }

    public static String getDateStr(int plusDay) {
        Calendar c = Calendar.getInstance();
        if (plusDay != 0) {
            c.add(Calendar.DATE, plusDay);
        }
        return DATE_FORMAT.format(c.getTime());
    }

    public static Calendar getToday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            Log.i("sleep error:");
            Log.printStackTrace(e);
        }
    }

    /**
     * 获取指定时间的周数
     * @param dateTime 时间
     * @return 当前年的第几周
     */
    public static int getWeekNumber(Date dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        // 设置周的第一天为周一
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

}
