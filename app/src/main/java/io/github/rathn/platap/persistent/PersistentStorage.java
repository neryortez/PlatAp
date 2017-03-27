package io.github.rathn.platap.persistent;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Calendar;

import io.github.rathn.platap.R;
import io.github.rathn.platap.utils.DateTimeUtils;
import io.github.rathn.platap.utils.TypeUtils;

public class PersistentStorage {
    private static final String BALANCE_END = "balance_end";
    private static final String BALANCE_START = "balance_start";
    private static final String DAILY_REMINDER = "daily_reminder";
    private static final String DAILY_REMINDER_HOUR = "daily_reminder_hour";
    private static final String DAILY_REMINDER_MINUTE = "daily_reminder_minute";
    private static final String DEVICE_ID = "deviceId";
    private static final String INCEXP_END = "income_expense_end";
    private static final String INCEXP_START = "income_expense_start";
    private static final String LAST_RESET_DATE = "lastResetDate";
    private static final String LAST_SYNC_DATE = "lastSyncDate";
    private static final String NAVIGATION_VIEW_MODE = "navigationViewMode";
    private static final String PIN_CODE = "pin_code";
    private static final String PIN_STATE = "pin_state";
    private static final String PREFERENCES_NAME = "PreferenciasCompartidas";
    private static final String RUN_MODE = "runMode";
    private static final String SELECTED_ACCOUNTS = "selectedAccounts";
    private static final String SESSION = "session";
    private static final String SHOW_DASHBOARD_SIGN_UP = "shhowDashboardSignUp";
    private static final String SHOW_DECIMALS = "show_decimals";
    private static final String SHOW_FIRST_TIME_EXPERIENCE = "shouldShowFirstTimeExperience";
    private static final String SUBSCRIPTION_END_DATE = "subscriptionEndData";
    private static final String SUBSCRIPTION_VALIDITY = "subscriptionValidity";
    private static final String USER_EMAIL = "userEmail";
    private static final String USER_ID = "userId";
    private static final String WEEK_START_DAY = "weekStartDay";
    private static SharedPreferences sStorage;

    public static void init(Context appContext) {
        if (sStorage == null) {
            sStorage = appContext.getSharedPreferences(PREFERENCES_NAME, 0);
        }
    }

    public static void setRunMode(boolean isFree) {
        Editor editor = sStorage.edit();
        editor.putBoolean(RUN_MODE, isFree);
        editor.apply();
    }

    public static boolean isFreeVersionRunning() {
        return sStorage.getBoolean(RUN_MODE, false);
    }

    public static void setShowFirstTimeExperience(boolean show) {
        Editor editor = sStorage.edit();
        editor.putBoolean(SHOW_FIRST_TIME_EXPERIENCE, show);
        editor.apply();
    }

    public static boolean showFirstTimeExperiense() {
        return sStorage.getBoolean(SHOW_FIRST_TIME_EXPERIENCE, true);
    }

    public static String getDeviceId() {
        return sStorage.getString(DEVICE_ID, null);
    }

    public static void setDeviceId(String deviceId) {
        Editor editor = sStorage.edit();
        editor.putString(DEVICE_ID, deviceId);
        editor.apply();
    }

    public static void saveSession(String session) {
        Editor editor = sStorage.edit();
        editor.putString(SESSION, session);
        editor.apply();
    }

    public static String getSession() {
        return sStorage.getString(SESSION, null);
    }

    public static void saveUserId(int id) {
        Editor editor = sStorage.edit();
        editor.putInt(USER_ID, id);
        editor.apply();
    }

    public static int getUserId() {
        return sStorage.getInt(USER_ID, 0);
    }

    public static void saveUserEmail(String email) {
        Editor editor = sStorage.edit();
        editor.putString(USER_EMAIL, email);
        editor.apply();
    }

    public static String getUserEmail() {
        return sStorage.getString(USER_EMAIL, null);
    }

