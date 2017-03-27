package io.github.rathn.platap.dto;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;

import io.github.rathn.platap.NotYetImplementedException;
import io.github.rathn.platap.utils.CalendarHelper;
import io.github.rathn.platap.utils.Line;
import io.github.rathn.platap.utils.NumberFormatter;

public class Balance implements Parcelable {
    public static final Creator<Balance> CREATOR = new Creator<Balance>() {
        public Balance createFromParcel(Parcel in) {
            return new Balance(in);
        }

        public Balance[] newArray(int size) {
            return new Balance[size];
        }
    };
    private Calendar mDate;
    private String mDateString;
    private double mMoovement;
    private String mMoovementString;
    private double mTotalValue;
    private String mTotalValueString;

    public Balance(double mTotalValue, double mMoovement, Calendar mDate) {
        this.mTotalValue = mTotalValue;
        this.mMoovement = mMoovement;
        this.mDate = mDate;
    }

    public double getTotalValue() {
        return this.mTotalValue;
    }

    public String getTotalValueString() {
        if (this.mTotalValueString == null || this.mTotalValueString.isEmpty()) {
            this.mTotalValueString = NumberFormatter.format(this.mTotalValue);
        }
        return this.mTotalValueString;
    }

    public void setTotalValue(double mTotalValue) {
        if (this.mTotalValue != mTotalValue) {
            this.mTotalValueString = null;
            this.mTotalValue = mTotalValue;
        }
    }

    public double getMoovement() {
        return this.mMoovement;
    }

    public String getMoovementString() {
        if (this.mMoovementString == null || this.mMoovementString.isEmpty()) {
            this.mMoovementString = NumberFormatter.format(this.mMoovement);
        }
        return this.mMoovementString;
    }

    public void setMoovement(double moovement) {
        if (this.mMoovement != moovement) {
            this.mMoovementString = null;
            this.mMoovement = moovement;
        }
    }

    public void incTotalValue(double newValue) {
        this.mTotalValueString = null;
        this.mTotalValue += newValue;
    }

    public Calendar getDate() {
        return this.mDate;
    }

    public void setDate(Calendar mDate) {
        this.mDateString = null;
        this.mDate = mDate;
    }

    public String getDateString() {
        if ((this.mDateString == null || this.mDateString.isEmpty()) && this.mDate != null) {
            this.mDateString = CalendarHelper.getLongStringFromCalendar(this.mDate);
        }
        return this.mDateString;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mTotalValue);
        dest.writeDouble(this.mMoovement);
        if (this.mDate == null) {
            this.mDate = Calendar.getInstance();
        }
        dest.writeLong(this.mDate.getTimeInMillis());
    }

    private Balance(Parcel in) {
        this.mTotalValue = in.readDouble();
        this.mMoovement = in.readDouble();
        this.mDate = Calendar.getInstance();
        this.mDate.setTimeInMillis(in.readLong());
    }

    public static Line getLineFromArray(ArrayList<Balance> balances, Context ct) {
        throw new NotYetImplementedException();
        /*Line l = new Line();
        if (balances != null && balances.size() > 0) {
            for (int i = 0; i < balances.size(); i++) {
                LinePoint lp = new LinePoint();
                lp.setX((float) i);
                lp.setY((float) ((Balance) balances.get(i)).getTotalValue());
                if ((Calendar.getInstance().get(Calendar.DATE) + 1) == (int) balances.get(i).getDate().get(Calendar.DATE)) {
                    lp.setShowPoint(true, ct.getResources().getColor(R.color.dashboard_line_graph_point));
                }
                l.addPoint(lp);
            }
        }
        l.setColor(ct.getResources().getColor(R.color.dashboard_line_graph));
        return l;*/
    }
}
