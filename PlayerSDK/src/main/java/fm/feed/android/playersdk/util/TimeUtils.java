package fm.feed.android.playersdk.util;

/**
 * Created by mharkins on 8/28/14.
 */
public class TimeUtils {
    private TimeUtils() {}

    public static String toProgressFormat(int s) {
        if (s >= 3600) {
            return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
        } else {
            return String.format("%d:%02d", (s % 3600) / 60, (s % 60));
        }
    }

    public static String toProgressAccessibilityFormat(int s) {
        int minutes = (s % 3600) / 60;
        int seconds = (s % 60);
        if (s >= 3600) {
            int hours = s / 3600;
            return String.format("%d %s %02 %s %02d %s", hours, pluralize("hour", hours), minutes, pluralize("minute", minutes), seconds, pluralize("second", seconds));
        } else {
            return String.format("%d %s %02d %s", minutes, pluralize("minute", minutes), seconds, pluralize("second", seconds));
        }
    }

    private static String pluralize(String str, int value) {
        if (value == 1) {
            return str;
        }
        return str + "s";
    }
}
