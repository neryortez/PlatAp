package io.github.rathn.platap.dto;

import android.os.Parcel;
import android.os.Parcelable;

//import com.google.gson.annotations.SerializedName;

public class Category implements Parcelable {
    public static final Creator<Category> CREATOR = new Creator<Category>() {
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
    public static final int TYPE_EXPENSE = 11;
    public static final int TYPE_INCOME = 12;
    public static final int TYPE_INVALID = -1;
    private int categoryColor;
//    @SerializedName("category_color_index")
    private int colorIndex;
//    @SerializedName("category_deleted")
    private int deleted;
//    @SerializedName("category_icon_index")
    private int iconIndex;
//    @SerializedName("category_id")
    private String id;
//    @SerializedName("category_is_expense")
    private boolean isExpense;
    private double mSpentValue;
//    @SerializedName("category_name")
    private String name;
//    @SerializedName("category_order")
    private int order;
//    @SerializedName("category_update_date")
    private int updateDate;
//    @SerializedName("category_user_id")
    private int userId;

    public Category() {
        this.mSpentValue = Double.MAX_VALUE;
        this.categoryColor = TYPE_INVALID;
        this.iconIndex = TYPE_INVALID;
        this.colorIndex = TYPE_INVALID;
    }

    public Category(String id, String name) {
        this.mSpentValue = Double.MAX_VALUE;
        this.categoryColor = TYPE_INVALID;
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getColorIndex() {
        return this.colorIndex;
    }

    public boolean isExpense() {
        return this.isExpense;
    }

    public int getIconIndex() {
        return this.iconIndex;
    }

    public int getOrder() {
        return this.order;
    }

    public int getUserId() {
        return this.userId;
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

    public void setColorIndex(int color) {
        this.colorIndex = color;
    }

    public void setIsExpense(boolean isExpense) {
        this.isExpense = isExpense;
    }

    public void setIconIndex(int icon) {
        this.iconIndex = icon;
    }

    public void setOrder(int order) {
        this.order = order;
    }
//
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Category)) {
            return false;
        }
        Category category = (Category) object;
        if (category == null || category.getId() == null || this.id == null || !category.getId().equalsIgnoreCase(this.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.colorIndex);
        dest.writeInt(this.isExpense ? 1 : 0);
        dest.writeInt(this.iconIndex);
        dest.writeInt(this.order);
        dest.writeInt(this.userId);
        dest.writeDouble(this.mSpentValue);
    }

    private Category(Parcel in) {
        boolean z = true;
        this.mSpentValue = Double.MAX_VALUE;
        this.categoryColor = TYPE_INVALID;
        this.id = in.readString();
        this.name = in.readString();
        this.colorIndex = in.readInt();
        if (in.readInt() != 1) {
            z = false;
        }
        this.isExpense = z;
        this.iconIndex = in.readInt();
        this.order = in.readInt();
        this.userId = in.readInt();
        this.mSpentValue = in.readDouble();
    }

    public int getCategoryColor() {
        return this.categoryColor;
    }

    public void setCategoryColor(int categoryColor) {
        this.categoryColor = categoryColor;
    }

    public double getSpentValue() {
        return this.mSpentValue;
    }

    public void setSpentValue(double mSpentValue) {
        this.mSpentValue = mSpentValue;
    }
}
