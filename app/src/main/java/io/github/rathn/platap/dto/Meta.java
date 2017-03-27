package io.github.rathn.platap.dto;

//import com.google.gson.annotations.SerializedName;
import java.util.Calendar;

import io.github.rathn.platap.persistent.PersistentStorage;
import io.github.rathn.platap.utils.DateTimeUtils;

public class Meta {
    public static final int DB_VERSION = 3;
    public static final int DEFAULT_REPEAT_YEARS = 5;
//    @SerializedName("meta_db_create_date")
    private Calendar databaseCreationDate;
//    @SerializedName("meta_db_version")
    private int databaseVersion;
//    @SerializedName("meta_repeating_end")
    private Calendar repeatingEndDate;
//    @SerializedName("meta_user_id")
    private int userId;

    public Meta(boolean useDefaultValues) {
        if (useDefaultValues) {
            this.databaseVersion = DB_VERSION;
            this.databaseCreationDate = DateTimeUtils.getDateWithoutTime(Calendar.getInstance());
            this.repeatingEndDate = DateTimeUtils.getDateWithoutTime(Calendar.getInstance());
            this.repeatingEndDate.set(Calendar.YEAR, this.repeatingEndDate.get(Calendar.YEAR) + DEFAULT_REPEAT_YEARS);
            this.userId = PersistentStorage.getUserId();
        }
    }

    public int getDatabaseVersion() {
        return this.databaseVersion;
    }

    public void setDatabaseVersion(int databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public Calendar getDatabaseCreationDate() {
        return this.databaseCreationDate;
    }

    public void setDatabaseCreationDate(Calendar databaseCreationDate) {
        this.databaseCreationDate = databaseCreationDate;
    }

    public Calendar getRepeatingEndDate() {
        return this.repeatingEndDate;
    }

    public void setRepeatingEndDate(Calendar repeatingEndDate) {
        this.repeatingEndDate = repeatingEndDate;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
