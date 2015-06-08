package fm.feed.android.playersdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import fm.feed.android.playersdk.NavListener;
import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.SocialListener;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;

/**
 * ToggleButton subclass that automatically updates its 'enabled' state
 * based on whether a song is currently playing, and updates its 'checked'
 * state depending on whether the user likes the current song. When
 * the button is clicked, the 'liked' state of the current song will update.
 */

public class LikeButton extends ToggleButton implements NavListener, SocialListener {

    private final static String TAG = LikeButton.class.getSimpleName();

    private Player mPlayer;

    public LikeButton(Context context) {
        super(context);

        init();
    }

    public LikeButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public LikeButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    // API level 21 ToggleButton(Context, AttributeSet, int, int) not implemented

    private void init() {
        if (isInEditMode())
            return;

        mPlayer = Player.getInstance();

        mPlayer.registerSocialListener(this);
        mPlayer.registerNavListener(this);

        updateButtonState();
    }

    private void updateButtonState() {
        Play play = mPlayer.getPlay();

        if (play != null) {
            updateButtonState(true, play.getLikeState() == Play.LikeState.LIKED);

        } else {
            updateButtonState(false, false);
        }
    }

    private void updateButtonState(boolean enabled, boolean liked) {
        if (isEnabled() != enabled) {
            setEnabled(enabled);
        }

        if (isChecked() != liked) {
            setChecked(liked);
        }
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

        if (mPlayer.hasPlay()) {
            if (isChecked()) {
                // dislike the song
                mPlayer.unlike();
            } else {
                mPlayer.like();
            }
        }

        return super.performClick();
    }

    @Override
    public void onStationChanged(Station station) {

    }

    @Override
    public void onTrackChanged(Play play) {
        updateButtonState();
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

    @Override
    public void onLiked() {
        updateButtonState();
    }

    @Override
    public void onUnliked() {
        updateButtonState();
    }

    @Override
    public void onDisliked() {
        updateButtonState();
    }
}
