package fm.feed.android.playersdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import fm.feed.android.playersdk.R;
import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.PlayerLibraryInfo;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.bus.BufferUpdate;
import fm.feed.android.playersdk.service.bus.BusProvider;
import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.bus.EventMessage;
import fm.feed.android.playersdk.service.bus.OutStationWrap;
import fm.feed.android.playersdk.service.bus.PlayerAction;
import fm.feed.android.playersdk.service.bus.ProgressUpdate;
import fm.feed.android.playersdk.service.task.ClientIdTask;
import fm.feed.android.playersdk.service.task.PlacementIdTask;
import fm.feed.android.playersdk.service.task.PlayTask;
import fm.feed.android.playersdk.service.task.SimpleNetworkTask;
import fm.feed.android.playersdk.service.task.StationIdTask;
import fm.feed.android.playersdk.service.task.TuneTask;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;
import fm.feed.android.playersdk.service.webservice.model.PlayerInfo;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by mharkins on 8/21/14.
 */
public class PlayerService extends Service {
    public static final String TAG = PlayerService.class.getSimpleName();

    protected static Bus eventBus = BusProvider.getInstance();

    protected Webservice mWebservice;
    protected PlayerInfo mPlayerInfo;

    protected TaskQueueManager mPrimaryQueue = new TaskQueueManager("Primary Queue");
    protected TaskQueueManager mTuningQueue = new TaskQueueManager("Tuning Queue");
    protected TaskQueueManager mSecondaryQueue = new TaskQueueManager("Secondary Queue");

    private Queue<FeedFMMediaPlayer> mMediaPlayerQueue = new LinkedList<FeedFMMediaPlayer>();

