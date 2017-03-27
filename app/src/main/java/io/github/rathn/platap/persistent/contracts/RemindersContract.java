package io.github.rathn.platap.persistent.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import io.github.rathn.platap.dto.Reminder;
import io.github.rathn.platap.persistent.DatabaseOpenHelper;
import io.github.rathn.platap.persistent.contracts.CalendarsContract.CalendarEntry;
import io.github.rathn.platap.persistent.contracts.TransactionsContract.TransactionEntry;

//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;

public class RemindersContract {

    public static abstract class ReminderEntry implements BaseColumns {
        public static final String COLUMN_DELETED = "reminder_deleted";
        public static final String COLUMN_ID = "reminder_id";
        public static final String COLUMN_INTERVAL = "reminder_interval";
        public static final String COLUMN_TYPE = "reminder_type";
        public static final String COLUMN_UPDATE_DATE = "reminder_update_date";
        public static final String TABLE_NAME = "Reminders";

        public static String getTableName(boolean inMemory) {
            StringBuffer tableName = new StringBuffer();
            if (!inMemory) {
                tableName.append(DatabaseOpenHelper.LOCAL_DATABASE_PREFIX).append("");
            }
            tableName.append(TABLE_NAME);
            return tableName.toString();
        }
    }

    public static List<Reminder> getAll(SQLiteDatabase database) {
        List<Reminder> reminders = new ArrayList();
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM ").append(ReminderEntry.TABLE_NAME).append(" WHERE ").append(ReminderEntry.COLUMN_DELETED).append(" = 0");
        Cursor cursor = database.rawQuery(query.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    reminders.add(getReminder(cursor));
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return reminders;
    }

    public static void resetAllReminders(SQLiteDatabase database, boolean inMemory, String userID) {
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(ReminderEntry.COLUMN_ID).append(" IN (SELECT DISTINCT ").append(TransactionEntry.COLUMN_REMINDER_ID).append(" FROM ");
        whereClause.append(TransactionEntry.getTableName(inMemory)).append(" JOIN ").append(CalendarEntry.getTableName(inMemory)).append(" ON ");
        whereClause.append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = ").append(CalendarEntry.COLUMN_ID).append(" WHERE ");
        whereClause.append(CalendarEntry.COLUMN_USER_ID).append(" = ?").append(")");
        database.delete(ReminderEntry.TABLE_NAME, whereClause.toString(), new String[]{userID});
    }

//    public static JsonArray getSyncables(SQLiteDatabase database) {
//        JsonArray reminders = new JsonArray();
//        StringBuffer query = new StringBuffer();
//        query.append("SELECT * FROM ").append(ReminderEntry.TABLE_NAME);
//        query.append(" WHERE ").append(ReminderEntry.COLUMN_UPDATE_DATE).append(" > ").append(PersistentStorage.getLastSyncDate());
//        Cursor cursor = database.rawQuery(query.toString(), null);
//        if (cursor != null && cursor.getCount() > 0) {
//            if (cursor.moveToFirst()) {
//                while (!cursor.isAfterLast()) {
//                    reminders.add(getJsonObject(cursor));
//                    cursor.moveToNext();
//                }
//            }
//            if (!cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return reminders;
//    }

    public static long insert(SQLiteDatabase database, Reminder reminder, boolean inMemory) {
        return database.insert(ReminderEntry.getTableName(inMemory), null, getContentValues(reminder));
    }

    public static void insert(SQLiteDatabase database, List<Reminder> reminders, boolean inMemory) {
        database.beginTransaction();
        try {
            for (Reminder reminder : reminders) {
                database.insertWithOnConflict(ReminderEntry.getTableName(inMemory), null, getContentValues(reminder), 5);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public static int update(SQLiteDatabase database, Reminder reminder, boolean inMemory) {
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(ReminderEntry.COLUMN_ID).append(" = ?");
        return database.update(ReminderEntry.getTableName(inMemory), getContentValues(reminder), whereClause.toString(), new String[]{reminder.getId()});
    }

    public static int delete(SQLiteDatabase database, Reminder reminder, boolean inMemory) {
        return -1;
    }

    private static ContentValues getContentValues(Reminder reminder) {
        return new ContentValues();
    }

    private static Reminder getReminder(Cursor cursor) {
        Reminder reminder = new Reminder();
        reminder.setId(cursor.getString(0));
        reminder.setRepeatType(cursor.getInt(1));
        reminder.setInterval(cursor.getInt(2));
        return reminder;
    }
//
//    private static JsonObject getJsonObject(Cursor cursor) {
//        JsonObject repreminder = new JsonObject();
//        repreminder.addProperty(ReminderEntry.COLUMN_ID, cursor.getString(0));
//        int deleted = cursor.getInt(4);
//        if (deleted != 1) {
//            repreminder.addProperty(ReminderEntry.COLUMN_TYPE, Integer.valueOf(cursor.getInt(1)));
//            repreminder.addProperty(ReminderEntry.COLUMN_INTERVAL, Integer.valueOf(cursor.getInt(2)));
//        }
//        repreminder.addProperty(ReminderEntry.COLUMN_UPDATE_DATE, Integer.valueOf(cursor.getInt(3)));
//        repreminder.addProperty(RepeatInfoEntry.COLUMN_DELETED, Integer.valueOf(deleted));
//        return repreminder;
//    }
}
