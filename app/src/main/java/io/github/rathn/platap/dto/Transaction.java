package io.github.rathn.platap.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import io.github.rathn.platap.BuildConfig;
import io.github.rathn.platap.persistent.DatabaseManager;
import io.github.rathn.platap.persistent.contracts.CategoriesContract;
import io.github.rathn.platap.utils.DateTimeUtils;

import static io.github.rathn.platap.dto.RepeatInfo.INTERVAL_QUINCENA;
import static io.github.rathn.platap.dto.RepeatInfo.INTERVAL_WEEKENS;
import static io.github.rathn.platap.dto.RepeatInfo.INTERVAL_WEEK_DAY;
import static io.github.rathn.platap.dto.RepeatInfo.INTERVAL_WEIRD;
import static io.github.rathn.platap.dto.RepeatInfo.TYPE_DAYLY;
import static io.github.rathn.platap.dto.RepeatInfo.TYPE_IRREGULAR;
import static io.github.rathn.platap.dto.RepeatInfo.TYPE_MONTHLY;
//
//import com.example.neriortez.dolarpajaro.config.Logger;
//import com.example.neriortez.dolarpajaro.storage.nonpersistent.DollarbirdApplication;
//import com.example.neriortez.dolarpajaro.storage.persistent.contracts.CategoriesContract;
//import com.example.neriortez.dolarpajaro.utils.DateTimeUtils;
//import com.example.neriortez.dolarpajaro.utils.TypeUtils;
//import com.google.gson.annotations.SerializedName;

public class Transaction implements Parcelable {
    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };
    public static final int TRANSACTION_FORECAST_ALL = -1;
    public static final int TRANSACTION_IS_FORECASTED = 1;
    public static final int TRANSACTION_TYPE_ACTUAL = 0;
    public static final int TRANSACTION_TYPE_ALL_TRANSACTIONS = -1;
    public static final int TRANSACTION_TYPE_EXPENSE = 1;
    public static final int TRANSACTION_TYPE_INCOME = 0;
    public static final String VALUE_FORMAT = "0.00";
    private Account calendar;
//    @SerializedName("ca")
    private String calendarId;
    private Category category;
//    @SerializedName("c")
    private String categoryId;
//    @SerializedName("d")
    private Calendar date;
//    @SerializedName("deleted")
    private int deleted;
    private Device device;
//    @SerializedName("u")
    private String deviceId;
//    @SerializedName("e")
    private boolean expense;
//    @SerializedName("f")
    private boolean forecasted;
//    @SerializedName("id")
    private String id;
    private long idInAdapter;
    private boolean mIsBiggestPayment;
    private double mTransactionPercent;
//    @SerializedName("n")
    private String note;
//    @SerializedName("o")
    private String originalTransactionId;
//    @SerializedName("p")
    private double price;
    private Reminder reminder;
//    @SerializedName("re")
    private String reminderId;
    private RepeatInfo repeatInfo;
//    @SerializedName("ri")
    private String repeatInfoId;
//    @SerializedName("r")
    private boolean repeating;
