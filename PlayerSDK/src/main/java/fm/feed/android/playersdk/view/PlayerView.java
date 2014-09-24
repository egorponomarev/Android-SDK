package fm.feed.android.playersdk.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;

import java.util.List;

import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.PlayerError;
import fm.feed.android.playersdk.R;
import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.util.TimeUtils;

/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * Created by mharkins on 9/22/14.
 */
public class PlayerView extends RelativeLayout {
    private static final String TAG = PlayerView.class.getSimpleName();

    private static final int CUSTOM_NOTIFICATION_ID = 12341212;

    private static final String AUTH_TOKEN = "d40b7cc98a001fc9be8dd3fd32c3a0c495d0db42";
    private static final String AUTH_SECRET = "b59c6d9c1b5a91d125f098ef9c2a7165dc1bd517";

    private boolean mIsAutoPlay;

    private Player mPlayer;

    private String mShareSubject;
    private String mShareBody;

    private static final int DEFAULT_SVG_SIZE_DP = 26;
    private int mSizeBaseline;

    private TextView mTitle;
    private TextView mArtist;
    private TextView mAlbum;
    private TextView mPrefix;
    private TextView mSuffix;

    private ProgressBar mProgressBar;

    private SVGImageView mDislike;
    private SVGImageView mLike;
    private SVGImageView mPlayPause;
    private SVGImageView mSkip;
    private SVGImageView mVolume;
    private SVGImageView mShare;

    public PlayerView(Context context) {
        super(context);
        init(null);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.PlayerView);

        mIsAutoPlay = a.getBoolean(R.styleable.PlayerView_autoPlay, false);

        //Don't forget this
        a.recycle();

        mShareSubject = null;
        mShareBody = null;

