package io.github.rathn.platap.persistent.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.rathn.platap.BuildConfig;
import io.github.rathn.platap.constants.RestfulConstants;
import io.github.rathn.platap.dto.Category;
import io.github.rathn.platap.persistent.DatabaseOpenHelper;
import io.github.rathn.platap.persistent.PersistentStorage;
import io.github.rathn.platap.persistent.contracts.base.BaseContract;

//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.example.neriortez.dolarpajaro.restful.RestfulConstants;
//import org.askerov.dynamicgrid.BuildConfig;

public class CategoriesContract extends BaseContract {
    public static final String BALANCE_UPDATE_ID = "balanceupdate";
    public static final int CATEGORY_INCOME_AND_EXPENSE = -1;
    public static final int CATEGORY_IS_EXPENSE = 1;
    public static final int CATEGORY_IS_INCOME = 0;
    public static final String TRANSFER_CATEGORY_ID = "transfer";

    public static abstract class CategoryEntry implements BaseColumns {
        public static final String COLUMN_COLOR_INDEX = "category_color_index";
        public static final String COLUMN_DELETED = "category_deleted";
        public static final String COLUMN_ICON_INDEX = "category_icon_index";
        public static final String COLUMN_ID = "category_id";
        public static final String COLUMN_IS_EXPENSE = "category_is_expense";
        public static final String COLUMN_NAME = "category_name";
        public static final String COLUMN_ORDER = "category_order";
        public static final String COLUMN_UPDATE_DATE = "category_update_date";
        public static final String COLUMN_USER_ID = "category_user_id";
        public static final String TABLE_NAME = "Categories";

        public static String getTableName(boolean inMemory) {
            StringBuffer tableName = new StringBuffer();
            if (!inMemory) {
                tableName.append(DatabaseOpenHelper.LOCAL_DATABASE_PREFIX).append("");
            }
            tableName.append(TABLE_NAME);
            return tableName.toString();
        }
    }

