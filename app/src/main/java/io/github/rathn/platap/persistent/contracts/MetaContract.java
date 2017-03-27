package io.github.rathn.platap.persistent.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
//import com.google.gson.JsonObject;
import io.github.rathn.platap.dto.Meta;
import io.github.rathn.platap.persistent.DatabaseOpenHelper;
import io.github.rathn.platap.persistent.PersistentStorage;
import io.github.rathn.platap.utils.DateTimeUtils;

public class MetaContract {

    public static abstract class MetaEntry implements BaseColumns {
        public static final String COLUMN_DB_CREATION_DATE = "meta_db_create_date";
        public static final String COLUMN_DB_VERSION = "meta_db_version";
        public static final String COLUMN_REPEATING_END = "meta_repeating_end";
        public static final String COLUMN_USER_ID = "meta_user_id";
        public static final String TABLE_NAME = "meta";

        public static String getLocalTable() {
            StringBuffer tableName = new StringBuffer();
            tableName.append(DatabaseOpenHelper.LOCAL_DATABASE_PREFIX).append("").append(TABLE_NAME);
            return tableName.toString();
        }
    }

    public static Meta getMetaData(SQLiteDatabase database) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM ").append(MetaEntry.getLocalTable()).append(" WHERE ").append(MetaEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        Cursor cursor = database.rawQuery(query.toString(), null);
        Meta metaData = null;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                metaData = getMetaDataFromCursor(cursor);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return metaData;
    }

    /*public static JsonObject getSyncable(SQLiteDatabase database) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM ").append(MetaEntry.getLocalTable()).append(" WHERE ").append(MetaEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        Cursor cursor = database.rawQuery(query.toString(), null);
        JsonObject meta = new JsonObject();
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                meta = getJsonObject(cursor);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return meta;
    }*/

    public static long insertDefaultMetaData(SQLiteDatabase database) {
        return database.insert(MetaEntry.getLocalTable(), null, getContentValues(new Meta(true)));
    }

    public static void updateRepeatingEndDate(SQLiteDatabase database, Meta meta) {
        if (meta != null && meta.getRepeatingEndDate() != null) {
            ContentValues values = new ContentValues();
            values.put(MetaEntry.COLUMN_REPEATING_END, Long.valueOf(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(meta.getRepeatingEndDate())));
            database.update(MetaEntry.getLocalTable(), values, null, null);
        }
    }

    private static ContentValues getContentValues(Meta metaData) {
        ContentValues values = new ContentValues();
        values.put(MetaEntry.COLUMN_DB_VERSION, Integer.valueOf(metaData.getDatabaseVersion()));
        values.put(MetaEntry.COLUMN_DB_CREATION_DATE, Long.valueOf(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(metaData.getDatabaseCreationDate())));
        values.put(MetaEntry.COLUMN_REPEATING_END, Long.valueOf(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(metaData.getRepeatingEndDate())));
        values.put(MetaEntry.COLUMN_USER_ID, Integer.valueOf(metaData.getUserId()));
        return values;
    }

    private static Meta getMetaDataFromCursor(Cursor cursor) {
        Meta metaData = new Meta(false);
        metaData.setDatabaseVersion(cursor.getInt(cursor.getColumnIndex(MetaEntry.COLUMN_DB_VERSION)));
        metaData.setDatabaseCreationDate(DateTimeUtils.getLocalDateFromServerSpecificGmtTime((long) cursor.getInt(cursor.getColumnIndex(MetaEntry.COLUMN_DB_CREATION_DATE))));
        metaData.setRepeatingEndDate(DateTimeUtils.getLocalDateFromServerSpecificGmtTime((long) cursor.getInt(cursor.getColumnIndex(MetaEntry.COLUMN_REPEATING_END))));
        metaData.setUserId(cursor.getInt(cursor.getColumnIndex(MetaEntry.COLUMN_USER_ID)));
        return metaData;
    }

    /*private static JsonObject getJsonObject(Cursor cursor) {
        JsonObject meta = new JsonObject();
        meta.addProperty(MetaEntry.COLUMN_DB_VERSION, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(MetaEntry.COLUMN_DB_VERSION))));
        meta.addProperty(MetaEntry.COLUMN_DB_CREATION_DATE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(MetaEntry.COLUMN_DB_CREATION_DATE))));
        meta.addProperty(MetaEntry.COLUMN_REPEATING_END, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(MetaEntry.COLUMN_REPEATING_END))));
        meta.addProperty(MetaEntry.COLUMN_USER_ID, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(MetaEntry.COLUMN_USER_ID))));
        return meta;
    }*/
}
