package io.github.rathn.platap.dto;

import android.os.Parcel;
import android.os.Parcelable;

//import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public class Reminder implements Parcelable {
    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };
//    @SerializedName("reminder_deleted")
    private int deleted;
//    @SerializedName("reminder_id")
    private String id;
//    @SerializedName("reminder_interval")
    private int interval;
//    @SerializedName("reminder_type")
    private int repeatType;
//    @SerializedName("reminder_update_date")
    private int updateDate;

    public Reminder() {
        this.id = UUID.randomUUID().toString();
        this.repeatType = -1;
        this.interval = 0;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeInt(this.repeatType);
        dest.writeInt(this.interval);
    }

    public Reminder(Parcel in) {
        this.id = in.readString();
        this.repeatType = in.readInt();
        this.interval = in.readInt();
    }
}