    public static List<Category> getAll(SQLiteDatabase database, int categoryType, boolean getExtraCategories, boolean inMemory) {
        List<Category> categories = new ArrayList<>();
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM ").append(CategoryEntry.getTableName(inMemory));
        query.append(" WHERE ");
        if (categoryType != CATEGORY_INCOME_AND_EXPENSE) {
            query.append(CategoryEntry.COLUMN_IS_EXPENSE).append(" = ").append(categoryType).append(" AND ");
        }
        if (!getExtraCategories) {
            query.append(CategoryEntry.COLUMN_ID).append(" NOT LIKE '%").append(BALANCE_UPDATE_ID).append("%' AND ");
            query.append(CategoryEntry.COLUMN_ID).append(" NOT LIKE '%").append(TRANSFER_CATEGORY_ID).append("%' AND ");
        }
        query.append(CategoryEntry.COLUMN_DELETED).append(" = 0 AND ");
        query.append(CategoryEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        query.append(" ORDER BY ").append(CategoryEntry.COLUMN_ORDER).append(" ASC");
        Cursor cursor = database.rawQuery(query.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    categories.add(getCategory(cursor));
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return categories;
    }

    public static String getBalanceUpdateCategoryId(SQLiteDatabase database) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT ").append(CategoryEntry.COLUMN_ID).append(" FROM ").append(CategoryEntry.TABLE_NAME);
        query.append(" WHERE ").append(CategoryEntry.COLUMN_ID).append(" LIKE '%").append(BALANCE_UPDATE_ID).append("%' AND ").append(CategoryEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        Cursor cursor = database.rawQuery(query.toString(), null);
        String categoryId = BuildConfig.FLAVOR;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                categoryId = cursor.getString(CATEGORY_IS_INCOME);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return categoryId;
    }

    public static String getTransferCategoryId(SQLiteDatabase database) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT ").append(CategoryEntry.COLUMN_ID).append(" FROM ").append(CategoryEntry.TABLE_NAME);
        query.append(" WHERE ").append(CategoryEntry.COLUMN_ID).append(" LIKE '%").append(TRANSFER_CATEGORY_ID).append("%' AND ").append(CategoryEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        Cursor cursor = database.rawQuery(query.toString(), null);
        String categoryId = BuildConfig.FLAVOR;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                categoryId = cursor.getString(CATEGORY_IS_INCOME);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return categoryId;
    }

    public static Category getFirstCategory(SQLiteDatabase database, boolean isExpense) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM ").append(CategoryEntry.TABLE_NAME);
        query.append(" WHERE ").append(CategoryEntry.COLUMN_IS_EXPENSE).append(" = ").append(isExpense ? CATEGORY_IS_EXPENSE : CATEGORY_IS_INCOME).append(" AND ");
        query.append(CategoryEntry.COLUMN_ID).append(" NOT LIKE '%").append(BALANCE_UPDATE_ID).append("%' AND ");
        query.append(CategoryEntry.COLUMN_DELETED).append(" = 0 AND ");
        query.append(CategoryEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        query.append(" ORDER BY ").append(CategoryEntry.COLUMN_ORDER).append(" ASC LIMIT 1");
        Cursor cursor = database.rawQuery(query.toString(), null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                return getCategory(cursor);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    public static int getHighestCategoryOrder(SQLiteDatabase database, boolean isExpense) {
        StringBuffer query = new StringBuffer();
        query.append("SELECT MAX(").append(CategoryEntry.COLUMN_ORDER).append(") FROM ").append(CategoryEntry.TABLE_NAME);
        query.append(" WHERE ").append(CategoryEntry.COLUMN_IS_EXPENSE).append(" = ").append(isExpense ? CATEGORY_IS_EXPENSE : CATEGORY_IS_INCOME);
        query.append(" AND ").append(CategoryEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        Cursor cursor = database.rawQuery(query.toString(), null);
        int highestOrder = Integer.MAX_VALUE;
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                highestOrder = cursor.getInt(CATEGORY_IS_INCOME);
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return highestOrder;
    }

//    public static JsonArray getSyncables(SQLiteDatabase database) {
//        JsonArray categories = new JsonArray();
//        StringBuffer query = new StringBuffer();
//        query.append("SELECT * FROM ").append(CategoryEntry.TABLE_NAME);
//        query.append(" WHERE ").append(CategoryEntry.COLUMN_UPDATE_DATE).append(" > ").append(BaseContract.getModifiedLastSyncDate());
//        query.append(" AND ").append(CategoryEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
//        Cursor cursor = database.rawQuery(query.toString(), null);
//        if (cursor != null && cursor.getCount() > 0) {
//            if (cursor.moveToFirst()) {
//                while (!cursor.isAfterLast()) {
//                    categories.add(getJsonObject(cursor));
//                    cursor.moveToNext();
//                }
//            }
//            if (!cursor.isClosed()) {
//                cursor.close();
//            }
//        }
//        return categories;
//    }

    public static long insert(SQLiteDatabase database, Category category, boolean inMemory) {
        return database.insert(CategoryEntry.getTableName(inMemory), null, getContentValues(category));
    }

    public static void insert(SQLiteDatabase database, List<Category> categories, boolean inMemory) {
        database.beginTransaction();
        try {
            for (Category category : categories) {
                delete(database, category, inMemory);
                String tableName = CategoryEntry.getTableName(inMemory);
                String str = "category_id = ? AND category_user_id = " + PersistentStorage.getUserId();
                String[] strArr = new String[CATEGORY_IS_EXPENSE];
                strArr[CATEGORY_IS_INCOME] = category.getId();
                database.delete(tableName, str, strArr);
                insert(database, category, inMemory);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public static int update(SQLiteDatabase database, Category category, boolean inMemory) {
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(CategoryEntry.COLUMN_ID).append(" = ?");
        String[] whereArgs = new String[CATEGORY_IS_EXPENSE];
        whereArgs[CATEGORY_IS_INCOME] = category.getId();
        return database.update(CategoryEntry.getTableName(inMemory), getContentValues(category), whereClause.toString(), whereArgs);
    }

    public static void reorder(SQLiteDatabase database, List<Category> newCategories, boolean inMemory) {
        database.beginTransaction();
        int index = CATEGORY_IS_INCOME;
        while (index < newCategories.size()) {
            try {
                ((Category) newCategories.get(index)).setOrder(index + CATEGORY_IS_EXPENSE);
                update(database, (Category) newCategories.get(index), inMemory);
                index += CATEGORY_IS_EXPENSE;
            } finally {
                database.endTransaction();
            }
        }
        database.setTransactionSuccessful();
    }

    public static void delete(SQLiteDatabase database, Category category, boolean inMemory) {
        ContentValues values = new ContentValues();
        values.put(CategoryEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate()));
        values.put(CategoryEntry.COLUMN_DELETED, Integer.valueOf(CATEGORY_IS_EXPENSE));
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(CategoryEntry.COLUMN_ID).append(" = ? AND ").append(CategoryEntry.COLUMN_USER_ID).append(" = ?");
        database.update(CategoryEntry.getTableName(inMemory), values, whereClause.toString(), new String[]{category.getId(), Integer.toString(PersistentStorage.getUserId())});
    }

    public static void deleteCategoriesForUser(SQLiteDatabase database, int userId, long toDate) {
        StringBuffer whereClause = new StringBuffer();
        whereClause.append(CategoryEntry.COLUMN_USER_ID).append(" = ").append(userId);
        if (toDate > 0) {
            whereClause.append(" AND ").append(CategoryEntry.COLUMN_UPDATE_DATE).append(" < ").append(toDate);
        }
        database.delete(CategoryEntry.getTableName(false), whereClause.toString(), null);
    }

    public static void factoryResetCategories(SQLiteDatabase database) {
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ").append(CategoryEntry.getTableName(false)).append(" VALUES ");
        query.append("('1_userid','Other',15,1,0,1,update_date,userid,0),");
        query.append("('2_userid','Other',15,0,0,2,update_date,userid,0),");
        query.append("('3_userid','Groceries',4,1,6,3,update_date,userid,0), ");
        query.append("('4_userid','Household',5,1,8,4,update_date,userid,0),");
        query.append("('5_userid','Eating Out',0,1,2,5,update_date,userid,0),");
        query.append("('6_userid','Fun',12,1,4,6,update_date,userid,0),");
        query.append("('7_userid','Gifts',1,1,5,7,update_date,userid,0),");
        query.append("('8_userid','Clothing',6,1,1,8,update_date,userid,0),");
        query.append("('9_userid','Car',25,1,11,9,update_date,userid,0),");
        query.append("('10_userid','Transport',22,1,12,10,update_date,userid,0),");
        query.append("('11_userid','Travel',18,1,13,11,update_date,userid,0),");
        query.append("('12_userid','Medical',13,1,7,12,update_date,userid,0),");
        query.append("('13_userid','Education',21,1,3,13,update_date,userid,0),");
        query.append("('14_userid','Utilities',33,1,14,14,update_date,userid,0), ");
        query.append("('15_userid','Rent / Loan',20,1,9,15,update_date,userid,0),");
        query.append("('19_userid','Salary',10,0,10,16,update_date,userid,0),");
        query.append("('balanceupdate_userid','Balance Update',0,0,0,0,update_date,userid,0)");
        if (!PersistentStorage.isFreeVersionRunning()) {
            query.append(", ('transfer_userid','Transfer',0,0,0,0,update_date,userid,0)");
        }
        Matcher matcher = Pattern.compile("userid").matcher(query.toString());
        query.setLength(CATEGORY_IS_INCOME);
        while (matcher.find()) {
            matcher.appendReplacement(query, Integer.toString(PersistentStorage.getUserId()));
        }
        matcher.appendTail(query);
        matcher = Pattern.compile(RestfulConstants.TRANSACTION_UPDATE_DATE).matcher(query.toString());
        query.setLength(CATEGORY_IS_INCOME);
        while (matcher.find()) {
            matcher.appendReplacement(query, Long.toString(BaseContract.getUpdateDate() + 1));
        }
        matcher.appendTail(query);
        database.execSQL(query.toString());
    }

    public static void cloneDataForRegisteredUser(SQLiteDatabase database, boolean inMemory) {
        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ").append(CategoryEntry.getTableName(inMemory)).append(" (");
        query.append(CategoryEntry.COLUMN_ID).append(", ");
        query.append(CategoryEntry.COLUMN_NAME).append(", ");
        query.append(CategoryEntry.COLUMN_COLOR_INDEX).append(", ");
        query.append(CategoryEntry.COLUMN_IS_EXPENSE).append(", ");
        query.append(CategoryEntry.COLUMN_ICON_INDEX).append(",");
        query.append(CategoryEntry.COLUMN_ORDER).append(", ");
        query.append(CategoryEntry.COLUMN_UPDATE_DATE).append(",");
        query.append(CategoryEntry.COLUMN_USER_ID).append(", ");
        query.append(CategoryEntry.COLUMN_DELETED).append(") ");
        query.append("SELECT ");
        query.append(CategoryEntry.COLUMN_ID).append(" || '_").append(PersistentStorage.getUserId()).append("', ");
        query.append(CategoryEntry.COLUMN_NAME).append(", ");
        query.append(CategoryEntry.COLUMN_COLOR_INDEX).append(", ");
        query.append(CategoryEntry.COLUMN_IS_EXPENSE).append(", ");
        query.append(CategoryEntry.COLUMN_ICON_INDEX).append(",");
        query.append(CategoryEntry.COLUMN_ORDER).append(", ");
        query.append(BaseContract.getUpdateDate()).append(",");
        query.append(PersistentStorage.getUserId()).append(", ");
        query.append(CATEGORY_IS_INCOME);
        query.append(" FROM ").append(CategoryEntry.getTableName(inMemory));
        query.append(" WHERE ").append(CategoryEntry.COLUMN_USER_ID).append(" = 0 AND ").append(CategoryEntry.COLUMN_DELETED).append(" = 0");
        database.execSQL(query.toString());
    }

    private static ContentValues getContentValues(Category category) {
        ContentValues values = new ContentValues();
        values.put(CategoryEntry.COLUMN_ID, category.getId());
        values.put(CategoryEntry.COLUMN_NAME, category.getName());
        values.put(CategoryEntry.COLUMN_COLOR_INDEX, Integer.valueOf(category.getColorIndex()));
        values.put(CategoryEntry.COLUMN_IS_EXPENSE, Boolean.valueOf(category.isExpense()));
        values.put(CategoryEntry.COLUMN_ICON_INDEX, Integer.valueOf(category.getIconIndex()));
        values.put(CategoryEntry.COLUMN_ORDER, Integer.valueOf(category.getOrder()));
        values.put(CategoryEntry.COLUMN_USER_ID, Integer.valueOf(PersistentStorage.getUserId()));
        values.put(CategoryEntry.COLUMN_UPDATE_DATE, Long.valueOf(BaseContract.getUpdateDate(category.getUpdateDate())));
        values.put(CategoryEntry.COLUMN_DELETED, Integer.valueOf(category.getDeleted()));
        return values;
    }

    public static Category getCategory(Cursor cursor) {
        boolean z = true;
        Category category = new Category();
        category.setId(cursor.getString(cursor.getColumnIndex(CategoryEntry.COLUMN_ID)));
        category.setName(cursor.getString(cursor.getColumnIndex(CategoryEntry.COLUMN_NAME)));
        category.setColorIndex(cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_COLOR_INDEX)));
        if (cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_IS_EXPENSE)) != CATEGORY_IS_EXPENSE) {
            z = false;
        }
        category.setIsExpense(z);
        category.setIconIndex(cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_ICON_INDEX)));
        category.setOrder(cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_ORDER)));
        category.setUserId(cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_USER_ID)));
        return category;
    }

