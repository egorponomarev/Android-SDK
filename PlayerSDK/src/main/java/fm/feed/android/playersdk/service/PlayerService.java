package fm.feed.android.playersdk.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.feed.android.playersdk.DefaultNotificationBuilder;
import fm.feed.android.playersdk.NotificationBuilder;
import fm.feed.android.playersdk.R;
import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Session;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.bus.BufferUpdate;
import fm.feed.android.playersdk.service.bus.BusProvider;
import fm.feed.android.playersdk.service.bus.ChangeStation;
import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.bus.EventMessage;
import fm.feed.android.playersdk.service.bus.LogEvent;
import fm.feed.android.playersdk.service.bus.ChangeNotificationBuilder;
import fm.feed.android.playersdk.service.bus.PlayerAction;
import fm.feed.android.playersdk.service.bus.ProgressUpdate;
import fm.feed.android.playersdk.service.constant.ApiErrorEnum;
import fm.feed.android.playersdk.service.constant.Configuration;
import fm.feed.android.playersdk.service.queue.MainQueue;
import fm.feed.android.playersdk.service.queue.TaskQueueManager;
import fm.feed.android.playersdk.service.queue.TuningQueue;
import fm.feed.android.playersdk.service.task.ClientIdTask;
import fm.feed.android.playersdk.service.task.CreateSessionTask;
import fm.feed.android.playersdk.service.task.PlayTask;
import fm.feed.android.playersdk.service.task.PlayerAbstractTask;
import fm.feed.android.playersdk.service.task.SimpleNetworkTask;
import fm.feed.android.playersdk.service.task.SkippableTask;
import fm.feed.android.playersdk.service.task.StationIdTask;
import fm.feed.android.playersdk.service.task.TaskFactory;
import fm.feed.android.playersdk.service.task.TuneTask;
import fm.feed.android.playersdk.service.util.AudioFocusManager;
import fm.feed.android.playersdk.service.util.DataPersister;
import fm.feed.android.playersdk.service.util.MediaPlayerPool;
import fm.feed.android.playersdk.service.webservice.Webservice;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;
import fm.feed.android.playersdk.service.webservice.util.ElapsedTimeManager;

/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * Created by mharkins on 8/21/14.
 */
@SuppressLint("Registered")
public class PlayerService extends Service {
    public static final String TAG = PlayerService.class.getSimpleName();

    protected static Bus eventBus = BusProvider.getInstance();

    protected Webservice mWebservice;

    protected AudioFocusManager mAudioFocusManager;

    protected MainQueue mMainQueue;
    protected TuningQueue mTuningQueue;
    protected TaskQueueManager mSecondaryQueue;

    protected TaskFactory mTaskFactory;
    protected MediaPlayerPool mMediaPlayerPool;

    protected ElapsedTimeManager mElapsedTimeManager;

    protected PlayInfo mPlayInfo;
    protected DataPersister mDataPersister;

    protected NotificationBuilder mNotificationBuilder;

    protected boolean mInitialized;

    protected int mForceSkipCount = 0;

    public static BuildType mDebug;

    public static enum BuildType {
        DEBUG,
        RELEASE
    }

