package io.github.rathn.platap.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import io.github.rathn.platap.BuildConfig;

public class DateTimeUtils {
    public static final int DAYS_IN_A_WEEK = 7;
    public static final long DAY_SWITCH = 86400000;
    public static final int MILLIS_SWITCH = 1000;
    public static final int YEAR_UPPER_LIMIT = 5;

    public static long getLocalTimeFromGmtTime(long time) {
        if (time == 0) {
            return 0;
        }
        long newTime = time - ((long) TimeZone.getDefault().getRawOffset());
        if (TimeZone.getDefault().inDaylightTime(new Date(time))) {
            return newTime - ((long) TimeZone.getDefault().getDSTSavings());
        }
        return newTime;
    }

    private static Calendar getLocalDateFromGmtTime(long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(getLocalTimeFromGmtTime(time));
        return date;
    }

    public static Calendar getLocalDateFromServerSpecificGmtTime(long time) {
        if (time == 0) {
            return null;
        }
        return getLocalDateFromGmtTime(1000 * time);
    }

    public static long getGmtTimeFromLocalTime(long time) {
        if (time == 0) {
            return 0;
        }
        long newTime = time + ((long) TimeZone.getDefault().getRawOffset());
        if (TimeZone.getDefault().inDaylightTime(new Date(time))) {
            return newTime + ((long) TimeZone.getDefault().getDSTSavings());
        }
        return newTime;
    }

    private static long getGmtTimeFromLocalDate(Calendar date) {
        return date == null ? 0 : getGmtTimeFromLocalTime(date.getTimeInMillis());
    }

    public static long getServerSpecificGmtTimeFromLocalDate(Calendar date) {
        return getGmtTimeFromLocalDate(date) / 1000;
    }

    public static Calendar getCurrentDateWithoutTime() {
        return getDateWithoutTime(Calendar.getInstance());
    }

    public static Calendar getDateWithoutTime(Calendar date) {
        if (date == null) {
            date = Calendar.getInstance();
        }
        Calendar dateWithoutTime = Calendar.getInstance();
        dateWithoutTime.set(Calendar.YEAR, date.get(Calendar.YEAR));
        dateWithoutTime.set(Calendar.MONTH, date.get(Calendar.MONTH));
        dateWithoutTime.set(Calendar.DATE, date.get(Calendar.DATE));
        dateWithoutTime.set(Calendar.HOUR_OF_DAY, 0);
        dateWithoutTime.set(Calendar.MINUTE, 0);
        dateWithoutTime.set(Calendar.SECOND, 0);
        dateWithoutTime.set(Calendar.MILLISECOND, 0);
        return dateWithoutTime;
    }

    public static long getTimeStampWithoutTime(Calendar date) {
        return getDateWithoutTime(date).getTimeInMillis();
    }

    public static String toString(long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        return toString(date);
    }

    public static String toTimedString(long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        return date.get(Calendar.YEAR) + "." + (date.get(Calendar.MONTH) + 1) + "." + date.get(Calendar.DATE) + " " + date.get(Calendar.HOUR) + ":" + date.get(Calendar.MINUTE) + ":" + date.get(Calendar.SECOND) + ":" + date.get(Calendar.MILLISECOND);
    }

    public static long getServerSpecificTime(Calendar date) {
        return date.getTimeInMillis() / 1000;
    }

    public static String toString(Calendar date) {
        if (date != null) {
            return date.get(Calendar.YEAR) + "." + (date.get(Calendar.MONTH) + 1) + "." + date.get(Calendar.DATE);
        }
        return BuildConfig.FLAVOR;
    }

    public static int weeksBetween(Calendar startDate, Calendar endDate) {
        return (int) ((endDate.getTimeInMillis() - startDate.getTimeInMillis()) / 604800000);
    }

