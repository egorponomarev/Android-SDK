package fm.feed.android.testapp.util;

/**
 * Created by mharkins on 8/28/14.
 */
public class TimeUtils {
    private TimeUtils() {}

    public static String toProgressFormat(int s) {
        if (s >= 3600) {
            return String.format("%dh %02dm %02ds", s / 3600, (s % 3600) / 60, (s % 60));
        } else {
            return String.format("%02dm %02ds", (s % 3600) / 60, (s % 60));
        }
    }
}
