package io.github.rathn.platap.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

import io.github.rathn.platap.utils.DateTimeUtils;

//import com.google.gson.annotations.SerializedName;

public class RepeatInfo implements Parcelable {

    public static final int TYPE_DAYLY = 0;
    public static final int TYPE_MONTHLY = 1;
    public static final int TYPE_IRREGULAR = 2;

    public static final int INTERVAL_WEEK_DAY = -2;
    public static final int INTERVAL_WEEKENS = -3;
    public static final int INTERVAL_WEIRD = -5;
    public static final int INTERVAL_QUINCENA = -4;

    private static final int DAILY  = 1;
    private static final int OTHER_DAY = 2;
    private static final int WEEKDAY = 3;
    private static final int WEEKEND = 4;
    private static final int WEEKLY = 5;
    private static final int OTHER_WEEK = 6;
    private static final int MONTHLY = 7;


    public static final Creator<RepeatInfo> CREATOR = new Creator<RepeatInfo>() {
        public RepeatInfo createFromParcel(Parcel in) {
            return new RepeatInfo(in);
        }

        public RepeatInfo[] newArray(int size) {
            return new RepeatInfo[size];
        }
    };
//    @SerializedName("repeat_info_deleted")
    private int deleted;
//    @SerializedName("repeat_info_end_date")
    private Calendar endDate;
//    @SerializedName("repeat_info_id")
    private String id;
//    @SerializedName("repeat_info_interval")
    private int interval;
//    @SerializedName("repeat_info_type")
    private int repeatType;
//    @SerializedName("repeat_info_start_date")
    private Calendar startDate;
//    @SerializedName("repeat_info_update_date")
    private int updateDate;

    public RepeatInfo() {
        /*this.repeatType = 1;*/
    }

    public RepeatInfo(RepeatInfo info) {
        this.id = info.getId();
        this.repeatType = info.getRepeatType();
        this.interval = info.getInterval();
        this.startDate = DateTimeUtils.clone(info.getStartDate());
        if (info.getEndDate() != null) {
            this.endDate = DateTimeUtils.clone(info.getEndDate());
        }
        this.updateDate = info.getUpdateDate();
        this.deleted = info.getDeleted();
    }

    public void setRepeat(int repeatType, int interval){
        this.repeatType = repeatType;
        this.interval = interval;
    }

    public int getRepeat(){
        switch (repeatType){
            case TYPE_DAYLY:
                switch (interval){
                    case 1:
                        return DAILY;
                    case 2:
                        return OTHER_DAY;
                    case 7:
                        return WEEKLY;
                    case 14:
                        return OTHER_WEEK;
                }
                return 0;
            case TYPE_IRREGULAR:
                switch (interval){
                    case INTERVAL_WEEK_DAY:
                        return WEEKDAY;
                    case INTERVAL_WEEKENS:
                        return WEEKEND;
                }
                return 0;
            case TYPE_MONTHLY:
                switch (interval){
                    case 1:
                        return MONTHLY;
                }
                return 0;
        }
        return 0;
    }

    public String getId() {
        return this.id;
    }

    public int getRepeatType() {
        return this.repeatType;
    }

    public int getInterval() {
        return this.interval;
    }

    public Calendar getStartDate() {
        return this.startDate;
    }

    public Calendar getEndDate() {
        return this.endDate;
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

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeInt(this.repeatType);
        dest.writeInt(this.interval);
        if (this.startDate != null) {
            dest.writeLong(this.startDate.getTimeInMillis());
        } else {
            dest.writeLong(0);
        }
        if (this.endDate != null) {
            dest.writeLong(this.endDate.getTimeInMillis());
        } else {
            dest.writeLong(0);
        }
    }

    private RepeatInfo(Parcel in) {
        this.id = in.readString();
        this.repeatType = in.readInt();
        this.interval = in.readInt();
        long time = in.readLong();
        if (time != 0) {
            this.startDate = Calendar.getInstance();
            this.startDate.setTimeInMillis(time);
        } else {
            this.startDate = null;
        }
        time = in.readLong();
        if (time != 0) {
            this.endDate = Calendar.getInstance();
            this.endDate.setTimeInMillis(time);
            return;
        }
        this.endDate = null;
    }
}
