package io.github.rathn.platap.persistent;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import io.github.rathn.platap.BuildConfig;
import io.github.rathn.platap.NewCalendarActivity;
import io.github.rathn.platap.NewCategoryActivity;
import io.github.rathn.platap.R;
import io.github.rathn.platap.dto.Account;
import io.github.rathn.platap.dto.Balance;
import io.github.rathn.platap.dto.Budget;
import io.github.rathn.platap.dto.BudgetHistory;
import io.github.rathn.platap.dto.Category;
import io.github.rathn.platap.dto.Device;
import io.github.rathn.platap.dto.Factura;
import io.github.rathn.platap.dto.FacturasContract;
import io.github.rathn.platap.dto.Meta;
import io.github.rathn.platap.dto.Payment;
import io.github.rathn.platap.dto.Reminder;
import io.github.rathn.platap.dto.RepeatInfo;
import io.github.rathn.platap.dto.Transaction;
import io.github.rathn.platap.persistent.contracts.BudgetHistoryContract;
import io.github.rathn.platap.persistent.contracts.BudgetsContract;
import io.github.rathn.platap.persistent.contracts.BudgetsContract.BudgetEntry;
import io.github.rathn.platap.persistent.contracts.CalendarsContract;
import io.github.rathn.platap.persistent.contracts.CalendarsContract.CalendarEntry;
import io.github.rathn.platap.persistent.contracts.CategoriesContract;
import io.github.rathn.platap.persistent.contracts.CategoriesContract.CategoryEntry;
import io.github.rathn.platap.persistent.contracts.DevicesContract;
import io.github.rathn.platap.persistent.contracts.DevicesContract.DeviceEntry;
import io.github.rathn.platap.persistent.contracts.MetaContract;
import io.github.rathn.platap.persistent.contracts.MetaContract.MetaEntry;
import io.github.rathn.platap.persistent.contracts.RemindersContract.ReminderEntry;
import io.github.rathn.platap.persistent.contracts.RepeatInfoContract;
import io.github.rathn.platap.persistent.contracts.RepeatInfoContract.RepeatInfoEntry;
import io.github.rathn.platap.persistent.contracts.TransactionsContract;
import io.github.rathn.platap.persistent.contracts.TransactionsContract.TransactionEntry;
import io.github.rathn.platap.utils.DateTimeUtils;
import io.github.rathn.platap.utils.TypeUtils;

import static io.github.rathn.platap.NewCalendarActivity.CALENDAR_RE_INSERTED;
import static io.github.rathn.platap.NewCalendarActivity.CALENDAR_UPDATE;
import static io.github.rathn.platap.NewCalendarActivity.CALENDAR_UPDATED;

public class DatabaseManager {
    private static final String[] sTableNames = new String[]{MetaEntry.TABLE_NAME, CategoryEntry.TABLE_NAME, BudgetEntry.TABLE_NAME, CalendarEntry.TABLE_NAME, ReminderEntry.TABLE_NAME, RepeatInfoEntry.TABLE_NAME, FacturasContract.FacturasEntry.TABLE_NAME};
    public boolean open;
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private DatabaseOpenHelper mDatabaseHelper;

    public DatabaseManager(Context appContext) {
        this.mContext = appContext;
        this.mDatabaseHelper = DatabaseOpenHelper.getInstance(appContext);
        open = false;
    }

   /* public String getJsonForDataUpload(JsonArray transactions, boolean isFirstRequest) {
        Log.d("SyncService", "Last sync date: " + PersistentStorage.getLastSyncDate());
        JsonObject jsonMap = new JsonObject();
        if (isFirstRequest) {
            JsonArray calendars = CalendarsContract.getSyncables(this.mDatabase);
            JsonArray categories = CategoriesContract.getSyncables(this.mDatabase);
            JsonArray repeatInfos = RepeatInfoContract.getSyncables(this.mDatabase);
            JsonArray budgets = BudgetsContract.getSyncables(this.mDatabase);
            jsonMap.add(MetaEntry.TABLE_NAME, MetaContract.getSyncable(this.mDatabase));
            jsonMap.add(RestfulConstants.TAG_CALENDARS, calendars);
            jsonMap.add(RestfulConstants.TAG_CATEGORIES, categories);
            jsonMap.add(RestfulConstants.TAG_REPEAT_INFOS, repeatInfos);
            jsonMap.add(RestfulConstants.TAG_TRANSACTIONS, transactions);
            jsonMap.add(RestfulConstants.TAG_BUDGETS, budgets);
        }
        jsonMap.add(RestfulConstants.TAG_TRANSACTIONS, transactions);
        return new Gson().toJson(jsonMap);
    }

    public JsonArray getTransactionsJsonArrayForUpload() {
        return TransactionsContract.getSyncables(this.mDatabase);
    }*/

    public static List<String> getCalendarIdsFromList(String[] selectedCalendars) {
        if (selectedCalendars != null) {
            return Arrays.asList(selectedCalendars);
        }
        return null;
    }

    public boolean isOpen() {
        return open;
    }

    public void open() throws SQLiteException {
//        this.mDatabaseHelper.open();
        this.mDatabase = ((DatabaseOpenHelper) this.mDatabaseHelper).getWritableDatabase();
        this.open = true;
        try {
            restoreFromLocalDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
//            Crashlytics.log("Local data/*/**/*/base restore failed: " + e.getMessage());
        }
    }

    public void close() {
        this.mDatabaseHelper.close();
        this.open = false;
    }

    private void restoreFromLocalDatabase() throws SQLiteException {
        //this.mDatabase.execSQL("ATTACH DATABASE ? AS localDatabase;", new String[]{this.mDatabaseHelper.getDatabaseFilePath(this.mContext, DatabaseOpenHelper.LOCAL_DATABASE_NAME)});
        if (/*MainActivity.mDatabaseManager.*/this.getMetaData() == null) {
            Account defaultCalendar = new Account();
            defaultCalendar.setId(UUID.randomUUID().toString());
            defaultCalendar.setName(this.mContext.getString(R.string.default_calendar_name));
            defaultCalendar.setIsDefault(true);
            defaultCalendar.setBalance(0.0d);
            defaultCalendar.setOrder(1);
            defaultCalendar.setUserId(PersistentStorage.getUserId());
            CalendarsContract.insert(this.mDatabase, defaultCalendar, false);
            String deviceId = UUID.randomUUID().toString();
            Device defaultDevice = new Device();
            defaultDevice.setId(deviceId);
            defaultDevice.setName(BuildConfig.FLAVOR);
            defaultDevice.setUserId(PersistentStorage.getUserId());
            PersistentStorage.setDeviceId(deviceId);
            DevicesContract.insert(this.mDatabase, defaultDevice, false);
            MetaContract.insertDefaultMetaData(this.mDatabase);

        }
        // copyAllTablesToInMemoryDatabase();
    }

