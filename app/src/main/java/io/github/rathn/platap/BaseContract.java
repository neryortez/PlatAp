package io.github.rathn.platap;

import io.github.rathn.platap.utils.DateTimeUtils;

public class BaseContract {
    private static final int LAST_SYNC_DATE_CORRECTION = 60;

    public static long getUpdateDate() {
        return System.currentTimeMillis() / 1000;
    }

    public static long getUpdateDate(int updateDate) {
        return updateDate == 0 ? getUpdateDate() : (long) updateDate;
    }

    public static long getCurrentDate() {
        return DateTimeUtils.getServerSpecificGmtTimeFromLocalDate(DateTimeUtils.getCurrentDateWithoutTime());
    }

//    public static long getModifiedLastSyncDate() {
//        return PersistentStorage.getLastSyncDate() - 60;
//    }
}