    public static void resetUserData() {
        Editor editor = sStorage.edit();
        editor.remove(SESSION);
        editor.remove(USER_EMAIL);
        editor.remove(USER_ID);
        editor.remove(RUN_MODE);
        editor.remove(SELECTED_ACCOUNTS);
        editor.commit();
    }

    public static void setSubscriptionValid(boolean isValid) {
        Editor editor = sStorage.edit();
        editor.putBoolean(SUBSCRIPTION_VALIDITY, isValid);
        editor.apply();
    }

    public static boolean isSubscriptionValid() {
        return sStorage.getBoolean(SUBSCRIPTION_VALIDITY, false);
    }

    public static void saveSubscriptionEndDate(long time) {
        Editor editor = sStorage.edit();
        editor.putLong(SUBSCRIPTION_END_DATE, time);
        editor.apply();
    }

    public static long getSubscriptionEndDate() {
        return sStorage.getLong(SUBSCRIPTION_END_DATE, 0);
    }

    public static long getLastSyncDate() {
        return sStorage.getLong("lastSyncDate_" + getUserId(), 0);
    }

    public static void setLastSyncDate(long time) {
        Editor editor = sStorage.edit();
        editor.putLong("lastSyncDate_" + getUserId(), time);
        editor.apply();
    }

    public static long getLastResetDate() {
        return sStorage.getLong("lastResetDate_" + getUserId(), 0);
    }

    public static void setLastResetDate(long time) {
        Editor editor = sStorage.edit();
        editor.putLong("lastResetDate_" + getUserId(), time);
        editor.apply();
    }

    public static void setStartDayOfWeek(int day) {
        if (day == 2 || day == 1) {
            Editor editor = sStorage.edit();
            editor.putInt(WEEK_START_DAY, day);
            editor.apply();
        }
    }

    public static int getStartDayOfWeek() {
        return sStorage.getInt(WEEK_START_DAY, 2);
    }

    public static String getStartDayNameOfWeek(Context ct) {
        if (getStartDayOfWeek() == 2) {
            return ct.getResources().getString(R.string.monday);
        }
        return ct.getResources().getString(R.string.sunday);
    }

    public static void setNavigationViewMode(int viewMode) {
        Editor editor = sStorage.edit();
        editor.putInt(NAVIGATION_VIEW_MODE, viewMode);
        editor.apply();
    }

    public static int getNavigationViewMode() {
        if (isFreeVersionRunning()) {
            return 0;
        }
        return sStorage.getInt(NAVIGATION_VIEW_MODE, 1);
    }

    public static void setSelectedCalendar(String[] selectedAccounts) {
        if (selectedAccounts != null && selectedAccounts.length > 0) {
            StringBuffer buffer = new StringBuffer();
            for (String calendarId : selectedAccounts) {
                buffer.append(calendarId).append(" ");
            }
            buffer.delete(buffer.length() - 1, buffer.length());
            Editor editor = sStorage.edit();
            editor.putString(SELECTED_ACCOUNTS, buffer.toString());
            editor.apply();
        }
    }

    public static String[] getSelectedAccounts() {
        String calendarIdsString = sStorage.getString(SELECTED_ACCOUNTS, null);
        if (TypeUtils.isEmpty(calendarIdsString)) {
            return null;
        }
        return calendarIdsString.split(" ");
    }

    public static void setDailyReminder(boolean isEnabled) {
        Editor editor = sStorage.edit();
        editor.putInt(DAILY_REMINDER, isEnabled ? 1 : 0);
        editor.apply();
    }

    public static boolean isDailyReminderEnabled() {
        return sStorage.getInt(DAILY_REMINDER, 1) == 1;
    }

    public static boolean hasDailyReminderEnabledKey() {
        return sStorage.contains(DAILY_REMINDER);
    }

    public static void setDailyReminderTime(int hourOfDay, int minute) {
        Editor editor = sStorage.edit();
        editor.putInt(DAILY_REMINDER_HOUR, hourOfDay);
        editor.putInt(DAILY_REMINDER_MINUTE, minute);
        editor.apply();
    }

