package fm.feed.android.playersdk.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Date;

import fm.feed.android.playersdk.R;
import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.bus.BufferUpdate;
import fm.feed.android.playersdk.service.bus.BusProvider;
import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.bus.EventMessage;
import fm.feed.android.playersdk.service.bus.OutPlacementWrap;
import fm.feed.android.playersdk.service.bus.OutStationWrap;
import fm.feed.android.playersdk.service.bus.PlayerAction;
import fm.feed.android.playersdk.service.bus.ProgressUpdate;
import fm.feed.android.playersdk.service.queue.MainQueue;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.queue.TuningQueue;
import fm.feed.android.playersdk.service.task.ClientIdTask;
import fm.feed.android.playersdk.service.task.PlacementIdTask;
import fm.feed.android.playersdk.service.task.PlayTask;
import fm.feed.android.playersdk.service.task.SimpleNetworkTask;
import fm.feed.android.playersdk.service.task.StationIdTask;
import fm.feed.android.playersdk.service.task.TuneTask;
import fm.feed.android.playersdk.service.util.AudioFocusManager;
import fm.feed.android.playersdk.service.util.DataPersister;
import fm.feed.android.playersdk.service.util.MediaPlayerPool;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * Created by mharkins on 8/21/14.
 */
public class PlayerService extends Service {
    public static final String TAG = PlayerService.class.getSimpleName();

    protected static Bus eventBus = BusProvider.getInstance();

    protected Webservice mWebservice;
    protected PlayInfo mPlayInfo;
    private DataPersister mDataPersister;


    protected MainQueue mMainQueue;
    protected TuningQueue mTuningQueue;
    protected TaskQueueManager mSecondaryQueue;

    private MediaPlayerPool mMediaPlayerPool;

    private boolean mInitialized;
    private boolean mValidStart;

    public static BuildType mDebug;

    public static enum BuildType {
        DEBUG,
        RELEASE
    }

    public static enum ExtraKeys {
        timestamp,
        notificationId,
        buildType;

        ExtraKeys() {
        }

        @Override
        public String toString() {
            return PlayerService.class.getPackage().toString() + "." + name();
        }
    }

    private BroadcastReceiver mConnectivityBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            Log.d(TAG, "Received Connectivity Broadcast: Connected ? " + isConnected);

            if (!isConnected) {
                if (mMainQueue.hasActivePlayTask()) {
                    ((PlayTask) mMainQueue.peek()).pause();
                }

                // Pause the Queues
                mMainQueue.pause();
                mTuningQueue.pause();
                mSecondaryQueue.pause();
            } else {
                //Resume the Queues
                mMainQueue.unpause();
                if (!mMainQueue.hasActivePlayTask()) {
                    mMainQueue.next();
                } else {
                    ((PlayTask) mMainQueue.peek()).play();
                }
                mTuningQueue.unpause();
                mTuningQueue.next();
                mSecondaryQueue.unpause();
                mSecondaryQueue.next();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mInitialized = false;
        mValidStart = false;

        mPlayInfo = new PlayInfo(getString(R.string.sdk_version));

        mDataPersister = new DataPersister(this);
        mWebservice = new Webservice(this);

        mMediaPlayerPool = new MediaPlayerPool();

        mMainQueue = new MainQueue();
        mTuningQueue = new TuningQueue();
        mSecondaryQueue = new TaskQueueManager("Secondary Queue");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mValidStart = false;

        if (intent != null) {
            long now = new Date().getTime();
            long intentTimestamp = intent.getLongExtra(ExtraKeys.timestamp.toString(), 0);

            // if it's been more than a second since the Service was called to start, cancel.
            // It's the android back stack relaunching the Intent.
            if (now - intentTimestamp < 1000) {
                mValidStart = true;
            }
        }

        if (mValidStart) {
            mDebug = BuildType.valueOf(intent.getStringExtra(ExtraKeys.buildType.toString()));

            if (!mInitialized) {
                init();
                mInitialized = true;
            }
            resume(intent);
        } else {
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void init() {
        initAudioManager();

        mMainQueue = new MainQueue();
        mTuningQueue = new TuningQueue();
        mSecondaryQueue = new TaskQueueManager("Secondary Queue");

        mMediaPlayerPool = new MediaPlayerPool();

        IntentFilter connectivityIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectivityBroadcastReceiver, connectivityIntentFilter);

        eventBus.register(this);
    }

    /**
     * Resend the Player information onto the Event bus. This is to allow the Client application to capture the current state of the application.
     *
     * @param intent
     */
    private void resume(Intent intent) {
        int notificationId = intent.getIntExtra(ExtraKeys.notificationId.toString(), -1);
        if (notificationId >= 0) {
            mPlayInfo.setNotificationId(notificationId);
        }

        eventBus.post(mPlayInfo);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // Was it a legit Service Startup?
        if (mValidStart) {
            releaseAudioManager();

            mMainQueue.clear();
            mTuningQueue.clear();
            mSecondaryQueue.clear();

            mMediaPlayerPool.release();

            eventBus.unregister(this);

            unregisterReceiver(mConnectivityBroadcastReceiver);
        }

        mInitialized = false;
    }


    public void enableForeground() {
        int stringId = getApplicationInfo().labelRes;
        String applicationName = getString(stringId);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this);
        if (mDebug == BuildType.DEBUG) {
            mBuilder.setContentTitle(applicationName + "-SDK");
        } else {
            mBuilder.setContentTitle(applicationName);
        }
        mBuilder.setSmallIcon(android.R.drawable.ic_media_play);

        // Make Service live even if Application is shut down by system
        startForeground(mPlayInfo.getNotificationId(), mBuilder.build());
        eventBus.post(new EventMessage(EventMessage.Status.NOTIFICATION_WILL_SHOW));
    }

