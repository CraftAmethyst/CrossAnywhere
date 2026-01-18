package io.github.qianmo0721.crossAnywhere.util;

public final class TimeUtil {
    private TimeUtil() {
    }

    public static String formatSeconds(long seconds) {
        if (seconds <= 0) {
            return "0";
        }
        long minutes = seconds / 60;
        long remaining = seconds % 60;
        if (minutes <= 0) {
            return String.valueOf(remaining);
        }
        return minutes + "m" + remaining + "s";
    }
}
