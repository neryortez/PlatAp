package io.github.rathn.platap.persistent.contracts;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.github.rathn.platap.dto.BudgetHistory;
import io.github.rathn.platap.persistent.PersistentStorage;
import io.github.rathn.platap.persistent.contracts.BudgetsContract.BudgetEntry;
import io.github.rathn.platap.persistent.contracts.CategoriesContract.CategoryEntry;
import io.github.rathn.platap.persistent.contracts.TransactionsContract.TransactionEntry;
import io.github.rathn.platap.utils.DateTimeUtils;

public class BudgetHistoryContract {
    public static List<BudgetHistory> getAll(SQLiteDatabase database) {
        List<BudgetHistory> budgets = new ArrayList();
        StringBuffer query2 = new StringBuffer();
        query2.append("SELECT ").append("b.").append(BudgetEntry.COLUMN_DATE).append(", SUM (b.").append(BudgetEntry.COLUMN_VALUE).append(") AS ");
        query2.append(BudgetEntry.BUDGET_SUM).append(", t.").append(TransactionEntry.COLUMN_PRICE).append(" AS ").append(BudgetEntry.SPENT_SUM);
        query2.append(", t.tms FROM ").append(BudgetEntry.TABLE_NAME).append(" b, ").append(CategoryEntry.TABLE_NAME);
        query2.append(" c LEFT JOIN ( SELECT SUM ( ").append(TransactionEntry.COLUMN_PRICE).append(" ) AS ").append(TransactionEntry.COLUMN_PRICE);
        query2.append(", strftime('%s', date ( datetime (").append(TransactionEntry.COLUMN_DATE).append(", 'unixepoch') ,'start of month')) AS tms FROM ");
        query2.append(TransactionEntry.TABLE_NAME).append(", ").append(BudgetEntry.TABLE_NAME).append(" WHERE ").append(BudgetEntry.COLUMN_DATE).append(" = tms AND ");
        query2.append(TransactionEntry.COLUMN_CATEGORY_ID).append(" = ").append(BudgetEntry.COLUMN_CATEGORY_ID).append(" AND ").append(TransactionEntry.COLUMN_DELETED);
        query2.append(" = ? ").append(" AND ").append(TransactionEntry.COLUMN_IS_FORECASTED).append(" = ? ").append(" AND ");
        query2.append(TransactionEntry.COLUMN_IS_EXPENSE).append(" = ? ").append(" GROUP BY  tms )").append(" t ON t.tms = b.").append(BudgetEntry.COLUMN_DATE);
        query2.append(" WHERE b.").append(BudgetEntry.COLUMN_CATEGORY_ID).append(" = c.").append(CategoryEntry.COLUMN_ID);
        query2.append(" AND c.").append(CategoryEntry.COLUMN_USER_ID).append(" = ").append(PersistentStorage.getUserId());
        query2.append(" AND b.").append(BudgetEntry.COLUMN_DELETED).append(" = ? ");
        query2.append(" GROUP BY b.").append(BudgetEntry.COLUMN_DATE).append(" ORDER BY b.").append(BudgetEntry.COLUMN_DATE).append(" DESC ");
        Cursor cursor = database.rawQuery(query2.toString(), new String[]{"0", "0", "1", "0"});
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    budgets.add(getBudgetHistory(cursor));
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return budgets;
    }

    private static BudgetHistory getBudgetHistory(Cursor cursor) {
        BudgetHistory bh = new BudgetHistory();
        bh.setDate(DateTimeUtils.getLocalDateFromServerSpecificGmtTime((long) cursor.getInt(0)));
        bh.setBudgetSum(Math.abs(cursor.getDouble(1)));
        bh.setSpentSum(Math.abs(cursor.getDouble(2)));
        return bh;
    }
}
