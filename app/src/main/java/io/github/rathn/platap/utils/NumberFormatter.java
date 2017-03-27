package io.github.rathn.platap.utils;

//import com.example.neriortez.dolarpajaro.storage.persistent.PersistentStorage;

//import org.askerov.dynamicgrid.BuildConfig;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import io.github.rathn.platap.BuildConfig;
import io.github.rathn.platap.persistent.PersistentStorage;

public class NumberFormatter {
    private static final String ABBREVIATION_B = "B";
    private static final String ABBREVIATION_K = "K";
    private static final double ABBREVIATION_K_UPPER_LIMIT =    999999.0d;
    private static final double ABBREVIATION_LOWER_LIMIT =       99999.0d;
    private static final String ABBREVIATION_M = "M";
    private static final double ABBREVIATION_M_UPPER_LIMIT =    9.99999999E8d;
    private static final int B = 1000000000;
    private static final int FRACTION_COUNT_ABBREVIATION = 1;
    private static final int FRACTION_COUNT_DEFAULT = 2;
    private static final int FRACTION_COUNT_NO_DECIMALS = 0;
    private static final int K = 1000;
    private static final int M = 1000000;
    private static final double ROUND_DOWN_UPPER_LIMIT = 0.5d;

    public static String format(double value) {
        return format(value, false);
    }

    public static String formatWithSign(double value) {
        StringBuffer buffer = new StringBuffer(format(value, false));
        if (value >= 0.0d) {
            buffer.insert(FRACTION_COUNT_NO_DECIMALS, "+");
        }
        return buffer.toString();
    }

    public static String formatForCalendarTile(double value) {
        return format(value, true);
    }

    public static String formatForCalendarTileWithSign(double value) {
        StringBuffer buffer = new StringBuffer(format(value, true));
        if (value >= 0.0d) {
            buffer.insert(FRACTION_COUNT_NO_DECIMALS, "+");
        }
        return buffer.toString();
    }

    public static String formatPercentage(double percentage) {
        DecimalFormat decimalFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        decimalFormatter.setRoundingMode(RoundingMode.DOWN);
        decimalFormatter.setMaximumFractionDigits(FRACTION_COUNT_DEFAULT);
        decimalFormatter.setMinimumFractionDigits(FRACTION_COUNT_NO_DECIMALS);
        return decimalFormatter.format(percentage);
    }

    private static String format(double value, boolean showInCalendarTile) {
        String abbreviation = getAbbreviationString(value);
        StringBuffer buffer = new StringBuffer();
        buffer.append(getFormattedString(value, abbreviation, PersistentStorage.shouldShowDecimals(), showInCalendarTile)).append(abbreviation);
        return buffer.toString();
    }

    private static String getAbbreviationString(double value) {
        double absoluteValue = Math.abs(value);
        if (absoluteValue <= ABBREVIATION_LOWER_LIMIT) {
            return BuildConfig.FLAVOR;
        }
        if (absoluteValue <= ABBREVIATION_K_UPPER_LIMIT) {
            return ABBREVIATION_K;
        }
        if (absoluteValue <= ABBREVIATION_M_UPPER_LIMIT) {
            return ABBREVIATION_M;
        }
        return ABBREVIATION_B;
    }

    private static double round(double value) {
        double absoluteValue = Math.abs(value);
        double fraction = absoluteValue % 1.0d;
        if (fraction != 0.0d) {
            if (fraction <= ROUND_DOWN_UPPER_LIMIT) {
                absoluteValue -= fraction;
            } else {
                absoluteValue = (absoluteValue - fraction) + 1.0d;
            }
        }
        return ((double) (value > 0.0d ? FRACTION_COUNT_ABBREVIATION : -1)) * absoluteValue;
    }

    private static double roundAbbreviated(double value) {
        double absoluteValue = Math.abs(value);
        double fraction = absoluteValue % 0.1d;
        if (fraction != 0.0d) {
            if (fraction <= 0.05d) {
                absoluteValue -= fraction;
            } else {
                absoluteValue = (absoluteValue - fraction) + 0.1d;
            }
        }
        return ((double) (value > 0.0d ? FRACTION_COUNT_ABBREVIATION : -1)) * absoluteValue;
    }

    private static String getFormattedString(double value, String abbreviation, boolean showDecimals, boolean showInCalendarTile) {
        int maximumFractions = FRACTION_COUNT_ABBREVIATION;
        double abbreviatedValue = value;
        if (abbreviation.equalsIgnoreCase(BuildConfig.FLAVOR)) {
            if (showDecimals) {
                maximumFractions = FRACTION_COUNT_DEFAULT;
            } else {
                abbreviatedValue = round(abbreviatedValue);
                maximumFractions = FRACTION_COUNT_NO_DECIMALS;
            }
        } else if (abbreviation.equalsIgnoreCase(ABBREVIATION_K)) {
            abbreviatedValue = roundAbbreviated(abbreviatedValue / 1000.0d);
        } else if (abbreviation.equalsIgnoreCase(ABBREVIATION_M)) {
            abbreviatedValue = roundAbbreviated(abbreviatedValue / 1000000.0d);
        } else if (abbreviation.equalsIgnoreCase(ABBREVIATION_B)) {
            abbreviatedValue = roundAbbreviated(abbreviatedValue / 1.0E9d);
        }
        if (showInCalendarTile) {
            if (abbreviatedValue < 1000.0d) {
                maximumFractions = FRACTION_COUNT_DEFAULT;
            } else {
                maximumFractions = FRACTION_COUNT_NO_DECIMALS;
            }
        }
        DecimalFormat decimalFormatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        decimalFormatter.setRoundingMode(RoundingMode.DOWN);
        if (abbreviatedValue % 1.0d != 0.0d) {
            decimalFormatter.setMaximumFractionDigits(maximumFractions);
            decimalFormatter.setMinimumFractionDigits(FRACTION_COUNT_NO_DECIMALS);
        } else {
            decimalFormatter.setMaximumFractionDigits(FRACTION_COUNT_NO_DECIMALS);
            decimalFormatter.setMinimumFractionDigits(FRACTION_COUNT_NO_DECIMALS);
        }
        return decimalFormatter.format(abbreviatedValue);
    }
}
