package fm.feed.android.playersdk.util;

import android.content.Context;

/**
 * Created by mharkins on 9/25/14.
 */
public class UIUtils {
    private UIUtils () {}



    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp
     *         A value in dp (density independent pixels) unit. Which we need to convert into pixels
     *
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px
     *         A value in px (pixels) unit. Which we need to convert into db
     *
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }
}
