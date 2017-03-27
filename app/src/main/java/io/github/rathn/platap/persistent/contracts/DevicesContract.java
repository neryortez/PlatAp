package io.github.rathn.platap.persistent.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.List;
import java.util.UUID;

import io.github.rathn.platap.BuildConfig;
import io.github.rathn.platap.dto.Device;
import io.github.rathn.platap.persistent.DatabaseOpenHelper;
import io.github.rathn.platap.persistent.PersistentStorage;

//import com.crashlytics.android.Crashlytics;
//import org.askerov.dynamicgrid.BuildConfig;

public class DevicesContract {

    public static abstract class DeviceEntry implements BaseColumns {
        public static final String COLUMN_ID = "device_id";
        public static final String COLUMN_NAME = "device_name";
        public static final String COLUMN_USER_ID = "device_user_id";
        public static final String TABLE_NAME = "Devices";

        public static String getTableName(boolean inMemory) {
            StringBuffer tableName = new StringBuffer();
            if (!inMemory) {
                tableName.append(DatabaseOpenHelper.LOCAL_DATABASE_PREFIX).append("");
            }
            tableName.append(TABLE_NAME);
            return tableName.toString();
        }
    }

    public static Device getDevice(SQLiteDatabase database) {
        Device device = null;
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM ").append(DeviceEntry.TABLE_NAME);
        query.append(" WHERE ").append(DeviceEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        query.append(" AND ").append(DeviceEntry.COLUMN_ID).append(" = ?");
        Cursor cursor = database.rawQuery(query.toString(), new String[]{PersistentStorage.getDeviceId()});
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                device = getDevice(cursor);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return device;
    }

    public static String getFreeUserDeviceId(SQLiteDatabase database) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM ").append(DeviceEntry.TABLE_NAME);
        query.append(" WHERE ").append(DeviceEntry.COLUMN_USER_ID).append(" = 0");
        Cursor cursor = database.rawQuery(query.toString(), null);
        String deviceId = null;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                deviceId = getDevice(cursor).getId();
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return deviceId;
    }

    public static Device getDeviceForAuthentication(SQLiteDatabase database, boolean registering, String email) {
        Device device = getDevice(database);
        if (device != null) {
            return device;
        }
        StringBuffer errorMessageBuffer = new StringBuffer("Null device upon ");
        errorMessageBuffer.append(registering ? "REGISTRATION " : "LOGIN ");
        errorMessageBuffer.append(" for ").append(email);
        errorMessageBuffer.append(" userId=").append(PersistentStorage.getUserId());
        errorMessageBuffer.append(" deviceId=").append(PersistentStorage.getDeviceId());
//        Crashlytics.log(errorMessageBuffer.toString());
        if (!registering) {
            return device;
        }
        device = new Device();
        device.setId(UUID.randomUUID().toString());
        device.setName(BuildConfig.FLAVOR);
        device.setUserId(0);
        insert(database, device, true);
        insert(database, device, false);
        return device;
    }

    public static void deleteAllDevicesOfTheCurrentUser(SQLiteDatabase database, boolean inMemory) {
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(DeviceEntry.COLUMN_USER_ID).append(" =  ").append(PersistentStorage.getUserId());
        database.delete(DeviceEntry.getTableName(inMemory), whereClause.toString(), null);
    }

    public static long insert(SQLiteDatabase database, Device device, boolean inMemory) {
        return database.insert(DeviceEntry.getTableName(inMemory), null, getContentValues(device));
    }

    public static int update(SQLiteDatabase database, Device device, boolean inMemory) {
        new ContentValues().put(DeviceEntry.COLUMN_NAME, device.getName());
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(DeviceEntry.COLUMN_ID).append(" = ? AND ").append(DeviceEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        return database.update(DeviceEntry.getTableName(inMemory), getContentValues(device), whereClause.toString(), new String[]{device.getId()});
    }

    public static void insertOrUpdate(SQLiteDatabase database, List<Device> devices, boolean inMemory) {
        database.beginTransaction();
        try {
            for (Device device : devices) {
                database.delete(DeviceEntry.getTableName(inMemory), "device_id = ? AND device_user_id = " + PersistentStorage.getUserId(), new String[]{device.getId()});
                insert(database, device, inMemory);
                if (inMemory) {
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public static void cloneDataForRegisteredUser(SQLiteDatabase database, boolean inMemory) {
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ").append(DeviceEntry.getTableName(inMemory)).append(" (");
        query.append(DeviceEntry.COLUMN_ID).append(", ");
        query.append(DeviceEntry.COLUMN_NAME).append(", ");
        query.append(DeviceEntry.COLUMN_USER_ID).append(")");
        query.append(" SELECT ");
        query.append(DeviceEntry.COLUMN_ID).append(", ");
        query.append(DeviceEntry.COLUMN_NAME).append(", ");
        query.append(PersistentStorage.getUserId());
        query.append(" FROM ").append(DeviceEntry.getTableName(inMemory));
        query.append(" WHERE ").append(DeviceEntry.COLUMN_USER_ID).append(" = 0");
        database.execSQL(query.toString());
    }

    private static ContentValues getContentValues(Device device) {
        ContentValues values = new ContentValues();
        values.put(DeviceEntry.COLUMN_ID, device.getId());
        values.put(DeviceEntry.COLUMN_NAME, device.getName());
        values.put(DeviceEntry.COLUMN_USER_ID, Integer.valueOf(device.getUserId()));
        return values;
    }

    public static Device getDevice(Cursor cursor) {
        Device device = new Device();
        device.setId(cursor.getString(cursor.getColumnIndex(DeviceEntry.COLUMN_ID)));
        device.setName(cursor.getString(cursor.getColumnIndex(DeviceEntry.COLUMN_NAME)));
        device.setUserId(cursor.getInt(cursor.getColumnIndex(DeviceEntry.COLUMN_USER_ID)));
        return device;
    }
}
