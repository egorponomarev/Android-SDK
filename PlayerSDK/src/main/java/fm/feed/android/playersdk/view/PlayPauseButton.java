package fm.feed.android.playersdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import fm.feed.android.playersdk.NavListener;
import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.PlayerError;
import fm.feed.android.playersdk.PlayerListener;
import fm.feed.android.playersdk.SocialListener;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.PlayInfo;

/**
 * ToggleButton subclass that automatically updates its 'enabled' state
 * based on whether music is available for playback, and its 'checked'
 * state depending on whether music is playing (= checked) or paused/stopped
 * (= unchecked). When the button is clicked, music is either paused
 * or started.
 */

public class PlayPauseButton extends ToggleButton implements PlayerListener {

    private final static String TAG = PlayPauseButton.class.getSimpleName();

    private Player mPlayer;

    public PlayPauseButton(Context context) {
        super(context);

        init();
    }

    public PlayPauseButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public PlayPauseButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    // API level 21 ToggleButton(Context, AttributeSet, int, int) not implemented

    private void init() {
        if (isInEditMode())
            return;

        mPlayer = Player.getInstance();

        mPlayer.registerPlayerListener(this);

        updateButtonState();
    }

    private void updateButtonState() {
        updateButtonState(mPlayer.getState());
    }

    private void updateButtonState(PlayInfo.State state) {
        if (state == null) {
            setEnabled(false);
            setChecked(false);
            return;
        }

        switch (state) {
            case UNAVAILABLE:
                setEnabled(false);
                setChecked(false);
                break;

            case TUNING:
            case TUNED:
            case PLAYING:
                setEnabled(true);
                setChecked(true);
                break;

            default:
                setEnabled(true);
                setChecked(false);
                break;
        }
    }

    /*
     * Request that the song start or pause playback.
     */

    @Override
    public boolean performClick() {
        if (isInEditMode()) {
            return super.performClick();
        }

        if (isChecked()) {
            mPlayer.pause();

        } else {
            mPlayer.play();

        }

        return super.performClick();
    }

    @Override
    public void onPlayerInitialized(PlayInfo playInfo) {
        updateButtonState(playInfo.getState());
    }

    @Override
    public void onPlaybackStateChanged(PlayInfo.State state) {
        updateButtonState(state);
    }

    @Override
    public void onSkipStatusChange(boolean skippable) {

    }

    @Override
    public void onError(PlayerError playerError) {

    }
}
