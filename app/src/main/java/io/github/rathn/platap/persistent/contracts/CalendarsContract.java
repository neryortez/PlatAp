package io.github.rathn.platap.persistent.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import io.github.rathn.platap.dto.Account;
import io.github.rathn.platap.persistent.DatabaseOpenHelper;
import io.github.rathn.platap.persistent.PersistentStorage;
import io.github.rathn.platap.persistent.contracts.TransactionsContract.TransactionEntry;
import io.github.rathn.platap.persistent.contracts.base.BaseContract;

//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;

public class CalendarsContract extends BaseContract {

    public static abstract class CalendarEntry implements BaseColumns {
        public static final String COLUMN_DELETED = "calendar_deleted";
        public static final String COLUMN_ID = "calendar_id";
        public static final String COLUMN_IS_DEFAULT = "calendar_is_default";
        public static final String COLUMN_NAME = "calendar_name";
        public static final String COLUMN_ORDER = "calendar_order";
        public static final String COLUMN_UPDATE_DATE = "calendar_update_date";
        public static final String COLUMN_USER_ID = "calendar_user_id";
        public static final String TABLE_NAME = "Calendars";

        public static String getTableName(boolean inMemory) {
            StringBuffer tableName = new StringBuffer();
            if (!inMemory) {
                tableName.append(DatabaseOpenHelper.LOCAL_DATABASE_PREFIX).append("");
            }
            tableName.append(TABLE_NAME);
            return tableName.toString();
        }
    }

