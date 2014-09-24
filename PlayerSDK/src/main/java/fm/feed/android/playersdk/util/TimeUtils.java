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
}