    private void copyAllTablesToInMemoryDatabase() {
        for (String tableName : sTableNames) {
            this.mDatabase.execSQL("CREATE TABLE " + tableName + " AS SELECT * FROM " + DatabaseOpenHelper.LOCAL_DATABASE_PREFIX + "" + tableName + ";");
        }
        StringBuffer createStmt = new StringBuffer();
        createStmt.append("CREATE TABLE ").append(TransactionEntry.getTableName(true)).append(" (");
        createStmt.append(TransactionEntry.COLUMN_ID).append(" text PRIMARY KEY NOT NULL, ");
        createStmt.append(TransactionEntry.COLUMN_NOTE).append(" text NOT NULL DEFAULT (null), ");
        createStmt.append(TransactionEntry.COLUMN_DATE).append(" integer NOT NULL, ");
        createStmt.append(TransactionEntry.COLUMN_PRICE).append(" real NOT NULL, ");
        createStmt.append(TransactionEntry.COLUMN_IS_REPEATING).append(" BOOL DEFAULT (0), ");
        createStmt.append(TransactionEntry.COLUMN_IS_FORECASTED).append(" BOOL DEFAULT (0), ");
        createStmt.append(TransactionEntry.COLUMN_CATEGORY_ID).append(" text NOT NULL, ");
        createStmt.append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(" text, ");
        createStmt.append(TransactionEntry.COLUMN_IS_EXPENSE).append(" BOOL DEFAULT (1), ");
        createStmt.append(TransactionEntry.COLUMN_REMINDER_ID).append(" text NOT NULL, ");
        createStmt.append(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID).append(" text NOT NULL DEFAULT (0), ");
        createStmt.append(TransactionEntry.COLUMN_CALENDAR_ID).append(" text, ");
        createStmt.append(TransactionEntry.COLUMN_DEVICE_ID).append(" text, ");
        createStmt.append(TransactionEntry.COLUMN_UPDATE_DATE).append(" INTEGER NOT NULL DEFAULT (0), ");
        createStmt.append(TransactionEntry.COLUMN_DELETED).append(" BOOL DEFAULT (0)");
        createStmt.append(" )");
        this.mDatabase.execSQL(createStmt.toString());
        createStmt.setLength(0);
        createStmt.append("INSERT INTO " + TransactionEntry.getTableName(true)).append(" (");
        createStmt.append(TransactionEntry.COLUMN_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_NOTE).append(", ");
        createStmt.append(TransactionEntry.COLUMN_DATE).append(", ");
        createStmt.append(TransactionEntry.COLUMN_PRICE).append(", ");
        createStmt.append(TransactionEntry.COLUMN_IS_REPEATING).append(", ");
        createStmt.append(TransactionEntry.COLUMN_IS_FORECASTED).append(", ");
        createStmt.append(TransactionEntry.COLUMN_CATEGORY_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_IS_EXPENSE).append(", ");
        createStmt.append(TransactionEntry.COLUMN_REMINDER_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_CALENDAR_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_DEVICE_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_UPDATE_DATE).append(", ");
        createStmt.append(TransactionEntry.COLUMN_DELETED);
        createStmt.append(") SELECT ");
        createStmt.append(TransactionEntry.COLUMN_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_NOTE).append(", ");
        createStmt.append(TransactionEntry.COLUMN_DATE).append(", ");
        createStmt.append(TransactionEntry.COLUMN_PRICE).append(", ");
        createStmt.append(TransactionEntry.COLUMN_IS_REPEATING).append(", ");
        createStmt.append(TransactionEntry.COLUMN_IS_FORECASTED).append(", ");
        createStmt.append(TransactionEntry.COLUMN_CATEGORY_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_REPEAT_INFO_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_IS_EXPENSE).append(", ");
        createStmt.append(TransactionEntry.COLUMN_REMINDER_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_ORIGINAL_TRANSACTION_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_CALENDAR_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_DEVICE_ID).append(", ");
        createStmt.append(TransactionEntry.COLUMN_UPDATE_DATE).append(", ");
        createStmt.append(TransactionEntry.COLUMN_DELETED);
        createStmt.append(" FROM ").append(TransactionEntry.getTableName(false));
        this.mDatabase.execSQL(createStmt.toString());
        createStmt.setLength(0);
        createStmt.append("CREATE TABLE ").append(DeviceEntry.getTableName(true)).append("( ");
        createStmt.append(DeviceEntry.COLUMN_ID).append(" TEXT NOT NULL DEFAULT (null), ");
        createStmt.append(DeviceEntry.COLUMN_NAME).append(" TEXT DEFAULT (null), ");
        createStmt.append(DeviceEntry.COLUMN_USER_ID).append(" INTEGER DEFAULT (0), ");
        createStmt.append("PRIMARY KEY (").append(DeviceEntry.COLUMN_ID).append(", ").append(DeviceEntry.COLUMN_USER_ID).append("))");
        this.mDatabase.execSQL(createStmt.toString());
        createStmt.setLength(0);
        createStmt.append("INSERT INTO " + DeviceEntry.getTableName(true)).append(" (");
        createStmt.append(DeviceEntry.COLUMN_ID).append(", ");
        createStmt.append(DeviceEntry.COLUMN_NAME).append(", ");
        createStmt.append(DeviceEntry.COLUMN_USER_ID);
        createStmt.append(") SELECT ");
        createStmt.append(DeviceEntry.COLUMN_ID).append(", ");
        createStmt.append(DeviceEntry.COLUMN_NAME).append(", ");
        createStmt.append(DeviceEntry.COLUMN_USER_ID);
        createStmt.append(" FROM ").append(DeviceEntry.getTableName(false));
        this.mDatabase.execSQL(createStmt.toString());
    }

    private void clearInMemoryDatabase() {
        String dropTableStatement = "DROP TABLE IF EXISTS ";
        for (String tableName : sTableNames) {
            this.mDatabase.execSQL(dropTableStatement + tableName);
        }
        this.mDatabase.execSQL(dropTableStatement + TransactionEntry.getTableName(true));
        this.mDatabase.execSQL(dropTableStatement + DeviceEntry.getTableName(true));
    }

//    public void cloneDataAfterRegistration(boolean inMemory) {
//        CalendarsContract.cloneDataForRegisteredUser(this.mDatabase, inMemory);
//        CategoriesContract.cloneDataForRegisteredUser(this.mDatabase, inMemory);
//        RepeatInfoContract.cloneDataForRegisteredUser(this.mDatabase, inMemory);
//        TransactionsContract.cloneDataForRegisteredUser(this.mDatabase, inMemory);
//        DevicesContract.cloneDataForRegisteredUser(this.mDatabase, inMemory);
//        Category transferCategory = new Category();
//        transferCategory.setId("transfer_" + PersistentStorage.getUserId());
//        transferCategory.setName(this.mContext.getString(R.string.transfer));
//        transferCategory.setColorIndex(0);
//        transferCategory.setIsExpense(false);
//        transferCategory.setIconIndex(0);
//        transferCategory.setOrder(0);
//        transferCategory.setUserId(PersistentStorage.getUserId());
//        CategoriesContract.insert(this.mDatabase, transferCategory, inMemory);
//        MetaContract.insertDefaultMetaData(this.mDatabase);
//    }

    private void resetInMemoryDatabase() {
        //clearInMemoryDatabase();
        //copyAllTablesToInMemoryDatabase();
    }

    public void insertMetaDataForUser() {
        MetaContract.insertDefaultMetaData(this.mDatabase);
    }

    public Meta getMetaData() {
        return MetaContract.getMetaData(this.mDatabase);
    }

    public void updateMetaData(Meta meta) {
        if (meta != null) {
            MetaContract.updateRepeatingEndDate(this.mDatabase, meta);
        }
    }

    public void insertTransaction(Transaction transaction) {
        if (transaction != null) {
//            processInsertTransaction(transaction, true);
            processInsertTransaction(transaction, false);
        }
    }

    public void insertTransactionWithEvent(Transaction transaction, String event) {
        if (!event.isEmpty()) {
            if (transaction != null) {
                processInsertTransaction(transaction, true);
                processInsertTransaction(transaction, false);
            }
        }
    }