    @Override
    public void onCreate() {
        super.onCreate();

        mPlayerInfo = new PlayerInfo();

        mWebservice = new Webservice(this);

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

        for (FeedFMMediaPlayer mediaPlayer : mMediaPlayerQueue) {
            mediaPlayer.release();

            mMediaPlayerQueue.remove(mediaPlayer);
        }

        mPrimaryQueue.clear();
        mTuningQueue.clear();
        mSecondaryQueue.clear();
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
        PlacementIdTask task = new PlacementIdTask(mPrimaryQueue, mWebservice, mPlayerInfo, placement.getId()) {
            @Override
            public void onPlacementChanged(Placement newPlacement) {
                eventBus.post(newPlacement);

                play();
            }
        };

        // PlacementIdTask cancels everything but:
        // - ClientIdTask
        mPrimaryQueue.clearLowerPriorities(task);

        // Cancel any Tunings that might be taking place
        mTuningQueue.clear();

        mPrimaryQueue.offerUnique(task);
        mPrimaryQueue.next();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setStationId(OutStationWrap wrapper) {
        Station s = wrapper.getObject();
        final Integer stationId = s.getId();

        StationIdTask task = new StationIdTask(null, mPlayerInfo, stationId) {
            @Override
            public void onStationChanged(Station station) {
                if (station != null) {
                    eventBus.post(station);

                    play();
                } else {
                    Log.w(TAG, String.format("Station %s could not be found or was already selected in current placement", stationId));
                }
            }
        };

        // StationIdTask cancels everything but:
        // - ClientIdTask
        mPrimaryQueue.clearLowerPriorities(task);
        mPrimaryQueue.offerUnique(task);
        mPrimaryQueue.next();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onPlayerAction(PlayerAction playerAction) {
        switch (playerAction.getAction()) {
            case TUNE:
                tune();
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
        ClientIdTask task = new ClientIdTask(mPrimaryQueue, mWebservice) {
            @Override
            public void onClientIdChanged(String clientId) {
                mPlayerInfo.setClientId(clientId);
            }
        };

        // Getting a new ClientId will cancel whatever task is currently in progress.
        mPrimaryQueue.clearLowerPriorities(task);
        mPrimaryQueue.offerUnique(task);
        mPrimaryQueue.next();
    }

    /**
     * Downloads and loads the the next Track into a {@link fm.feed.android.playersdk.service.FeedFMMediaPlayer}
     */
    public void tune() {
        if (!mTuningQueue.isEmpty()) {
            Log.i(TAG, String.format("Switching TuneTask from %s to %s", mTuningQueue.getIdentifier(), mPrimaryQueue.getIdentifier()));
            mPrimaryQueue.offerUnique(mTuningQueue.poll());
            mPrimaryQueue.next();
        } else {
            // Only tune on the Primary Queue if it is empty of a PlayTask.
            TaskQueueManager queueManager = !mPrimaryQueue.hasTaskType(PlayTask.class) ? mPrimaryQueue : mTuningQueue;


            // Tune in a separate Queue if we are already playing something.
            TuneTask task = new TuneTask(queueManager, mWebservice, mPlayerInfo) {
                @Override
                public void onTuned(FeedFMMediaPlayer mediaPlayer, Play play) {
                    mMediaPlayerQueue.offer(mediaPlayer);
                }
            };

            // TuneTask is low priority and cancels nothing. It will only be queued.
            queueManager.offerUnique(task);
            queueManager.next();
        }
    }

    /**
     * If there is currently a Playing task:
     * <ul>
     * <li>
     * Don't do anything if Playing
     * </li>
     * <li>
     * Resume if Paused
     * </li>
     * </ul>
     * <p/>
     * If there is no currently Playing Task:
     * <ol>
     * <li>If the media player queue is empty, {@link PlayerService#tune()}</li>
     * <li>Queue up a new {@link fm.feed.android.playersdk.service.task.PlayTask}.</li>
     * </ol>
     */
    private void play() {
        if (mPrimaryQueue.isPlayingTask()) {
            PlayTask playTask = (PlayTask) mPrimaryQueue.peek();

            // If the Play is Paused, Resume.
            if (playTask.isPaused()) {
                playTask.play();
            }
        }


        if (mMediaPlayerQueue.isEmpty()) {
            tune();
        }

        PlayTask task = new PlayTask(mPrimaryQueue, mWebservice) {
            @Override
            public FeedFMMediaPlayer getMediaPlayer() {
                return mMediaPlayerQueue.poll();
            }

            @Override
            public void onPlayBegin(PlayTask playTask, Play play) {
                eventBus.post(play);

                final String playId = play.getId();

                SimpleNetworkTask playStartTask = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice) {
                    @Override
                    public Boolean performRequestSynchronous() throws FeedFMError {
                        return mWebservice.playStarted(playId);
                    }

                    @Override
                    public void onDone(Boolean canSkip) {
                        boolean skippable = canSkip != null && canSkip == true;
                        if (!skippable) {
                            if (mPrimaryQueue.isPlayingTask()) {
                                PlayTask playTask = (PlayTask) mPrimaryQueue.peek();

                                if (playTask.getPlay() != null && playTask.getPlay().getId() == playId) {
                                    playTask.setSkippable(skippable);
                                }
                            }
                            eventBus.post(new EventMessage(EventMessage.Status.SKIP_FAILED));
                        }

                    }

                };
                mSecondaryQueue.offer(playStartTask);
                mSecondaryQueue.next();
            }

            @Override
            public void onProgressUpdate(Play play, Integer progressInMillis, Integer durationInMillis) {
                eventBus.post(new ProgressUpdate(play, progressInMillis / 1000, durationInMillis / 1000));
            }

            @Override
            public void onBufferingUpdate(Play play, Integer percent) {
                eventBus.post(new BufferUpdate(play, percent));

                if (percent == 100) {
                    // Tune the next song once the buffering of the current song is complete.
                    tune();
                }
            }

            @Override
            public void onPlayFinished(final Play play, boolean isSkipped) {
                if (!isSkipped) {
                    SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice) {
                        @Override
                        public Boolean performRequestSynchronous() throws FeedFMError {
                            return mWebservice.playCompleted(play.getId());
                        }

                        @Override
                        public void onDone(Boolean aBoolean) {

                        }
                    };
                    mSecondaryQueue.offer(task);
                    mSecondaryQueue.next();
                }

                // Keep cycling through plays.
                PlayerService.this.play();
            }
        };

        mPrimaryQueue.clearLowerPriorities(task);
        mPrimaryQueue.offerIfNotExist(task);
        mPrimaryQueue.next();
    }

    private void skip() {
        // TODO: check if Can skip!
        // eventBus.post(new EventMessage(EventMessage.Status.SKIP_FAILED));
        if (mPrimaryQueue.isPlayingTask()) {
            PlayTask playTask = (PlayTask) mPrimaryQueue.peek();
            playTask.cancel(true);
        } else {
            Log.i(TAG, "Could not Skip track. No active Play");
        }
    }

    private void pause() {
        if (mPrimaryQueue.isPlayingTask()) {
            PlayTask playTask = (PlayTask) mPrimaryQueue.peek();

            // If the Play is Paused, Resume.
            if (playTask.isPlaying()) {
                playTask.pause();
            }
            return;
        }
        Log.i(TAG, "Could not Pause track. Not playing.");
    }

    private void like() {
        if (mPrimaryQueue.isPlayingTask()) {
            PlayTask playTask = (PlayTask) mPrimaryQueue.peek();
            final String playId = playTask.getPlay().getId();

            SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice) {
                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.like(playId);
                }

                @Override
                public void onDone(Boolean aBoolean) {
                    eventBus.post(new EventMessage(EventMessage.Status.LIKE));
                }
            };
            mSecondaryQueue.offer(task);
            mSecondaryQueue.next();
        } else {
            Log.w(TAG, "Could not Like track. No active Play");
        }
    }

    private void unlike() {

        if (mPrimaryQueue.isPlayingTask()) {
            PlayTask playTask = (PlayTask) mPrimaryQueue.peek();
            final String playId = playTask.getPlay().getId();

            SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice) {
                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.unlike(playId);
                }

                @Override
                public void onDone(Boolean aBoolean) {
                    eventBus.post(new EventMessage(EventMessage.Status.UNLIKE));
                }
            };
            mSecondaryQueue.offer(task);
            mSecondaryQueue.next();
        } else {
            Log.w(TAG, "Could not Unlike track. No active Play");
        }
    }

    private void dislike() {
        if (mPrimaryQueue.isPlayingTask()) {
            PlayTask playTask = (PlayTask) mPrimaryQueue.peek();
            final String playId = playTask.getPlay().getId();

            SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice) {
                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.dislike(playId);
                }

                @Override
                public void onDone(Boolean aBoolean) {
                    eventBus.post(new EventMessage(EventMessage.Status.DISLIKE));
                }
            };
            mSecondaryQueue.offer(task);
            mSecondaryQueue.next();
        } else {
            Log.w(TAG, "Could not Dislike track. No active Play");
        }
    }
}
