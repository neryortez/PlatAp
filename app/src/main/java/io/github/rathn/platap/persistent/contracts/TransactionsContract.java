package io.github.rathn.platap.persistent.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.github.rathn.platap.BuildConfig;
import io.github.rathn.platap.dto.FacturasContract;
import io.github.rathn.platap.utils.DateTimeUtils;
import io.github.rathn.platap.dto.Transaction;
import io.github.rathn.platap.persistent.DatabaseOpenHelper;
import io.github.rathn.platap.persistent.PersistentStorage;
import io.github.rathn.platap.persistent.contracts.CalendarsContract.CalendarEntry;
import io.github.rathn.platap.persistent.contracts.CategoriesContract.CategoryEntry;
import io.github.rathn.platap.persistent.contracts.DevicesContract.DeviceEntry;
import io.github.rathn.platap.persistent.contracts.RemindersContract.ReminderEntry;
import io.github.rathn.platap.persistent.contracts.RepeatInfoContract.RepeatInfoEntry;
import io.github.rathn.platap.persistent.contracts.base.BaseContract;
import io.github.rathn.platap.utils.TypeUtils;

//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.example.neriortez.dolarpajaro.restful.RestfulConstants;
//import com.example.neriortez.dolarpajaro.utils.TypeUtils;
//import org.askerov.dynamicgrid.BuildConfig;

public class TransactionsContract extends BaseContract {

    public static abstract class TransactionEntry implements BaseColumns {
        public static final String COLUMN_CALENDAR_ID = "transaction_calendar_id";
        public static final String COLUMN_CATEGORY_ID = "transaction_category_id";
        public static final String COLUMN_DATE = "transaction_date";
        public static final String COLUMN_DELETED = "transaction_deleted";
        public static final String COLUMN_DEVICE_ID = "transaction_device_id";
        public static final String COLUMN_ID = "transaction_id";
        public static final String COLUMN_IS_EXPENSE = "transaction_is_expense";
        public static final String COLUMN_IS_FORECASTED = "transaction_is_forecasted";
        public static final String COLUMN_IS_REPEATING = "transaction_is_repeating";
        public static final String COLUMN_NOTE = "transaction_note";
        public static final String COLUMN_ORIGINAL_TRANSACTION_ID = "transaction_original_transaction_id";
        public static final String COLUMN_PRICE = "transaction_price";
        public static final String COLUMN_REMINDER_ID = "transaction_reminder_id";
        public static final String COLUMN_REPEAT_INFO_ID = "transaction_repeat_info_id";
        public static final String COLUMN_UPDATE_DATE = "transaction_update_date";
        public static final String TABLE_NAME = "Transactions";

        public static String getTableName(boolean inMemory) {
            StringBuffer tableName = new StringBuffer();
            if (!inMemory) {
                tableName.append(DatabaseOpenHelper.LOCAL_DATABASE_PREFIX).append("");
            }
            tableName.append(TABLE_NAME);
            return tableName.toString();
        }
    }

