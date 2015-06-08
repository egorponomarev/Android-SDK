package fm.feed.android.playersdk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import java.util.List;

import fm.feed.android.playersdk.NavListener;
import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.R;
import fm.feed.android.playersdk.SocialListener;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;

/**
 * ToggleButton subclass that automatically updates its 'enabled' state
 * based on whether the current station matches the 'stationName'
 * attribute.
 */

public class StationButton extends ToggleButton implements NavListener {

    private final static String TAG = StationButton.class.getSimpleName();

    private Player mPlayer;
    private String mStationName;

    public StationButton(Context context) {
        super(context);

        init(null);
    }

    public StationButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public StationButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs);
    }

    // API level 21 ToggleButton(Context, AttributeSet, int, int) not implemented

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FeedFmStationButton);

        mStationName = a.getString(R.styleable.FeedFmStationButton_stationName);

        a.recycle();

        if (isInEditMode())
            return;

        mPlayer = Player.getInstance();

        mPlayer.registerNavListener(this);

        updateButtonState(mPlayer.getStation());
    }

    private void updateButtonState(Station station) {
        if (station == null) {
            setChecked(false);

        } else {
            setChecked(station.getName().equals(mStationName));
        }
    }

    public void setStationName(String stationName) {
        mStationName = stationName;
    }

    public String getStationName() {
        return mStationName;
    }

    /*
     * Toggle whether the current song is liked or not, then do the
     * default
     */

    @Override
    public boolean performClick() {
        if (isInEditMode()) {
            return super.performClick();
        }

        if (!mPlayer.getStation().getName().equals(mStationName)) {
            // loop through the list of stations and find the one
            // with the matching name:
            for (Station station: mPlayer.getStationList()) {
                if (station.getName().equals(mStationName)) {
                    mPlayer.setStationId(station.getId());
                    break;
                }
            }
        }

        return super.performClick();
    }

    @Override
    public void onStationChanged(Station station) {
        updateButtonState(station);
    }

    @Override
    public void onTrackChanged(Play play) {

    }

    @Override
    public void onEndOfPlaylist() {

    }

    @Override
    public void onSkipFailed() {

    }

    @Override
    public void onBufferUpdate(Play play, int percentage) {

    }

    @Override
    public void onProgressUpdate(Play play, int elapsedTime, int totalTime) {

    }

}