    public static List<Account> getAll(SQLiteDatabase database, boolean inMemory) {
        List<Account> calendars = new ArrayList();
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM ").append(CalendarEntry.getTableName(inMemory));
        query.append(" WHERE ").append(CalendarEntry.COLUMN_DELETED).append(" = ").append(0);
        query.append(" AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        query.append(" ORDER BY ").append(CalendarEntry.COLUMN_ORDER).append(" ASC");
        Cursor cursor = database.rawQuery(query.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    calendars.add(getCalendar(cursor));
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return calendars;
    }

    public static List<Account> getAllWithBalance(SQLiteDatabase database) {
        List<Account> calendars = new ArrayList();
        StringBuffer query = new StringBuffer();
        query.append("SELECT c.*, SUM (t.").append(TransactionEntry.COLUMN_PRICE).append(") FROM ").append(CalendarEntry.TABLE_NAME).append(" c ");
        query.append("LEFT JOIN ").append(TransactionEntry.TABLE_NAME).append(" t ON t.").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = c.").append(CalendarEntry.COLUMN_ID);
        query.append(" AND t.").append(TransactionEntry.COLUMN_DELETED).append(" = 0").append(" AND t.").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = 0");
        query.append(" WHERE c.").append(CalendarEntry.COLUMN_DELETED).append(" = 0");
        query.append(" AND c.").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        query.append(" GROUP BY c.").append(CalendarEntry.COLUMN_ID);
        query.append(" ORDER BY c.").append(CalendarEntry.COLUMN_ORDER).append(" ASC");
        Cursor cursor = database.rawQuery(query.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    calendars.add(getCalendarWithBalance(cursor));
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return calendars;
    }

    public static String getFreeUserCalendarId(SQLiteDatabase database) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT ").append(CalendarEntry.COLUMN_ID).append(" FROM ").append(CalendarEntry.getTableName(false));
        query.append(" WHERE ").append(CalendarEntry.COLUMN_DELETED).append(" = ").append(0);
        query.append(" AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(0);
        Cursor cursor = database.rawQuery(query.toString(), null);
        String calendarId = null;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                calendarId = cursor.getString(0);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return calendarId;
    }

    public static String getDefaultAccountId(SQLiteDatabase database) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT ").append(CalendarEntry.COLUMN_ID).append(" FROM ").append(CalendarEntry.TABLE_NAME);
        query.append(" WHERE ").append(CalendarEntry.COLUMN_DELETED).append(" = ").append(0);
        query.append(" AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        query.append(" AND ").append(CalendarEntry.COLUMN_IS_DEFAULT).append(" = ").append(1);
        String accountId = null;
        Cursor cursor = database.rawQuery(query.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                accountId = cursor.getString(cursor.getColumnIndex(CalendarEntry.COLUMN_ID));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return accountId;
    }

    public static Account getCalendar(SQLiteDatabase database, String id) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT c.*, SUM (t.").append(TransactionEntry.COLUMN_PRICE).append(") FROM ").append(CalendarEntry.TABLE_NAME).append(" c ");
        query.append("LEFT JOIN ").append(TransactionEntry.TABLE_NAME).append(" t ON t.").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = c.").append(CalendarEntry.COLUMN_ID);
        query.append(" AND t.").append(TransactionEntry.COLUMN_DELETED).append(" = 0").append(" AND t.").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = 0");
        query.append(" WHERE c.").append(CalendarEntry.COLUMN_DELETED).append(" = 0 AND ").append(CalendarEntry.COLUMN_ID).append(" = '").append(id).append("'");
        query.append(" AND c.").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        query.append(" GROUP BY c.").append(CalendarEntry.COLUMN_ID);
        query.append(" ORDER BY c.").append(CalendarEntry.COLUMN_ORDER).append(" ASC");
        Cursor cursor = database.rawQuery(query.toString(), null);
        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }
        if (cursor.moveToFirst()) {
            return getCalendarWithBalance(cursor);
        }
        if (cursor.isClosed()) {
            return null;
        }
        cursor.close();
        return null;
    }

    public static Account getDefaultCalendar(SQLiteDatabase database) {
        return getCalendar(database, getDefaultAccountId(database));
    }

    public static String[] getAllCalendarIds(SQLiteDatabase database, boolean inMemory) {
        String[] calendarIds = null;
        StringBuffer query = new StringBuffer();
        query.append("SELECT ").append(CalendarEntry.COLUMN_ID).append(" FROM ").append(CalendarEntry.getTableName(inMemory));
        query.append(" WHERE ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId()).append(" AND ");
        query.append(CalendarEntry.COLUMN_DELETED).append(" = 0");
        query.append(" ORDER BY ").append(CalendarEntry.COLUMN_ORDER).append(" ASC");
        Cursor cursor = database.rawQuery(query.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            calendarIds = new String[cursor.getCount()];
            if (cursor.moveToFirst()) {
                int index = 0;
                while (!cursor.isAfterLast()) {
                    calendarIds[index] = cursor.getString(0);
                    index++;
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return calendarIds;
    }

//    public static JsonArray getSyncables(SQLiteDatabase database) {
//        JsonArray calendars = new JsonArray();
//        StringBuffer query = new StringBuffer();
//        query.append("SELECT * FROM ").append(CalendarEntry.TABLE_NAME);
//        query.append(" WHERE ").append(CalendarEntry.COLUMN_UPDATE_DATE).append(" > ").append(BaseContract.getModifiedLastSyncDate());
//        query.append(" AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
//        Cursor cursor = database.rawQuery(query.toString(), null);
//        if (cursor != null && cursor.getCount() > 0) {
//            if (cursor.moveToFirst()) {
//                while (!cursor.isAfterLast()) {
//                    calendars.add(getJsonObject(cursor));
//                    cursor.moveToNext();
//                }
//            }
//            if (!cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return calendars;
//    }

    public static long insert(SQLiteDatabase database, Account calendar, boolean inMemory) {
        calendar.setOrder(getHighestCalendarOrder(database) + 1);
        if (calendar.isDefault() && calendar.getOrder() != 1) {
            resetDefaultCalendarState(database, inMemory);
        }
        return database.insert(CalendarEntry.getTableName(inMemory), null, getContentValues(calendar));
    }

    public static void insert(SQLiteDatabase database, List<Account> calendars, boolean inMemory) {
        database.beginTransaction();
        try {
            for (Account calendar : calendars) {
                delete(database, calendar, inMemory);
                database.delete(CalendarEntry.getTableName(inMemory), "calendar_id = ? AND calendar_user_id = " + PersistentStorage.getUserId(), new String[]{calendar.getId()});
                insert(database, calendar, inMemory);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public static int update(SQLiteDatabase database, Account calendar, boolean inMemory) {
        StringBuffer query = new StringBuffer();
        query.append(CalendarEntry.COLUMN_ID).append(" = ?");
        return database.update(CalendarEntry.getTableName(inMemory), getContentValues(calendar), query.toString(), new String[]{calendar.getId()});
    }

    public static void updateBySettingNewDefault(SQLiteDatabase database, Account calendar, boolean inMemory) {
        calendar.setIsDefault(true);
        resetDefaultCalendarState(database, inMemory);
        update(database, calendar, inMemory);
        StringBuffer query = new StringBuffer();
        query.append(CalendarEntry.COLUMN_ID).append(" = ?");
        database.update(CalendarEntry.getTableName(inMemory), getContentValues(calendar), query.toString(), new String[]{calendar.getId()});
    }

    private static void resetDefaultCalendarState(SQLiteDatabase database, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(CalendarEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate()));
        values.put(CalendarEntry.COLUMN_IS_DEFAULT, Integer.valueOf(0));
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(CalendarEntry.COLUMN_IS_DEFAULT).append(" = ").append(1).append(" AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        database.update(CalendarEntry.getTableName(inMemory), values, whereClause.toString(), null);
    }

    public static void delete(SQLiteDatabase database, Account calendar, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(CalendarEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate()));
        values.put(CalendarEntry.COLUMN_DELETED, Integer.valueOf(1));
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(CalendarEntry.COLUMN_ID).append(" = ? AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = ?");
        database.update(CalendarEntry.getTableName(inMemory), values, whereClause.toString(), new String[]{calendar.getId(), Integer.toString(PersistentStorage.getUserId())});
    }

    public static int getHighestCalendarOrder(SQLiteDatabase database) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT MAX(").append(CalendarEntry.COLUMN_ORDER).append(") FROM ").append(CalendarEntry.TABLE_NAME);
        query.append(" WHERE ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        Cursor cursor = database.rawQuery(query.toString(), null);
        int highestOrder = Integer.MAX_VALUE;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                highestOrder = cursor.getInt(0);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return highestOrder;
    }

    public static void cloneDataForRegisteredUser(SQLiteDatabase database, boolean inMemory) {
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ").append(CalendarEntry.getTableName(inMemory)).append(" (");
        query.append(CalendarEntry.COLUMN_ID).append(", ");
        query.append(CalendarEntry.COLUMN_NAME).append(", ");
        query.append(CalendarEntry.COLUMN_IS_DEFAULT).append(", ");
        query.append(CalendarEntry.COLUMN_ORDER).append(", ");
        query.append(CalendarEntry.COLUMN_UPDATE_DATE).append(", ");
        query.append(CalendarEntry.COLUMN_USER_ID).append(", ");
        query.append(CalendarEntry.COLUMN_DELETED).append(") ");
        query.append("SELECT ");
        query.append(CalendarEntry.COLUMN_ID).append(" || '_");
        query.append(PersistentStorage.getUserId()).append("', ");
        query.append(CalendarEntry.COLUMN_NAME).append(", ");
        query.append(CalendarEntry.COLUMN_IS_DEFAULT).append(", ");
        query.append(CalendarEntry.COLUMN_ORDER).append(", ");
        query.append(BaseContract.getUpdateDate()).append(", ");
        query.append(PersistentStorage.getUserId()).append(", ").append(0);
        query.append(" FROM ").append(CalendarEntry.getTableName(inMemory));
        query.append(" WHERE ").append(CalendarEntry.COLUMN_USER_ID).append(" = 0");
        query.append(" AND ").append(CalendarEntry.COLUMN_DELETED).append(" = 0");
        database.execSQL(query.toString());
    }

    public static void deleteAllCalendarsOfTheCurrentUser(SQLiteDatabase database, boolean inMemory, long toDate) {
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId()).append(" AND ");
        whereClause.append(CalendarEntry.COLUMN_IS_DEFAULT).append(" = ").append(0);
        if (toDate > 0) {
            whereClause.append(" AND ").append(CalendarEntry.COLUMN_UPDATE_DATE).append(" < ").append(toDate);
        }
        database.delete(CalendarEntry.getTableName(inMemory), whereClause.toString(), null);
    }

    private static ContentValues getContentValues(Account calendar) {
        ContentValues values = new ContentValues();
        values.put(CalendarEntry.COLUMN_ID, calendar.getId());
        values.put(CalendarEntry.COLUMN_NAME, calendar.getName());
        values.put(CalendarEntry.COLUMN_IS_DEFAULT, Boolean.valueOf(calendar.isDefault()));
        values.put(CalendarEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate(calendar.getUpdateDate())));
        values.put(CalendarEntry.COLUMN_USER_ID, Integer.valueOf(PersistentStorage.getUserId()));
        values.put(CalendarEntry.COLUMN_ORDER, Integer.valueOf(calendar.getOrder()));
        values.put(CalendarEntry.COLUMN_DELETED, Integer.valueOf(calendar.getDeleted()));
        return values;
    }

    public static Account getCalendar(Cursor cursor) {
        boolean z = true;
        Account calendar = new Account();
        calendar.setId(cursor.getString(cursor.getColumnIndex(CalendarEntry.COLUMN_ID)));
        calendar.setName(cursor.getString(cursor.getColumnIndex(CalendarEntry.COLUMN_NAME)));
        if (cursor.getInt(cursor.getColumnIndex(CalendarEntry.COLUMN_IS_DEFAULT)) != 1) {
            z = false;
        }
        calendar.setIsDefault(z);
        calendar.setUserId(cursor.getInt(cursor.getColumnIndex(CalendarEntry.COLUMN_USER_ID)));
        calendar.setOrder(cursor.getInt(cursor.getColumnIndex(CalendarEntry.COLUMN_ORDER)));
        return calendar;
    }

    private static Account getCalendarWithBalance(Cursor cursor) {
        Account calendar = getCalendar(cursor);
        calendar.setBalance(cursor.getDouble(7));
        return calendar;
    }

//    private static JsonObject getJsonObject(Cursor cursor) {
//        JsonObject calendar = new JsonObject();
//        calendar.addProperty(CalendarEntry.COLUMN_ID, cursor.getString(cursor.getColumnIndex(CalendarEntry.COLUMN_ID)));
//        int deleted = cursor.getInt(cursor.getColumnIndex(CalendarEntry.COLUMN_DELETED));
//        if (deleted != 1) {
//            calendar.addProperty(CalendarEntry.COLUMN_NAME, cursor.getString(cursor.getColumnIndex(CalendarEntry.COLUMN_NAME)));
//            calendar.addProperty(CalendarEntry.COLUMN_IS_DEFAULT, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CalendarEntry.COLUMN_IS_DEFAULT))));
//            calendar.addProperty(CalendarEntry.COLUMN_USER_ID, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CalendarEntry.COLUMN_USER_ID))));
//            calendar.addProperty(CalendarEntry.COLUMN_ORDER, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CalendarEntry.COLUMN_ORDER))));
//        }
//        calendar.addProperty(CalendarEntry.COLUMN_UPDATE_DATE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CalendarEntry.COLUMN_UPDATE_DATE))));
//        calendar.addProperty(CalendarEntry.COLUMN_DELETED, Integer.valueOf(deleted));
//        return calendar;
//    }
}