    public static List<Transaction> getTransactionsForTimeRange(SQLiteDatabase database, List<String> calendarId, Calendar startDate, Calendar endDate, int transactionType, String categoryID, int forecastType, boolean showPastedForecasted, boolean showTransfers) {
        List<Transaction> transactions = new ArrayList();
        StringBuffer where = new StringBuffer();
        where.append(" WHERE ");
        if (calendarId != null && calendarId.size() > 0) {
            where.append("( ");
            int calendarCount = calendarId.size();
            for (int i = 0; i < calendarCount; i++) {
                if (i == 0) {
                    where.append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = \"").append((String) calendarId.get(i)).append("\"");
                } else {
                    where.append(" OR ").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = '").append((String) calendarId.get(i)).append("'");
                }
            }
            where.append(" ) AND ");
        }
        where.append(TransactionEntry.COLUMN_DELETED).append(" = 0 AND ");
        if (showPastedForecasted || forecastType == 0) {
            where.append(getDateString(startDate, endDate));
            if (forecastType != -1) {
                where.append(" AND ").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = ").append(forecastType);
            }
        } else if (!showPastedForecasted && forecastType == -1) {
            Calendar today = DateTimeUtils.getCurrentDateWithoutTime();
            if (startDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) && startDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
                where.append(" (( ").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = ").append(1).append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" > ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(today));
                if (endDate != null) {
                    where.append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" <= ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(endDate));
                }
                where.append(" ) OR ");
                where.append(" ( ").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = ").append(0).append(" AND ");
                where.append(getDateString(startDate, endDate));
                where.append(" )) ");
            } else {
                where.append(getDateString(startDate, endDate));
            }
        } else if (!showPastedForecasted && forecastType == 1) {
            where.append(" ( ").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = ").append(1).append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" > ");
            where.append(BaseContract.getCurrentDate());
            if (endDate != null) {
                where.append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" <= ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(endDate));
            }
            where.append(" ) ");
        }
        if (transactionType != -1) {
            where.append(" AND ").append(TransactionEntry.COLUMN_IS_EXPENSE).append(" = ").append(transactionType);
        }
        if (categoryID != null) {
            where.append(" AND ").append(TransactionEntry.COLUMN_CATEGORY_ID).append(" = '").append(categoryID).append("' ");
        }
        if (!showTransfers) {
            where.append(" AND ").append(TransactionEntry.COLUMN_CATEGORY_ID).append(" NOT LIKE '%").append(CategoriesContract.TRANSFER_CATEGORY_ID).append("%' ");
        }
        StringBuffer actualQuery = new StringBuffer();
        actualQuery.append("SELECT * FROM ").append(TransactionEntry.getTableName(true));
        actualQuery.append(" JOIN ").append(CategoryEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_CATEGORY_ID).append(" = ").append(CategoryEntry.COLUMN_ID);
        actualQuery.append(" LEFT JOIN ").append(ReminderEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_REMINDER_ID).append(" = ").append(ReminderEntry.COLUMN_ID);
        actualQuery.append(" LEFT JOIN ").append(RepeatInfoEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(" = ").append(RepeatInfoEntry.COLUMN_ID);
        actualQuery.append(" LEFT JOIN ").append(DeviceEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_DEVICE_ID).append(" = ").append(DeviceEntry.COLUMN_ID);
        actualQuery.append(" INNER JOIN ").append(CalendarEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = ").append(CalendarEntry.COLUMN_ID);
        actualQuery.append(where.toString());
//        actualQuery.append(" AND ").append(DeviceEntry.COLUMN_USER_ID).append(" = ").append(CategoryEntry.COLUMN_USER_ID);
        actualQuery.append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" > 0");
        actualQuery.append(" ORDER BY ").append(TransactionEntry.COLUMN_DATE);
        Cursor cursor = database.rawQuery(actualQuery.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Transaction transaction = getDetailedTransaction(cursor);
                    transaction.setFacturas(FacturasContract.getFacturasForTransaction(database, transaction));
                    transactions.add(transaction);
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return transactions;
    }

    public static double getTodaysSpending(SQLiteDatabase database) {
        String[] ids = CalendarsContract.getAllCalendarIds(database, false);
        if (ids == null || ids.length == 0) {
            return 0.0d;
        }
        List<String> calendarId = Arrays.asList(CalendarsContract.getAllCalendarIds(database, false));
        StringBuffer where = new StringBuffer();
        where.append(" WHERE ");
        if (calendarId != null && calendarId.size() > 0) {
            where.append("( ");
            int calendarCount = calendarId.size();
            for (int i = 0; i < calendarCount; i++) {
                if (i == 0) {
                    where.append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = '").append((String) calendarId.get(i)).append("'");
                } else {
                    where.append(" OR ").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = '").append((String) calendarId.get(i)).append("'");
                }
            }
            where.append(" ) AND ");
        }
        where.append(TransactionEntry.COLUMN_DELETED).append(" = 0 AND ");
        where.append(getDateString(DateTimeUtils.getCurrentDateWithoutTime(), null));
        where.append(" AND ").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = ").append(0);
        where.append(" AND ").append(TransactionEntry.COLUMN_IS_EXPENSE).append(" = ").append(1);
        where.append(" AND ").append(TransactionEntry.COLUMN_CATEGORY_ID).append(" NOT LIKE '%").append(CategoriesContract.TRANSFER_CATEGORY_ID).append("%' ");
        StringBuffer actualQuery = new StringBuffer();
        actualQuery.append("SELECT * FROM ").append(TransactionEntry.TABLE_NAME);
        actualQuery.append(" JOIN ").append(CategoryEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_CATEGORY_ID).append(" = ").append(CategoryEntry.COLUMN_ID);
        actualQuery.append(" LEFT JOIN ").append(ReminderEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_REMINDER_ID).append(" = ").append(ReminderEntry.COLUMN_ID);
        actualQuery.append(" LEFT JOIN ").append(RepeatInfoEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(" = ").append(RepeatInfoEntry.COLUMN_ID);
        actualQuery.append(" LEFT JOIN ").append(DeviceEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_DEVICE_ID).append(" = ").append(DeviceEntry.COLUMN_ID);
        actualQuery.append(" INNER JOIN ").append(CalendarEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = ").append(CalendarEntry.COLUMN_ID);
        actualQuery.append(where.toString());
        actualQuery.append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" > 0");
        actualQuery.append(" AND ").append(DeviceEntry.COLUMN_USER_ID).append(" = ").append(CategoryEntry.COLUMN_USER_ID);
        List<Transaction> transactions = new ArrayList();
        Cursor cursor = database.rawQuery(actualQuery.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    transactions.add(getTransaction(cursor));
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        double spending = 0.0d;
        for (Transaction transaction : transactions) {
            spending += transaction.getPrice();
        }
        return spending;
    }

    private static String getDateString(Calendar startDate, Calendar endDate) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(BuildConfig.FLAVOR);
        if (endDate != null && startDate != null) {
            buffer.append(TransactionEntry.COLUMN_DATE).append(" >= ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(startDate)).append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" <= ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(endDate));
        } else if (startDate != null) {
            buffer.append(TransactionEntry.COLUMN_DATE).append(" = ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(startDate));
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(0);
            buffer.append(TransactionEntry.COLUMN_DATE).append(" >= ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(DateTimeUtils.getDateWithoutTime(calendar)));
        }
        return buffer.toString();
    }

    public static double getBalance(List<String> calendarId, SQLiteDatabase database, Calendar startDate, Calendar endDate, int transactionType, boolean showPastForecasted) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT SUM (").append(TransactionEntry.COLUMN_PRICE).append(") FROM ").append(TransactionEntry.TABLE_NAME).append(" WHERE ");
        if (calendarId != null && calendarId.size() > 0) {
            query.append("( ");
            int calendarCount = calendarId.size();
            for (int i = 0; i < calendarCount; i++) {
                if (i == 0) {
                    query.append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = '").append((String) calendarId.get(i)).append("'");
                } else {
                    query.append(" OR ").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = '").append((String) calendarId.get(i)).append("'");
                }
            }
            query.append(" ) AND ");
        }
        query.append(TransactionEntry.COLUMN_DELETED).append(" = 0 AND ");
        if (showPastForecasted) {
            query.append(TransactionEntry.COLUMN_DATE).append(" <= ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(endDate));
            if (startDate != null) {
                query.append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" >= ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(startDate));
            }
        } else {
            query.append(" (( ").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = ").append(1).append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" > ").append(BaseContract.getCurrentDate());
            query.append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" <= ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(endDate));
            query.append(" ) OR ");
            query.append(" ( ").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = ").append(0).append(" AND ");
            query.append(TransactionEntry.COLUMN_DATE).append(" <= ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(endDate));
            if (startDate != null) {
                query.append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" >= ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(startDate));
            }
            query.append(" )) ");
        }
        query.append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" > 0");
        if (transactionType != -1) {
            query.append(" AND ").append(TransactionEntry.COLUMN_IS_EXPENSE).append(" = ").append(transactionType);
        }
        Cursor cursor = database.rawQuery(query.toString(), null);
        double balance = 0.0d;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                balance = cursor.getDouble(0);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return balance;
    }

    public static double getBalance(List<String> calendarId, SQLiteDatabase database, Calendar endDate, boolean showPastForecasted) {
        return getBalance(calendarId, database, null, endDate, -1, showPastForecasted);
    }

    private static List<Transaction> getRepeatingTransactions(SQLiteDatabase database, String originalTransactionId) {
        List<Transaction> transactions = new ArrayList();
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM ").append(TransactionEntry.TABLE_NAME).append(" WHERE ").append(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID).append(" = '").append(originalTransactionId).append("'");
        Cursor cursor = database.rawQuery(query.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    transactions.add(getTransaction(cursor));
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return transactions;
    }

//    public static JsonArray getSyncables(SQLiteDatabase database) {
//        JsonArray transactions = new JsonArray();
//        StringBuffer query = new StringBuffer();
//        query.append("SELECT * FROM ").append(TransactionEntry.TABLE_NAME);
//        query.append(" JOIN ").append(CalendarEntry.TABLE_NAME).append(" ON ").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" = ").append(CalendarEntry.COLUMN_ID);
//        query.append(" WHERE ").append(TransactionEntry.COLUMN_UPDATE_DATE).append(" > ").append(BaseContract.getModifiedLastSyncDate());
//        query.append(" AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
//        Cursor cursor = database.rawQuery(query.toString(), null);
//        if (cursor != null && cursor.getCount() > 0) {
//            if (cursor.moveToFirst()) {
//                while (!cursor.isAfterLast()) {
//                    transactions.add(getJsonObject(cursor));
//                    cursor.moveToNext();
//                }
//            }
//            if (!cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return transactions;
//    }

    public static long insert(SQLiteDatabase database, Transaction transaction, boolean inMemory) {
        return database.insert(TransactionEntry.getTableName(inMemory), null, getContentValues(transaction));
    }

    public static void insert(SQLiteDatabase database, List<Transaction> transactions, boolean inMemory) {
        if (!TypeUtils.isEmpty((List) transactions)) {
            database.beginTransaction();
            try {
                int transactionCount = transactions.size();
                for (int index = 0; index < transactionCount; index++) {
                    int i;
                    Transaction transaction = (Transaction) transactions.get(index);
                    StringBuffer stmt = new StringBuffer();
                    stmt.append("INSERT OR REPLACE INTO ").append(TransactionEntry.getTableName(inMemory)).append(" (");
                    stmt.append(TransactionEntry.COLUMN_ID).append(", ");
                    stmt.append(TransactionEntry.COLUMN_NOTE).append(", ");
                    stmt.append(TransactionEntry.COLUMN_DATE).append(", ");
                    stmt.append(TransactionEntry.COLUMN_PRICE).append(", ");
                    stmt.append(TransactionEntry.COLUMN_IS_EXPENSE).append(", ");
                    stmt.append(TransactionEntry.COLUMN_IS_REPEATING).append(", ");
                    stmt.append(TransactionEntry.COLUMN_IS_FORECASTED).append(", ");
                    stmt.append(TransactionEntry.COLUMN_CALENDAR_ID).append(", ");
                    stmt.append(TransactionEntry.COLUMN_CATEGORY_ID).append(", ");
                    stmt.append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(", ");
                    stmt.append(TransactionEntry.COLUMN_REMINDER_ID).append(", ");
                    stmt.append(TransactionEntry.COLUMN_DEVICE_ID).append(", ");
                    stmt.append(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID).append(", ");
                    stmt.append(TransactionEntry.COLUMN_UPDATE_DATE).append(", ");
                    stmt.append(TransactionEntry.COLUMN_DELETED);
                    stmt.append(") VALUES (");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?, ");
                    stmt.append("?");
                    stmt.append(")");
                    String[] args = new String[15];
                    args[0] = transaction.getId();
                    args[1] = transaction.getNote();
                    args[2] = Long.toString(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(transaction.getDate()));
                    args[3] = Double.toString(transaction.getPrice());
                    args[4] = Integer.toString(transaction.isExpense() ? 1 : 0);
                    args[5] = Integer.toString(transaction.isRepeating() ? 1 : 0);
                    args[6] = Integer.toString(transaction.isForecasted() ? 1 : 0);
                    args[7] = transaction.getCalendarId();
                    args[8] = transaction.getCategoryId();
                    args[9] = transaction.getRepeatInfoId();
                    args[10] = transaction.getReminderId();
                    args[11] = transaction.getDeviceId();
                    args[12] = transaction.getOriginalTransactionId();
                    args[13] = Long.toString(BaseContract.getUpdateDate(transaction.getUpdateDate()));
                    args[14] = Integer.toString(transaction.getDeleted());
                    database.execSQL(stmt.toString(), args);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    }

    public static int update(SQLiteDatabase database, Transaction transaction, boolean inMemory) {
        if (transaction == null || transaction.getId() == null) {
            return 0;
        }
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(TransactionEntry.COLUMN_ID).append(" = ?");
        return database.update(TransactionEntry.getTableName(inMemory), getContentValues(transaction), whereClause.toString(), new String[]{transaction.getId()});
    }

    public static void update(SQLiteDatabase database, List<Transaction> transactions, boolean inMemory) {
        if (!TypeUtils.isEmpty((List) transactions)) {
            database.beginTransaction();
            try {
                StringBuffer whereClause = new StringBuffer();
                whereClause.append(TransactionEntry.COLUMN_ID).append(" = ? AND ");
                whereClause.append(TransactionEntry.COLUMN_DELETED).append(" != 1");
                int transactionCount = transactions.size();
                for (int index = 0; index < transactionCount; index++) {
                    Transaction transaction = (Transaction) transactions.get(index);
                    database.update(TransactionEntry.getTableName(inMemory), getContentValues(transaction), whereClause.toString(), new String[]{transaction.getId()});
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    }

    /**
     * Used to change the transaction that´ll be seen as the ORIGINAL on a list of repeating transactions.
     *
     * @param database The SQLite Database to be used.
     * @param originalTransaction The old original transaction.
     * @param inMemory Ya know... If tis or not in memory.
     */
    public static void changeOriginalTransaction(SQLiteDatabase database, Transaction originalTransaction, boolean inMemory) {
        List repeatingTransactions = getRepeatingTransactions(database, originalTransaction.getId());
        if (!repeatingTransactions.isEmpty()) {
            String newOriginalTransactionId = ((Transaction) repeatingTransactions.get(0)).getId();
            int transactionCount = repeatingTransactions.size();
            for (int index = 1; index < transactionCount; index++) {
                ((Transaction) repeatingTransactions.get(index)).setOriginalTransactionId(newOriginalTransactionId);
            }
            ((Transaction) repeatingTransactions.get(0)).setOriginalTransactionId(BuildConfig.FLAVOR);
            update(database, repeatingTransactions, inMemory);
        }
    }

    public static int resetForecast(SQLiteDatabase database, Transaction transaction, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(TransactionEntry.COLUMN_IS_FORECASTED, Integer.valueOf(0));
        values.put(TransactionEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate()));
        if (DateTimeUtils.getCurrentDateWithoutTime().getTimeInMillis() - transaction.getDate().getTimeInMillis() < 0) {
            values.put(TransactionEntry.COLUMN_DATE, Long.valueOf(BaseContract.getCurrentDate()));
            values.put(TransactionEntry.COLUMN_IS_REPEATING, Integer.valueOf(0));
            values.put(TransactionEntry.COLUMN_REPEAT_INFO_ID, BuildConfig.FLAVOR);
            values.put(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID, BuildConfig.FLAVOR);
        }
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(TransactionEntry.COLUMN_ID).append(" = ?");
        String[] whereArgs = new String[]{transaction.getId()};
        if (transaction == null || transaction.getId() == null) {
            return 0;
        }
        return database.update(TransactionEntry.getTableName(inMemory), values, whereClause.toString(), whereArgs);
    }

    public static void delete(SQLiteDatabase database, Transaction transaction, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(TransactionEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate()));
        values.put(TransactionEntry.COLUMN_DELETED, Integer.valueOf(1));
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(TransactionEntry.COLUMN_ID).append(" = ?");
        database.update(TransactionEntry.getTableName(inMemory), values, whereClause.toString(), new String[]{transaction.getId()});
    }

    public static void delete(SQLiteDatabase database, String categoryId, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(TransactionEntry.COLUMN_UPDATE_DATE, BaseContract.getUpdateDate());
        values.put(TransactionEntry.COLUMN_DELETED, 1);
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(TransactionEntry.COLUMN_CATEGORY_ID).append(" = ?");
        database.update(TransactionEntry.getTableName(inMemory), values, whereClause.toString(), new String[]{categoryId});
    }

    public static void deleteAllFromCalendar(SQLiteDatabase database, String calendarID) {
        List<Transaction> transactions = getTransactionsForTimeRange(database, Arrays.asList(new String[]{calendarID}), null, null, -1, null, -1, true, true);
        database.beginTransaction();
        try {
            for (Transaction transaction : transactions) {
                delete(database, transaction, true);
                delete(database, transaction, false);
                if (!TypeUtils.isEmpty(transaction.getRepeatInfoId())) {
                    RepeatInfoContract.delete(database, transaction.getRepeatInfoId(), true);
                    RepeatInfoContract.delete(database, transaction.getRepeatInfoId(), false);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public static int deleteRepeatingTransactions(SQLiteDatabase database, Transaction transaction, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(TransactionEntry.COLUMN_UPDATE_DATE, BaseContract.getUpdateDate());
        values.put(TransactionEntry.COLUMN_DELETED, 1);
        String originalTransactionId = transaction.isOriginal() ? transaction.getId() : transaction.getOriginalTransactionId();
        return database.update(TransactionEntry.getTableName(inMemory), values, TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID + " = ?", new String[]{originalTransactionId});
    }

    public static int deleteRepeatingTransactions(SQLiteDatabase database, Transaction transaction, Calendar fromDate, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(TransactionEntry.COLUMN_UPDATE_DATE, BaseContract.getUpdateDate());
        values.put(TransactionEntry.COLUMN_DELETED, 1);
        String whereClause = TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID + " = ? AND " +
                TransactionEntry.COLUMN_DATE + " >= ?";
        String originalTransactionId = transaction.isOriginal() ? transaction.getId() : transaction.getOriginalTransactionId();
        return database.update(TransactionEntry.getTableName(inMemory), values, whereClause, new String[]{originalTransactionId, Long.toString(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(fromDate))});
    }

    public static void deleteTransactionsForUser(SQLiteDatabase database, int userId, long toDate) {
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(TransactionEntry.COLUMN_CALENDAR_ID).append(" IN (SELECT DISTINCT ").append(CalendarEntry.COLUMN_ID);
        whereClause.append(" FROM ").append(CalendarEntry.getTableName(false)).append(" WHERE ").append(CalendarEntry.COLUMN_USER_ID).append(" = ").append(userId).append(")");
        if (toDate > 0) {
            whereClause.append(" AND ").append(TransactionEntry.COLUMN_UPDATE_DATE).append(" < ").append(toDate);
        }
        database.delete(TransactionEntry.getTableName(false), whereClause.toString(), null);
    }

    public static void cloneDataForRegisteredUser(SQLiteDatabase database, boolean inMemory) {
        int userId = PersistentStorage.getUserId();
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ").append(TransactionEntry.getTableName(inMemory)).append(" (");
        query.append(TransactionEntry.COLUMN_ID).append(", ");
        query.append(TransactionEntry.COLUMN_NOTE).append(", ");
        query.append(TransactionEntry.COLUMN_DATE).append(", ");
        query.append(TransactionEntry.COLUMN_PRICE).append(", ");
        query.append(TransactionEntry.COLUMN_IS_REPEATING).append(", ");
        query.append(TransactionEntry.COLUMN_IS_FORECASTED).append(", ");
        query.append(TransactionEntry.COLUMN_CATEGORY_ID).append(", ");
        query.append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(", ");
        query.append(TransactionEntry.COLUMN_IS_EXPENSE).append(", ");
        query.append(TransactionEntry.COLUMN_REMINDER_ID).append(", ");
        query.append(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID).append(", ");
        query.append(TransactionEntry.COLUMN_CALENDAR_ID).append(", ");
        query.append(TransactionEntry.COLUMN_DEVICE_ID).append(", ");
        query.append(TransactionEntry.COLUMN_UPDATE_DATE).append(", ");
        query.append(TransactionEntry.COLUMN_DELETED);
        query.append(") SELECT ");
        query.append(TransactionEntry.COLUMN_ID).append(" || '_").append(userId).append("', ");
        query.append(TransactionEntry.COLUMN_NOTE).append(", ");
        query.append(TransactionEntry.COLUMN_DATE).append(", ");
        query.append(TransactionEntry.COLUMN_PRICE).append(", ");
        query.append(TransactionEntry.COLUMN_IS_REPEATING).append(", ");
        query.append(TransactionEntry.COLUMN_IS_FORECASTED).append(", ");
        query.append(TransactionEntry.COLUMN_CATEGORY_ID).append(" || '_");
        query.append(userId).append("',");
        query.append(" CASE");
        query.append(" WHEN ").append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(" IS NULL THEN ''");
        query.append(" WHEN ").append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(" = '' THEN ''");
        query.append(" ELSE ").append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(" || '_").append(userId).append("'");
        query.append(" END, ");
        query.append(TransactionEntry.COLUMN_IS_EXPENSE).append(",");
        query.append(" CASE");
        query.append(" WHEN ").append(TransactionEntry.COLUMN_REMINDER_ID).append(" IS NULL THEN ''");
        query.append(" WHEN ").append(TransactionEntry.COLUMN_REMINDER_ID).append(" = '' THEN ''");
        query.append(" ELSE ").append(TransactionEntry.COLUMN_REMINDER_ID).append(" || '_").append(userId).append("'");
        query.append(" END,");
        query.append(" CASE");
        query.append(" WHEN ").append(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID).append(" IS NULL THEN ''");
        query.append(" WHEN ").append(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID).append(" = '' THEN ''");
        query.append(" ELSE ").append(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID).append(" || '_").append(userId).append("'");
        query.append(" END, ");
        query.append(TransactionEntry.COLUMN_CALENDAR_ID).append(" || '_").append(userId).append("', ");
        query.append(TransactionEntry.COLUMN_DEVICE_ID).append(", ");
        query.append(BaseContract.getUpdateDate()).append(", ");
        query.append(0);
        query.append(" FROM ").append(TransactionEntry.getTableName(inMemory));
        query.append(" WHERE ").append(TransactionEntry.COLUMN_CALENDAR_ID).append(" IN (SELECT ");
        query.append(CalendarEntry.COLUMN_ID).append(" FROM ").append(CalendarEntry.getTableName(inMemory));
        query.append(" WHERE ").append(CalendarEntry.COLUMN_ID).append(" = ").append(TransactionEntry.COLUMN_CALENDAR_ID);
        query.append(" AND ").append(CalendarEntry.COLUMN_USER_ID).append(" = 0)");
        query.append(" AND ").append(TransactionEntry.COLUMN_DELETED).append(" = 0");
        database.execSQL(query.toString());
    }





    private static ContentValues getContentValues(Transaction transaction) {
        int i;
        int i2 = 1;
        ContentValues values = new ContentValues();
        values.put(TransactionEntry.COLUMN_ID, transaction.getId());
        values.put(TransactionEntry.COLUMN_NOTE, transaction.getNote());
        values.put(TransactionEntry.COLUMN_DATE, Long.valueOf(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(transaction.getDate())));
        values.put(TransactionEntry.COLUMN_PRICE, Double.valueOf(transaction.getPrice()));
        values.put(TransactionEntry.COLUMN_IS_EXPENSE, Integer.valueOf(transaction.isExpense() ? 1 : 0));
        String str = TransactionEntry.COLUMN_IS_REPEATING;
        if (transaction.isRepeating()) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        String str2 = TransactionEntry.COLUMN_IS_FORECASTED;
        if (!transaction.isForecasted()) {
            i2 = 0;
        }
        values.put(str2, Integer.valueOf(i2));
        values.put(TransactionEntry.COLUMN_CALENDAR_ID, transaction.getCalendarId());
        values.put(TransactionEntry.COLUMN_CATEGORY_ID, transaction.getCategoryId());
        values.put(TransactionEntry.COLUMN_REPEAT_INFO_ID, transaction.getRepeatInfoId());
        values.put(TransactionEntry.COLUMN_REMINDER_ID, transaction.getReminderId());
        values.put(TransactionEntry.COLUMN_DEVICE_ID, transaction.getDeviceId());
        values.put(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID, transaction.getOriginalTransactionId());
        values.put(TransactionEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate(transaction.getUpdateDate())));
        values.put(TransactionEntry.COLUMN_DELETED, Integer.valueOf(transaction.getDeleted()));
        return values;
    }

    private static Transaction getDetailedTransaction(Cursor cursor) {
        Transaction transaction = getTransaction(cursor);
        transaction.setCategory(CategoriesContract.getCategory(cursor));
        if (transaction.isRepeating()) {
            transaction.setRepeatInfo(RepeatInfoContract.getRepeatInfo(cursor));
        }
        transaction.setDevice(DevicesContract.getDevice(cursor));
        transaction.setCalendar(CalendarsContract.getCalendar(cursor));
        return transaction;
    }

    private static Transaction getTransaction(Cursor cursor) {
        boolean z;
        boolean z2 = true;
        Transaction transaction = new Transaction();
        transaction.setId(cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_ID)));
        transaction.setNote(cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_NOTE)));
        transaction.setDate(DateTimeUtils.getLocalDateFromServerSpecificGmtTime((long) cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_DATE))));
        transaction.setPrice(cursor.getDouble(cursor.getColumnIndex(TransactionEntry.COLUMN_PRICE)));
        transaction.setRepeating(cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_IS_REPEATING)) == 1);
        if (cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_IS_FORECASTED)) == 1) {
            z = true;
        } else {
            z = false;
        }
        transaction.setForecasted(z);
        transaction.setCategoryId(cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_CATEGORY_ID)));
        transaction.setRepeatInfoId(cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_REPEAT_INFO_ID)));
        if (cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_IS_EXPENSE)) != 1) {
            z2 = false;
        }
        transaction.setExpense(z2);
        transaction.setReminderId(cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_REMINDER_ID)));
        transaction.setOriginalTransactionId(cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID)));
        transaction.setCalendarId(cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_CALENDAR_ID)));
        transaction.setDeviceId(cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_DEVICE_ID)));
        return transaction;
    }

