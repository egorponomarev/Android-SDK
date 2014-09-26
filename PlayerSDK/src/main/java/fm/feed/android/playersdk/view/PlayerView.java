package fm.feed.android.playersdk.view;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
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
import fm.feed.android.playersdk.observer.AudioSettingsContentObserver;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.util.TimeUtils;
import fm.feed.android.playersdk.util.UIUtils;

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

    private AudioSettingsContentObserver mAudioSettingsContentObserver;

    // XML attributes
    private boolean mAutoPlay;
    private boolean mHandlesNotification;

    private Player mPlayer;

    private String mShareSubject;
    private String mShareBody;

    private static final int DEFAULT_SVG_SIZE_DP = 26;
    private static final int DEFAULT_PADDING_DP = 10;

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

        mAutoPlay = a.getBoolean(R.styleable.PlayerView_autoPlay, false);
        mHandlesNotification = a.getBoolean(R.styleable.PlayerView_handlesNotification, true);

        //Don't forget this
        a.recycle();

        mShareSubject = null;
        mShareBody = null;

        mAudioSettingsContentObserver = new AudioSettingsContentObserver(getContext(), new Handler(), new AudioSettingsContentObserver.VolumeListener() {
            @Override
            public void onChange(int volume, boolean increased) {
                updateSpeakerUI();
            }
        });

        initializeView();
        initializePlayer();
    }

    /**
     * Returns the {@link fm.feed.android.playersdk.Player} object
     *
     * @return The {@link fm.feed.android.playersdk.Player} object
     */
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
     * <p>
     * Default subject: <i>Music on my embedded web-radio!</i>
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
     * <p>
     * Default body: <i>I'm listening to <b>{track title}</b> from <b>{artist name}</b> of the album <b>{album name}</b></i>
     * </p>
     *
     * @param body
     *         The Text to be sent in the Shared body
     */
    public void setShareBody(String body) {
        mShareBody = body;
    }

    public boolean isAutoPlay() {
        return mAutoPlay;
    }

    public void setIsAutoPlay(boolean mAutoPlay) {
        this.mAutoPlay = mAutoPlay;
    }

    public boolean handlesNotification() {
        return mHandlesNotification;
    }

    public void setHandlesNotification(boolean mHandlesNotification) {
        this.mHandlesNotification = mHandlesNotification;
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

        // We need to create the SVGImageView instances and add them programmatically.
        mDislike = newSvgImage(1, ImageView.ScaleType.FIT_START);
        mLike = newSvgImage(2, ImageView.ScaleType.FIT_CENTER);
        mPlayPause = newSvgImage(2, ImageView.ScaleType.FIT_CENTER);
        mSkip = newSvgImage(1, ImageView.ScaleType.FIT_END);
        mVolume = newSvgImage(1, ImageView.ScaleType.FIT_START);
        mShare = newSvgImage(1, ImageView.ScaleType.FIT_END);

        // Set the SVG resource to the SVGImageView
        setSvgResource(mDislike, R.drawable.ic_thumbdown_faded, R.string.accessibility_dislike);
        setSvgResource(mLike, R.drawable.ic_thumbup_faded, R.string.accessibility_like);
        setSvgResource(mPlayPause, R.drawable.ic_play_faded, R.string.accessibility_play);
        setSvgResource(mSkip, R.drawable.ic_skip_faded, R.string.accessibility_skip);
        setSvgResource(mVolume, R.drawable.ic_speakermute_faded, R.string.accessibility_volume_muted);
        setSvgResource(mShare, R.drawable.ic_share_faded, R.string.accessibility_share);

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
                if (!mPlayer.hasPlay()) {
                    return;
                }

                Play.LikeState likeState = mPlayer.getPlay().getLikeState();
                switch (likeState) {
                    case NONE:
                        setSvgResource(mDislike, R.drawable.ic_thumbdown_normal, R.string.accessibility_disliked);
                        mPlayer.dislike();
                        break;
                    case LIKED:
                        setSvgResource(mLike, R.drawable.ic_thumbup_faded, R.string.accessibility_like);
                        mPlayer.unlike();
                        break;
                    case DISLIKED:
                        // Do nothing
                        break;
                }
            }
        });

        mLike.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPlayer.hasPlay()) {
                    return;
                }

                Play.LikeState likeState = mPlayer.getPlay().getLikeState();
                switch (likeState) {
                    case NONE:
                        setSvgResource(mLike, R.drawable.ic_thumbup_normal, R.string.accessibility_liked);
                        mPlayer.like();
                        break;
                    case LIKED:
                        // Do nothing
                        break;
                    case DISLIKED:
                        setSvgResource(mDislike, R.drawable.ic_thumbdown_faded, R.string.accessibility_unlike);
                        setSvgResource(mLike, R.drawable.ic_thumbup_normal, R.string.accessibility_liked);
                        mPlayer.like();
                        break;
                }
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
                setSvgResource(mSkip, R.drawable.ic_skip_normal, R.string.accessibility_skipping);
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

        mVolume.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager am =
                        (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
                int volume = mAudioSettingsContentObserver.getCurrentVolume();
                am.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        volume,
                        AudioManager.FLAG_SHOW_UI);
            }
        });

        updateSpeakerUI();
    }

    private void updateSpeakerUI() {
        if (mAudioSettingsContentObserver.getCurrentVolume() == 0) {
            setSvgResource(mVolume, R.drawable.ic_speakerhigh_faded, R.string.accessibility_volume_muted);
        } else {
            setSvgResource(mVolume, R.drawable.ic_speakerhigh_normal, R.string.accessibility_volume_on);
        }
    }

    private void initializePlayer() {
        mPlayer = Player.getInstance(getContext(), mPlayerListener, AUTH_TOKEN, AUTH_SECRET, CUSTOM_NOTIFICATION_ID);
        mPlayer.registerPlayerListener(mPlayerListener);
        mPlayer.registerNavListener(mNavListener);
        mPlayer.registerSocialListener(mSocialListener);

        if (mPlayer.hasPlay()) {
            updatePlayInfo(mPlayer.getPlay());
            updateState(mPlayer.getState());
        } else {
            resetPlayInfo();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        getContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mAudioSettingsContentObserver);

        // If the Player has been initialized
        mPlayer.registerNavListener(mNavListener);
        mPlayer.registerPlayerListener(mPlayerListener);
        mPlayer.registerSocialListener(mSocialListener);

        if (mPlayer.hasPlay()) {
            updatePlayInfo(mPlayer.getPlay());

            mPlayerListener.onNotificationWillShow(mPlayer.getNotificationId());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mPlayer.unregisterNavListener(mNavListener);
        mPlayer.unregisterPlayerListener(mPlayerListener);
        mPlayer.unregisterSocialListener(mSocialListener);

        getContext().getContentResolver().unregisterContentObserver(mAudioSettingsContentObserver);
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

    private Player.PlayerListener mPlayerListener = new Player.PlayerListener() {
        @Override
        public void onPlayerInitialized(PlayInfo playInfo) {
            if (mAutoPlay) {
                mPlayer.play();
            }
        }

        @Override
        public void onPlaybackStateChanged(PlayInfo.State state) {
            updateState(state);
        }

        @Override
        public void onError(PlayerError playerError) {

        }

        @Override
        public void onNotificationWillShow(int notificationId) {
            // The user can decide not to override the notification when it shows.
            if (!mHandlesNotification) {
                return;
            }

            int stringId = getContext().getApplicationInfo().labelRes;
            String applicationName = getContext().getString(stringId);

            Intent i;
            PackageManager manager = getContext().getPackageManager();
            try {
                i = manager.getLaunchIntentForPackage(getContext().getApplicationInfo().packageName);
                if (i == null)
                    throw new PackageManager.NameNotFoundException();
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                i.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT |
                        i.FLAG_ACTIVITY_SINGLE_TOP);
            } catch (PackageManager.NameNotFoundException e) {
                return;
            }

            PendingIntent pi = PendingIntent.getActivity(getContext().getApplicationContext(), 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            String title = mPlayer.getPlay().getAudioFile().getTrack().getTitle();

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getContext());
            mBuilder.setContentIntent(pi);
            mBuilder.setContentTitle(applicationName);
            mBuilder.setContentText(getContext().getString(R.string.notification_body_template, title));
            mBuilder.setOngoing(true);
            mBuilder.setSmallIcon(android.R.drawable.ic_media_play);

            NotificationManager mNotificationManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            // NOTIFICATION_ID allows you to update the notification later on.
            mNotificationManager.notify(mPlayer.getNotificationId(), mBuilder.build());
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
            setSvgResource(mDislike, R.drawable.ic_thumbdown_faded, R.string.accessibility_dislike);
            setSvgResource(mLike, R.drawable.ic_thumbup_faded, R.string.accessibility_like);
            setSvgResource(mSkip, R.drawable.ic_skip_faded, R.string.accessibility_skip);
            mSkip.setVisibility(View.VISIBLE);
        }

        @Override
        public void onEndOfPlaylist() {
            resetPlayInfo();

            mArtist.setText(getContext().getString(R.string.end_of_playlist));
        }

        @Override
        public void onSkipFailed() {
            setSvgResource(mSkip, R.drawable.ic_skip_faded, R.string.accessibility_skip);

            mSkip.setVisibility(View.GONE);
        }

        @Override
        public void onBufferUpdate(Play play, int percentage) {

        }

        @Override
        public void onProgressUpdate(Play play, int elapsedTime, int totalTime) {
            mProgressBar.setProgress(elapsedTime);
            mPrefix.setText(TimeUtils.toProgressFormat(elapsedTime));
            mPrefix.setContentDescription(TimeUtils.toProgressAccessibilityFormat(elapsedTime));
        }
    };

    private Player.SocialListener mSocialListener = new Player.SocialListener() {
        @Override
        public void onLiked() {
            updateLikeState(mPlayer.getPlay().getLikeState());
        }

        @Override
        public void onUnliked() {
            updateLikeState(mPlayer.getPlay().getLikeState());
        }

        @Override
        public void onDisliked() {
            updateLikeState(mPlayer.getPlay().getLikeState());
        }
    };

    private void updateLikeState(Play.LikeState likeState) {
        if (likeState == null) {
            likeState = Play.LikeState.NONE;
        }
        switch (likeState) {
            case NONE:
                setSvgResource(mDislike, R.drawable.ic_thumbdown_faded, R.string.accessibility_dislike);
                setSvgResource(mLike, R.drawable.ic_thumbup_faded, R.string.accessibility_like);
                break;
            case LIKED:
                setSvgResource(mDislike, R.drawable.ic_thumbdown_faded, R.string.accessibility_unlike);
                setSvgResource(mLike, R.drawable.ic_thumbup_normal, R.string.accessibility_liked);
                break;
            case DISLIKED:
                setSvgResource(mDislike, R.drawable.ic_thumbdown_normal, R.string.accessibility_disliked);
                setSvgResource(mLike, R.drawable.ic_thumbup_faded, R.string.accessibility_like);
                break;
        }
    }

    private void resetPlayInfo() {
        mTitle.setText("");
        mArtist.setText(getContext().getString(R.string.play_to_start));
        mAlbum.setText("");
        mPrefix.setText(TimeUtils.toProgressFormat(0));
        mPrefix.setContentDescription(TimeUtils.toProgressAccessibilityFormat(0));
        mSuffix.setText(TimeUtils.toProgressFormat(0));
        mSuffix.setContentDescription(TimeUtils.toProgressAccessibilityFormat(0));
        mProgressBar.setProgress(0);
        mProgressBar.setMax(0);

        updateLikeState(null);
    }

    private void updatePlayInfo(Play play) {
        mTitle.setText(play.getAudioFile().getTrack().getTitle());
        mArtist.setText(play.getAudioFile().getArtist().getName());
        mAlbum.setText(play.getAudioFile().getRelease().getTitle());

        int duration = play.getAudioFile().getDurationInSeconds();
        mProgressBar.setMax(duration);
        mSuffix.setText(TimeUtils.toProgressFormat(duration));

        updateLikeState(play.getLikeState());
    }


    private void updateState(PlayInfo.State state) {
        // Set the SVG resource to the SVGImageView
        setSvgResource(mPlayPause, R.drawable.ic_play_faded, R.string.accessibility_play);
        setSvgResource(mSkip, R.drawable.ic_skip_faded, R.string.accessibility_skip);

        switch (state) {
            case WAITING:
            case READY:
                resetPlayInfo();
                break;
            case PAUSED:
                setSvgResource(mPlayPause, R.drawable.ic_play_normal, R.string.accessibility_play);
                break;
            case TUNING:
                setSvgResource(mPlayPause, R.drawable.ic_pause_normal, R.string.accessibility_pause);
                resetPlayInfo();
                mArtist.setText(getContext().getString(R.string.tuning));
                break;
            case TUNED:
            case PLAYING:
                setSvgResource(mPlayPause, R.drawable.ic_pause_normal, R.string.accessibility_pause);
                if (mPlayer.hasPlay()) {
                    updatePlayInfo(mPlayer.getPlay());
                }
                break;
            case STALLED:
                break;
            case COMPLETE:
                break;
            case REQUESTING_SKIP:
                setSvgResource(mSkip, R.drawable.ic_skip_normal, R.string.accessibility_skipping);
                break;
        }
    }

    private SVGImageView newSvgImage(int weight, ImageView.ScaleType scaleType) {
        SVGImageView imgView = new SVGImageView(getContext());

        int paddingInPixels = (int) UIUtils.convertDpToPixel(getContext(), DEFAULT_PADDING_DP);
        int sizeInPixels = (int) UIUtils.convertDpToPixel(getContext(), DEFAULT_SVG_SIZE_DP);
        imgView.setLayoutParams(new LinearLayout.LayoutParams(sizeInPixels, sizeInPixels + paddingInPixels * 2, weight));
        imgView.setScaleType(scaleType);
        imgView.setPadding(0, paddingInPixels, 0, paddingInPixels);
        return imgView;
    }

    private SVGImageView setSvgResource(SVGImageView imageView, int resourceId, int contentDescriptionResId) {
        try {
            imageView.setSVG(SVG.getFromResource(getContext(), resourceId));
            imageView.setContentDescription(getContext().getString(contentDescriptionResId));

        } catch (SVGParseException e) {
            e.printStackTrace();
        }
        return imageView;
    }
}
