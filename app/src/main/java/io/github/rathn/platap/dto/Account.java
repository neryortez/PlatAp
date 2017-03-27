package io.github.rathn.platap.dto;

import android.os.Parcel;
import android.os.Parcelable;

//import com.google.gson.annotations.SerializedName;

public class Account implements Parcelable {

    public Account(){}

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
    public static final int MAX_LIMIT = 10;
    private double balance;
//    @SerializedName("calendar_deleted")
    private int deleted;
//    @SerializedName("calendar_id")
    private String id;
//    @SerializedName("calendar_is_default")
    private boolean isDefault;
//    @SerializedName("calendar_name")
    private String name;
//    @SerializedName("calendar_order")
    private int order;
//    @SerializedName("calendar_update_date")
    private int updateDate;
//    @SerializedName("calendar_user_id")
    private int userId;

    public Account(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public int getUserId() {
        return this.userId;
    }

    public double getBalance() {
        return this.balance;
    }

    public int getOrder() {
        return this.order;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setUpdateDate(int updateDate) {
        this.updateDate = updateDate;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.isDefault ? 1 : 0);
        dest.writeInt(this.userId);
        dest.writeDouble(this.balance);
        dest.writeInt(this.order);
    }

    public Account(Parcel in) {
        boolean z = true;
        this.id = in.readString();
        this.name = in.readString();
        if (in.readInt() != 1) {
            z = false;
        }
        this.isDefault = z;
        this.userId = in.readInt();
        this.balance = in.readDouble();
        this.order = in.readInt();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Account)) {
            return false;
        }
        if (((Account) obj).getId().equalsIgnoreCase(this.id) && ((Account) obj).getUserId() == this.userId) {
            return true;
        }
        return false;
    }
}
