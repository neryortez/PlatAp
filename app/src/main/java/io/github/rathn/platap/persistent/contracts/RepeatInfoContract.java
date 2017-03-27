package io.github.rathn.platap.persistent.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import io.github.rathn.platap.utils.DateTimeUtils;
import io.github.rathn.platap.dto.RepeatInfo;
import io.github.rathn.platap.persistent.DatabaseOpenHelper;
import io.github.rathn.platap.persistent.PersistentStorage;
import io.github.rathn.platap.persistent.contracts.CalendarsContract.CalendarEntry;
import io.github.rathn.platap.persistent.contracts.TransactionsContract.TransactionEntry;
import io.github.rathn.platap.persistent.contracts.base.BaseContract;

//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.example.neriortez.dolarpajaro.utils.DateTimeUtils;

public class RepeatInfoContract extends BaseContract {

    public static abstract class RepeatInfoEntry implements BaseColumns {
        public static final String COLUMN_DELETED = "repeat_info_deleted";
        public static final String COLUMN_END_DATE = "repeat_info_end_date";
        public static final String COLUMN_ID = "repeat_info_id";
        public static final String COLUMN_INTERVAL = "repeat_info_interval";
        public static final String COLUMN_START_DATE = "repeat_info_start_date";
        public static final String COLUMN_TYPE = "repeat_info_type";
        public static final String COLUMN_UPDATE_DATE = "repeat_info_update_date";
        public static final String TABLE_NAME = "RepeatInfo";

        public static String getTableName(boolean inMemory) {
            StringBuffer tableName = new StringBuffer();
            if (!inMemory) {
                tableName.append(DatabaseOpenHelper.LOCAL_DATABASE_PREFIX).append("");
            }
            tableName.append(TABLE_NAME);
            return tableName.toString();
        }
    }

//    public static JsonArray getSyncables(SQLiteDatabase database) {
//        JsonArray repeatInfos = new JsonArray();
//        StringBuffer query = new StringBuffer();
//        query.append("SELECT DISTINCT ");
//        query.append(RepeatInfoEntry.COLUMN_ID).append(", ");
//        query.append(RepeatInfoEntry.COLUMN_TYPE).append(", ");
//        query.append(RepeatInfoEntry.COLUMN_INTERVAL).append(", ");
//        query.append(RepeatInfoEntry.COLUMN_END_DATE).append(", ");
//        query.append(RepeatInfoEntry.COLUMN_START_DATE).append(", ");
//        query.append(RepeatInfoEntry.COLUMN_UPDATE_DATE).append(", ");
//        query.append(RepeatInfoEntry.COLUMN_DELETED);
//        query.append(" FROM ").append(RepeatInfoEntry.TABLE_NAME);
//        query.append(" JOIN ").append(TransactionEntry.TABLE_NAME).append(" ON ").append(RepeatInfoEntry.COLUMN_ID).append(" = ").append(TransactionEntry.COLUMN_REPEAT_INFO_ID);
//        query.append(" JOIN ").append(CalendarEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = ").append(CalendarEntry.COLUMN_ID);
//        query.append(" WHERE ").append(RepeatInfoEntry.COLUMN_UPDATE_DATE).append(" > ").append(BaseContract.getModifiedLastSyncDate());
//        query.append(" AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
//        Cursor cursor = database.rawQuery(query.toString(), null);
//        if (cursor != null && cursor.getCount() > 0) {
//            if (cursor.moveToFirst()) {
//                while (!cursor.isAfterLast()) {
//                    repeatInfos.add(getJsonObject(cursor));
//                    cursor.moveToNext();
//                }
//            }
//            if (!cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return repeatInfos;
//    }