        initializeView();
        initializePlayer();
    }

    public Player getPlayer() {
        return mPlayer;
    }

    /**
     * Set the Subject of the shared message.
     * <p/>
     * <p>
     * You can get the current song information using:<br/>
     * <code>
     * Player player = playerView.getPlayer();<br/>
     * Play play = player.getPlay();<br/>
     * String title = play.getAudioFile().getTrack().getTitle();<br/>
     * String artist = play.getAudioFile().getArtist().getName();<br/>
     * String album = play.getAudioFile().getRelease().getTitle();<br/>
     * </code>
     * </p>
     *
     * @param subject
     *         The Subject of the shared message.
     */
    public void setShareSubject(String subject) {
        mShareSubject = subject;
    }

    /**
     * Set the Body of the Sharing message
     * <p>
     * You can get the current song information using:<br/>
     * <code>
     * Player player = playerView.getPlayer();<br/>
     * Play play = player.getPlay();<br/>
     * String title = play.getAudioFile().getTrack().getTitle();<br/>
     * String artist = play.getAudioFile().getArtist().getName();<br/>
     * String album = play.getAudioFile().getRelease().getTitle();<br/>
     * </code>
     * </p>
     *
     * @param body
     *         The Text to be sent in the Shared body
     */
    public void setShareBody(String body) {
        mShareBody = body;
    }

    private void initializeView() {
        RelativeLayout rootView = (RelativeLayout) inflate(getContext(), R.layout.view_player, this);
        mTitle = (TextView) rootView.findViewById(R.id.pu_title);
        mArtist = (TextView) rootView.findViewById(R.id.pu_artist);
        mAlbum = (TextView) rootView.findViewById(R.id.pu_album);
        mPrefix = (TextView) rootView.findViewById(R.id.pu_prefix);
        mSuffix = (TextView) rootView.findViewById(R.id.pu_suffix);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pu_progress);

        LinearLayout topContainer = (LinearLayout) rootView.findViewById(R.id.pu_top_icons);
        LinearLayout bottomContainer = (LinearLayout) rootView.findViewById(R.id.pu_bottom_icons);

        mSizeBaseline = (int) convertDpToPixel(DEFAULT_SVG_SIZE_DP);

        // We need to create the SVGImageView instances and add them programmatically.
        mDislike = newSvgImage(1, ImageView.ScaleType.FIT_START);
        mLike = newSvgImage(2, ImageView.ScaleType.FIT_CENTER);
        mPlayPause = newSvgImage(2, ImageView.ScaleType.FIT_CENTER);
        mSkip = newSvgImage(1, ImageView.ScaleType.FIT_END);
        mVolume = newSvgImage(1, ImageView.ScaleType.FIT_START);
        mShare = newSvgImage(1, ImageView.ScaleType.FIT_END);

        // Set the SVG resource to the SVGImageView
        setSvgResource(mDislike, R.drawable.ic_thumbdown_faded);
        setSvgResource(mLike, R.drawable.ic_thumbup_faded);
        setSvgResource(mPlayPause, R.drawable.ic_play_faded);
        setSvgResource(mSkip, R.drawable.ic_skip_faded);
        setSvgResource(mVolume, R.drawable.ic_speakermute_faded);
        setSvgResource(mShare, R.drawable.ic_share_faded);

        // Add SVGImageViews to the layout.
        topContainer.addView(mDislike);
        topContainer.addView(mLike);
        topContainer.addView(mPlayPause);
        topContainer.addView(mSkip);
        bottomContainer.addView(mVolume);
        bottomContainer.addView(mShare);

        mDislike.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSvgResource(mDislike, R.drawable.ic_thumbdown_normal);
                mPlayer.dislike();
                // TODO: handle unlike
            }
        });

        mLike.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSvgResource(mLike, R.drawable.ic_thumbup_normal);
                mPlayer.like();
            }
        });
        mPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer.getState() == PlayInfo.State.PLAYING) {
                    mPlayer.pause();
                } else {
                    mPlayer.play();
                }
            }
        });
        mSkip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.skip();
            }
        });
        mShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer == null || !mPlayer.hasPlay()) {
                    Log.i(TAG, "Cannot share if not playing");
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

                String subject = mShareSubject;
                String body = mShareBody;
                if (subject == null) {
                    subject = getContext().getString(R.string.share_subject_template);
                }
                if (body == null) {
                    Play play = mPlayer.getPlay();
                    String title = play.getAudioFile().getTrack().getTitle();
                    String artist = play.getAudioFile().getArtist().getName();
                    String album = play.getAudioFile().getRelease().getTitle();

                    body = getContext().getString(R.string.share_body_template, title, artist, album);
                }

                // Add data to the intent, the receiving app will decide what to do with it.
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, body);

                getContext().startActivity(Intent.createChooser(intent, getContext().getString(R.string.share_via)));
            }
        });
    }

    // Hack while Player registers multiple times a same instance of a listener.
    private boolean mDetachedFromWindow = false;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // If the Player has been initialized
        if (mPlayer != null) {
            if (mDetachedFromWindow) {
                mPlayer.registerNavListener(mNavListener);
                mPlayer.registerPlayerListener(mPlayerListener);

                if (mPlayer.hasPlay()) {
                    updatePlayInfo(mPlayer.getPlay());
                }
            }
        } else {
//            resetPlayInfo();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mDetachedFromWindow = true;

        if (mPlayer != null) {
            mPlayer.unregisterNavListener(mNavListener);
            mPlayer.unregisterPlayerListener(mPlayerListener);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superInstanceState", super.onSaveInstanceState());

        bundle.putParcelable("mArtist", mArtist.onSaveInstanceState());

        // PlayerView properties
        bundle.putString("shareSubject", mShareSubject);
        bundle.putString("shareBody", mShareBody);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            // PlayerView properties
            mShareSubject = bundle.getString("shareSubject");
            mShareBody = bundle.getString("shareBody");

            mArtist.onRestoreInstanceState(bundle.getParcelable("mArtist"));

            state = bundle.getParcelable("superInstanceState");
        }
        super.onRestoreInstanceState(state);
    }

    private void initializePlayer() {
        mPlayer = Player.getInstance(getContext(), mPlayerListener, AUTH_TOKEN, AUTH_SECRET, CUSTOM_NOTIFICATION_ID);
        mPlayer.unregisterPlayerListener(mPlayerListener);
        mPlayer.registerPlayerListener(mPlayerListener);
        mPlayer.registerNavListener(mNavListener);

        if (mPlayer.hasPlay()) {
            updatePlayInfo(mPlayer.getPlay());
        } else {
            resetPlayInfo();
        }
    }

    private Player.PlayerListener mPlayerListener = new Player.PlayerListener() {
        @Override
        public void onPlayerInitialized(PlayInfo playInfo) {
            if (mIsAutoPlay) {
                mPlayer.play();
            }
        }

        @Override
        public void onPlaybackStateChanged(PlayInfo.State state) {
            // Set the SVG resource to the SVGImageView
            setSvgResource(mPlayPause, R.drawable.ic_play_faded);
            setSvgResource(mSkip, R.drawable.ic_skip_faded);

            switch (state) {
                case WAITING:
                case READY:
                    resetPlayInfo();
                    break;
                case PAUSED:
                    setSvgResource(mPlayPause, R.drawable.ic_play_normal);
                    break;
                case TUNING:
                    setSvgResource(mPlayPause, R.drawable.ic_pause_normal);
                    resetPlayInfo();
                    mArtist.setText(getContext().getString(R.string.tuning));
                    break;
                case TUNED:
                case PLAYING:
                    setSvgResource(mPlayPause, R.drawable.ic_pause_normal);
                    if (mPlayer.hasPlay()) {
                        updatePlayInfo(mPlayer.getPlay());
                    }
                    break;
                case STALLED:
                    break;
                case COMPLETE:
                    break;
                case REQUESTING_SKIP:
                    setSvgResource(mSkip, R.drawable.ic_skip_normal);
                    break;
            }
        }

        @Override
        public void onError(PlayerError playerError) {
        }

        @Override
        public void onNotificationWillShow(int notificationId) {

        }
    };

    private Player.NavListener mNavListener = new Player.NavListener() {
        @Override
        public void onPlacementChanged(Placement placement, List<Station> stations) {

        }

        @Override
        public void onStationChanged(Station station) {

        }

        @Override
        public void onTrackChanged(Play play) {
            // Set the SVG resource to the SVGImageView
            setSvgResource(mDislike, R.drawable.ic_thumbdown_faded);
            setSvgResource(mLike, R.drawable.ic_thumbup_faded);
            setSvgResource(mSkip, R.drawable.ic_skip_faded);
        }

        @Override
        public void onEndOfPlaylist() {
            resetPlayInfo();

            mArtist.setText(getContext().getString(R.string.end_of_playlist));
        }

        @Override
        public void onSkipFailed() {

        }

        @Override
        public void onBufferUpdate(Play play, int percentage) {

        }

        @Override
        public void onProgressUpdate(Play play, int elapsedTime, int totalTime) {
            mProgressBar.setProgress(elapsedTime);
            mPrefix.setText(TimeUtils.toProgressFormat(elapsedTime));
        }
    };

    private void resetPlayInfo() {
        mTitle.setText("");
        mArtist.setText(getContext().getString(R.string.play_to_start));
        mAlbum.setText("");
        mPrefix.setText(TimeUtils.toProgressFormat(0));
        mSuffix.setText(TimeUtils.toProgressFormat(0));
        mProgressBar.setProgress(0);
        mProgressBar.setMax(0);
    }

    private void updatePlayInfo(Play play) {
        mTitle.setText(play.getAudioFile().getTrack().getTitle());
        mArtist.setText(play.getAudioFile().getArtist().getName());
        mAlbum.setText(play.getAudioFile().getRelease().getTitle());

        int duration = play.getAudioFile().getDurationInSeconds();
        mProgressBar.setMax(duration);
        mSuffix.setText(TimeUtils.toProgressFormat(duration));
    }

    private SVGImageView newSvgImage(int weight, ImageView.ScaleType scaleType) {
        SVGImageView imgView = new SVGImageView(getContext());
        imgView.setLayoutParams(new LinearLayout.LayoutParams(mSizeBaseline, mSizeBaseline, weight));
        imgView.setScaleType(scaleType);
        return imgView;
    }

    private SVGImageView setSvgResource(SVGImageView imageView, int resourceId) {
        try {
            imageView.setSVG(SVG.getFromResource(getContext(), resourceId));

        } catch (SVGParseException e) {
            e.printStackTrace();
        }
        return imageView;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp
     *         A value in dp (density independent pixels) unit. Which we need to convert into pixels
     *
     * @return A float value to represent px equivalent to dp depending on device density
     */
    private float convertDpToPixel(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px
     *         A value in px (pixels) unit. Which we need to convert into db
     *
     * @return A float value to represent dp equivalent to px value
     */
    private float convertPixelsToDp(float px) {
        return px / getContext().getResources().getDisplayMetrics().density;
    }
}
