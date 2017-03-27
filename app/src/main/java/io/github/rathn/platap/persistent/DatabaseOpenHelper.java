package io.github.rathn.platap.persistent;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String ASSETS_FILE_PATH = "database/PlataP.sqlite";
    public static final String BACKUP_DATABASE_NAME = "Backup.sqlite";
    public static final String BACKUP_DATABASE_PREFIX = "";
    private static final int BUFFER_SIZE = 1024;
    private static final int DATABASE_VERSION = 1;
    public static final String LOCAL_DATABASE_NAME = "PlataP.sqlite";
    public static final String LOCAL_DATABASE_PREFIX = "";
    private static final String UPGRADE_DATABASE = "";
    private static DatabaseOpenHelper sIntance = null;
    private final Context mContext;

    public static DatabaseOpenHelper getInstance(Context context) {
        if (sIntance == null) {
            try {
                sIntance = new DatabaseOpenHelper(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sIntance;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    public DatabaseOpenHelper(Context context) throws IOException {
        super(context, LOCAL_DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
        if (!isDatabaseFileStored(context)) {
            copyDatabaseFromAssets(context);
        }
    }

    public void onCreate(SQLiteDatabase db) {
//        try {
//            copyDatabaseFromAssets(mContext);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(UPGRADE_DATABASE, new String[0]);
        onCreate(db);
    }

    public String getDatabaseFilePath(Context context, String databaseName) {
        return context.getDatabasePath(LOCAL_DATABASE_NAME).getPath();
    }

    private boolean isDatabaseFileStored(Context context) {
        return new File(getDatabaseFilePath(context, LOCAL_DATABASE_NAME)).exists();
    }

    private void copyDatabaseFromAssets(Context context) throws IOException {
        InputStream inputStream = context.getAssets().open(ASSETS_FILE_PATH);
        String localDatabasePath = getDatabaseFilePath(context, LOCAL_DATABASE_NAME);
        new File(localDatabasePath.replace("/"+LOCAL_DATABASE_NAME, UPGRADE_DATABASE)).mkdirs();
        OutputStream outputStream = new FileOutputStream(localDatabasePath);
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int length = inputStream.read(buffer);
            if (length > 0) {
                outputStream.write(buffer, 0, length);
            } else {
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                return;
            }
        }
    }
}
