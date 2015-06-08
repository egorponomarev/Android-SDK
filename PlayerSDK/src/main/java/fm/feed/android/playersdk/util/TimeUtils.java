package fm.feed.android.playersdk.util;

/**
 * Created by mharkins on 8/28/14.
 */

public class TimeUtils {
    private TimeUtils() {}

    public static String toProgressFormat(int s) {
        String prefix;

        if (s < 0) {
            prefix = "-";
            s *= -1;
        } else {
            prefix = "";
        }

        if (s >= 3600) {
            return String.format("%s%d:%02d:%02d", prefix, s / 3600, (s % 3600) / 60, (s % 60));
        } else {
            return String.format("%s%d:%02d", prefix, (s % 3600) / 60, (s % 60));
        }
    }

    public static String toProgressAccessibilityFormat(int s) {
        String suffix;

        if (s < 0) {
            suffix = " remaining";
            s *= -1;
        } else {
            suffix = "";
        }

        int minutes = (s % 3600) / 60;
        int seconds = (s % 60);
        if (s >= 3600) {
            int hours = s / 3600;
            return String.format("%d %s %02 %s %02d %s%s", hours, pluralize("hour", hours), minutes, pluralize("minute", minutes), seconds, pluralize("second", seconds), suffix);
        } else {
            return String.format("%d %s %02d %s%s", minutes, pluralize("minute", minutes), seconds, pluralize("second", seconds), suffix);
        }
    }

    private static String pluralize(String str, int value) {
        if (value == 1) {
            return str;
        }
        return str + "s";
    }
}