    private void disableForeground() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mPlayInfo.getNotificationId());
        stopForeground(true);
    }

    private void updateState(PlayInfo.State state) {
        mPlayInfo.setState(state);
        eventBus.post(new EventMessage(EventMessage.Status.STATUS_UPDATED));
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
        setPlacement(null, false); // Get the default placement information
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setPlacementId(OutPlacementWrap wrapper) {
        Placement p = wrapper.getObject();

        setPlacement(p, true);
    }

    private void setPlacement(Placement p, final boolean isUserInteraction) {
        final Integer placementId = p != null ? p.getId() : null;

        PlacementIdTask task = new PlacementIdTask(mMainQueue, mWebservice, new PlacementIdTask.OnPlacementIdChanged() {
            @Override
            public void onSuccess(Placement placement) {
                mPlayInfo.setStationList(placement.getStationList());

                boolean didChangePlacement =
                        mPlayInfo.getPlacement() == null ||
                                !mPlayInfo.getPlacement().getId().equals(placement.getId());
                if (didChangePlacement) {
                    // Save user Placement
                    mPlayInfo.setPlacement(placement);
                    mPlayInfo.setStation(null);
                }

                updateState(PlayInfo.State.READY);

                eventBus.post(placement);

                if (isUserInteraction) {
                    play();
                }
            }
        }, placementId);


        // PlacementIdTask cancels everything but:
        // - ClientIdTask
        mMainQueue.clearLowerPriorities(task);

        // Cancel any Tunings that might be taking place
        mTuningQueue.clear();

        mMainQueue.offerUnique(task);
        mMainQueue.next();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setStationId(OutStationWrap wrapper) {
        Station s = wrapper.getObject();
        final Integer stationId = s.getId();

        Integer currentStationId = mPlayInfo.getStation() != null ? mPlayInfo.getStation().getId() : null;

        // If the user selects the same station, do nothing.
        boolean didChangeStation =
                currentStationId == null ||
                        !currentStationId.equals(stationId);
        if (!didChangeStation) {
            Log.w(TAG, String.format("Station %s is already selected in current placement", stationId));
            return;
        }

        StationIdTask task = new StationIdTask(mMainQueue, new StationIdTask.OnStationIdChanged() {
            @Override
            public void onSuccess(Station station) {
                if (station != null) {
                    mPlayInfo.setStation(station);

                    eventBus.post(station);

                    play();
                } else {
                    Log.w(TAG, String.format("Station %s could not be found or was already selected in current placement", stationId));
                }
            }
        }, mPlayInfo.getStationList(), currentStationId, stationId);

        // StationIdTask cancels everything but:
        // - ClientIdTask
        mMainQueue.clearLowerPriorities(task);

        mTuningQueue.clear();

        mMainQueue.offerUnique(task);
        mMainQueue.next();
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
        String clientId = mDataPersister.getString(DataPersister.Key.clientId, null);

        if (clientId != null && mDebug != BuildType.DEBUG) {
            mPlayInfo.setClientId(clientId);
            Toast.makeText(PlayerService.this, "Retrieved existing Client ID!", Toast.LENGTH_LONG).show();
        } else {
            ClientIdTask task = new ClientIdTask(mMainQueue, mWebservice, new ClientIdTask.OnClientIdChanged() {
                @Override
                public void onSuccess(String clientId) {
                    mDataPersister.putString(DataPersister.Key.clientId, clientId);
                    // TODO: perhaps encrypt clientId first.
                    mPlayInfo.setClientId(clientId);
                    Toast.makeText(PlayerService.this, "New Client ID!", Toast.LENGTH_LONG).show();
                }
            });

            // Getting a new ClientId will cancel whatever task is currently in progress.
            mMainQueue.clearLowerPriorities(task);
            mMainQueue.offerUnique(task);
            mMainQueue.next();
        }
    }

    /**
     * Downloads and loads the the next Track into a {@link fm.feed.android.playersdk.service.FeedFMMediaPlayer}
     */
    public void tune() {
        if (mTuningQueue.isTuning()) {
            Log.i(TAG, String.format("Switching TuneTask from %s to %s", mTuningQueue.getIdentifier(), mMainQueue.getIdentifier()));

            mMainQueue.offerUnique(mTuningQueue.poll());
            mMainQueue.next();
        } else {
            // Only tune on the Primary Queue if it is empty of a Tuning or Playing Task.
            TaskQueueManager queueManager;
            if (!mMainQueue.hasActivePlayTask()) {
                queueManager = mMainQueue;
            } else {
                PlayTask playTask = (PlayTask) mMainQueue.peek();
                if (playTask.isBuffering()) {
                    Log.i(TAG, "Can't Tune while still Buffering a PlayTask.");
                    return;
                }
                queueManager = mTuningQueue;
            }

            // Tune in a separate Queue if we are already playing something.
            final TuneTask task = new TuneTask(queueManager, mWebservice, mMediaPlayerPool, new TuneTask.TuneTaskListener() {
                @Override
                public void onMetaDataLoaded(TuneTask tuneTask, Play play) {
                    // Only publish the Play info if the Tuning is done on the main queue (this means that this TuneTask isn't in the background).
                    if (!mMainQueue.hasActivePlayTask()) {
                        updateState(PlayInfo.State.TUNING);

                        eventBus.post(play);
                    }
                }

                @Override
                public void onBufferingStarted() {
                    // Only publish the Play info if the Tuning is done on the main queue (this means that this TuneTask isn't in the background).
                    if (!mMainQueue.hasActivePlayTask()) {
                        updateState(PlayInfo.State.STALLED);
                    }
                }

                @Override
                public void onBufferingEnded() {
                    updateState(PlayInfo.State.TUNED);
                }

                @Override
                public void onSuccess(TuneTask tuneTask, FeedFMMediaPlayer mediaPlayer, Play play) {
                    updateState(PlayInfo.State.TUNED);
                }

                @Override
                public void onApiError(FeedFMError mApiError) {
                    handleApiError(mApiError);
                }
            }, mPlayInfo, mPlayInfo.getClientId());

            // TuneTask is low priority and cancels nothing. It will only be queued.
            queueManager.offerIfNotExist(task);
            queueManager.next();
        }
    }

    public void handleApiError(FeedFMError error) {
        if (error.getCode() == FeedFMError.CODE_NOT_IN_US) {
            eventBus.post(new EventMessage(EventMessage.Status.NOT_IN_US));
            disableForeground();
        } else if (error.getCode() == FeedFMError.CODE_END_OF_PLAYLIST) {
            eventBus.post(new EventMessage(EventMessage.Status.END_OF_PLAYLIST));
            disableForeground();
        } else if (error.getCode() == FeedFMError.CODE_PLAYBACK_ALREADY_STARTED) {
            Log.w(TAG, error);
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
        // Don't play if we don't have rights for it.
        if (!mAudioFocusManager.isAudioFocusGranted()) {
            return;
        }

        if (mMainQueue.hasActivePlayTask()) {
            PlayTask playTask = (PlayTask) mMainQueue.peek();

            // If the Play is Paused, Resume.
            if (playTask.isPaused()) {
                playTask.play();

                enableForeground();
                return;
            }
        } else {
            enableForeground();
        }


        if (!mMediaPlayerPool.hasTunedMediaPlayer()) {
            tune();
        }

        PlayTask task = new PlayTask(mMainQueue, mWebservice, PlayerService.this, mMediaPlayerPool, new PlayTask.PlayTaskListener() {
            @Override
            public void onPlayBegin(PlayTask playTask, Play play) {
                mPlayInfo.setCurrentPlay(play);

                eventBus.post(play);

                final String playId = play.getId();

                SimpleNetworkTask playStartTask = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                    @Override
                    public void onStart() {
                        updateState(PlayInfo.State.REQUESTING_SKIP);
                    }

                    @Override
                    public Boolean performRequestSynchronous() throws FeedFMError {
                        return mWebservice.playStarted(playId);
                    }

                    private void updateSkipStatus(boolean canSkip) {
                        if (mMainQueue.hasActivePlayTask()) {
                            PlayTask playTask = (PlayTask) mMainQueue.peek();

                            if (playTask.getPlay() != null && playTask.getPlay().getId() == playId) {
                                playTask.setSkippable(canSkip);
                            }

                            if (playTask.isPlaying()) {
                                updateState(PlayInfo.State.PLAYING);
                            } else if (playTask.isPaused()) {
                                updateState(PlayInfo.State.PAUSED);
                            } else {
                                updateState(PlayInfo.State.TUNED);
                            }
                        }
                    }

                    @Override
                    public void onSuccess(Boolean canSkip) {
                        boolean skippable = canSkip != null && canSkip == true;
                        updateSkipStatus(skippable);

                    }

                    @Override
                    public void onFail() {
                        updateSkipStatus(false);
                    }
                });
                mSecondaryQueue.offer(playStartTask);
                mSecondaryQueue.next();
            }

            @Override
            public void onPlay(PlayTask playTask) {
                updateState(PlayInfo.State.PLAYING);
            }

            @Override
            public void onPause(PlayTask playTask) {
                updateState(PlayInfo.State.PAUSED);
            }

            @Override
            public void onBufferingStarted(PlayTask playTask) {
                updateState(PlayInfo.State.STALLED);
            }

            @Override
            public void onBufferingEnded(PlayTask playTask) {
                if (playTask.isPlaying()) {
                    updateState(PlayInfo.State.PLAYING);
                } else if (playTask.isPaused()) {
                    updateState(PlayInfo.State.PAUSED);
                } else {
                    updateState(PlayInfo.State.TUNED);
                }
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
                updateState(PlayInfo.State.COMPLETE);

                if (!isSkipped) {
                    SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public Boolean performRequestSynchronous() throws FeedFMError {
                            return mWebservice.playCompleted(play.getId());
                        }

                        @Override
                        public void onSuccess(Boolean aBoolean) {

                        }

                        @Override
                        public void onFail() {

                        }
                    });
                    mSecondaryQueue.offer(task);
                    mSecondaryQueue.next();

                    // Keep cycling through plays.
                    PlayerService.this.play();
                }
            }
        });


        mMainQueue.clearLowerPriorities(task);
        mMainQueue.offerIfNotExist(task);
        mMainQueue.next();
    }

    private void skip() {
        if (mMainQueue.hasActivePlayTask()) {
            PlayTask playTask = (PlayTask) mMainQueue.peek();

            if (playTask.isSkippable()) {
                mMainQueue.remove(playTask);
                playTask.cancel(true);

                play();
            } else {
                eventBus.post(new EventMessage(EventMessage.Status.SKIP_FAILED));
            }
        } else {
            Log.i(TAG, "Could not Skip track. No active Play");
        }
    }

    private void pause() {
        if (mMainQueue.hasActivePlayTask()) {
            PlayTask playTask = (PlayTask) mMainQueue.peek();

            // If the Play is Paused, Resume.
            if (playTask.isPlaying()) {
                playTask.pause();

                disableForeground();
            }
            return;
        }
        Log.i(TAG, "Could not Pause track. Not playing.");
    }

    private void like() {
        if (mMainQueue.hasActivePlayTask()) {
            PlayTask playTask = (PlayTask) mMainQueue.peek();
            final String playId = playTask.getPlay().getId();

            SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                @Override
                public void onStart() {

                }

                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.like(playId);
                }

                @Override
                public void onSuccess(Boolean aBoolean) {
                    eventBus.post(new EventMessage(EventMessage.Status.LIKE));
                }

                @Override
                public void onFail() {

                }
            });
            mSecondaryQueue.offer(task);
            mSecondaryQueue.next();
        } else {
            Log.w(TAG, "Could not Like track. No active Play");
        }
    }

    private void unlike() {

        if (mMainQueue.hasActivePlayTask()) {
            PlayTask playTask = (PlayTask) mMainQueue.peek();
            final String playId = playTask.getPlay().getId();

            SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                @Override
                public void onStart() {

                }

                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.unlike(playId);
                }

                @Override
                public void onSuccess(Boolean aBoolean) {
                    eventBus.post(new EventMessage(EventMessage.Status.UNLIKE));
                }

                @Override
                public void onFail() {

                }
            });
            mSecondaryQueue.offer(task);
            mSecondaryQueue.next();
        } else {
            Log.w(TAG, "Could not Unlike track. No active Play");
        }
    }

    private void dislike() {
        if (mMainQueue.hasActivePlayTask()) {
            PlayTask playTask = (PlayTask) mMainQueue.peek();
            final String playId = playTask.getPlay().getId();

            SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                @Override
                public void onStart() {

                }

                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.dislike(playId);
                }

                @Override
                public void onSuccess(Boolean aBoolean) {
                    eventBus.post(new EventMessage(EventMessage.Status.DISLIKE));
                }

                @Override
                public void onFail() {

                }
            });
            mSecondaryQueue.offer(task);
            mSecondaryQueue.next();

            skip();
        } else {
            Log.w(TAG, "Could not Dislike track. No active Play");
        }
    }

    private AudioFocusManager mAudioFocusManager;

    private void initAudioManager() {
        mAudioFocusManager = new AudioFocusManager(this, new AudioFocusManager.Listener() {
            @Override
            public void play() {
                PlayerService.this.play();
            }

            @Override
            public boolean pause() {
                boolean retval = false;
                if (mMainQueue.hasActivePlayTask()) {
                    PlayTask task = (PlayTask) mMainQueue.peek();
                    task.pause();

                    retval = true;
                } else if (mMainQueue.isTuning() || mTuningQueue.isTuning()) {
                    // If we are tuning, make sure to remove the Play tasks from the queues
                    // The play task can be recreated from the Tuning Tasks.
                    mMainQueue.removeAllPlayTasks();
                    retval = true;
                }

                return retval;
            }

            @Override
            public void releaseResources() {
                mMainQueue.clear();
                mTuningQueue.clear();
            }

            @Override
            public void duckVolume() {
                mMediaPlayerPool.duckVolume();
            }

            @Override
            public void restoreVolume() {
                mMediaPlayerPool.restoreVolume();
            }
        });
    }

    private void releaseAudioManager() {
        mAudioFocusManager.release();
    }
}
