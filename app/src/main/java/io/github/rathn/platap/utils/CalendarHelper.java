package io.github.rathn.platap.utils;

//import hirondelle.date4j.DateTime;

import org.intelligentsia.hirondelle.date4j.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class CalendarHelper {
    public static SimpleDateFormat MMM = new SimpleDateFormat("MMM", Locale.ENGLISH);
    public static SimpleDateFormat MMMMMd = new SimpleDateFormat("MMMM d", Locale.ENGLISH);
    public static SimpleDateFormat MMMMd = new SimpleDateFormat("MMM d", Locale.ENGLISH);
    public static SimpleDateFormat MMMMyyyy = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    public static SimpleDateFormat MMM_yy = new SimpleDateFormat("MMM yy", Locale.ENGLISH);
    public static SimpleDateFormat MMMd = new SimpleDateFormat("MMM d", Locale.ENGLISH);
    public static SimpleDateFormat MMMyy = new SimpleDateFormat("MMM `yy", Locale.ENGLISH);
    public static SimpleDateFormat MMMyyyy = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
    public static SimpleDateFormat fulltime = new SimpleDateFormat("yyyy MMMM d H:mm a", Locale.ENGLISH);
    public static SimpleDateFormat mmss = new SimpleDateFormat("H:mm a", Locale.ENGLISH);
    public static SimpleDateFormat yy = new SimpleDateFormat("`yy", Locale.ENGLISH);
    public static SimpleDateFormat yyyMMMd = new SimpleDateFormat("yyyy MMMM d", Locale.ENGLISH);
    public static SimpleDateFormat yyyyMMddFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static ArrayList<DateTime> getFullWeeks(int month, int year, int startDayOfWeek) {
        int i;
        ArrayList<DateTime> datetimeList = new ArrayList();
        DateTime firstDateOfMonth = new DateTime(Integer.valueOf(year), Integer.valueOf(month), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
        DateTime lastDateOfMonth = firstDateOfMonth.getEndOfMonth();
        int weekdayOfFirstDate = firstDateOfMonth.getWeekDay().intValue();
        if (weekdayOfFirstDate < startDayOfWeek) {
            weekdayOfFirstDate += 7;
        }
        while (weekdayOfFirstDate > 0) {
            DateTime dateTime = firstDateOfMonth.minusDays(Integer.valueOf(weekdayOfFirstDate - startDayOfWeek));
            if (!dateTime.lt(firstDateOfMonth)) {
                break;
            }
            datetimeList.add(dateTime);
            weekdayOfFirstDate--;
        }
        for (i = 0; i < lastDateOfMonth.getDay().intValue(); i++) {
            datetimeList.add(firstDateOfMonth.plusDays(Integer.valueOf(i)));
        }
        int endDayOfWeek = startDayOfWeek - 1;
        if (endDayOfWeek == 0) {
            endDayOfWeek = 7;
        }
        if (lastDateOfMonth.getWeekDay().intValue() != endDayOfWeek) {
            i = 1;
            DateTime nextDay;
            do {
                nextDay = lastDateOfMonth.plusDays(Integer.valueOf(i));
                datetimeList.add(nextDay);
                i++;
            } while (nextDay.getWeekDay().intValue() != endDayOfWeek);
        }
        return datetimeList;
    }

    public static DateTime convertDateToDateTime(Date date) {
        DateTime dateTime = new DateTime(yyyyMMddFormat.format(date));
        return new DateTime(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
    }

    public static Date convertDateTimeToDate(DateTime dateTime) {
        Date date = null;
        try {
            date = getDateFromString(dateTime.format("YYYY-MM-DD"), null);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date getDateFromString(String dateString, String dateFormat) throws ParseException {
        SimpleDateFormat formatter;
        if (dateFormat == null) {
            formatter = yyyyMMddFormat;
        } else {
            formatter = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        }
        return formatter.parse(dateString);
    }

    public static DateTime getDateTimeFromString(String dateString, String dateFormat) {
        try {
            return convertDateToDateTime(getDateFromString(dateString, dateFormat));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> convertToStringList(ArrayList<DateTime> dateTimes) {
        ArrayList<String> list = new ArrayList();
        Iterator i$ = dateTimes.iterator();
        while (i$.hasNext()) {
            list.add(((DateTime) i$.next()).format("YYYY-MM-DD"));
        }
        return list;
    }

    public static String getShortStringFromCalendar(Calendar c) {
        MMMyyyy.setTimeZone(c.getTimeZone());
        return MMMyyyy.format(c.getTime());
    }

    public static String getShortMonthYearFromCalendar(Calendar c) {
        MMM_yy.setTimeZone(c.getTimeZone());
        return MMM_yy.format(c.getTime());
    }

    public static String getShorterStringFromCalendar(Calendar c) {
        MMMyy.setTimeZone(c.getTimeZone());
        return MMMyy.format(c.getTime());
    }

    public static String getLongStringFromCalendar(Calendar c) {
        MMMMyyyy.setTimeZone(c.getTimeZone());
        return MMMMyyyy.format(c.getTime());
    }

    public static String getMonthDayFromCalendar(Calendar c) {
        yyyMMMd.setTimeZone(c.getTimeZone());
        return yyyMMMd.format(c.getTime());
    }

    public static String getDateForChart(Calendar c) {
        MMMd.setTimeZone(c.getTimeZone());
        return MMMd.format(c.getTime());
    }

    public static String getMonthFromCalendar(Calendar c) {
        MMM.setTimeZone(c.getTimeZone());
        return MMM.format(c.getTime());
    }

    public static String getShortYearFromCalendar(Calendar c) {
        yy.setTimeZone(c.getTimeZone());
        return yy.format(c.getTime());
    }

    public static String getShortMonth(Calendar c) {
        MMMMd.setTimeZone(c.getTimeZone());
        return MMMMd.format(c.getTime());
    }

    public static String getLongMonth(Calendar c) {
        MMMMMd.setTimeZone(c.getTimeZone());
        return MMMMMd.format(c.getTime());
    }

    public static String getTimeFromCalendarInCurrentTimezone(Calendar c) {
        return mmss.format(c.getTime());
    }

    public static String getFullTimeString(Calendar c) {
        return fulltime.format(c.getTime());
    }
}
