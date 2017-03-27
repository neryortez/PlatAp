package io.github.rathn.platap.dto;

import android.os.Parcel;
import android.os.Parcelable;

//import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

public class Budget implements Parcelable {
    public static final Creator<Budget> CREATOR = new Creator<Budget>() {
        public Budget createFromParcel(Parcel in) {
            return new Budget(in);
        }

        public Budget[] newArray(int size) {
            return new Budget[size];
        }
    };
    private Category category;
//    @SerializedName("budget_category_id")
    private String categoryId;
//    @SerializedName("budget_date")
    private Calendar date;
//    @SerializedName("budget_deleted")
    private int deleted;
//    @SerializedName("budget_id")
    private String id;
    private double spentValue;
//    @SerializedName("budget_update_date")
    private int updateDate;
//    @SerializedName("budget_value")
    private double value;

    public String getId() {
        return this.id;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public Calendar getDate() {
        return this.date;
    }

    public double getValue() {
        return this.value;
    }

    public Category getCategory() {
        return this.category;
    }

    public int getDeleted() {
        return this.deleted;
    }

    public int getUpdateDate() {
        return this.updateDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategory(Category category) {
        this.category = category;
        if (category != null) {
            this.categoryId = category.getId();
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.categoryId);
        dest.writeLong(this.date.getTimeInMillis());
        dest.writeDouble(this.value);
        dest.writeParcelable(this.category, 0);
    }

    public Budget(){}
    public Budget(Parcel in) {
        this.id = in.readString();
        this.categoryId = in.readString();
        this.date = Calendar.getInstance();
        this.date.setTimeInMillis(in.readLong());
        this.value = in.readDouble();
        this.category = (Category) in.readParcelable(Category.class.getClassLoader());
    }

    public double getSpentValue() {
        return this.spentValue;
    }

    public void setSpentValue(double spentValue) {
        this.spentValue = spentValue;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Budget)) {
            return false;
        }
        Budget budget = (Budget) object;
        if (budget == null || budget.getId() == null || this.id == null || !budget.getId().equalsIgnoreCase(this.id)) {
            return false;
        }
        return true;
    }
}