//    private static JsonObject getJsonObject(Cursor cursor) {
//        JsonObject category = new JsonObject();
//        category.addProperty(CategoryEntry.COLUMN_ID, cursor.getString(cursor.getColumnIndex(CategoryEntry.COLUMN_ID)));
//        int deleted = cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_DELETED));
//        if (deleted != CATEGORY_IS_EXPENSE) {
//            category.addProperty(CategoryEntry.COLUMN_NAME, cursor.getString(cursor.getColumnIndex(CategoryEntry.COLUMN_NAME)));
//            category.addProperty(CategoryEntry.COLUMN_COLOR_INDEX, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_COLOR_INDEX))));
//            category.addProperty(CategoryEntry.COLUMN_IS_EXPENSE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_IS_EXPENSE))));
//            category.addProperty(CategoryEntry.COLUMN_ICON_INDEX, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_ICON_INDEX))));
//            category.addProperty(CategoryEntry.COLUMN_ORDER, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_ORDER))));
//            category.addProperty(CategoryEntry.COLUMN_USER_ID, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_USER_ID))));
//        }
//        category.addProperty(CategoryEntry.COLUMN_UPDATE_DATE, Integer.valueOf(cursor.getInt(cursor.getColumnIndex(CategoryEntry.COLUMN_UPDATE_DATE))));
//        category.addProperty(CategoryEntry.COLUMN_DELETED, Integer.valueOf(deleted));
//        return category;
//    }
}
