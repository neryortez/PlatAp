package io.github.rathn.platap.persistent.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.github.rathn.platap.dto.Budget;
import io.github.rathn.platap.persistent.DatabaseOpenHelper;
import io.github.rathn.platap.persistent.PersistentStorage;
import io.github.rathn.platap.persistent.contracts.CategoriesContract.CategoryEntry;
import io.github.rathn.platap.persistent.contracts.TransactionsContract.TransactionEntry;
import io.github.rathn.platap.persistent.contracts.base.BaseContract;
import io.github.rathn.platap.utils.DateTimeUtils;

//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;

public class BudgetsContract extends BaseContract {

    public static abstract class BudgetEntry implements BaseColumns {
        public static final String BUDGET_SUM = "budget_sum";
        public static final String COLUMN_CATEGORY_ID = "budget_category_id";
        public static final String COLUMN_DATE = "budget_date";
        public static final String COLUMN_DELETED = "budget_deleted";
        public static final String COLUMN_ID = "budget_id";
        public static final String COLUMN_UPDATE_DATE = "budget_update_date";
        public static final String COLUMN_USER_ID = "budget_user_id";
        public static final String COLUMN_VALUE = "budget_value";
        public static final String SPENT_SUM = "spent_sum";
        public static final String TABLE_NAME = "Budgets";

        public static String getTableName(boolean inMemory) {
            StringBuffer tableName = new StringBuffer();
            if (!inMemory) {
                tableName.append(DatabaseOpenHelper.LOCAL_DATABASE_PREFIX).append("");
            }
            tableName.append(TABLE_NAME);
            return tableName.toString();
        }
    }