    public static enum ExtraKeys {
        timestamp,
        buildType,
        token,
        secret;

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
                // Pause the Queues
                mMainQueue.pause();
                mTuningQueue.pause();
                mSecondaryQueue.pause();
            } else {
                //Resume the Queues
                mMainQueue.unpause();
                if (!mMainQueue.hasActivePlayTask()) {
                    mMainQueue.next();
                }
                mTuningQueue.unpause();
                mTuningQueue.next();
                mSecondaryQueue.unpause();
                mSecondaryQueue.next();
            }
        }
    };


    //
    //
    // Service lifecycle events
    //
    //

    @Override
    public void onCreate() {
        Log.e(TAG, "PlayerService created");
        super.onCreate();

        mNotificationBuilder = new DefaultNotificationBuilder();

        mInitialized = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mInitialized || (intent == null)) {
            Log.i(TAG, "PlayerService onStartCommand: already started or null intent sent");

            // Our code uses a singleton to create the service, so technically
            // this should never be started twice. Any secondary starts are initiated
            // by the OS and I think can be ignored.

            return START_NOT_STICKY;
        }

        Log.i(TAG, "PlayerService onStartCommand: starting up");

        try {
            mDebug = BuildType.valueOf(intent.getStringExtra(ExtraKeys.buildType.toString()));
        } catch (IllegalArgumentException e) {
            mDebug = BuildType.RELEASE;
        }

        String token = intent.getStringExtra(ExtraKeys.token.toString());
        String secret = intent.getStringExtra(ExtraKeys.secret.toString());
        Credentials credz = new Credentials(token, secret);

        onFirstStartCommand(credz);

        mInitialized = true;

        createSession();

        // if the app gets killed, the user has to restart things
        return START_NOT_STICKY;
    }

    protected void onFirstStartCommand(Credentials credz) {
        mPlayInfo = new PlayInfo(getString(R.string.sdk_version));

        mDataPersister = new DataPersister(getContext());
        mWebservice = new Webservice(getContext());

        mMediaPlayerPool = new MediaPlayerPool();

        mMainQueue = new MainQueue();
        mTuningQueue = new TuningQueue();
        mSecondaryQueue = new TaskQueueManager("Secondary Queue");

        mTaskFactory = new TaskFactory();
        mMediaPlayerPool = new MediaPlayerPool();

        mElapsedTimeManager = ElapsedTimeManager.getInstance(mWebservice, mSecondaryQueue);

        initAudioManager();

        registerReceiver(mConnectivityBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        eventBus.register(this);

        mWebservice.setCredentials(credz);

        logEvent("serviceLaunched");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        if (mInitialized) {
            logEvent("serviceDestroyed");

            releaseAudioManager();

            mMainQueue.clear();
            mTuningQueue.clear();
            mSecondaryQueue.clear();

            mMediaPlayerPool.release();

            // TODO: is it ever the case that this service will quit but the app doesn't?
            eventBus.unregister(this);

            unregisterReceiver(mConnectivityBroadcastReceiver);
        }

        mInitialized = false;

        Log.e(TAG, "PlayerService destroyed!");

        super.onDestroy();
    }

    protected Context getContext() {
        return this;
    }

    protected void updateNotification(Play play) {
        // If there is no notification builder, then don't enable foreground
        if (mNotificationBuilder == null) {
            return;
        }

        Notification notification = mNotificationBuilder.build(this, play);
        if (notification != null) {
            startForeground(mNotificationBuilder.getNotificationId(), notification);
        }
    }

    protected void disableForeground() {
        if (mNotificationBuilder == null) {
            return;
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mNotificationBuilder.getNotificationId());

        stopForeground(true);
    }

    private void updateState(PlayInfo.State state) {
        mPlayInfo.setState(state);
        eventBus.post(new EventMessage(EventMessage.Status.STATUS_UPDATED));
    }

    private void updateSkippable(boolean canSkip) {
        mPlayInfo.setSkippable(canSkip);
        eventBus.post(new EventMessage(EventMessage.Status.SKIP_STATUS_UPDATED));
    }

    /**
     * Is the Service currently in the process of playing music or at least preparing to play music.
     *
     * @return {@code true} if playing, {@code false} otherwise.
     */
    private boolean isPlayingPlaylist() {
        if (mMainQueue.isPaused()) {
            return false;
        }

        if (mMainQueue.hasActivePlayTask()) {
            return ((PlayTask) mMainQueue.peek()).isPlaying();
        }

        // Check for a Tune task followed by a Play Task.
        PlayerAbstractTask task = mMainQueue.peek();
        return (task != null && task instanceof TuneTask && mMainQueue.hasPlayTask());
    }

    /**
     * *************************************
     * Bus receivers
     */

    @Subscribe
    public void logEvent(LogEvent event) {
        logEvent(event.getEvent(), event.getParams());
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setNotificationBuilder(ChangeNotificationBuilder notificationBuilderWrapper) {
        this.mNotificationBuilder = notificationBuilderWrapper.getObject();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void setStationId(ChangeStation wrapper) {
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

        final boolean wasPlaying = isPlayingPlaylist();

        StationIdTask task = new StationIdTask(mMainQueue, new StationIdTask.OnStationIdChanged() {
            @Override
            public void onSuccess(Station station) {
                if (station != null) {
                    mPlayInfo.setStation(station);

                    eventBus.post(station);

                    if (wasPlaying) {
                        play();
                    }
                } else {
                    Log.w(TAG, String.format("Station %s could not be found or was already selected in current placement", stationId));
                }
            }

            @Override
            public void onFail(FeedFMError error) {
                // TODO: log error
                handleError(error);
            }
        }, mPlayInfo.getStationList(), currentStationId, stationId);

        // StationIdTask cancels everything but:
        // - ClientIdTask
        mMainQueue.clearLowerPriorities(task);

        mTuningQueue.clear();
        mMediaPlayerPool.releaseTunedPlayers();

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

    public void createSession() {
        String clientId = mDataPersister.getString(DataPersister.Key.clientId, null);

        if (clientId != null) {
            mWebservice.setClientId(clientId);
        }

        CreateSessionTask task = new CreateSessionTask(mMainQueue, mWebservice, new CreateSessionTask.OnSessionCreated() {
            @Override
            public void onSuccess(Session session) {
                Log.i(TAG, "Created new session");

                mDataPersister.putString(DataPersister.Key.clientId, session.getClientId());
                mWebservice.setClientId(session.getClientId());
                mPlayInfo.setClientId(session.getClientId());

                Log.i(TAG, "Retrieved new session from server");

                if (!session.isAvailable()) {
                    mPlayInfo.setState(PlayInfo.State.UNAVAILABLE);

                    eventBus.post(mPlayInfo);

                    return;
                }

                Placement placement = session.getPlacement();

                mPlayInfo.setStationList(placement.getStationList());

                mPlayInfo.setPlacement(placement);

                List<Station> stationList = placement.getStationList();
                if (!stationList.isEmpty()) {
                    mPlayInfo.setStation(stationList.get(0));
                } else {
                    mPlayInfo.setStation(null);
                }

                // tell everybody we're up and running
                eventBus.post(mPlayInfo);

                // starting state
                updateState(PlayInfo.State.READY);

                // share the placement and station info
                eventBus.post(mPlayInfo.getPlacement());
                eventBus.post(mPlayInfo.getStation());
            }

            @Override
            public void onFail(FeedFMError error) {
                Log.i(TAG, "Unable to perform CreateSession");

                // let everybody know that we're boned
                mPlayInfo.setState(PlayInfo.State.UNAVAILABLE);

                eventBus.post(mPlayInfo);

                // pass the error down
                handleError(error);
            }
        });

        // Getting a new Session will cancel whatever task is currently in progress.
        mMainQueue.clearLowerPriorities(task);

        // Cancel any Tunings that might be taking place
        mTuningQueue.clear();
        mMediaPlayerPool.releaseTunedPlayers();

        mMainQueue.offerUnique(task);
        mMainQueue.next();
    }

    /*
     * Bus receivers
     ****************************************/
    public void getClientId() {
        String clientId = mDataPersister.getString(DataPersister.Key.clientId, null);

        if (clientId != null) { // TODO: remove that!
            mWebservice.setClientId(clientId);
            mPlayInfo.setClientId(clientId);
            Log.i(TAG, "Retrieved existing Client ID from storage");

        } else {
            ClientIdTask task = new ClientIdTask(mMainQueue, mWebservice, new ClientIdTask.OnClientIdChanged() {
                @Override
                public void onSuccess(String clientId) {
                    mDataPersister.putString(DataPersister.Key.clientId, clientId);
                    // TODO: perhaps encrypt clientId first.
                    mWebservice.setClientId(clientId);
                    mPlayInfo.setClientId(clientId);
                    Log.i(TAG, "Retrieved new Client ID from server");
                }

                @Override
                public void onFail(FeedFMError error) {
                    handleError(error);
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
            final TuneTask task = new TuneTask(getContext(), queueManager, mWebservice, mMediaPlayerPool, new TuneTask.TuneTaskListener() {

                @Override
                public void onTuneTaskBegin(TuneTask tuneTask) {
                    // Only publish the Play info if the Tuning is being done on the main queue (this means that this TuneTask isn't in the background).
                    if (!mMainQueue.hasActivePlayTask()) {
                        updateState(PlayInfo.State.TUNING);
                    }
                }

                @Override
                public void onMetaDataLoaded(TuneTask tuneTask, Play play) {
                    // Only publish the Play info if the Tuning is being done on the main queue (this means that this TuneTask isn't in the background).
                    if (!mMainQueue.hasActivePlayTask()) {
                        mPlayInfo.setCurrentPlay(play);
                        mPlayInfo.setSkippable(false);

                        updateNotification(play);

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
                }

                @Override
                public void onSuccess(TuneTask tuneTask, FeedFMMediaPlayer mediaPlayer, Play play) {
                    // Only publish the Play info if the Tuning is being done on the main queue (this means that this TuneTask isn't in the background).
                    if (!mMainQueue.hasActivePlayTask()) {
                        updateState(PlayInfo.State.TUNED);
                    }
                }

                /*
                 * If some unknown error happens, try to skip this play and continue.
                 */

                @Override
                public void onUnkownError(final TuneTask tuneTask, FeedFMError feedFMError) {
                    if (!mMainQueue.hasActivePlayTask()) {
                        mPlayInfo.setCurrentPlay(null);

                        disableForeground();

                        // Remove the following PlayTask if it exists
                        mMainQueue.removeAllPlayTasks();
                    }

                    // If we have an unknown error for this stream, forcefully skip this song.
                    // First we need to mark this song as started.

                    SimpleNetworkTask playStartTask = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                        @Override
                        public String getTag() {
                            return "PlayStartTask";
                        }

                        @Override
                        public void onStart() {
                            if (!mMainQueue.hasActivePlayTask()) {
                                updateState(PlayInfo.State.REQUESTING_SKIP);
                            }
                        }

                        @Override
                        public Boolean performRequestSynchronous() throws FeedFMError {
                            return mWebservice.playStarted(tuneTask.getPlay().getId());
                        }

                        @Override
                        public void onSuccess(Boolean canSkip) {
                            // Only force skip if we are in the foreground.
                            skip(tuneTask, true);//!mMainQueue.hasActivePlayTask());
                        }

                        @Override
                        public void onFail(FeedFMError error) {
                            Log.d(TAG, "Failed to start task:" + tuneTask.getPlay() + ". Error: " + error.toString());
                        }
                    });

                    mSecondaryQueue.offer(playStartTask);
                    mSecondaryQueue.next();
                }

                @Override
                public void onApiError(TuneTask tuneTask, FeedFMError mApiError) {
                    if (!mMainQueue.hasActivePlayTask()) {
                        mPlayInfo.setCurrentPlay(null);

                        disableForeground();
                    }

                    handleError(mApiError);
                }
            }, mPlayInfo, mPlayInfo.getClientId());

            // TuneTask is low priority and cancels nothing. It will only be queued.
            queueManager.offerIfNotExist(task);
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
        // Don't play if we don't have rights for it.
        if (!mAudioFocusManager.isAudioFocusGranted()) {
            return;
        }

        if (mMainQueue.hasActivePlayTask()) {
            PlayTask playTask = (PlayTask) mMainQueue.peek();

            // If the Play is Paused, Resume.
            if (playTask.isPaused()) {
                playTask.play(true);

                updateNotification(playTask.getPlay());
                return;
            }
        }

        if (!mMediaPlayerPool.hasTunedMediaPlayer()) {
            tune();
        }

        PlayTask task = mTaskFactory.newPlayTask(mMainQueue, mWebservice, getContext(), mMediaPlayerPool, mElapsedTimeManager, new PlayTask.PlayTaskListener() {
            // if we get fully buffered and report the start to the remote server, we can send an early 'tune()'
            // while we're still playing the current audio file.
            private boolean fullyBuffered = false;
            private boolean startReported = false;
            private boolean earlyTuneSent = false;

            @Override
            public void onPlayBegin(PlayTask playTask, Play play) {

                mForceSkipCount = 0;
                mPlayInfo.setCurrentPlay(play);

                updateNotification(play);

                eventBus.post(play);

                final String playId = play.getId();

                SimpleNetworkTask playStartTask = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {

                    @Override
                    public String getTag() {
                        return "PlayStartTask";
                    }

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public Boolean performRequestSynchronous() throws FeedFMError {
                        return mWebservice.playStarted(playId);
                    }

                    private void updateSkipStatus(boolean canSkip) {
                        if (mMainQueue.hasActivePlayTask()) {
                            PlayTask playTask = (PlayTask) mMainQueue.peek();

                            if (playTask.getPlay() != null && playTask.getPlay().getId().equals(playId)) {
                                playTask.setSkippable(canSkip);
                            }
                            updateSkippable(canSkip);
                        }
                    }

                    @Override
                    public void onSuccess(Boolean canSkip) {
                        boolean skippable = canSkip != null && canSkip;
                        updateSkipStatus(skippable);

                        startReported = true;
                        if (fullyBuffered && !earlyTuneSent) {
                            earlyTuneSent = true;
                            tune();
                        }

                    }

                    @Override
                    public void onFail(FeedFMError error) {
                        mPlayInfo.setCurrentPlay(null);

                        if (ApiErrorEnum.fromError(error) == ApiErrorEnum.PLAYBACK_ALREADY_STARTED) {
                            // Server already got the Start message even if we didn't get a response.
                            // Set to skippable and let server decide if we can skip the song later on.
                            updateSkipStatus(true);
                        } else {
                            handleError(error);
                        }

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
                    fullyBuffered = true;
                    if (startReported && !earlyTuneSent) {
                        earlyTuneSent = true;
                        tune();
                    }
                }
            }

            @Override
            public void onPlayFinished(final Play play, boolean isSkipped) {
                if (play == mPlayInfo.getPlay()) {
                    mPlayInfo.setCurrentPlay(null);
                }
                if (!isSkipped) {
                    updateSkippable(false);
                    updateState(PlayInfo.State.COMPLETE);

                    SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                        @Override
                        public String getTag() {
                            return "CompleteTask";
                        }

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
                        public void onFail(FeedFMError error) {
                            // Don't report the error
                            // TODO: log error
                            handleError(error);
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

    /*
     * Ask the server if we can skip whatever we're currently doing.
     */

    private void skip() {
        PlayerAbstractTask task = mMainQueue.peek();

        if (task != null && task instanceof SkippableTask && ((SkippableTask) task).isSkippableCandidate()) {
            skip((SkippableTask) task, false);
        } else {
            eventBus.post(new EventMessage(EventMessage.Status.SKIP_FAILED));
        }
    }

    /*
     * Ask the feed server if we can skip this SkippableTask. If so, call 'play' again
     * to move on to the next song. If not, let everybody know that the skip failed.
     */

    private void skip(final SkippableTask task, final boolean force) {
        if (force) {
            if (mForceSkipCount >= Configuration.MAX_FORCE_SKIP_COUNT) {

                mMainQueue.clearLowerPriorities(task);
                mTuningQueue.clear();

                eventBus.post(new EventMessage(EventMessage.Status.END_OF_PLAYLIST));

                mForceSkipCount = 0;
                return;
            }

            mForceSkipCount++;
        }

        final Play play = task.getPlay();
        if (play != null) {
            final SimpleNetworkTask<Boolean> skipTask = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                @Override
                public String getTag() {
                    return "SkipTask";
                }

                @Override
                public void onStart() {
                    updateState(PlayInfo.State.REQUESTING_SKIP);
                }

                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.skip(play.getId(), task.getElapsedTimeMillis() / 1000, force);
                }

                @Override
                public void onSuccess(Boolean canSkip) {
                    if (!force) {
                        mPlayInfo.setSkippable(false);
                    }

                    if (canSkip) {
                        task.getQueueManager().remove(task);

                        if (!task.isCancelled() && task.getState() != PlayerAbstractTask.State.FINISHED) {
                            task.cancel(true);
                        }

                        // The skipped task might not be the one currently playing.
                        if (!mMainQueue.hasActivePlayTask()) {
                            play();
                        }
                    } else {
                        onFail(null);
                    }
                    updateSkippable(canSkip);
                }

                @Override
                public void onFail(FeedFMError error) {
                    eventBus.post(new EventMessage(EventMessage.Status.SKIP_FAILED));
                    handleError(error);
                }
            });

            mSecondaryQueue.clearLowerPriorities(skipTask);
            mSecondaryQueue.offerIfNotExist(skipTask);
            mSecondaryQueue.next();
        } else {
            Log.i(TAG, "Could not Skip track. No active Play");
        }
    }

    private void pause() {
        // If currently playing, pause
        if (mMainQueue.hasActivePlayTask()) {
            PlayTask playTask = (PlayTask) mMainQueue.peek();

            // If the Play is Paused, Resume.
            if (playTask.isPlaying()) {
                playTask.pause(true);

                disableForeground();
            }
            return;
        } else if (mMainQueue.hasPlayTask()) {
            // If a PlayTask is queued, remove it from the queue.
            // When Play is hit, the PlayTask will be recreated.
            mMainQueue.removeAllPlayTasks();
            return;
        }
        Log.i(TAG, "Could not Pause track. Not playing.");
    }

    private void like() {
        if (mMainQueue.hasActivePlayTask()) {
            PlayTask playTask = (PlayTask) mMainQueue.peek();
            final Play play = playTask.getPlay();

            SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                @Override
                public String getTag() {
                    return "LikeTask";
                }

                @Override
                public void onStart() {

                }

                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.like(play.getId());
                }

                @Override
                public void onSuccess(Boolean aBoolean) {
                    play.setLikeState(Play.LikeState.LIKED);
                    eventBus.post(new EventMessage(EventMessage.Status.LIKE));
                }

                @Override
                public void onFail(FeedFMError error) {
                    handleError(error);
                }
            });
            mSecondaryQueue.offer(task);
            mSecondaryQueue.next();
        } else {
            Log.w(TAG, "Could not Like track. No active Play");
        }
    }

    private void logEvent(final String event, String ... parameters) {
        if (parameters.length > 0) {
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < parameters.length - 1; i += 2) {
                map.put(parameters[i], parameters[i+1]);
            }
            logEvent(event, map);

        } else {
            logEvent(event, Collections.EMPTY_MAP);
        }
    }

    private void logEvent(final String event, final Map<String, String> parameters) {
        SimpleNetworkTask logEventTask = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
            @Override
            public String getTag() { return "LogEventTask"; }

            @Override
            public void onStart() {

            }

            @Override
            public Boolean performRequestSynchronous() throws FeedFMError {
                return mWebservice.logEvent(event, parameters);
            }

            @Override
            public void onSuccess(Boolean aBoolean) {
                // nobody cares
            }

            @Override
            public void onFail(FeedFMError error) {
                // nobody cares
            }
        });
        mSecondaryQueue.offer(logEventTask);
        mSecondaryQueue.next();
    }

    private void unlike() {

        if (mMainQueue.hasActivePlayTask()) {
            PlayTask playTask = (PlayTask) mMainQueue.peek();
            final Play play = playTask.getPlay();

            SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                @Override
                public String getTag() {
                    return "UnlikeTask";
                }

                @Override
                public void onStart() {

                }

                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.unlike(play.getId());
                }

                @Override
                public void onSuccess(Boolean aBoolean) {
                    play.setLikeState(Play.LikeState.NONE);
                    eventBus.post(new EventMessage(EventMessage.Status.UNLIKE));
                }

                @Override
                public void onFail(FeedFMError error) {
                    handleError(error);
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
            final Play play = playTask.getPlay();

            SimpleNetworkTask task = new SimpleNetworkTask<Boolean>(mSecondaryQueue, mWebservice, new SimpleNetworkTask.SimpleNetworkTaskListener<Boolean>() {
                @Override
                public String getTag() {
                    return "DislikeTask";
                }

                @Override
                public void onStart() {

                }

                @Override
                public Boolean performRequestSynchronous() throws FeedFMError {
                    return mWebservice.dislike(play.getId());
                }

                @Override
                public void onSuccess(Boolean aBoolean) {
                    play.setLikeState(Play.LikeState.DISLIKED);
                    eventBus.post(new EventMessage(EventMessage.Status.DISLIKE));

                    // When a song is disliked it should be skipped
                    skip();
                }

                @Override
                public void onFail(FeedFMError error) {
                    // TODO: log error
                    handleError(error);
                }
            });
            mSecondaryQueue.offer(task);
            mSecondaryQueue.next();

            skip();
        } else {
            Log.w(TAG, "Could not Dislike track. No active Play");
        }
    }

    private void handleError(FeedFMError error) {
        if (error != null) {
            Log.e(TAG, "Player Error >>> " + error.toString());
            if (error.isApiError()) {
                switch (error.getApiError()) {
                    case END_OF_PLAYLIST:
                        eventBus.post(new EventMessage(EventMessage.Status.END_OF_PLAYLIST));
                        disableForeground();
                        return;
                    case NOT_IN_US:
                        eventBus.post(error);
                        disableForeground();
                        return;
                    case PLAYBACK_ALREADY_STARTED:
                        Log.w(TAG, error);
                        return;
                    case INVALID_CREDENTIALS:
                    case FORBIDDEN:
                    case SKIP_LIMIT_REACHED:
                    case CANT_SKIP_NO_PLAY:
                    case INVALID_PARAMETER:
                    case MISSING_PARAMETER:
                    case NO_SUCH_OBJECT:
                    case UNHANDLED_INTERNAL_ERROR:
                        break;
                }
            } else if (error.isPlayerError()) {
                switch (error.getPlayerError()) {
                    case NO_NETWORK:
                        break;
                    case TUNE_UNKNOWN:
                        break;
                    case INVALID_CREDENTIALS:
                        break;
                    case UNKNOWN:
                        break;
                    case RETROFIT_UNKNOWN:
                        break;
                }
            }
            eventBus.post(error);

        }
    }

    protected void initAudioManager() {
        mAudioFocusManager = new AudioFocusManager(getContext(), new AudioFocusManager.Listener() {
            @Override
            public void play() {
                PlayerService.this.play();
            }

            @Override
            public boolean pause() {
                boolean retval = false;
                if (mMainQueue.hasActivePlayTask()) {
                    PlayTask task = (PlayTask) mMainQueue.peek();
                    task.pause(false);

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

    protected void releaseAudioManager() {
        mAudioFocusManager.release();
    }
}