//    @SerializedName("update_date")
    private int updateDate;

    private List<String> facturasId = new ArrayList<>();
    private List<Factura> facturas = new ArrayList<>();

    public Transaction() {
        this.mIsBiggestPayment = false;
        this.mTransactionPercent = Double.MIN_VALUE;
        this.note = BuildConfig.FLAVOR;
        this.calendarId = BuildConfig.FLAVOR;
        this.categoryId = BuildConfig.FLAVOR;
        this.repeatInfoId = BuildConfig.FLAVOR;
        this.reminderId = BuildConfig.FLAVOR;
        this.deviceId = BuildConfig.FLAVOR;
        this.originalTransactionId = BuildConfig.FLAVOR;
    }

    public Transaction(Calendar date, double price, Category category) {
        this.mIsBiggestPayment = false;
        this.mTransactionPercent = Double.MIN_VALUE;
        this.date = date;
        this.price = price;
        this.category = category;
    }

    public Transaction copy() {
        Transaction transaction = new Transaction();
        transaction.setId(this.id);
        transaction.setNote(this.note);
        transaction.setDate(this.date);
        transaction.setPrice(this.price);
        transaction.setExpense(this.expense);
        transaction.setRepeating(this.repeating);
        transaction.setForecasted(this.forecasted);
        transaction.setCalendarId(this.calendarId);
        transaction.setCategoryId(this.categoryId);
        transaction.setRepeatInfoId(this.repeatInfoId);
        transaction.setReminderId(this.reminderId);
        transaction.setDeviceId(this.deviceId);
        transaction.setOriginalTransactionId(this.id);
        transaction.setFacturasId(this.facturasId);
        return transaction;
    }

    public Transaction copyWithCategory() {
        Transaction transaction = new Transaction();
        transaction.setId(this.id);
        transaction.setNote(this.note);
        transaction.setDate(this.date);
        transaction.setPrice(this.price);
        transaction.setExpense(this.expense);
        transaction.setRepeating(this.repeating);
        transaction.setForecasted(this.forecasted);
        transaction.setCalendarId(this.calendarId);
        transaction.setCategoryId(this.categoryId);
        transaction.setRepeatInfoId(this.repeatInfoId);
        transaction.setReminderId(this.reminderId);
        transaction.setDeviceId(this.deviceId);
        transaction.setOriginalTransactionId(this.id);
        transaction.setCategory(this.category);
        transaction.setRepeatInfo(this.repeatInfo);
        transaction.setReminder(this.reminder);
        transaction.setDevice(this.device);
        transaction.setFacturasId(this.facturasId);
//        transaction.setCalendar(this.calendar);
        return transaction;
    }

    /**
     * Crea una lista de transacciones que seran insertadas en la base de datos, como parte de las
     * transacciones repetidas
     * @param dbmanager El DatabaseManager para obtener MetaData (Se puede simplificar)
     * @return La lista creada
     */
    public List<Transaction> getRepeatingTransactions(DatabaseManager dbmanager) {

        if (this.repeatInfo != null && this.repeatInfo.getInterval() > 0) {
            int difference = TRANSACTION_TYPE_INCOME;
            Calendar endDate;
            if (this.repeatInfo.getEndDate() == null)
                endDate = dbmanager.getMetaData().getRepeatingEndDate();
            else endDate = this.repeatInfo.getEndDate();
            switch (this.repeatInfo.getRepeatType()) {
                case TYPE_DAYLY /*0*/:
                    difference = DateTimeUtils.weeksBetween(this.repeatInfo.getStartDate(), endDate);
                    break;
                case TYPE_MONTHLY /*1*/:
                    difference = DateTimeUtils.monthsBetween(this.repeatInfo.getStartDate(), endDate);
                    break;
                case TYPE_IRREGULAR:
                    return createIrregularInterval(DateTimeUtils.clone(this.date));
            }
            difference /= this.repeatInfo.getInterval();
            Calendar today = DateTimeUtils.getCurrentDateWithoutTime();
            Calendar transactionDate = DateTimeUtils.clone(this.date);
            return createNormalInterval(difference, transactionDate, today);
        }
//        Logger.logRepeatingBug("======================================================");
        return null;
    }

    private ArrayList<Transaction> createIrregularInterval(Calendar transactionDate) {
        //TODO: Repasar funcionalidad y que este funcionando bien.
        ArrayList<Transaction> transactions = new ArrayList<>();
        int difference;
        Transaction transaction = copy();

        Calendar today = DateTimeUtils.getCurrentDateWithoutTime();

        transaction.setOriginalTransactionId(this.id);

        int differenceIndex = 1;

        switch (this.repeatInfo.getInterval()){
            case INTERVAL_WEEK_DAY:
                difference = DateTimeUtils.weeksBetween(this.repeatInfo.getStartDate(), this.repeatInfo.getEndDate());
                while (differenceIndex <= difference) {
                    transactionDate.add(Calendar.DATE, 1);
                    while (transactionDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
                            transactionDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                        transactions.add(formatTransaction(transactionDate, transaction, today));

                        transactionDate.add(Calendar.DATE, 1);
                    }
                    differenceIndex++;
                }
                break;
            case INTERVAL_WEEKENS:
                difference = DateTimeUtils.weeksBetween(this.repeatInfo.getStartDate(), this.repeatInfo.getEndDate());
                transactionDate.add(Calendar.DATE, 1);
                while (differenceIndex <= difference) {
                    while (transactionDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY &&
                            transactionDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY &&
                            differenceIndex <= difference) {
                        transactions.add(formatTransaction(transactionDate, transaction, today));
                        transactionDate.add(Calendar.DATE, 1);
                    }
                    transactionDate.add(Calendar.DATE, 6);
                    differenceIndex++;
                }
                break;
            case INTERVAL_QUINCENA:
                difference = DateTimeUtils.monthsBetween(this.repeatInfo.getStartDate(), this.repeatInfo.getEndDate());
                int firstDate = transactionDate.get(Calendar.DATE), secondDate;
                if (firstDate > 15){
                    secondDate = firstDate - 15;
                    transactionDate.add(Calendar.MONTH, 1);
                }
                else secondDate = firstDate + 15;
                if (secondDate > transactionDate.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    secondDate = transactionDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                }
                while (differenceIndex <= difference) {
                    transactionDate.set(Calendar.DATE, firstDate);
                    transactions.add(formatTransaction(transactionDate, transaction, today));
                    transactionDate.set(Calendar.DATE, secondDate);
                    transactions.add(formatTransaction(transactionDate, transaction, today));
                    transactionDate.add(Calendar.MONTH, 1);
                    differenceIndex++;
                }
                break;
            case INTERVAL_WEIRD:
                break;
        }
        return transactions;
    }

    private Transaction formatTransaction(Calendar transactionDate, Transaction transaction, Calendar today) {
        transaction.setId(UUID.randomUUID().toString());
        transaction.setDate(DateTimeUtils.clone(transactionDate));
        transaction.setForecasted(today.compareTo(transaction.getDate()) == TRANSACTION_TYPE_ALL_TRANSACTIONS /*-1*/);
        transaction.setOriginalTransactionId(this.id);
        return transaction;
    }

    private ArrayList<Transaction>  createNormalInterval(int difference, Calendar transactionDate, Calendar today){
        ArrayList<Transaction> transactions = new ArrayList<>();
        for (int differenceIndex = 1; differenceIndex <= difference; differenceIndex += 1) {
            Transaction transaction = copy();
//            transaction.setId(UUID.randomUUID().toString());
            switch (this.repeatInfo.getRepeatType()) {
                case TYPE_DAYLY /*0*/:
                    transactionDate.add(Calendar.DATE, this.repeatInfo.getInterval());
                    break;
                case TYPE_MONTHLY /*1*/:
                    transactionDate.add(Calendar.MONTH, this.repeatInfo.getInterval() * TRANSACTION_TYPE_EXPENSE);
                    break;
            }
//            transaction.setDate(DateTimeUtils.clone(transactionDate));
//            transaction.setForecasted(today.compareTo(transaction.getDate()) == TRANSACTION_TYPE_ALL_TRANSACTIONS /*-1*/);
            transactions.add(formatTransaction(transactionDate, transaction, today));
        }
        return transactions;
    }

    public boolean isOriginal() {
        return this.id.equalsIgnoreCase(this.originalTransactionId) || (this.originalTransactionId.isEmpty());
    }

    public String getId() {
        return this.id;
    }

    public String getNote() {
        return this.note;
    }

    public Calendar getDate() {
        return this.date;
    }

    public double getPrice() {
        return this.price;
    }

    public boolean isExpense() {
        return this.expense;
    }

    public boolean isRepeating() {
        return this.repeating;
    }

    public boolean isForecasted() {
        return this.forecasted;
    }

    public String getCalendarId() {
        return this.calendarId;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public String getRepeatInfoId() {
        return this.repeatInfoId;
    }

    public String getReminderId() {
        return this.reminderId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public String getOriginalTransactionId() {
        return this.originalTransactionId;
    }

    public Category getCategory() {
        return this.category;
    }
//
    public RepeatInfo getRepeatInfo() {
        return this.repeatInfo;
    }
//
    public Reminder getReminder() {
        return this.reminder;
    }

    public Device getDevice() {
        return this.device;
    }

//    public Account getCalendar() {
//        return this.calendar;
//    }

    public long getIdInAdapter() {
        return this.idInAdapter;
    }

    public int getUpdateDate() {
        return this.updateDate;
    }

    public int getDeleted() {
        return this.deleted;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setExpense(boolean expense) {
        this.expense = expense;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public void setForecasted(boolean forecasted) {
        this.forecasted = forecasted;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setRepeatInfoId(String repeatInfoId) {
        this.repeatInfoId = repeatInfoId;
    }

    public void setReminderId(String reminderId) {
        this.reminderId = reminderId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public void setCategory(Category category) {
        this.category = category;
        if (category != null) {
            this.categoryId = category.getId();
        }
    }

    public void setRepeatInfo(RepeatInfo repeatInfo) {
        this.repeatInfo = repeatInfo;
        if (repeatInfo != null) {
            this.repeatInfoId = repeatInfo.getId();
            this.repeating = true;
            return;
        }
        this.repeatInfoId = BuildConfig.FLAVOR;
        this.repeating = false;
    }
//
    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
        if (reminder != null) {
            this.reminderId = reminder.getId();
        } else {
            this.reminderId = BuildConfig.FLAVOR;
        }
    }

    public void setDevice(Device device) {
        this.device = device;
        if (device != null) {
            this.deviceId = device.getId();
        } else {
            this.deviceId = BuildConfig.FLAVOR;
        }
    }
//
    public void setCalendar(Account calendar) {
        this.calendar = calendar;
        if (calendar != null) {
            this.calendarId = calendar.getId();
        } else {
            this.calendarId = BuildConfig.FLAVOR;
        }
    }

    public void setIdInAdapter(long idInAdapter) {
        this.idInAdapter = idInAdapter;
    }

    public boolean isBalanceUpdate() {
        return this.categoryId.contains(CategoriesContract.BALANCE_UPDATE_ID);
    }

    public boolean isTransfer() {
        return this.categoryId.contains(CategoriesContract.TRANSFER_CATEGORY_ID);
    }

    public int describeContents() {
        return TRANSACTION_TYPE_INCOME;
    }

    public void writeToParcel(Parcel dest, int flags)
    {
        int i = TRANSACTION_TYPE_EXPENSE;
        dest.writeString(this.id);
        dest.writeString(this.note);
        if (this.date != null) {
            dest.writeLong(this.date.getTimeInMillis());
        } else {
            dest.writeLong(0);
        }
        dest.writeDouble(this.price);
        dest.writeInt(this.expense ? TRANSACTION_TYPE_EXPENSE : TRANSACTION_TYPE_INCOME);
        dest.writeInt(this.repeating ? TRANSACTION_TYPE_EXPENSE : TRANSACTION_TYPE_INCOME);
        if (!this.forecasted) {
            i = TRANSACTION_TYPE_INCOME;
        }
        dest.writeInt(i);
        dest.writeString(this.calendarId);
        dest.writeString(this.categoryId);
        dest.writeString(this.repeatInfoId);
        dest.writeString(this.reminderId);
        dest.writeString(this.deviceId);
        dest.writeString(this.originalTransactionId);
        dest.writeStringList(this.facturasId);
//        dest.writeParcelable(this.category, TRANSACTION_TYPE_INCOME);
//        dest.writeParcelable(this.repeatInfo, TRANSACTION_TYPE_INCOME);
//        dest.writeParcelable(this.reminder, TRANSACTION_TYPE_INCOME);
//        dest.writeParcelable(this.device, TRANSACTION_TYPE_INCOME);
//        dest.writeParcelable(this.calendar, TRANSACTION_TYPE_INCOME);
    }

    private Transaction(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mIsBiggestPayment = false;
        this.mTransactionPercent = Double.MIN_VALUE;
        this.id = in.readString();
        this.note = in.readString();
        long time = in.readLong();
        if (time != 0) {
            this.date = Calendar.getInstance();
            this.date.setTimeInMillis(time);
        }
        this.price = in.readDouble();
        this.expense = TRANSACTION_TYPE_EXPENSE == in.readInt();
        if (TRANSACTION_TYPE_EXPENSE == in.readInt()) {
            z = true;
        } else {
            z = false;
        }
        this.repeating = z;
        if (TRANSACTION_TYPE_EXPENSE != in.readInt()) {
            z2 = false;
        }
        this.forecasted = z2;
        this.calendarId = in.readString();
        this.categoryId = in.readString();
        this.repeatInfoId = in.readString();
        this.reminderId = in.readString();
        this.deviceId = in.readString();
        this.originalTransactionId = in.readString();
        this.facturasId = in.createStringArrayList();
//        this.category = (Category) in.readParcelable(Category.class.getClassLoader());
//        this.repeatInfo = (RepeatInfo) in.readParcelable(RepeatInfo.class.getClassLoader());
//        this.reminder = (Reminder) in.readParcelable(Reminder.class.getClassLoader());
//        this.device = (Device) in.readParcelable(Device.class.getClassLoader());
//        this.calendar = (Account) in.readParcelable(Account.class.getClassLoader());
    }

    public static double getTransactionsSummary(List<Transaction> transactions) {
        double ret = 0.0d;
        if (transactions != null) {
            for (Transaction t : transactions) {
                ret += t.getPrice();
            }
        }
        return ret;
    }

    public boolean isBiggestPayment() {
        return this.mIsBiggestPayment;
    }

    public void setBiggestPayment(boolean mIsBiggestPayment) {
        this.mIsBiggestPayment = mIsBiggestPayment;
    }

    public double getTransactionPercent() {
        return this.mTransactionPercent;
    }

    public void setTransactionPercent(double mTransactionPercent) {
        this.mTransactionPercent = mTransactionPercent;
    }

    public List<String> getFacturasId() {
        return facturasId;
    }

    public void setFacturasId(List<String> facturaId) {
        this.facturasId = facturaId;
    }

    public void removeFacturaId(String facturaId){
        this.facturasId.remove(facturaId);
    }
    public void removeFacturaId(int pos){
        this.facturasId.remove(pos);
    }

    public void addFactura(Factura factura){
        this.facturasId.add(factura.getId());
        this.facturas.add(factura);
    }
    public void removeFactura(Factura factura){
        for (Factura factura1 : this.facturas) {
            if (factura.getId().equals(factura1.getId())){
                this.facturas.remove(factura1);
                removeFacturaId(factura1.getId());
            }
        }
    }

    public List<Factura> getFacturas() {
        return facturas;
    }

    public void setFacturas(List<Factura> facturas) {
        this.facturas = facturas;
    }

    public boolean isPastForecasted() {
        return isForecasted() && DateTimeUtils.isCalendarsPasted(DateTimeUtils.getCalendarToMidnight(Calendar.getInstance()), getDate());
    }
}
