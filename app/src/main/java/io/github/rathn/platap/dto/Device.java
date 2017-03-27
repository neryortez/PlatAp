package io.github.rathn.platap.dto;

import android.os.Parcel;
import android.os.Parcelable;

//import com.google.gson.annotations.SerializedName;

public class Device implements Parcelable {

    public Device(){

    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
//    @SerializedName("udid")
    private String id;
//    @SerializedName("device_name")
    private String name;
//    @SerializedName("userID")
    private int userId;

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.userId);
    }

    public Device(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.userId = in.readInt();
    }
}
