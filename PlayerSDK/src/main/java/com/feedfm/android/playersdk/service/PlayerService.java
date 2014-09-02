package com.feedfm.android.playersdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.feedfm.android.playersdk.R;
import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Play;
import com.feedfm.android.playersdk.model.PlayerLibraryInfo;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.bus.BufferUpdate;
import com.feedfm.android.playersdk.service.bus.BusProvider;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.bus.EventMessage;
import com.feedfm.android.playersdk.service.bus.OutStationWrap;
import com.feedfm.android.playersdk.service.bus.PlayerAction;
import com.feedfm.android.playersdk.service.bus.ProgressUpdate;
import com.feedfm.android.playersdk.service.webservice.DefaultWebserviceCallback;
import com.feedfm.android.playersdk.service.webservice.Webservice;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;
import com.feedfm.android.playersdk.service.webservice.model.PlayerInfo;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * Created by mharkins on 8/21/14.
 */
public class PlayerService extends Service implements MediaPlayerManager.Listener, ProgressTracker.OnProgressListener {
    public static final String TAG = PlayerService.class.getSimpleName();

    protected static Bus eventBus = BusProvider.getInstance();

    protected Webservice mWebservice;
    protected MediaPlayerManager mMediaPlayerManager;
    private ProgressTracker mProgressTracker;

    protected PlayerInfo mPlayerInfo;

    // Client State data
    private boolean mDidTune = false;

    @Override
    public void onCreate() {
        super.onCreate();

        mPlayerInfo = new PlayerInfo();

        mWebservice = new Webservice(this);
        mMediaPlayerManager = new MediaPlayerManager(this, this);

        mProgressTracker = new ProgressTracker(this);

        eventBus.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PlayerLibraryInfo playerLibraryInfo = new PlayerLibraryInfo();
        playerLibraryInfo.versionName = getString(R.string.sdk_version);
        eventBus.post(playerLibraryInfo);

        // TODO: modify notification
        // Common notification ID (Should be sent along in the startIntent, or returned in the PlayerLibraryInfo or alternate object).
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Feed.FM");
        mBuilder.setSmallIcon(android.R.drawable.ic_media_play);
        // Make Service live even if Application is shut down by system
        startForeground(1234532, mBuilder.build());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mMediaPlayerManager.release();
    }

    /**
     * *************************************
     * Bus receivers
     */