    public static List<RepeatInfo> getAllRepeatInfosForTheCurrentUser(SQLiteDatabase database, boolean inMemory) {
        List<RepeatInfo> repeatInfos = new ArrayList();
        StringBuffer query = new StringBuffer();
        query.append("SELECT * ");
        query.append(" FROM ").append(RepeatInfoEntry.getTableName(inMemory));
        query.append(" JOIN ").append(TransactionEntry.TABLE_NAME).append(" ON ").append(RepeatInfoEntry.COLUMN_ID).append(" = ").append(TransactionEntry.COLUMN_REPEAT_INFO_ID);
        query.append(" JOIN ").append(CalendarEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = ").append(CalendarEntry.COLUMN_ID);
        query.append(" AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        Cursor cursor = database.rawQuery(query.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    repeatInfos.add(getRepeatInfo(cursor));
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return repeatInfos;
    }

    public static long insert(SQLiteDatabase database, RepeatInfo info, boolean inMemory) {
        if (info != null) {
            return database.insert(RepeatInfoEntry.getTableName(inMemory), null, getContentValues(info));
        }
        return -1;
    }

    public static void insert(SQLiteDatabase database, List<RepeatInfo> infos, boolean inMemory) {
        database.beginTransaction();
        try {
            for (RepeatInfo info : infos) {
                delete(database, info.getId(), inMemory);
                database.delete(RepeatInfoEntry.getTableName(inMemory), "repeat_info_id = ?", new String[]{info.getId()});
                insert(database, info, inMemory);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public static int update(SQLiteDatabase database, RepeatInfo info, boolean inMemory) {
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(RepeatInfoEntry.COLUMN_ID).append(" = ?");
        return database.update(RepeatInfoEntry.getTableName(inMemory), getContentValues(info), whereClause.toString(), new String[]{info.getId()});
    }

    public static int delete(SQLiteDatabase database, String info, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(RepeatInfoEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate()));
        values.put(RepeatInfoEntry.COLUMN_DELETED, Integer.valueOf(1));
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(RepeatInfoEntry.COLUMN_ID).append(" = ?");
        return database.update(RepeatInfoEntry.getTableName(inMemory), values, whereClause.toString(), new String[]{info});
    }

    public static void deleteRepeatInfosForUser(SQLiteDatabase database, int userId, long toDate) {
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(RepeatInfoEntry.COLUMN_ID).append(" IN (SELECT DISTINCT ").append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(" FROM ");
        whereClause.append(TransactionEntry.getTableName(false)).append(" JOIN ").append(CalendarEntry.getTableName(false)).append(" ON ");
        whereClause.append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = ").append(CalendarEntry.COLUMN_ID).append(" WHERE ");
        whereClause.append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(userId).append(")");
        if (toDate > 0) {
            whereClause.append(" AND ").append(RepeatInfoEntry.COLUMN_UPDATE_DATE).append(" < ").append(toDate);
        }
        database.delete(RepeatInfoEntry.getTableName(false), whereClause.toString(), null);
    }

    public static void cloneDataForRegisteredUser(SQLiteDatabase database, boolean inMemory) {
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ").append(RepeatInfoEntry.getTableName(inMemory)).append(" (");
        query.append(RepeatInfoEntry.COLUMN_ID).append(", ");
        query.append(RepeatInfoEntry.COLUMN_TYPE).append(", ");
        query.append(RepeatInfoEntry.COLUMN_INTERVAL).append(", ");
        query.append(RepeatInfoEntry.COLUMN_END_DATE).append(", ");
        query.append(RepeatInfoEntry.COLUMN_START_DATE).append(", ");
        query.append(RepeatInfoEntry.COLUMN_UPDATE_DATE).append(", ");
        query.append(RepeatInfoEntry.COLUMN_DELETED).append(") ");
        query.append("SELECT ");
        query.append(RepeatInfoEntry.COLUMN_ID).append(" || '_").append(PersistentStorage.getUserId()).append("', ");
        query.append(RepeatInfoEntry.COLUMN_TYPE).append(", ");
        query.append(RepeatInfoEntry.COLUMN_INTERVAL).append(", ");
        query.append(RepeatInfoEntry.COLUMN_END_DATE).append(", ");
        query.append(RepeatInfoEntry.COLUMN_START_DATE).append(", ");
        query.append(BaseContract.getUpdateDate()).append(", ");
        query.append(0);
        query.append(" FROM ").append(RepeatInfoEntry.getTableName(inMemory));
        query.append(" WHERE ").append(RepeatInfoEntry.COLUMN_ID);
        query.append(" IN (SELECT ").append(TransactionEntry.COLUMN_REPEAT_INFO_ID);
        query.append(" FROM ").append(CalendarEntry.getTableName(inMemory));
        query.append(" JOIN ").append(TransactionEntry.getTableName(inMemory)).append(" ON ");
        query.append(CalendarEntry.COLUMN_ID).append(" = ").append(TransactionEntry.COLUMN_CALENDAR_ID);
        query.append(" WHERE ").append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(" = ").append(RepeatInfoEntry.COLUMN_ID);
        query.append(" AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = 0)");
        query.append(" AND ").append(RepeatInfoEntry.COLUMN_DELETED).append(" = 0");
        database.execSQL(query.toString());
    }

    private static ContentValues getContentValues(RepeatInfo info) {
        ContentValues values = new ContentValues();
        values.put(RepeatInfoEntry.COLUMN_ID, info.getId());
        values.put(RepeatInfoEntry.COLUMN_TYPE, info.getRepeatType());
        values.put(RepeatInfoEntry.COLUMN_INTERVAL, info.getInterval());
        values.put(RepeatInfoEntry.COLUMN_START_DATE, (int) DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(info.getStartDate()));
        values.put(RepeatInfoEntry.COLUMN_END_DATE, (int) DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(info.getEndDate()));
        values.put(RepeatInfoEntry.COLUMN_UPDATE_DATE, BaseContract.getUpdateDate(info.getUpdateDate()));
        values.put(RepeatInfoEntry.COLUMN_DELETED, info.getDeleted());
        return values;
    }

    public static RepeatInfo getRepeatInfo(Cursor cursor) {
        RepeatInfo reminder = new RepeatInfo();
        reminder.setId(cursor.getString(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_ID)));
        reminder.setRepeatType(cursor.getInt(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_TYPE)));
        reminder.setInterval(cursor.getInt(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_INTERVAL)));
        reminder.setStartDate(DateTimeUtils.getLocalDateFromServerSpecificGmtTime((long) cursor.getInt(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_START_DATE))));
        reminder.setEndDate(DateTimeUtils.getLocalDateFromServerSpecificGmtTime((long) cursor.getInt(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_END_DATE))));
        return reminder;
    }

//    private static JsonObject getJsonObject(Cursor cursor) {
//        JsonObject repeatInfo = new JsonObject();
//        repeatInfo.addProperty(RepeatInfoEntry.COLUMN_ID, cursor.getString(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_ID)));
//        int deleted = cursor.getInt(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_DELETED));
//        if (deleted != 1) {
//            repeatInfo.addProperty(RepeatInfoEntry.COLUMN_TYPE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_TYPE))));
//            repeatInfo.addProperty(RepeatInfoEntry.COLUMN_INTERVAL, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_INTERVAL))));
//            repeatInfo.addProperty(RepeatInfoEntry.COLUMN_END_DATE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_END_DATE))));
//            repeatInfo.addProperty(RepeatInfoEntry.COLUMN_START_DATE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_START_DATE))));
//        }
//        repeatInfo.addProperty(RepeatInfoEntry.COLUMN_UPDATE_DATE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(RepeatInfoEntry.COLUMN_UPDATE_DATE))));
//        repeatInfo.addProperty(RepeatInfoEntry.COLUMN_DELETED, Integer.valueOf(deleted));
//        return repeatInfo;
//    }
}
