package io.github.rathn.platap.dto;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.github.rathn.platap.persistent.DatabaseOpenHelper;
import io.github.rathn.platap.persistent.contracts.base.BaseContract;

import static io.github.rathn.platap.dto.FacturasContract.FacturasEntry.TABLE_NAME;


public class FacturasContract extends BaseContract {


    public abstract static class FacturasEntry {
        public static final String COLUMN_ID = "factura_id";
        static final String COLUMN_TRANSACTION_ID = "factura_transaction_id";
        static final String COLUMN_PRICE = "factura_price";
        public static final String TABLE_NAME = "Facturas";
        static final String COLUMN_FILE_PATH = "factura_file_path";

        static String getTableName(boolean inMemory) {
            StringBuilder tableName = new StringBuilder();
            if (!inMemory) {
                tableName.append(DatabaseOpenHelper.LOCAL_DATABASE_PREFIX).append("");
            }
            tableName.append(TABLE_NAME);
            return tableName.toString();
        }
    }


    /**
     * Convenience method for inserting a <code>Factura</code> into the database.
     *
     * @param database the database to be inserted in
     * @param factura  factura to be inserted
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public static long insert(SQLiteDatabase database, Factura factura) {
        return database.insert(FacturasEntry.getTableName(false), null, getContentValues(factura));
    }

    public static void insert(SQLiteDatabase db, List<Factura> facturas) {
        for (Factura factura : facturas) {
            insert(db, factura);
        }
    }

    public static int update(SQLiteDatabase db, Factura factura) {
        return db.update(FacturasEntry.getTableName(false), getContentValues(factura), FacturasEntry.COLUMN_ID + " = ?", new String[]{factura.getId()});
    }

    public static int delete(SQLiteDatabase database, Factura factura) {
        return database.delete(FacturasEntry.getTableName(false), FacturasEntry.COLUMN_ID + " = ?", new String[]{factura.getId()});
    }

    public static long delete(SQLiteDatabase db, Transaction transaction) {
        return db.delete(FacturasEntry.getTableName(false), FacturasEntry.COLUMN_TRANSACTION_ID + " = ?", new String[]{transaction.getId()});
    }


    public static Factura getFactura(SQLiteDatabase database, String id) {
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + FacturasEntry.COLUMN_ID + " = '" + id + "'", null);
        Factura factura = new Factura();
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                factura = getFactura(cursor);
                cursor.moveToNext();
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return factura;
    }

    public static List<Factura> getFacturasForTransaction(SQLiteDatabase database, Transaction transaction) {
        List<Factura> facturas = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + FacturasEntry.COLUMN_TRANSACTION_ID + " = '" + transaction.getId() + "'", null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    facturas.add(getFactura(cursor));
                    cursor.moveToNext();
                }
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return facturas;
    }

    public static List<Factura> getFacturasForTransaction(SQLiteDatabase database, String transactionId) {
        Transaction t = new Transaction();
        t.setId(transactionId);
        return getFacturasForTransaction(database, t);
    }


    private static ContentValues getContentValues(Factura factura) {
        ContentValues values = new ContentValues();
        values.put(FacturasEntry.COLUMN_ID, factura.getId());
        values.put(FacturasEntry.COLUMN_TRANSACTION_ID, factura.getTransactionId());
        values.put(FacturasEntry.COLUMN_PRICE, factura.getPrice());
        values.put(FacturasEntry.COLUMN_FILE_PATH, factura.getFilePath());
        return values;
    }

    private static Factura getFactura(Cursor cursor) {
        Factura factura = new Factura();
        factura.setFilePath(cursor.getString(cursor.getColumnIndex(FacturasEntry.COLUMN_FILE_PATH)));
        factura.setId(cursor.getString(cursor.getColumnIndex(FacturasEntry.COLUMN_ID)));
        factura.setPrice(cursor.getDouble(cursor.getColumnIndex(FacturasEntry.COLUMN_PRICE)));
        factura.setTransactionId(cursor.getString(cursor.getColumnIndex(FacturasEntry.COLUMN_TRANSACTION_ID)));
        return factura;
    }
}
