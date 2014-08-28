package com.feedfm.android.playersdk.service.webservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Play;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.FeedFMMediaPlayer;
import com.feedfm.android.playersdk.service.MediaPlayerManager;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.bus.EventMessage;
import com.feedfm.android.playersdk.service.bus.OutStationWrap;
import com.feedfm.android.playersdk.service.bus.PlayerAction;
import com.feedfm.android.playersdk.service.bus.SingleEventBus;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

/**
 * Created by mharkins on 8/21/14.
 */
public class PlayerService extends Service implements MediaPlayerManager.Listener {
    public static final String TAG = PlayerService.class.getSimpleName();

    protected static Bus eventBus = SingleEventBus.getInstance();

    protected Webservice mWebservice;
    protected MediaPlayerManager mMediaPlayerManager;

    // Client State data
    protected String mClientId;
    private List<Station> mStationList;

    private Placement mSelectedPlacement = null;
    private Station mSelectedStation = null;

    private static enum Status {
        IDLE,
        TUNING,
        TUNED,
        PLAYING,
        PAUSED,
        COMPLETE
    }

    private Status mActiveStatus = Status.IDLE;
    protected boolean mCanSkip;

    @Override
    public void onCreate() {
        super.onCreate();

        mWebservice = new Webservice(this);
        mMediaPlayerManager = new MediaPlayerManager(this, this);

        eventBus.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        eventBus.post(new EventMessage(EventMessage.Status.STARTED));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setCredentials(Credentials credentials) {
        mWebservice.setCredentials(credentials);

        getClientId();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setPlacementId(Placement placement) {
        mWebservice.setPlacementId(placement.getId(), new Webservice.Callback<Pair<Placement, List<Station>>>() {
            @Override
            public void onSuccess(Pair<Placement, List<Station>> result) {
                // Save user Placement
                mSelectedPlacement = result.first;
                mStationList = result.second;

                eventBus.post(result);
            }

            @Override
            public void onFailure(FeedFMError error) {
                Toast.makeText(PlayerService.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setStationId(OutStationWrap wrapper) {
        Station station = wrapper.getObject();
        if (mStationList != null) {
            for (Station s : mStationList) {
                if (s.getId().equals(station.getId())) {
                    mSelectedStation = s;
                    eventBus.post(mSelectedStation);
                    return;
                }
            }
        }
        Log.w(TAG, String.format("Station %s could not be found for current placement.", station.getId()));
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onPlayerAction(PlayerAction playerAction) {
        if (mClientId != null) {
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
    }

    public void getClientId() {
        mWebservice.getClientId(new Webservice.Callback<String>() {
            @Override
            public void onSuccess(String clientId) {
                mClientId = clientId;
                Toast.makeText(PlayerService.this, "Client Id: " + clientId, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(FeedFMError error) {
                Toast.makeText(PlayerService.this, error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private FeedFMMediaPlayer.State getActiveMediaPlayerState() {
        FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
        if (mediaPlayer != null) {
            mediaPlayer.getState();
        }
        return FeedFMMediaPlayer.State.IDLE;
    }

    public void tune(final boolean autoPlay) {
        if (!mMediaPlayerManager.isReadyForTuning()) {
            FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
            if (mediaPlayer != null) {
                mediaPlayer.setAutoPlay(true);
            }
            return;
        }
        // TODO: make the autoplay work when a song is being downloaded.

        mMediaPlayerManager.preTune(autoPlay);
        mWebservice.tune(
                mClientId,
                mSelectedPlacement != null ? mSelectedPlacement.getId() : null,
                mSelectedStation != null ? mSelectedStation.getId() : null,
                new Webservice.Callback<Play>() {

                    @Override
                    public void onSuccess(final Play play) {
                        mMediaPlayerManager.tune(play);
                    }

                    @Override
                    public void onFailure(FeedFMError error) {
                        mMediaPlayerManager.deTune();
                    }
                });
    }

    private void play() {
        if (mMediaPlayerManager.isPaused()) {
            FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
            mediaPlayer.start();

            mActiveStatus = Status.PLAYING;
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

        mWebservice.skip(playId, new Webservice.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    mMediaPlayerManager.stop();
                    play();
                }
            }

            @Override
            public void onFailure(FeedFMError error) {
                if (error != null) {
                    Toast.makeText(PlayerService.this, "Cannot Skip: " + error.toString(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, String.format("Skip (%s): failed", playId));
                }
                eventBus.post(new EventMessage(EventMessage.Status.SKIP_FAILED));
            }
        });
    }

    private void pause() {
        if (mMediaPlayerManager.isPlaying()) {
            FeedFMMediaPlayer mediaPlayer = mMediaPlayerManager.getActiveMediaPlayer();
            mediaPlayer.pause();

            mActiveStatus = Status.PAUSED;
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
        mWebservice.like(playId, new Webservice.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    eventBus.post(new EventMessage(EventMessage.Status.LIKE));
                }
            }

            @Override
            public void onFailure(FeedFMError error) {
                if (error != null) {
                    Log.e(TAG, String.format("Like (%s): failed - %s", playId, error.toString()));
                }
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
        mWebservice.unlike(playId, new Webservice.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    eventBus.post(new EventMessage(EventMessage.Status.UNLIKE));
                }
            }

            @Override
            public void onFailure(FeedFMError error) {
                if (error != null) {
                    Log.e(TAG, String.format("Unlike (%s): failed - %s", playId, error.toString()));
                }
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
        mWebservice.unlike(playId, new Webservice.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    eventBus.post(new EventMessage(EventMessage.Status.DISLIKE));
                }
            }

            @Override
            public void onFailure(FeedFMError error) {
                if (error != null) {
                    Log.e(TAG, String.format("Dislike (%s): failed - %s", playId, error.toString()));
                }
            }
        });
    }

    @Override
    public void onPrepared(FeedFMMediaPlayer mp) {
        mActiveStatus = Status.TUNED;

        // Only start the prepared media player if it is the active one.
        if (mMediaPlayerManager.isActiveMediaPlayer(mp)) {
            if (mp.isAutoPlay()) {
                mMediaPlayerManager.playNext();
            }
        }
    }

    @Override
    public void onPlayStart(Play play) {
        mWebservice.playStarted(play.getId(), new Webservice.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean canSkip) {
                mCanSkip = canSkip;

                Toast.makeText(PlayerService.this, "Skip: " + canSkip, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(FeedFMError error) {
                Toast.makeText(PlayerService.this, "failed to get Skip info: " + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPlayCompleted(Play play) {
        Toast.makeText(PlayerService.this, "Done with play:" + play.getAudioFile().getTrack().getTitle(), Toast.LENGTH_LONG).show();

        mWebservice.playCompleted(play.getId(), new Webservice.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                Toast.makeText(PlayerService.this, "Track Completed: " + success, Toast.LENGTH_LONG).show();
                play();
            }

            @Override
            public void onFailure(FeedFMError error) {
                Toast.makeText(PlayerService.this, "Track Completed: " + error.toString(), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onQueueDone() {
        Toast.makeText(PlayerService.this, "Done with queued Plays", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mMediaPlayerManager.release();
    }
}
