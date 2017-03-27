package io.github.rathn.platap.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

import io.github.rathn.platap.utils.CalendarHelper;
import io.github.rathn.platap.utils.NumberFormatter;

//import com.example.neriortez.dolarpajaro.utils.CalendarHelper;
//import com.example.neriortez.dolarpajaro.utils.NumberFormatter;

public class Payment implements Parcelable {
    public static final Creator<Payment> CREATOR = new Creator<Payment>() {
        public Payment createFromParcel(Parcel in) {
            return new Payment(in);
        }

        public Payment[] newArray(int size) {
            return new Payment[size];
        }
    };
    private String mDateString;
    private double mExpense;
    private String mExpenseString;
    private double mIncome;
    private String mIncomeString;
    private Calendar mPaymentDate;

    public Payment() {
        this.mIncome = 0.0d;
        this.mExpense = 0.0d;
    }

    public Payment(double income, double expense, Calendar paymentDate) {
        this.mIncome = 0.0d;
        this.mExpense = 0.0d;
        this.mIncome = income;
        this.mExpense = expense;
        this.mPaymentDate = paymentDate;
    }

    public double getIncome() {
        return this.mIncome;
    }

    public String getIncomeString() {
        if (this.mIncomeString == null || this.mIncomeString.length() <= 0) {
            this.mIncomeString = NumberFormatter.format(this.mIncome);
        }
        return this.mIncomeString;
    }

    public void setIncome(double income) {
        if (this.mIncome != income) {
            this.mIncomeString = null;
            this.mIncome = income;
        }
    }

    public void incIncome(double income) {
        this.mIncomeString = null;
        this.mIncome += income;
    }

    public double getExpense() {
        return this.mExpense;
    }

    public String getExpenseString() {
        if (this.mExpenseString == null || this.mExpenseString.isEmpty()) {
            this.mExpenseString = NumberFormatter.format(this.mExpense);
        }
        return this.mExpenseString;
    }

    public void setExpense(double expense) {
        if (this.mExpense != expense) {
            this.mExpenseString = null;
            this.mExpense = expense;
        }
    }

    public void incExpense(double expense) {
        this.mExpenseString = null;
        this.mExpense += expense;
    }

    public Calendar getPaymentDate() {
        return this.mPaymentDate;
    }

    public void setPaymentDate(Calendar paymentDate) {
        this.mDateString = null;
        this.mPaymentDate = paymentDate;
    }

    public String getDateString() {
        if ((this.mDateString == null || this.mDateString.isEmpty()) && this.mPaymentDate != null) {
            this.mDateString = CalendarHelper.getLongStringFromCalendar(this.mPaymentDate);
        }
        return this.mDateString;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mIncome);
        dest.writeDouble(this.mExpense);
        if (this.mPaymentDate == null) {
            this.mPaymentDate = Calendar.getInstance();
        }
        dest.writeLong(this.mPaymentDate.getTimeInMillis());
    }

    private Payment(Parcel in) {
        this.mIncome = 0.0d;
        this.mExpense = 0.0d;
        this.mIncome = in.readDouble();
        this.mExpense = in.readDouble();
        this.mPaymentDate = Calendar.getInstance();
        this.mPaymentDate.setTimeInMillis(in.readLong());
    }
}