    @Subscribe
    @SuppressWarnings("unused")
    public void setCredentials(Credentials credentials) {
        mWebservice.setCredentials(credentials);

        getClientId();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setPlacementId(Placement placement) {
        mWebservice.setPlacementId(placement.getId(), new DefaultWebserviceCallback<Pair<Placement, List<Station>>>() {
            @Override
            public void onSuccess(Pair<Placement, List<Station>> result) {
                mPlayerInfo.setStationList(result.second);

                boolean didChangePlacement =
                        mPlayerInfo.getPlacement() == null ||
                                !mPlayerInfo.getPlacement().getId().equals(result.first.getId());
                if (didChangePlacement) {
                    // Save user Placement
                    mPlayerInfo.setPlacement(result.first);
                    mPlayerInfo.setStation(null);

                    // TODO: perhaps cancel a tuning request?
                    mMediaPlayerManager.clearPendingQueue();
                }
                eventBus.post(result);
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setStationId(OutStationWrap wrapper) {
        Station station = wrapper.getObject();

        // If the user selects the same station, do nothing.
        boolean didChangeStation =
                mPlayerInfo.getStation() == null ||
                        !mPlayerInfo.getStation().getId().equals(station.getId());
        if (!didChangeStation) {
            return;
        }

        if (mPlayerInfo.hasStationList()) {
            for (Station s : mPlayerInfo.getStationList()) {
                if (s.getId().equals(station.getId())) {
                    mPlayerInfo.setStation(s);

                    // TODO: perhaps cancel a tuning request?
                    mMediaPlayerManager.clearPendingQueue();

                    eventBus.post(s);
                    return;
                }
            }
        }

        Log.w(TAG, String.format("Station %s could not be found for current placement.", station.getId()));
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onPlayerAction(PlayerAction playerAction) {
        switch (playerAction.getAction()) {
            case TUNE:
                tune(false);
                break;
            case PLAY:
                play();
                break;
            case SKIP:
                skip();
                break;
            case PAUSE:
                pause();
                break;
            case LIKE:
                like();
                break;
            case UNLIKE:
                unlike();
                break;
            case DISLIKE:
                dislike();
                break;
        }
    }

    /*
     * Bus receivers
     ****************************************/
    public void getClientId() {
        mWebservice.getClientId(new DefaultWebserviceCallback<String>() {
            @Override
            public void onSuccess(String clientId) {
                mPlayerInfo.setClientId(clientId);
            }
        });
    }

    /**
     * Downloads and loads the the next Track into a {@link com.feedfm.android.playersdk.service.FeedFMMediaPlayer}
     *
     * @param autoPlay {@code true} to have the track start automatically when it is reached.
     */
    public void tune(final boolean autoPlay) {
        if (!mMediaPlayerManager.isReadyForTuning()) {
            FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
            if (mediaPlayer != null) {
                mediaPlayer.setAutoPlay(true);
            }
            return;
        }

        mMediaPlayerManager.preTune(autoPlay);
        mWebservice.tune(
                mPlayerInfo.getClientId(),
                mPlayerInfo.getPlacement(),
                mPlayerInfo.getStation(),
                null, // For now don't put in the AudioFormat
                null,
                new DefaultWebserviceCallback<Play>() {

                    @Override
                    public void onSuccess(final Play play) {

                        // This exception is called if the MediaPlayer dataSource is set while the media player is in an invalid state.
                        // In that case we will abandon this media player and start over the tuning.
                        try {
                            mMediaPlayerManager.tune(play);
                        } catch (IllegalStateException e) {
                            mMediaPlayerManager.preTune(autoPlay);
                            mMediaPlayerManager.tune(play);
                        }
                    }

                    @Override
                    public void onFailure(FeedFMError error) {
                        super.onFailure(error);

                        mMediaPlayerManager.deTune();
                    }
                });
    }

    private void play() {
        if (mMediaPlayerManager.isPaused()) {
            FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
            mediaPlayer.start();
            mProgressTracker.resume();

        } else if (mMediaPlayerManager.isReadyForPlay()) {
            mMediaPlayerManager.playNext();
        } else if (!mMediaPlayerManager.isPlaying()) {
            tune(true);
        }
    }

    private void skip() {
        if (!mMediaPlayerManager.isPaused() && !mMediaPlayerManager.isPlaying()) {
            Log.i(TAG, "Could not Skip track. No active Play");
            return;
        }

        final FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
        final String playId = mediaPlayer.getPlay().getId();

        mWebservice.skip(playId, new DefaultWebserviceCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    mProgressTracker.stop();
                    mMediaPlayerManager.skip();
                    play();
                }
            }

            @Override
            public void onFailure(FeedFMError error) {
                super.onFailure(error);

                eventBus.post(new EventMessage(EventMessage.Status.SKIP_FAILED));
            }
        });
    }

    private void pause() {
        if (mMediaPlayerManager.isPlaying()) {
            FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
            mediaPlayer.pause();
            mProgressTracker.pause();
        } else {
            Log.i(TAG, "Could not Pause track. Not playing.");
        }
    }

    private void like() {
        if (!mMediaPlayerManager.isPaused() && !mMediaPlayerManager.isPlaying()) {
            Log.w(TAG, "Could not Like track. No active Play");
            return;
        }

        final FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
        final String playId = mediaPlayer.getPlay().getId();
        mWebservice.like(playId, new DefaultWebserviceCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                eventBus.post(new EventMessage(EventMessage.Status.LIKE));
            }
        });
    }

    private void unlike() {
        if (!mMediaPlayerManager.isPaused() && !mMediaPlayerManager.isPlaying()) {
            Log.i(TAG, "Could not Like track. No active Play");
            return;
        }

        final FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
        final String playId = mediaPlayer.getPlay().getId();
        mWebservice.unlike(playId, new DefaultWebserviceCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                eventBus.post(new EventMessage(EventMessage.Status.UNLIKE));
            }
        });
    }

    private void dislike() {
        if (!mMediaPlayerManager.isPaused() && !mMediaPlayerManager.isPlaying()) {
            Log.i(TAG, "Could not Like track. No active Play");
            return;
        }

        final FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
        final String playId = mediaPlayer.getPlay().getId();
        mWebservice.dislike(playId, new DefaultWebserviceCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                eventBus.post(new EventMessage(EventMessage.Status.DISLIKE));
            }
        });
    }

    /**
     * ******************************************************
     * MediaPlayerManager.Listener implementation
     */

    @Override
    public void onPrepared(FeedFMMediaPlayer mp) {
        // Only start the prepared media player if it is the active one.
        if (mMediaPlayerManager.isActiveMediaPlayer(mp)) {
            if (mp.isAutoPlay()) {
                mMediaPlayerManager.playNext();
            }
        }
    }

    @Override
    public void onPlayStart(Play play) {
        mDidTune = false;

        eventBus.post(play);
        mProgressTracker.start(play);

        mWebservice.playStarted(play.getId(), new DefaultWebserviceCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean canSkip) {
                mPlayerInfo.setSkippable(canSkip);
            }
        });
    }

    @Override
    public void onPlayCompleted(Play play, boolean isSkipped) {
        mProgressTracker.stop();

        // Keep cycling through plays.
        play();

        if (!isSkipped) {
            mWebservice.playCompleted(play.getId(), new DefaultWebserviceCallback<Boolean>() {});
        }
    }

    @Override
    public void onBufferingUpdate(Play play, int percent) {
        eventBus.post(new BufferUpdate(play, percent));

        // Tune the next song once the buffering of the current song is complete.
        // TODO: Do this better than with a flag.
        if (!mDidTune && percent == 100) {
            mDidTune = true;
            tune(false);
        }
    }

    @Override
    public void onQueueDone() {
        Toast.makeText(PlayerService.this, "Done with queued Plays", Toast.LENGTH_LONG).show();
    }

    /*
     * MediaPlayerManager.Listener implementation
     **********************************************************/

    /**
     * *******************************************************
     * ProgressTracker.OnProgressListener implementation
     */

    @Override
    public void onProgressUpdate(Play play, int elapsed, int totalDuration) {
        FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();

        eventBus.post(new ProgressUpdate(play, mediaPlayer.getCurrentPosition() / 1000, mediaPlayer.getDuration() / 1000));
    }

    /*
     * ProgressTracker.OnProgressListener implementation
     **********************************************************/
}