//    private static JsonObject getJsonObject(Cursor cursor) {
//        JsonObject transaction = new JsonObject();
//        transaction.addProperty(RestfulConstants.TRANSACTION_ID, cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_ID)));
//        int deleted = cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_DELETED));
//        if (deleted != 1) {
//            transaction.addProperty(RestfulConstants.TRANSACTION_NOTE, cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_NOTE)));
//            transaction.addProperty(RestfulConstants.TRANSACTION_DATE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_DATE))));
//            transaction.addProperty(RestfulConstants.TRANSACTION_PRICE, Double.valueOf(cursor.getDouble(cursor.getColumnIndex(TransactionEntry.COLUMN_PRICE))));
//            transaction.addProperty(RestfulConstants.TRANSACTION_IS_REPEATING, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_IS_REPEATING))));
//            transaction.addProperty(RestfulConstants.TRANSACTION_IS_FORECASTED, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_IS_FORECASTED))));
//            transaction.addProperty(RestfulConstants.TRANSACTION_CATEGORY_ID, cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_CATEGORY_ID)));
//            transaction.addProperty(RestfulConstants.TRANSACTION_REPEAT_INFO_ID, cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_REPEAT_INFO_ID)));
//            transaction.addProperty(RestfulConstants.TRANSACTION_IS_EXPENSE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_IS_EXPENSE))));
//            transaction.addProperty(RestfulConstants.TRANSACTION_REMINDER_ID, cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_REMINDER_ID)));
//            transaction.addProperty(RestfulConstants.TRANSACTION_ORIGINAL_TRANSACTION_ID, cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID)));
//            transaction.addProperty(RestfulConstants.TRANSACTION_CALENDAR_ID, cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_CALENDAR_ID)));
//            transaction.addProperty(RestfulConstants.TRANSACTION_DEVICE_ID, cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_DEVICE_ID)));
//        }
//        transaction.addProperty(RestfulConstants.TRANSACTION_UPDATE_DATE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_UPDATE_DATE))));
//        transaction.addProperty(RestfulConstants.TRANSACTION_DELETED, Integer.valueOf(deleted));
//        return transaction;
//    }
}