    private void processInsertTransaction(final Transaction transaction, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                if (transaction.getRepeatInfo() != null) {
                    RepeatInfoContract.insert(DatabaseManager.this.mDatabase, transaction.getRepeatInfo(), inMemory);
                    TransactionsContract.insert(DatabaseManager.this.mDatabase, transaction.getRepeatingTransactions(DatabaseManager.this), inMemory);
                }
                TransactionsContract.insert(DatabaseManager.this.mDatabase, transaction, inMemory);
                FacturasContract.insert(DatabaseManager.this.mDatabase, transaction.getFacturas());
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                enviarAvisoDeActualizacion(CompactCalendarView.TRANSACTIONS_UPDATED);
            }
        }.execute();
    }

    private void enviarAvisoDeActualizacion(String aviso) {
        if (/*inMemory*/ true) {//TODO: Widget
//                    Log.d("SyncBug", "DatabaseManager - processInsertTransaction");
            Intent intent = new Intent(aviso);

            LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(intent);
//                    Intent intent = new Intent(DatabaseManager.this.mContext);
//                    intent.setAction(CalendarFragment.ACTION_TRANSACTIONS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
        }
    }

    public void insertTransactions(List<Transaction> transactions, boolean shouldUpdateUi) {
        if (!TypeUtils.isEmpty((List) transactions)) {
            Log.d("SyncBug", "DatabaseManager insertTransactions()");
            TransactionsContract.insert(this.mDatabase, (List) transactions, true);
//            TransactionsContract.insert(this.mDatabase, (List) transactions, false);
            if (shouldUpdateUi) {//TODO: Widget
//                Log.d("SyncBug", "DatabaseManager insertTransactions() - sends broadcast");
//                LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(new Intent(CalendarFragment.ACTION_TRANSACTIONS_UPDATED));
//                Intent intent = new Intent(this.mContext, DollarbirdAppWidgetProvider.class);
//                intent.setAction(CalendarFragment.ACTION_TRANSACTIONS_UPDATED);
//                this.mContext.sendBroadcast(intent);
            }
        }
    }

    public void insertTransfer(Transaction sourceTransaction, Account source, Account destination) {
        if (sourceTransaction != null && source != null && destination != null) {
            sourceTransaction.setCategoryId(CategoriesContract.getTransferCategoryId(this.mDatabase));
            sourceTransaction.setPrice(-1.0d * sourceTransaction.getPrice());
            sourceTransaction.setCalendarId(source.getId());
            sourceTransaction.setExpense(true);
            insertTransaction(sourceTransaction);
            Transaction destinationTransaction = sourceTransaction.copy();
            destinationTransaction.setId(UUID.randomUUID().toString());
            destinationTransaction.setPrice(Math.abs(destinationTransaction.getPrice()));
            destinationTransaction.setCalendarId(destination.getId());
            destinationTransaction.setExpense(false);
            destinationTransaction.setOriginalTransactionId(BuildConfig.FLAVOR);
            if (destinationTransaction.isRepeating()) {
                RepeatInfo destinationRepeatInfo = new RepeatInfo(sourceTransaction.getRepeatInfo());
                destinationRepeatInfo.setId(UUID.randomUUID().toString());
                destinationTransaction.setRepeatInfo(destinationRepeatInfo);
            }
            insertTransaction(destinationTransaction);
        }
    }

    public void updateTransaction(Transaction oldTransaction, Transaction newTransaction, boolean onlyThis) {
        if (oldTransaction.isRepeating()) {
            updateRepeatingTransaction(oldTransaction, newTransaction, onlyThis);
        } else {
            updateSimpleTransaction(oldTransaction, newTransaction);
        }
        try {
            Map<String, String> parameters = new HashMap();
//            parameters.put(FlurryManager.VALUE_UPDATE_TRANSACTION_CALENDAR_CHANGED, oldTransaction.getCalendarId().equals(newTransaction.getCalendarId()) ? FlurryManager.VALUE_UPDATE_TRANSACTION_CALENDAR_NOT_CHANGED : FlurryManager.VALUE_UPDATE_TRANSACTION_CALENDAR_CHANGED);
//            FlurryManager.log(FlurryManager.EVENT_EDIT_TRANSACTION, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSimpleTransaction(Transaction oldTransaction, Transaction newTransaction) {
        if (oldTransaction != null && newTransaction != null) {
            processUpdateSimpleTransaction(oldTransaction, newTransaction, true);
            // processUpdateSimpleTransaction(oldTransaction, newTransaction, false);
        }
    }

    private void processUpdateSimpleTransaction(final Transaction oldTransaction, final Transaction newTransaction, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                if (newTransaction.getRepeatInfo() != null) {
                    RepeatInfoContract.insert(DatabaseManager.this.mDatabase, newTransaction.getRepeatInfo(), inMemory);
                    TransactionsContract.insert(DatabaseManager.this.mDatabase, newTransaction.getRepeatingTransactions(DatabaseManager.this), inMemory);
                }
                TransactionsContract.update(DatabaseManager.this.mDatabase, newTransaction, inMemory);
                FacturasContract.delete(DatabaseManager.this.mDatabase, oldTransaction);
                FacturasContract.insert(DatabaseManager.this.mDatabase, newTransaction.getFacturas());
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {
//                    Log.d("SyncBug", "DatabaseManager - processUpdateSimpleTransaction");
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(CalendarFragment.ACTION_TRANSACTIONS_UPDATED));
//                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
//                    intent.setAction(CalendarFragment.ACTION_TRANSACTIONS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
                    enviarAvisoDeActualizacion(CompactCalendarView.TRANSACTIONS_UPDATED);
                }
            }
        }.execute();
    }

    private void updateRepeatingTransaction(Transaction oldTransaction, Transaction newTransaction, boolean onlyThis) {
        if (onlyThis) {
            updateOnlyThisRepeatingTransaction(newTransaction);
        } else {
            updateThisAndAllFutureRepeatingTransaction(oldTransaction, newTransaction);
        }
    }

    private void updateOnlyThisRepeatingTransaction(Transaction transaction) {
        if (transaction != null) {
            processUpdateOnlyThisRepeatingTransaction(transaction, true);
//            processUpdateOnlyThisRepeatingTransaction(transaction, false);
        }
    }

    private void processUpdateOnlyThisRepeatingTransaction(final Transaction transaction, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                transaction.setRepeatInfo(null);
                if (transaction.isOriginal()) {
                    TransactionsContract.changeOriginalTransaction(DatabaseManager.this.mDatabase, transaction, inMemory);
                }
                TransactionsContract.update(DatabaseManager.this.mDatabase, transaction, inMemory);
                FacturasContract.delete(DatabaseManager.this.mDatabase, transaction);
                FacturasContract.insert(DatabaseManager.this.mDatabase, transaction.getFacturas());
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {//
//                    Log.d("SyncBug", "DatabaseManager - processUpdateOnlyThisRepeatingTransaction");
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(CalendarFragment.ACTION_TRANSACTIONS_UPDATED));
//                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
//                    intent.setAction(CalendarFragment.ACTION_TRANSACTIONS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
                    enviarAvisoDeActualizacion(CompactCalendarView.TRANSACTIONS_UPDATED);
                }
            }
        }.execute();
    }

    private void updateThisAndAllFutureRepeatingTransaction(Transaction oldTransaction, Transaction newTransaction) {
        if (oldTransaction != null && newTransaction != null) {
            processUpdateThisAndAllFutureRepeatingTransactions(oldTransaction, newTransaction, true);
//            processUpdateThisAndAllFutureRepeatingTransactions(oldTransaction, newTransaction, false);
        }
    }

    private void processUpdateThisAndAllFutureRepeatingTransactions(final Transaction oldTransaction, final Transaction newTransaction, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                if (newTransaction.getRepeatInfo() != null) {
                    if (oldTransaction.isOriginal()) {
                        TransactionsContract.deleteRepeatingTransactions(DatabaseManager.this.mDatabase, newTransaction, inMemory);
                        RepeatInfoContract.update(DatabaseManager.this.mDatabase, newTransaction.getRepeatInfo(), inMemory);
                    } else {
                        TransactionsContract.deleteRepeatingTransactions(DatabaseManager.this.mDatabase, oldTransaction, newTransaction.getDate(), inMemory);
                        newTransaction.getRepeatInfo().setId(UUID.randomUUID().toString());
                        newTransaction.setRepeatInfoId(newTransaction.getRepeatInfo().getId());
                        newTransaction.getRepeatInfo().setStartDate(newTransaction.getDate());
                        RepeatInfoContract.insert(DatabaseManager.this.mDatabase, newTransaction.getRepeatInfo(), inMemory);
                        Calendar oldEndDate = DateTimeUtils.clone(newTransaction.getRepeatInfo().getStartDate());
                        oldEndDate.add(Calendar.DATE, -1);
                        oldTransaction.getRepeatInfo().setEndDate(oldEndDate);
                        RepeatInfoContract.update(DatabaseManager.this.mDatabase, oldTransaction.getRepeatInfo(), inMemory);
                    }
                } else if (newTransaction.isOriginal()) {
                    TransactionsContract.deleteRepeatingTransactions(DatabaseManager.this.mDatabase, oldTransaction, inMemory);
                    RepeatInfoContract.delete(DatabaseManager.this.mDatabase, oldTransaction.getRepeatInfoId(), inMemory);
                }
                if (newTransaction.isRepeating()) {
                    TransactionsContract.insert(DatabaseManager.this.mDatabase, newTransaction.getRepeatingTransactions(DatabaseManager.this), inMemory);
                }
                TransactionsContract.update(DatabaseManager.this.mDatabase, newTransaction, inMemory);
                FacturasContract.delete(DatabaseManager.this.mDatabase, oldTransaction);
                FacturasContract.insert(DatabaseManager.this.mDatabase, newTransaction.getFacturas());
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                enviarAvisoDeActualizacion(CompactCalendarView.TRANSACTIONS_UPDATED);
                if (inMemory) {//TODO: Widget
//                    Log.d("SyncBug", "DatabaseManager - processUpdateThisAndAllFutureRepeatingTransactions");
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(CalendarFragment.ACTION_TRANSACTIONS_UPDATED));
//                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
//                    intent.setAction(CalendarFragment.ACTION_TRANSACTIONS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
                }
            }
        }.execute();
    }

    public void resetForecast(Transaction transaction) {
        if (transaction != null) {
            processResetForecast(transaction, true);
            processResetForecast(transaction, false);
        }
//        FlurryManager.log(FlurryManager.EVENT_CONVERT_TRANSACTION);
    }

    private void processResetForecast(final Transaction transaction, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                TransactionsContract.resetForecast(DatabaseManager.this.mDatabase, transaction, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {//TODO: Widget
//                    Log.d("SyncBug", "DatabaseManager - processResetForecast");
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(CalendarFragment.ACTION_TRANSACTIONS_UPDATED));
//                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
//                    intent.setAction(CalendarFragment.ACTION_TRANSACTIONS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
                }
            }
        }.execute();
    }

    public void deleteTransaction(Transaction transaction, boolean onlyThis) {
        if (transaction.isRepeating()) {
            deleteRepeatingTransaction(transaction, onlyThis);
        } else {
            deleteSimpleTransaction(transaction);
        }
//        FlurryManager.log(FlurryManager.EVENT_DELETE_TRANSACTION);
    }

    private void deleteSimpleTransaction(Transaction transaction) {
        if (transaction != null) {
            processDeleteSimpleTransaction(transaction, true);
            //processDeleteSimpleTransaction(transaction, false);
        }
    }

    private void processDeleteSimpleTransaction(final Transaction transaction, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                TransactionsContract.delete(DatabaseManager.this.mDatabase, transaction, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (/*inMemory*/ true) {//TODO: Widget
//                    Log.d("SyncBug", "DatabaseManager - processDeleteSimpleTransaction");
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(CalendarFragment.ACTION_TRANSACTIONS_UPDATED));
//                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
//                    intent.setAction(CalendarFragment.ACTION_TRANSACTIONS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
                    enviarAvisoDeActualizacion(CompactCalendarView.TRANSACTIONS_UPDATED);
                }
            }
        }.execute();
    }

    private void deleteRepeatingTransaction(Transaction transaction, boolean onlyThis) {
        if (onlyThis) {
            deleteOnlyThisRepeatingTransaction(transaction);
        } else {
            deleteThisAndAllFutureRepeatingTransaction(transaction);
        }
    }

    private void deleteOnlyThisRepeatingTransaction(Transaction transaction) {
        if (transaction != null) {
            processDeleteOnlyThisRepeatingTransaction(transaction, true);
//            processDeleteOnlyThisRepeatingTransaction(transaction, false);
        }
    }

    private void processDeleteOnlyThisRepeatingTransaction(final Transaction transaction, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                if (transaction.isOriginal()) {
                    TransactionsContract.changeOriginalTransaction(DatabaseManager.this.mDatabase, transaction, inMemory);
                }
                TransactionsContract.delete(DatabaseManager.this.mDatabase, transaction, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {//TODO: Widget
//                    Log.d("SyncBug", "DatabaseManager - processDeleteOnlyThisRepeatingTransaction");
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(CalendarFragment.ACTION_TRANSACTIONS_UPDATED));
//                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
//                    intent.setAction(CalendarFragment.ACTION_TRANSACTIONS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
                    enviarAvisoDeActualizacion(CompactCalendarView.TRANSACTIONS_UPDATED);
                }
            }
        }.execute();
    }

    private void deleteThisAndAllFutureRepeatingTransaction(Transaction transaction) {
        if (transaction != null) {
            processDeleteThisAndAllFutureRepeatingTransaction(transaction, true);
//            processDeleteThisAndAllFutureRepeatingTransaction(transaction, false);
        }
    }

    private void processDeleteThisAndAllFutureRepeatingTransaction(final Transaction transaction, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                boolean delete = false;
                if (transaction.isOriginal()) {
                    if (transaction.getRepeatInfo() != null) {
                        RepeatInfoContract.delete(DatabaseManager.this.mDatabase, transaction.getRepeatInfoId(), inMemory);
                    }
                    TransactionsContract.deleteRepeatingTransactions(DatabaseManager.this.mDatabase, transaction, inMemory);
                    delete = true;
                } else if (transaction.getRepeatInfo().getStartDate() != null) {
                    Calendar updatedEndDate = DateTimeUtils.clone(transaction.getDate());
                    updatedEndDate.add(Calendar.DATE, -1);
                    transaction.getRepeatInfo().setEndDate(updatedEndDate);
                    RepeatInfoContract.update(DatabaseManager.this.mDatabase, transaction.getRepeatInfo(), inMemory);
                    TransactionsContract.deleteRepeatingTransactions(DatabaseManager.this.mDatabase, transaction, transaction.getDate(), inMemory);
                    delete = true;
                }
                if (delete) {
                    TransactionsContract.delete(DatabaseManager.this.mDatabase, transaction, inMemory);
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {//TODO: Widget
//                    Log.d("SyncBug", "DatabaseManager - processDeleteThisAndAllFutureRepeatingTransaction");
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(CalendarFragment.ACTION_TRANSACTIONS_UPDATED));
//                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
//                    intent.setAction(CalendarFragment.ACTION_TRANSACTIONS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
                    enviarAvisoDeActualizacion(CompactCalendarView.TRANSACTIONS_UPDATED);
                }
            }
        }.execute();
    }

    public List<Transaction> getTransactionsForRangeWithType(String[] calendarIds, Calendar startDate, Calendar endDate, int transactionType) {
        if (calendarIds == null || calendarIds.length == 0) {
            return new ArrayList<>();
        }
        return TransactionsContract.getTransactionsForTimeRange(this.mDatabase, Arrays.asList(calendarIds), startDate, endDate, transactionType, null, -1, true, true);
    }

    public List<Transaction> getTransactionsForExportCSV() {
        Calendar start = Calendar.getInstance();
        start.add(Calendar.YEAR, -20);
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 20);
        return TransactionsContract.getTransactionsForTimeRange(this.mDatabase, getCalendarIdsFromList(CalendarsContract.getAllCalendarIds(this.mDatabase, true)), start, end, -1, null, -1, true, true);
    }

    public List<Transaction> getTransactionsForRangeWithTypeByCategory(String[] selectedCalendars, Calendar date, int mForecastType, String categoryId) {
        if (selectedCalendars == null || selectedCalendars.length <= 0) {
            return new ArrayList<>();
        }
        return TransactionsContract.getTransactionsForTimeRange(this.mDatabase, getCalendarIdsFromList(selectedCalendars), DateTimeUtils.getFirstDateOfMonth(date), DateTimeUtils.getLastDateOfMonth(date), -1, categoryId, mForecastType, false, false);
    }

    public List<Transaction> getTransactionsForMonth(String[] calendars, Calendar date) {
        if (calendars == null || calendars.length <= 0) {
            return new ArrayList<>();
        }
        return getTransactionsForMonthWithType(calendars, date, -1, -1, false);
    }

    public List<Transaction> getTransactionsForMonthWithType(String[] selectedCalendars, Calendar date, int transactionType, int forecastType, boolean showPastForecasted) {
        if (selectedCalendars == null || selectedCalendars.length <= 0) {
            return new ArrayList<>();
        }
        return TransactionsContract.getTransactionsForTimeRange(this.mDatabase, getCalendarIdsFromList(selectedCalendars), DateTimeUtils.getFirstDateOfMonth(date), DateTimeUtils.getLastDateOfMonth(date), transactionType, null, forecastType, showPastForecasted, false);
    }

    public List<Transaction> getTransactionsForMonthWithCategory(String[] selectedCalendars, Calendar date, int transactionType, int forecastType) {
        List<Transaction> transactions = /*MainActivity.mDatabaseManager.*/getTransactionsForMonthWithType(selectedCalendars, date, transactionType, forecastType, false);
        if (transactions == null) {
            return transactions;
        }
        int i;
        double allprice = 0.0d;
        for (i = 0; i < transactions.size(); i++) {
            allprice += transactions.get(i).getPrice();
        }
        HashMap<String, Transaction> transactionsHash = new HashMap();
        for (i = 0; i < transactions.size(); i++) {
            if (transactionsHash.containsKey(transactions.get(i).getCategoryId())) {
                transactionsHash.get(transactions.get(i).getCategoryId()).setPrice(transactionsHash.get(transactions.get(i).getCategoryId()).getPrice() + transactions.get(i).getPrice());
                transactionsHash.get(transactions.get(i).getCategoryId()).setTransactionPercent((transactionsHash.get(transactions.get(i).getCategoryId()).getPrice() * 100.0d) / allprice);
            } else {
                transactions.get(i).setNote(BuildConfig.FLAVOR);
                transactions.get(i).setTransactionPercent((transactions.get(i).getPrice() * 100.0d) / allprice);
                transactionsHash.put(transactions.get(i).getCategoryId(), transactions.get(i));
            }
        }
        List<Transaction> ret = new ArrayList<>();
        for (Entry<String, Transaction> entry : transactionsHash.entrySet()) {
            Transaction t = entry.getValue();
            t.setRepeating(false);
            t.setForecasted(false);
            ret.add(t);
        }
        Collections.sort(ret, new Comparator<Transaction>() {
            public int compare(Transaction o1, Transaction o2) {
                if (o2.getTransactionPercent() < o1.getTransactionPercent()) {
                    return -1;
                }
                if (o2.getTransactionPercent() == o1.getTransactionPercent()) {
                    return 0;
                }
                return 1;
            }
        });
        return ret;
    }

    public List<Transaction> getTransactionsForDay(String[] sCalendars, Calendar date) {
        return getTransactionsForDay(sCalendars, date, -1, -1);
    }

    private List<Transaction> getTransactionsForDay(String[] sCalendars, Calendar date, int transactionType, int forecastType) {
        if (sCalendars == null || sCalendars.length <= 0) {
            return new ArrayList<>();
        }
        date = DateTimeUtils.getDateWithoutTime(date);
        Calendar endDate = DateTimeUtils.clone(date);
        endDate = DateTimeUtils.getCalendarToMidnight(endDate);
        return TransactionsContract.getTransactionsForTimeRange(this.mDatabase,
                getCalendarIdsFromList(sCalendars),
                date,
                endDate,
                transactionType, null, forecastType, true, true);
    }

    public double getBalance(String[] selectedCalendars, Calendar endDate, boolean showPastForecasted) {
        if (selectedCalendars == null || selectedCalendars.length <= 0) {
            return 0.0d;
        }
        endDate = DateTimeUtils.getCalendarToMidnight(endDate);
        return TransactionsContract.getBalance(getCalendarIdsFromList(selectedCalendars), this.mDatabase, endDate, showPastForecasted);
    }

    public String getBalanceUpdateId() {
        return CategoriesContract.getBalanceUpdateCategoryId(this.mDatabase);
    }

    public ArrayList<Balance> getBalanceWithIntervallByDay(@NonNull String[] selectedCalendars, Calendar startDate, Calendar endDate) {
        ArrayList<Balance> balances = new ArrayList<>();
        if (selectedCalendars.length > 0 && startDate.getTimeInMillis() < endDate.getTimeInMillis()) {
            double initialBalance = getBalance(selectedCalendars, startDate, false);
            List<Transaction> allTransactions = TransactionsContract.getTransactionsForTimeRange(this.mDatabase, getCalendarIdsFromList(selectedCalendars), startDate, endDate, -1, null, -1, false, true);
            Calendar tempDate = DateTimeUtils.clone(startDate);
            int i = 0;
            while (tempDate.compareTo(endDate) < 0) {
                Balance balance = new Balance(initialBalance, 0.0d, DateTimeUtils.clone(tempDate));
                try {
                    while (DateTimeUtils.isCalendarsTheSameDay(allTransactions.get(i).getDate(), tempDate)) {
                        balance.setTotalValue(initialBalance += allTransactions.get(i).getPrice());
                        i++;
                        if (i >= allTransactions.size()) break;
                    }
                } catch (Exception e) {
                    Log.d("Mejorar Balances", "");
                }
                balances.add(balance);
                tempDate.add(Calendar.DATE, 1);
            }
        }
        return balances;
    }

    public ArrayList<Balance> getBalancesForEveryDayOnMonth(String[] selectedCalendars, Calendar startDate, Calendar endDate) {
        ArrayList<Balance> balances = new ArrayList<>();
        if (selectedCalendars.length > 0 && startDate.getTimeInMillis() < endDate.getTimeInMillis()) {
            double initialBalance = 0 /*getBalance(selectedCalendars, startDate, false)*/;
            List<Transaction> allTransactions = TransactionsContract.getTransactionsForTimeRange(this.mDatabase, getCalendarIdsFromList(selectedCalendars), startDate, endDate, -1, null, -1, false, true);
            Calendar tempDate = DateTimeUtils.clone(startDate);
            int i = 0;
            while (tempDate.compareTo(endDate) < 0) {
                initialBalance = 0;
                Balance balance = new Balance(initialBalance, 0.0d, DateTimeUtils.clone(tempDate));
                try {
                    while (DateTimeUtils.isCalendarsTheSameDay(allTransactions.get(i).getDate(), tempDate)) {
                        balance.setTotalValue(initialBalance += allTransactions.get(i).getPrice());
                        i++;
                        if (i >= allTransactions.size()) break;
                    }
                } catch (Exception e) {
                    Log.d("Mejorar Balances", "");
                }
                balances.add(balance);
                tempDate.add(Calendar.DATE, 1);
            }
        }
        return balances;
    }

    public ArrayList<Balance> getBalanceWithIntervallByDays(String[] selectedCalendars, Calendar startDate, Calendar endDate) {
        //Crear la lista
        ArrayList<Balance> balanceList = new ArrayList<>();
        if (selectedCalendars != null && selectedCalendars.length > 0) {
            //Creamos las fechas iniciales y finales
            Calendar start = DateTimeUtils.getFirstDateOfMonth(startDate);
            Calendar end = DateTimeUtils.getLastDateOfMonth(endDate);
            if (start.compareTo(end) == -1) {
//                Toast.makeText(this.mContext, "start.compareTo(end) == -1", Toast.LENGTH_SHORT).show();
                start.setTimeInMillis(start.getTimeInMillis() - 1);
                double initialBalance = getBalance(selectedCalendars, start, false);
                start.setTimeInMillis(start.getTimeInMillis() + 1);
                List<Transaction> allTransactions = TransactionsContract.getTransactionsForTimeRange(this.mDatabase, getCalendarIdsFromList(selectedCalendars), start, end, -1, null, -1, false, true);
                Calendar tempDate = DateTimeUtils.clone(start);
                double initialBalance2 = initialBalance;
                while (tempDate.compareTo(end) < 0) {
                    boolean findOne = false;
                    Balance balance = new Balance(initialBalance2, 0.0d, DateTimeUtils.clone(tempDate));
                    for (int i = 0; i < allTransactions.size(); i++) {
                        if (DateTimeUtils.isCalendarsTheSameDay(allTransactions.get(i).getDate(), tempDate)) {
                            initialBalance2 += allTransactions.get(i).getPrice();
                            findOne = true;
                        } else if (findOne) {
                            break;
                        }
                    }
                    balanceList.add(balance);
                    tempDate.add(Calendar.DATE, 1);
                }
            } else
                Toast.makeText(this.mContext, "start.compareTo(end) != " + start.compareTo(end), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this.mContext, "start.compareTo(end) No llego", Toast.LENGTH_SHORT).show();

        return balanceList;
    }

    public ArrayList<Balance> getBalanceWithIntervallByMonths(String[] selectedCalendars, Calendar startDate, Calendar endDate) {
        ArrayList<Balance> balanceList = new ArrayList<>();
        Calendar start = DateTimeUtils.getLastDateOfMonth(startDate);
        Calendar end = DateTimeUtils.getLastDateOfMonth(endDate);
        if (start.compareTo(end) < 1) {
            while (true) {
                if (start.get(Calendar.YEAR) == end.get(Calendar.YEAR) && start.get(Calendar.MONTH) == end.get(Calendar.MONTH)) {
                    break;
                }
                balanceList.add(new Balance(getBalance(selectedCalendars, start, false), 0.0d, DateTimeUtils.clone(start)));
                start.add(Calendar.MONTH, 1);
            }
            balanceList.add(new Balance(getBalance(selectedCalendars, start, false), 0.0d, DateTimeUtils.clone(start)));
            start.add(Calendar.MONTH, 1);
            start = DateTimeUtils.getLastDateOfMonth(start);
        }
        if (balanceList.size() > 0) {
            balanceList.get(0).setMoovement(balanceList.get(0).getTotalValue());
            if (balanceList.size() > 1) {
                for (int i = 1; i < balanceList.size(); i++) {
                    balanceList.get(i).setMoovement(balanceList.get(i).getTotalValue() - balanceList.get(i - 1).getTotalValue());
                }
            }
        }
        return balanceList;
    }

    public ArrayList<Payment> getTransactionsSummary(String[] selectedCalendars, Calendar startDate, Calendar endDate) {
        int i;
        Calendar tempDate = DateTimeUtils.clone(startDate);
        ArrayList<Payment> payments = new ArrayList<>();
        List<Transaction> expenses = TransactionsContract.getTransactionsForTimeRange(this.mDatabase, getCalendarIdsFromList(selectedCalendars), startDate, endDate, 1, null, -1, false, false);
        List<Transaction> incomes = TransactionsContract.getTransactionsForTimeRange(this.mDatabase, getCalendarIdsFromList(selectedCalendars), startDate, endDate, 0, null, -1, false, false);
        LinkedHashMap<Calendar, Payment> paymentss = new LinkedHashMap();
        while (tempDate.compareTo(endDate) < 0) {
            Payment payment = new Payment();
            payment.setPaymentDate(DateTimeUtils.clone(tempDate));
            paymentss.put(DateTimeUtils.getFirstDateOfMonth(tempDate), payment);
            tempDate.add(Calendar.MONTH, 1);
        }
        for (i = 0; i < expenses.size(); i++) {
            if (paymentss.containsKey(DateTimeUtils.getFirstDateOfMonth(expenses.get(i).getDate()))) {
                paymentss.get(DateTimeUtils.getFirstDateOfMonth(expenses.get(i).getDate())).incExpense(Math.abs(expenses.get(i).getPrice()));
            }
        }
        for (i = 0; i < incomes.size(); i++) {
            if (paymentss.containsKey(DateTimeUtils.getFirstDateOfMonth(incomes.get(i).getDate()))) {
                paymentss.get(DateTimeUtils.getFirstDateOfMonth(incomes.get(i).getDate())).incIncome(Math.abs(incomes.get(i).getPrice()));
            }
        }
        for (Entry<Calendar, Payment> entry : paymentss.entrySet()) {
            payments.add(entry.getValue());
        }
        return payments;
    }

    public double getTodaysSpending() {
        return TransactionsContract.getTodaysSpending(this.mDatabase);
    }

    public void insertBudget(Budget budget) {
        if (budget != null) {
            processInsertBudget(budget, true);
            processInsertBudget(budget, false);
        }
        try {
            Map<String, String> parameters = new HashMap();
//            parameters.put(FlurryManager.PARAM_CATEGORY_NAME, budget.getCategory().getName());
//            parameters.put(FlurryManager.PARAM_PRICE, Double.toString(budget.getValue()));
//            FlurryManager.log(FlurryManager.EVENT_ADD_BUDGET, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBudget(Budget budget) {
        if (budget != null) {
            processUpdateBudget(budget, true);
            processUpdateBudget(budget, false);
        }
        try {
            Map<String, String> parameters = new HashMap();
//            parameters.put(FlurryManager.PARAM_CATEGORY_NAME, budget.getCategory().getName());
//            FlurryManager.log(FlurryManager.EVENT_EDIT_BUDGET, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processUpdateBudget(final Budget budget, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                BudgetsContract.update(DatabaseManager.this.mDatabase, budget, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {
                    //TODO: BudgetActivity
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(BudgetActivity.ACTION_BUDGET_UPDATED));
                }
            }
        }.execute();
    }

    /**
     * NOT READY..!
     *
     * @param budgets
     */
    public void insertBudgets(List<Budget> budgets) {////TODO: BudgetActivity
//        if (!TypeUtils.isEmpty((List) budgets)) {
//            BudgetsContract.insert(this.mDatabase, (List) budgets, true);
//            LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(new Intent(BudgetActivity.ACTION_BUDGET_INSERTED));
//            BudgetsContract.insert(this.mDatabase, (List) budgets, false);
//        }
    }

    private void processInsertBudget(final Budget budget, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                BudgetsContract.insert(DatabaseManager.this.mDatabase, budget, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {//TODO: BudgetActivity
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(BudgetActivity.ACTION_BUDGET_INSERTED));
                }
            }
        }.execute();
    }

    public List<Budget> getBudgets(Calendar startDate) {
        return BudgetsContract.getAllByIntervall(this.mDatabase, DateTimeUtils.getFirstDateOfMonth(startDate), DateTimeUtils.getLastDateOfMonth(startDate));
    }

    public List<BudgetHistory> getBudgetHistory() {
        return BudgetHistoryContract.getAll(this.mDatabase);
    }

    public void deleteBudget(Budget budget) {
        if (budget != null) {
            processDeleteBudget(budget, true);
            processDeleteBudget(budget, false);
        }
    }

    private void processDeleteBudget(final Budget budget, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                BudgetsContract.delete(DatabaseManager.this.mDatabase, budget, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {//TODO: BudgetActivity
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(BudgetActivity.ACTION_BUDGET_DELETED));
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(BudgetActivity.ACTION_BUDGET_UPDATED));
                }
            }
        }.execute();
    }

    public void insertCalendar(Account calendar) {
        if (calendar != null) {
            processInsertCalendar(calendar, true);
//            processInsertCalendar(calendar, false);
        }
    }

    private void processInsertCalendar(final Account calendar, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                DatabaseManager.this.open();
                CalendarsContract.insert(DatabaseManager.this.mDatabase, calendar, inMemory);
                Intent intent = new Intent(CALENDAR_UPDATE);
                Bundle b = new Bundle();
                b.putParcelable(CALENDAR_UPDATED, calendar);
                intent.putExtras(b);
                LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(intent);
                DatabaseManager.this.close();
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {//TODO: Widget
                    /*LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(MainActivity.ACTION_CALENDARS_UPDATED));
                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
                    intent.setAction(MainActivity.ACTION_CALENDARS_UPDATED);
                    DatabaseManager.this.mContext.sendBroadcast(intent);*///TODO: Widget
                }
            }
        }.execute();
    }

    public void insertCalendars(List<Account> calendars) {
        if (!TypeUtils.isEmpty((List) calendars)) {
        }
    }

    public void updateCalendar(Account calendar) {
        if (calendar != null) {
            processUpdateCalendar(calendar, true);
//            processUpdateCalendar(calendar, false);
        }
    }

    private void processUpdateCalendar(final Account calendar, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                DatabaseManager.this.open();
                CalendarsContract.update(DatabaseManager.this.mDatabase, calendar, inMemory);
                DatabaseManager.this.close();
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                enviarAvisoDeActualizacion(CALENDAR_RE_INSERTED);
            }
        }.execute();
    }

    public void updateBySettingNewDefaultCalendar(Account calendar) {
        if (calendar != null) {
            processBySettingNewDefaultCalendar(calendar, true);
            processBySettingNewDefaultCalendar(calendar, false);
        }
    }

    private void processBySettingNewDefaultCalendar(final Account calendar, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                CalendarsContract.updateBySettingNewDefault(DatabaseManager.this.mDatabase, calendar, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {//TODO: Widget
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(MainActivity.ACTION_CALENDARS_UPDATED));
//                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
//                    intent.setAction(MainActivity.ACTION_CALENDARS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
                }
            }
        }.execute();
    }

    public void deleteCalendar(Account calendar) {
        if (calendar != null) {
            processDeleteCalendar(calendar, true);
//            processDeleteCalendar(calendar, false);
        }
    }

    private void processDeleteCalendar(final Account calendar, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                DatabaseManager.this.open();

                CalendarsContract.delete(DatabaseManager.this.mDatabase, calendar, inMemory);
                TransactionsContract.deleteAllFromCalendar(DatabaseManager.this.mDatabase, calendar.getId());

                Intent intent = new Intent(NewCalendarActivity.CALENDAR_DELETED);
                Bundle b = new Bundle();
                b.putParcelable(CALENDAR_UPDATED, calendar);
                intent.putExtras(b);
                LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(intent);
                DatabaseManager.this.close();
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (inMemory) {//TODO: Widget
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(MainActivity.ACTION_CALENDARS_UPDATED));
//                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
//                    intent.setAction(MainActivity.ACTION_CALENDARS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
                }
            }
        }.execute();
    }

    public List<Account> getCalendarsWithBalance() {
        return CalendarsContract.getAllWithBalance(this.mDatabase);
    }

    public List<Account> getCalendars() {
        return CalendarsContract.getAll(this.mDatabase, true);
    }

    public String[] getCalendarIds() {
        List<Account> calendars = /*MainActivity.mDatabaseManager.*/getCalendars();
        String[] calendarIds = new String[calendars.size()];
        for (int i = 0; i < calendars.size(); i++) {
            calendarIds[i] = calendars.get(i).getId();
        }
        return calendarIds;
    }

    public Account getCalendarWithId(String id) {
        return CalendarsContract.getCalendar(this.mDatabase, id);
    }

    public String getDefaultAccountId() {
        return CalendarsContract.getDefaultAccountId(this.mDatabase);
    }

    public void insertCategory(Category category) {
        if (category != null) {
//            CategoriesContract.insert(this.mDatabase, category, true);
            processInsertCategory(category, false);
        }
        try {
            Map<String, String> parameters = new HashMap();
//            parameters.put(FlurryManager.PARAM_CATEGORY_TYPE, category.isExpense() ? FlurryManager.VALUE_ADD_TRANSACTION_EXPENSE : FlurryManager.VALUE_ADD_TRANSACTION_INCOME);
//            parameters.put(FlurryManager.PARAM_CATEGORY_NAME, category.getName());
//            parameters.put(FlurryManager.PARAM_ICON_INDEX, Integer.toString(category.getIconIndex()));
//            parameters.put(FlurryManager.PARAM_COLOR_INDEX, Integer.toString(category.getColorIndex()));
//            FlurryManager.log(FlurryManager.EVENT_ADD_CATEGORY, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processInsertCategory(final Category category, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                CategoriesContract.insert(DatabaseManager.this.mDatabase, category, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                enviarAvisoDeActualizacion(NewCategoryActivity.CATEGORIA_INSERTADA);
                if (!inMemory) {
                }
            }
        }.execute();
    }

    public void insertCategories(List<Category> categories) {
        if (!TypeUtils.isEmpty((List) categories)) {
            CategoriesContract.insert(this.mDatabase, (List) categories, true);
            CategoriesContract.insert(this.mDatabase, (List) categories, false);
        }
    }

    public void updateCategory(Category category) {
        if (category != null) {
//            CategoriesContract.update(this.mDatabase, category, true);
            processUpdateCategory(category, false);
        }
    }

    private void processUpdateCategory(final Category category, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                CategoriesContract.update(DatabaseManager.this.mDatabase, category, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                enviarAvisoDeActualizacion(NewCategoryActivity.CATEGORIA_INSERTADA);
            }
        }.execute();
    }

    public void deleteCategory(Category category) {
        if (category != null) {
            processDeleteCategory(category, true);
//            processDeleteCategory(category, false);
        }
    }

    private void processDeleteCategory(final Category category, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                TransactionsContract.delete(DatabaseManager.this.mDatabase, category.getId(), inMemory);
                BudgetsContract.delete(DatabaseManager.this.mDatabase, category.getId(), inMemory);
                CategoriesContract.delete(DatabaseManager.this.mDatabase, category, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                enviarAvisoDeActualizacion(NewCategoryActivity.CATEGORIA_BORRADA);
                if (inMemory) {////TODO: Widget
//                    Log.d("SyncBug", "DatabaseManager - processDeleteCategory");
//                    LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(CalendarFragment.ACTION_TRANSACTIONS_UPDATED));
//                    Intent intent = new Intent(DatabaseManager.this.mContext, DollarbirdAppWidgetProvider.class);
//                    intent.setAction(CalendarFragment.ACTION_TRANSACTIONS_UPDATED);
//                    DatabaseManager.this.mContext.sendBroadcast(intent);
                }
            }
        }.execute();
    }

    public List<Category> getExpenseCategories() {
        return CategoriesContract.getAll(this.mDatabase, 1, false, true);
    }

    public List<Category> getIncomeCategories() {
        return CategoriesContract.getAll(this.mDatabase, 0, false, true);
    }

    public Category getFirstExepenseCategory() {
        return CategoriesContract.getFirstCategory(this.mDatabase, true);
    }

    public Category getFirstIncomeCategory() {
        return CategoriesContract.getFirstCategory(this.mDatabase, false);
    }

    public int getHighestCategoryOrder(boolean isExpense) {
        return CategoriesContract.getHighestCategoryOrder(this.mDatabase, isExpense);
    }

    public void reorderCategories(List<Category> categories) {
        if (!TypeUtils.isEmpty((List) categories)) {
            processReorderCategory(categories, true);
            processReorderCategory(categories, false);
        }
    }

    private void processReorderCategory(final List<Category> categories, final boolean inMemory) {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                CategoriesContract.reorder(DatabaseManager.this.mDatabase, categories, inMemory);
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    public Device getDevice() {
        return DevicesContract.getDevice(this.mDatabase);
    }

    public Device getDeviceForAuthentication(boolean registering, String email) {
        return DevicesContract.getDeviceForAuthentication(this.mDatabase, registering, email);
    }

    public void insertDevice(Device device) {
        if (device != null) {
            processInsertDevice(device, true);
            processInsertDevice(device, false);
        }
    }

    private void processInsertDevice(Device device, boolean inMemory) {
        DevicesContract.insert(this.mDatabase, device, inMemory);
    }

    public void updateDevice(Device device) {
        if (device != null) {
            processUpdateDevice(device, true);
            processUpdateDevice(device, false);
        }
//        FlurryManager.log(FlurryManager.EVENT_RENAME_DEVICE);
    }

    private void processUpdateDevice(Device device, boolean inMemory) {
        DevicesContract.update(this.mDatabase, device, inMemory);
    }

    public void insertDevices(List<Device> devices) {
        if (!TypeUtils.isEmpty((List) devices)) {
            DevicesContract.insertOrUpdate(this.mDatabase, devices, true);
            DevicesContract.insertOrUpdate(this.mDatabase, devices, false);
        }
    }

    public void cloneDevice() {
        DevicesContract.cloneDataForRegisteredUser(this.mDatabase, true);
        DevicesContract.cloneDataForRegisteredUser(this.mDatabase, false);
    }

    public void insertRepeatInfos(List<RepeatInfo> infos) {
        if (!TypeUtils.isEmpty((List) infos)) {
            RepeatInfoContract.insert(this.mDatabase, (List) infos, true);
            RepeatInfoContract.insert(this.mDatabase, (List) infos, false);
        }
    }

    public List<Reminder> getReminders() {
        return null;
    }

    public void insertReminders(List<Reminder> reminders) {
        if (!TypeUtils.isEmpty((List) reminders)) {
        }
    }

    public void resetAllData(boolean fromThisDevice) {
        int userId = PersistentStorage.getUserId();
        BudgetsContract.deleteAllBudgetsForTheCurrentUser(this.mDatabase, 0);
        CategoriesContract.deleteCategoriesForUser(this.mDatabase, userId, 0);
        TransactionsContract.deleteTransactionsForUser(this.mDatabase, userId, 0);
        RepeatInfoContract.deleteRepeatInfosForUser(this.mDatabase, userId, 0);
        Account defaultCalendar = null;
        if (fromThisDevice) {
            defaultCalendar = CalendarsContract.getDefaultCalendar(this.mDatabase);
        }
        CalendarsContract.deleteAllCalendarsOfTheCurrentUser(this.mDatabase, false, 0);
        if (fromThisDevice && defaultCalendar != null) {
            defaultCalendar.setName(this.mContext.getString(R.string.default_calendar_name));
            defaultCalendar.setUpdateDate(0);
            PersistentStorage.setSelectedCalendar(new String[]{defaultCalendar.getId()});
        }
        CategoriesContract.factoryResetCategories(this.mDatabase);
        resetInMemoryDatabase();
        if (fromThisDevice && defaultCalendar != null) {
            CalendarsContract.update(this.mDatabase, defaultCalendar, false);
        }
        //LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(new Intent(RenewSubscriptionActivity.ACTION_DOWNGRADE_COMPLETED)); TODO: RenewSubscription
    }

    public void downgradeAccount() {
        new AsyncTask<Object, Void, Void>() {
            protected Void doInBackground(Object... params) {
                DatabaseManager.this.deleteFreeUserData();
                String freeUserAccountId = CalendarsContract.getFreeUserCalendarId(DatabaseManager.this.mDatabase);
                String freeUserDeviceId = DevicesContract.getFreeUserDeviceId(DatabaseManager.this.mDatabase);
                List<Transaction> transactions = TransactionsContract.getTransactionsForTimeRange(DatabaseManager.this.mDatabase, DatabaseManager.getCalendarIdsFromList(CalendarsContract.getAllCalendarIds(DatabaseManager.this.mDatabase, true)), null, null, -1, null, -1, true, true);
                List<Transaction> transactionsWithoutTransfer = new ArrayList<>();
                for (Transaction transaction : transactions) {
                    if (!transaction.isTransfer()) {
                        transaction.setCalendarId(freeUserAccountId);
                        transaction.setDeviceId(freeUserDeviceId);
                        transactionsWithoutTransfer.add(transaction);
                    }
                }
                TransactionsContract.insert(DatabaseManager.this.mDatabase, (List) transactionsWithoutTransfer, false);
                RepeatInfoContract.insert(DatabaseManager.this.mDatabase, (List) RepeatInfoContract.getAllRepeatInfosForTheCurrentUser(DatabaseManager.this.mDatabase, false), false);
                List<Category> categories = CategoriesContract.getAll(DatabaseManager.this.mDatabase, -1, true, false);
                for (Category category : categories) {
                    category.setUserId(0);
                }
                DatabaseManager.this.deleteAllDataOfTheCurrentUserUntil(0);
                PersistentStorage.resetUserData();
                PersistentStorage.setRunMode(true);
                CategoriesContract.insert(DatabaseManager.this.mDatabase, (List) categories, false);
                DatabaseManager.this.resetInMemoryDatabase();
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
//                LocalBroadcastManager.getInstance(DatabaseManager.this.mContext).sendBroadcast(new Intent(RenewSubscriptionActivity.ACTION_DOWNGRADE_COMPLETED));
            }
        }.execute();
    }

    private void deleteFreeUserData() {
        CategoriesContract.deleteCategoriesForUser(this.mDatabase, 0, 0);
        TransactionsContract.deleteTransactionsForUser(this.mDatabase, 0, 0);
        RepeatInfoContract.deleteRepeatInfosForUser(this.mDatabase, 0, 0);
    }

    public void deleteAllDataOfTheCurrentUserUntil(long toTime) {
        int userId = PersistentStorage.getUserId();
        BudgetsContract.deleteAllBudgetsForTheCurrentUser(this.mDatabase, toTime);
        DevicesContract.deleteAllDevicesOfTheCurrentUser(this.mDatabase, false);
        CategoriesContract.deleteCategoriesForUser(this.mDatabase, userId, toTime);
        TransactionsContract.deleteTransactionsForUser(this.mDatabase, userId, toTime);
        RepeatInfoContract.deleteRepeatInfosForUser(this.mDatabase, userId, toTime);
        CalendarsContract.deleteAllCalendarsOfTheCurrentUser(this.mDatabase, false, toTime);
    }

    public String getSelectedCalendarNames(Context context, String[] calendar, String basicTitle, String proPrefix) {
        if (PersistentStorage.isFreeVersionRunning()) {
            return basicTitle;
        }
        String title = proPrefix;
        if (calendar != null && calendar.length > 0) {
            if (!(title == null || title.isEmpty())) {
                title = title + " - ";
            }
            List<Account> calendars = /*MainActivity.mDatabaseManager.*/getCalendars();
            List<String> calendarIds = getCalendarIdsFromList(calendar);
            for (int i = 0; i < calendars.size(); i++) {
                if (calendarIds.contains(calendars.get(i).getId())) {
                    title = title + calendars.get(i).getName() + ", ";
                }
            }
            if (title.length() > 3) {
                title = title.substring(0, title.length() - 2);
            }
        }
        return title;
    }

    public Account getDefaultCalendar() {
        return CalendarsContract.getDefaultCalendar(this.mDatabase);
    }

    public String[] getIncomeCategoriesString() {
        ArrayList<Category> cats = new ArrayList<>();
        cats.addAll(getIncomeCategories());
        String[] calendarIds = new String[cats.size()];
        for (int i = 0; i < cats.size(); i++) {
            calendarIds[i] = cats.get(i).getName();
        }
        return calendarIds;
    }

    public String[] getExpenseCategoriesString() {
        ArrayList<Category> cats = new ArrayList<>();
        cats.addAll(getExpenseCategories());
        String[] calendarIds = new String[cats.size()];
        for (int i = 0; i < cats.size(); i++) {
            calendarIds[i] = cats.get(i).getName();
        }
        return calendarIds;
    }

    public List<Factura> getFacturasFromTransaction(Transaction transaction) {
        return FacturasContract.getFacturasForTransaction(DatabaseManager.this.mDatabase, transaction);
    }
}
