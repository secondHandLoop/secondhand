package com.secondhand.common.tool;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * jerry on 2015/12/2.
 */
@SuppressWarnings("all")
public class DateUtils {


    public static final String DAY = "yyyy-MM-dd";

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

    public static final String HH_MM_SS = "HH:mm:ss";
    public static final String HH_MM = "HH:mm";

    public static final String YYYY_MMDD = "yyyyMMdd";

    public static final String YY_MMDD = "yyMMdd";

    public static final String YYYY_MM = "yyyyMM";

    public static final String MONTH_S_PATTERN = "yyyy-MM";

    //unix 时间戳
    public static long now() {
        return System.currentTimeMillis() / 1000;
    }

    //将Date转换为时间戳long
    public static long dateToUnixtime(Date date) {
        return date.getTime() / 1000;
    }

    public static Date parse(String endTime) {
        return parse(endTime, "yyyy-MM-dd");
    }

    public static Date parse(String time, String pattern) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.parse(time);
        } catch (ParseException e) {
            return new Date();
        }
    }

    public static String dateString() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");
        return simpleDateFormat.format(date);
    }


    public static String dateString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(date);
    }

    public static String dateString(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public static String getUnixTimeString(Long unixTime) {
        Date date = new Date();
        date.setTime(unixTime * 1000);
        return DateUtils.dateToString(date, DAY);

    }

    public static String getUnixTimeString(Long unixTime, String pattern) {
        Date date = new Date();
        date.setTime(unixTime * 1000);
        return DateUtils.dateToString(date, pattern);

    }

    /**
     * 获取当前时间的最大时间
     *
     * @param dateTime
     * @return
     */
    public static DateTime getCurDayMaxDateTime(DateTime dateTime) {
        String dateString = dateTime.toString(DAY);
        return new DateTime(dateString).plusDays(1).minus(1);
    }


    /**
     * 获取当前时间的最小时间
     *
     * @param dateTime
     * @return
     */
    public static DateTime getCurDayMinDateTime(DateTime dateTime) {
        String dateString = dateTime.toString(DAY);
        return new DateTime(dateString);
    }

    public static Long getCurDayMinUnixTime(DateTime dateTime) {
        String dateString = dateTime.toString(DAY);
        return new DateTime(dateString).getMillis() / 1000;
    }

    /**
     * 获取当前时间的所在月的第一天
     * nowDate
     *
     * @return
     */
    public static DateTime getFirstDayOfMonth(DateTime nowDate) {
        DateTime dateTime = nowDate.dayOfMonth().withMinimumValue();
        return new DateTime(dateTime.toString(DAY));
    }

    public static String getMillisTimeString(Long millis, String pattern) {
        Date date = new Date();
        date.setTime(millis);
        return DateUtils.dateToString(date, pattern);

    }


    public static String formatDate(Date date, String pattern) {
        return dateToString(date, pattern);
    }

    public static String labelTime(Date time) {
        DateTime labelTime = new DateTime(time);
        DateTime currentTime = new DateTime();
        if (currentTime.minusMinutes(1).isBefore(labelTime)) {
            return "just now";
        }
        if (currentTime.minusHours(1).isBefore(labelTime)) {
            return (currentTime.getMillis() - labelTime.getMillis()) / 60000 + " minutes ago";
        }
        if (currentTime.getYear() == labelTime.getYear() && currentTime.getMonthOfYear() == labelTime.getMonthOfYear() && currentTime.getDayOfMonth() == labelTime.getDayOfMonth()) {
            return (currentTime.getHourOfDay() - labelTime.getHourOfDay()) + " hours ago";
        }

        if (currentTime.getYear() == labelTime.getYear() && currentTime.getMonthOfYear() == labelTime.getMonthOfYear() && (currentTime.getDayOfMonth() - labelTime.getDayOfMonth()) == 1) {
            return formatDate(labelTime.toDate(), "HH:mm") + " Yesterday";
        }
        return formatDate(labelTime.toDate(), "yyyy-MM-dd");
    }

    public static String dateToString(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    public static Date stringToDate(String dateStr) {
        return stringToDate(dateStr, YYYY_MM_DD_HH_MM_SS);
    }

    public static long stringToUnixTime(Object object) {
        if (Objects.isNull(object)) {
            return 0L;
        }
        String string = object.toString();
        if (string.length() == 16) {
            string = string + ":00";
        }
        try {
            String format = "yyyy-MM-dd HH:mm:ss.S";
            DateTime dateTime = DateTime.parse(string, DateTimeFormat.forPattern(format));
            return dateTime.getMillis() / 1000;
        } catch (Exception e) {
            try {
                String format = "yyyy-MM-dd HH:mm:ss";
                DateTime dateTime = DateTime.parse(string.replace("T", " "), DateTimeFormat.forPattern(format));
                return dateTime.getMillis() / 1000;
            } catch (Exception ex) {
                String format = "yyyy-MM-dd HH:mm:ss";
                DateTime dateTime = DateTime.parse(string.replace("T", " "), DateTimeFormat.forPattern(format));
                return dateTime.getMillis() / 1000;
            }
        }
    }

    public static long localDateTimeToUnixTime(Object object) {
        LocalDateTime createAt = (LocalDateTime) object;

        ZoneId zoneId = ZoneId.systemDefault();

        ZonedDateTime zonedDateTime = createAt.atZone(zoneId);

        Instant instant = zonedDateTime.toInstant();

        long timestamp = instant.toEpochMilli();

        return timestamp / 1000;
    }

    public static void main(String[] args) throws Exception {
        Date date = new Date();
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String formattedDate = sdf.format(date);
        System.out.println("旧版格式化结果: " + formattedDate);

        LocalDate localDate = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern(pattern));
        System.out.println(localDate);


    }

    public static Integer getIntDate(String dateStr) {
        return Integer.valueOf(dateStr.replace("-", ""));
    }

    public static Date stringToDate(String dateStr, String pattern) {
        Date date = null;
        try {
            date = new SimpleDateFormat(pattern).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public static String dayMove(String date, int len) {
        return dateMove(date, len, Calendar.DATE, YYYY_MM_DD_HH_MM_SS);
    }

    public static String dayMove(Date date, int len) {
        return dateMove(date, len, Calendar.DATE, YYYY_MM_DD_HH_MM_SS);
    }

    public static String dayMove(Date date, int len, String pattern) {
        return dateMove(date, len, Calendar.DATE, pattern);
    }

    public static String dayMove(String date, int len, String pattern) {
        return dateMove(date, len, Calendar.DATE, pattern);
    }

    private static String dateMove(String date, int len, int field, String pattern) {
        return dateMove(stringToDate(date, pattern), len, field, pattern);
    }

    private static String dateMove(Date date, int len, int field, String pattern) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, len);
        return dateToString(cal.getTime(), pattern);
    }


    public static String getNonce_str() {
        Date now = new Date();
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String bg = outFormat.format(now);
        bg.substring(8, bg.length());
        String end = String.valueOf(buildRandom(4));
        return bg + end;
    }

    public static int buildRandom(int length) {
        int num = 1;
        double random = Math.random();
        if (random < 0.1) {
            random = random + 0.1;
        }
        for (int i = 0; i < length; i++) {
            num = num * 10;
        }
        return (int) ((random * num));
    }

    public static Long getUnixTime(Object date) {
        if (date == null) {
            return 0L;
        }
        return new DateTime(date).getMillis() / 1000;
    }

    /**
     * 获取指定时间当月的开始时间
     *
     * @param year
     * @param month
     * @return
     */
    public static Date getBeginTime(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate localDate = yearMonth.atDay(1);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        ZonedDateTime zonedDateTime = startOfDay.atZone(ZoneId.of("Asia/Shanghai"));

        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * 获取指定时间当月的截止时间
     *
     * @param year
     * @param month
     * @return
     */
    public static Date getEndTime(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        LocalDateTime localDateTime = endOfMonth.atTime(23, 59, 59, 999);
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return Date.from(zonedDateTime.toInstant());
    }


    public static Date date() {
        return new Date();
    }

    public static DateTime of(long day) {
        DateTime dateTime = new DateTime(day);
        return dateTime;
    }
}
