package fm.feed.android.playersdk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import fm.feed.android.playersdk.NavListener;
import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.R;
import fm.feed.android.playersdk.model.AudioFile;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.util.TimeUtils;

/**
 * This TextView extension updates itself to display metadata about the currently
 * playing song. Set the 'format' attribute, and it will be automatically copied
 * to the 'text' attribute, but with the following strings replaced to reflect
 * the currently playing song:
 *
 *    %ARTIST
 *    %TRACK
 *    %ALBUM
 *
 * If not song is playing, then the text is set to the empty string.
 *
 */

public class MetadataTextView extends TextView implements NavListener {

    private static final String TAG = MetadataTextView.class.getSimpleName();

    private Player mPlayer;
    private String mFormat = "";

    public MetadataTextView(Context context) {
        super(context);

        init(null);
    }

    public MetadataTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public MetadataTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FeedFmMetadataTextView);

        String format = a.getString(R.styleable.FeedFmMetadataTextView_format);
        if (format != null) {
            setFormat(format);
        }

        a.recycle();

        if (isInEditMode()) {
            return;
        }

        mPlayer = Player.getInstance();
        mPlayer.registerNavListener(this);
    }

    public void setFormat(String format) {
        this.mFormat = format;
    }

    public String getFormat() {
        return mFormat;
    }

    @Override
    public void onStationChanged(Station station) {

    }

    @Override
    public void onTrackChanged(Play play) {
        if (play == null) {
            setText("");

        } else {
            AudioFile af = play.getAudioFile();
            String text = mFormat;

            text = text.replaceAll("%ARTIST", af.getArtist().getName());
            text = text.replaceAll("%TRACK", af.getTrack().getTitle());
            text = text.replaceAll("%ALBUM", af.getRelease().getTitle());

            setText(text);
        }
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