    public static List<Budget> getAllByIntervall(SQLiteDatabase database, Calendar startDate, Calendar endDate) {
        List<Budget> budgets = new ArrayList();
        StringBuffer query2 = new StringBuffer();
        query2.append("SELECT * FROM ").append(CategoryEntry.TABLE_NAME);
        query2.append(" INNER JOIN ").append(BudgetEntry.TABLE_NAME).append(" ON ").append(CategoryEntry.COLUMN_ID).append(" = ").append(BudgetEntry.COLUMN_CATEGORY_ID).append(" LEFT JOIN ");
        query2.append("(SELECT ").append(TransactionEntry.COLUMN_CATEGORY_ID).append(", SUM(").append(TransactionEntry.COLUMN_PRICE).append(") AS ");
        query2.append(BudgetEntry.SPENT_SUM).append(" FROM ").append(TransactionEntry.TABLE_NAME).append(" WHERE ").append(TransactionEntry.COLUMN_DATE).append(" >= ");
        query2.append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(startDate)).append(" AND ").append(TransactionEntry.COLUMN_DATE).append(" < ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(endDate)).append(" AND ").append(TransactionEntry.COLUMN_DELETED);
        query2.append(" = ? ").append(" AND ").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = ? ").append(" AND ");
        query2.append(TransactionEntry.COLUMN_IS_EXPENSE).append(" = ? ").append(" GROUP BY ").append(TransactionEntry.COLUMN_CATEGORY_ID).append(") t ON t.");
        query2.append(TransactionEntry.COLUMN_CATEGORY_ID).append(" = ").append(CategoryEntry.COLUMN_ID).append(" WHERE ").append(BudgetEntry.COLUMN_DATE);
        query2.append(" = ").append(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(startDate)).append(" AND ").append(CategoryEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        query2.append(" AND ").append(BudgetEntry.COLUMN_DELETED).append(" = ? ").append(" ORDER BY ").append(BudgetEntry.COLUMN_VALUE).append(" DESC ");
        Cursor cursor = database.rawQuery(query2.toString(), new String[]{"0", "0", "1", "0"});
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    budgets.add(getComplexBudget(cursor));
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return budgets;
    }

//    public static JsonArray getSyncables(SQLiteDatabase database) {
//        JsonArray budgets = new JsonArray();
//        StringBuffer query = new StringBuffer();
//        query.append("SELECT * FROM ").append(BudgetEntry.TABLE_NAME);
//        query.append(" JOIN ").append(CategoryEntry.TABLE_NAME).append(" ON ").append(BudgetEntry.COLUMN_CATEGORY_ID).append(" = ").append(CategoryEntry.COLUMN_ID);
//        query.append(" WHERE ").append(BudgetEntry.COLUMN_UPDATE_DATE).append(" > ").append(BaseContract.getModifiedLastSyncDate());
//        query.append(" AND ").append(CategoryEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
//        Cursor cursor = database.rawQuery(query.toString(), null);
//        if (cursor != null && cursor.getCount() > 0) {
//            if (cursor.moveToFirst()) {
//                while (!cursor.isAfterLast()) {
//                    budgets.add(getJsonObject(cursor));
//                    cursor.moveToNext();
//                }
//            }
//            if (!cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return budgets;
//    }

    public static long insert(SQLiteDatabase database, Budget budget, boolean inMemory) {
        if (budget.getDate() != null) {
            database.insert(BudgetEntry.getTableName(inMemory), null, getContentValues(budget));
        }
        return -1;
    }

    public static void insert(SQLiteDatabase database, List<Budget> budgets, boolean inMemory) {
        database.beginTransaction();
        try {
            for (Budget budget : budgets) {
                if (budget != null) {
                    delete(database, budget, inMemory);
                    database.delete(BudgetEntry.getTableName(inMemory), "budget_id = ?", new String[]{budget.getId()});
                    insert(database, budget, inMemory);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public static int update(SQLiteDatabase database, Budget budget, boolean inMemory) {
        if (budget.getDate() == null) {
            return 0;
        }
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(BudgetEntry.COLUMN_ID).append(" = ?");
        return database.update(BudgetEntry.getTableName(inMemory), getContentValues(budget), whereClause.toString(), new String[]{budget.getId()});
    }

    public static void delete(SQLiteDatabase database, Budget budget, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(BudgetEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate()));
        values.put(BudgetEntry.COLUMN_DELETED, Integer.valueOf(1));
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(BudgetEntry.COLUMN_ID).append(" = ?");
        database.update(BudgetEntry.getTableName(inMemory), values, whereClause.toString(), new String[]{budget.getId()});
    }

    public static void delete(SQLiteDatabase database, String categoryId, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(BudgetEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate()));
        values.put(BudgetEntry.COLUMN_DELETED, Integer.valueOf(1));
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(BudgetEntry.COLUMN_CATEGORY_ID).append(" = ?");
        database.update(BudgetEntry.getTableName(inMemory), values, whereClause.toString(), new String[]{categoryId});
    }

    public static void deleteAllBudgetsForTheCurrentUser(SQLiteDatabase database, long toDate) {
        StringBuffer whereClause = new StringBuffer();
        String where = null;
        if (toDate > 0) {
            whereClause.append(BudgetEntry.COLUMN_UPDATE_DATE).append(" < ").append(toDate);
            where = whereClause.toString();
        }
        database.delete(BudgetEntry.getTableName(false), where, null);
    }

    private static ContentValues getContentValues(Budget budget) {
        ContentValues values = new ContentValues();
        values.put(BudgetEntry.COLUMN_ID, budget.getId());
        values.put(BudgetEntry.COLUMN_CATEGORY_ID, budget.getCategoryId());
        values.put(BudgetEntry.COLUMN_DATE, Long.valueOf(DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(budget.getDate())));
        values.put(BudgetEntry.COLUMN_VALUE, Double.valueOf(budget.getValue()));
        values.put(BudgetEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate(budget.getUpdateDate())));
        values.put(BudgetEntry.COLUMN_DELETED, Integer.valueOf(budget.getDeleted()));
        return values;
    }

    private static Budget getComplexBudget(Cursor cursor) {
        Budget budget = new Budget();
        budget.setCategoryId(cursor.getString(cursor.getColumnIndex(BudgetEntry.COLUMN_CATEGORY_ID)));
        budget.setCategory(CategoriesContract.getCategory(cursor));
        budget.setId(cursor.getString(cursor.getColumnIndex(BudgetEntry.COLUMN_ID)));
        budget.setDate(DateTimeUtils.getLocalDateFromServerSpecificGmtTime((long) cursor.getInt(cursor.getColumnIndex(BudgetEntry.COLUMN_DATE))));
        budget.setValue(cursor.getDouble(cursor.getColumnIndex(BudgetEntry.COLUMN_VALUE)));
        budget.setSpentValue(Math.abs(cursor.getDouble(16)));
        return budget;
    }

//    private static JsonObject getJsonObject(Cursor cursor) {
//        JsonObject budget = new JsonObject();
//        budget.addProperty(BudgetEntry.COLUMN_ID, cursor.getString(0));
//        int deleted = cursor.getInt(5);
//        if (deleted != 1) {
//            budget.addProperty(BudgetEntry.COLUMN_CATEGORY_ID, cursor.getString(1));
//            budget.addProperty(BudgetEntry.COLUMN_DATE, Integer.valueOf(cursor.getInt(2)));
//            budget.addProperty(BudgetEntry.COLUMN_VALUE, Double.valueOf(cursor.getDouble(3)));
//        }
//        budget.addProperty(BudgetEntry.COLUMN_UPDATE_DATE, Integer.valueOf(cursor.getInt(4)));
//        budget.addProperty(BudgetEntry.COLUMN_DELETED, Integer.valueOf(deleted));
//        return budget;
//    }
}