    public static int monthsBetween(Calendar startDate, Calendar endDate) {
        return (((endDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR)) * 12) + endDate.get(Calendar.MONTH)) - startDate.get(Calendar.MONTH);
    }

    private static int yearsBetween(Calendar startDate, Calendar endDate) {
        return (((endDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR))));
    }

    private static int daysBetweenApprox(Calendar startDate, Calendar endDate){
        return ((endDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR)) * 365)
                + ((endDate.get(Calendar.MONTH) - startDate.get(Calendar.MONTH)) * 30)
                + (endDate.get(Calendar.DAY_OF_MONTH) - startDate.get(Calendar.DAY_OF_MONTH));
    }

    public static Calendar getDate(int year, int month, int day) {
        Calendar date = getFirstDateOfMonth(year, month);
        date.set(Calendar.DATE, day);
        return date;
    }

    public static Calendar getFirstDateOfMonth(Calendar date) {
        Calendar firstDate = getDateWithoutTime(Calendar.getInstance());
        firstDate.setTimeInMillis(date.getTimeInMillis());
        firstDate.set(Calendar.HOUR_OF_DAY, 0);
        firstDate.set(Calendar.MINUTE, 0);
        firstDate.set(Calendar.SECOND, 0);
        firstDate.set(Calendar.MILLISECOND, 0);
        firstDate.set(Calendar.DATE, firstDate.getActualMinimum(Calendar.DATE));
        return firstDate;
    }

    public static Calendar getFirstDateOfMonth(int year, int month) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.DATE, date.getActualMinimum(Calendar.DATE));
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date;
    }

    public static Calendar getLastDateOfMonth(Calendar date) {
        Calendar lastDate = getDateWithoutTime(Calendar.getInstance());
        lastDate.setTimeInMillis(date.getTimeInMillis());
        lastDate.set(Calendar.HOUR_OF_DAY, 23);
        lastDate.set(Calendar.MINUTE, 59);
        lastDate.set(Calendar.SECOND, 59);
        lastDate.set(Calendar.MILLISECOND, 999);
        lastDate.set(Calendar.DATE, lastDate.getActualMaximum(Calendar.DATE));
        return lastDate;
    }

    public static Calendar getLastDateOfMonth(int year, int month) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, month);
        date.set(Calendar.HOUR, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.set(Calendar.DATE, date.getActualMaximum(Calendar.DATE));
        return date;
    }

    public static Calendar clone(Calendar cal) {
        return getDateWithoutTime(cal);
    }

    public static boolean isCalendarsTheSameDay(Calendar lc, Calendar rc) {
        return lc.get(Calendar.YEAR) == rc.get(Calendar.YEAR) && lc.get(Calendar.MONTH) == rc.get(Calendar.MONTH) && lc.get(Calendar.DATE) == rc.get(Calendar.DATE);
    }

    public static Calendar getCalendarFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        return calendar;
    }

    public static Calendar getCalendarToMidnight(Calendar date) {
        Calendar lastDate = getDateWithoutTime(Calendar.getInstance());
        lastDate.setTimeInMillis(date.getTimeInMillis());
        lastDate.set(Calendar.HOUR_OF_DAY, 23);
        lastDate.set(Calendar.MINUTE, 59);
        lastDate.set(Calendar.SECOND, 59);
        lastDate.set(Calendar.MILLISECOND, 999);
        return lastDate;
    }

    public static boolean isCalendarsPasted(Calendar baseCalendar, Calendar comparingCalendar) {
        if(comparingCalendar.before(baseCalendar)){
            return true;
        }
        return false;
//
//        ((baseCalendar.get(Calendar.YEAR) - comparingCalendar.get(Calendar.YEAR)) * 365)
//                + ((baseCalendar.get(Calendar.MONTH) - comparingCalendar.get(Calendar.MONTH)) * 30)
//                + (baseCalendar.get(Calendar.DAY_OF_MONTH) - comparingCalendar.get(Calendar.DAY_OF_MONTH));
    }
}