    public static Calendar getDailyReminderTime() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, sStorage.getInt(DAILY_REMINDER_HOUR, 19));
        c.set(Calendar.MINUTE, sStorage.getInt(DAILY_REMINDER_MINUTE, 0));
        return c;
    }

    public static void setPinEnabled(boolean isEnabled) {
        Editor editor = sStorage.edit();
        editor.putInt(PIN_STATE, isEnabled ? 1 : 0);
        editor.apply();
    }

    public static boolean isPinEnabled() {
        return sStorage.getInt(PIN_STATE, 0) == 1;
    }

    public static void setPinCode(String pincode) {
        Editor editor = sStorage.edit();
        editor.putString(PIN_CODE, pincode);
        editor.apply();
    }

    public static String getPinCode() {
        return sStorage.getString(PIN_CODE, null);
    }

    public static void setShowDecimals(boolean show) {
        Editor editor = sStorage.edit();
        editor.putBoolean(SHOW_DECIMALS, show);
        editor.apply();
    }

    public static boolean shouldShowDecimals() {
        return sStorage.getBoolean(SHOW_DECIMALS, true);
    }

    public static void setShowDashboardSignUp(boolean show) {
        Editor editor = sStorage.edit();
        editor.putBoolean(SHOW_DASHBOARD_SIGN_UP, show);
        editor.apply();
    }

    public static boolean shouldShowDashboardSignUp() {
        return sStorage.getBoolean(SHOW_DASHBOARD_SIGN_UP, true);
    }

    public static void setBalanceChartStartDate(Calendar startDate) {
        Editor editor = sStorage.edit();
        editor.putLong(BALANCE_START, startDate.getTimeInMillis());
        editor.apply();
    }

    public static Calendar getBalanceChartStartDate() {
        Calendar def = Calendar.getInstance();
        def.add(Calendar.MONTH, -3);
        Calendar ret = Calendar.getInstance();
        ret.setTimeInMillis(sStorage.getLong(BALANCE_START, def.getTimeInMillis()));
        return DateTimeUtils.getFirstDateOfMonth(ret);
    }

    public static void setBalanceChartEndDate(Calendar endDate) {
        Editor editor = sStorage.edit();
        editor.putLong(BALANCE_END, endDate.getTimeInMillis());
        editor.apply();
    }

    public static Calendar getBalanceChartEndDate() {
        Calendar def = Calendar.getInstance();
        def.add(Calendar.MONTH, 2);
        Calendar ret = Calendar.getInstance();
        ret.setTimeInMillis(sStorage.getLong(BALANCE_END, def.getTimeInMillis()));
        return DateTimeUtils.getLastDateOfMonth(ret);
    }

    public static void setIncExpStartDate(Calendar startDate) {
        Editor editor = sStorage.edit();
        editor.putLong(INCEXP_START, startDate.getTimeInMillis());
        editor.apply();
    }

    public static Calendar getIncExpStartDate() {
        Calendar def = Calendar.getInstance();
        def.add(Calendar.MONTH, -3);
        Calendar ret = Calendar.getInstance();
        ret.setTimeInMillis(sStorage.getLong(INCEXP_START, def.getTimeInMillis()));
        return DateTimeUtils.getFirstDateOfMonth(ret);
    }

    public static void setIncExpEndDate(Calendar endDate) {
        Editor editor = sStorage.edit();
        editor.putLong(INCEXP_END, endDate.getTimeInMillis());
        editor.apply();
    }

    public static Calendar getIncExpEndDate() {
        Calendar def = Calendar.getInstance();
        def.add(Calendar.MONTH, 2);
        Calendar ret = Calendar.getInstance();
        ret.setTimeInMillis(sStorage.getLong(INCEXP_END, def.getTimeInMillis()));
        return DateTimeUtils.getLastDateOfMonth(ret);
    }

    public static void resetAllData() {
        Editor editor = sStorage.edit();
        editor.clear();
        editor.apply();
    }
}
