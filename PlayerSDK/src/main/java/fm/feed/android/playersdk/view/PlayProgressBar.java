package fm.feed.android.playersdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import fm.feed.android.playersdk.NavListener;
import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;

/**
 * This ProgressBar extension will update itself to dynamically
 * show how much of the current song has elapsed. When no song is
 * playing, this class will set reset its progress to 0 and disable
 * itself.
 */

public class PlayProgressBar extends ProgressBar implements NavListener {

    private static final String TAG = PlayProgressBar.class.getSimpleName();

    private Player mPlayer;

    public PlayProgressBar(Context context) {
        super(context);

        init();
    }

    public PlayProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public PlayProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        if (isInEditMode())
            return;

        mPlayer = Player.getInstance();

        resetProgress(mPlayer.getPlay());

        mPlayer.registerNavListener(this);
    }

    private void resetProgress(Play play) {
        setProgress(0);

        if (play == null) {
            setMax(0);
            setEnabled(false);

            return;
        }

        setEnabled(true);

        int duration = play.getAudioFile().getDurationInSeconds();
        setMax(duration);
    }

    @Override
    public void onStationChanged(Station station) {

    }

    @Override
    public void onTrackChanged(Play play) {
        resetProgress(play);
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
        setProgress(elapsedTime);
    }
}
