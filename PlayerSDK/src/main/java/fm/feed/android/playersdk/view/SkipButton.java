package fm.feed.android.playersdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

import fm.feed.android.playersdk.NavListener;
import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.PlayerError;
import fm.feed.android.playersdk.PlayerListener;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.PlayInfo;

/**
 * Button subclass that automatically updates its 'enabled' state
 * based on whether the currently playing song is skippable. Also, when
 * the button is clicked, a request is made to skip the current song.
 */

public class SkipButton extends Button implements PlayerListener, NavListener {

    private final static String TAG = SkipButton.class.getSimpleName();

    private Player mPlayer;

    public SkipButton(Context context) {
        super(context);

        init();
    }

    public SkipButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public SkipButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        if (isInEditMode())
            return;

        mPlayer = Player.getInstance();

        mPlayer.registerPlayerListener(this);
        mPlayer.registerNavListener(this);

        updateButtonState(mPlayer.isSkippable());
    }

    private void updateButtonState(boolean skippable) {
        setEnabled(skippable);
    }

    /*
     * Skip the current song, if possible, then do the default
     */

    @Override
    public boolean performClick() {
        if (!isInEditMode() && mPlayer.isSkippable()) {
            Log.d(TAG, "Skipping song");
            mPlayer.skip();
        } else {
            Log.d(TAG, "Ignoring skip attempt");
        }

        return super.performClick();
    }

    @Override
    public void onPlayerInitialized(PlayInfo playInfo) {
        updateButtonState(mPlayer.isSkippable());
    }

    @Override
    public void onPlaybackStateChanged(PlayInfo.State state) {
        updateButtonState(mPlayer.isSkippable());
    }

    @Override
    public void onSkipStatusChange(boolean skippable) {
        updateButtonState(skippable);
    }

    @Override
    public void onError(PlayerError playerError) {
        updateButtonState(mPlayer.isSkippable());
    }

    @Override
    public void onStationChanged(Station station) {
        updateButtonState(false);
    }

    @Override
    public void onTrackChanged(Play play) {
        updateButtonState(mPlayer.isSkippable());
    }

    @Override
    public void onEndOfPlaylist() {
        updateButtonState(false);
    }

    @Override
    public void onSkipFailed() {
        updateButtonState(false);
    }

    @Override
    public void onBufferUpdate(Play play, int percentage) {
        // ignore
    }

    @Override
    public void onProgressUpdate(Play play, int elapsedTime, int totalTime) {
        // ignore
    }
}
