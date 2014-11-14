package fm.feed.android.testapp.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.PlayerError;
import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.playersdk.util.TimeUtils;
import fm.feed.android.testapp.MainActivity;
import fm.feed.android.testapp.R;

/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * Created by mharkins on 8/22/14.
 */
public class TestFragment extends Fragment {

    private static final int CUSTOM_NOTIFICATION_ID = 12341212;

    private static final String AUTH_TOKEN = "c1c2e6dadf84a8bdee82ac618f23af88020c4667";
    private static final String AUTH_SECRET = "887b155ef0a464b5f6e830dd85ee7eb33b061397";

    // Extra for the Save instance state.
    public static final String PLACEMENTS = "save_placements";

    private Player mPlayer;

    private int[] mPlacements;

    // Views
    private Button mBtnTune;
    private ImageButton mBtnPlay;
    private ImageButton mBtnPause;
    private ImageButton mBtnSkip;
    private Button mBtnLike;
    private Button mBtnUnlike;
    private Button mBtnDislike;

    private TextView mTxtTitle;
    private TextView mTxtArtist;
    private TextView mTxtAlbum;


    private TextView mTxtCurrentProgress;
    private TextView mTxtDuration;

    private ProgressBar mProgressBar;

    private ListView mStationsView;
    private ListView mPlacementsView;

    public Button mBtnToggleWifi;

    private int mSelectedStationIndex = -1;
    private int mSelectedPlacementsIndex = -1;

    public TestFragment() {
    }

    public static TestFragment newFragment(int[] placements) {
        TestFragment fragment = new TestFragment();
        fragment.mPlacements = placements;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Initializes the Player Object.
         * Starts the foreground Service (music will keep running when app is killed, unless mPlayer.pause() is called prior to that).
         */
        mPlayer = Player.getInstance(getActivity(), mPlayerListener, AUTH_TOKEN, AUTH_SECRET);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test, container, false);

        mBtnTune = (Button) rootView.findViewById(R.id.tune);
        mBtnPlay = (ImageButton) rootView.findViewById(R.id.play);
        mBtnPause = (ImageButton) rootView.findViewById(R.id.pause);
        mBtnSkip = (ImageButton) rootView.findViewById(R.id.skip);
        mBtnLike = (Button) rootView.findViewById(R.id.like);
        mBtnUnlike = (Button) rootView.findViewById(R.id.unlike);
        mBtnDislike = (Button) rootView.findViewById(R.id.dislike);

        mTxtTitle = (TextView) rootView.findViewById(R.id.title);
        mTxtArtist = (TextView) rootView.findViewById(R.id.artist);
        mTxtAlbum = (TextView) rootView.findViewById(R.id.album);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        mTxtCurrentProgress = (TextView) rootView.findViewById(R.id.current_progress);
        mTxtDuration = (TextView) rootView.findViewById(R.id.duration);

        mStationsView = (ListView) rootView.findViewById(R.id.stations);
        mPlacementsView = (ListView) rootView.findViewById(R.id.placements);

        if (savedInstanceState != null) {
            mPlacements = savedInstanceState.getIntArray(PLACEMENTS);
        }

        mBtnToggleWifi = (Button) rootView.findViewById(R.id.wifi);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBtnTune.setOnClickListener(tune);
        mBtnPlay.setOnClickListener(play);
        mBtnPause.setOnClickListener(pause);
        mBtnSkip.setOnClickListener(skip);
        mBtnLike.setOnClickListener(like);
        mBtnUnlike.setOnClickListener(unlike);
        mBtnDislike.setOnClickListener(dislike);

        mPlacementsView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mStationsView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        mBtnToggleWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm =
                        (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                wifi.setWifiEnabled(!isConnected); // true or false to activate/deactivate wifi

                mBtnToggleWifi.setText(!isConnected ? "Wifi ON" : "Wifi OFF");
            }
        });

        List<HashMap<String, Integer>> fillMaps = new ArrayList<HashMap<String, Integer>>();
        for (Integer p : mPlacements) {
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            map.put("Placement", p);
            fillMaps.add(map);
        }

        final SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                fillMaps,
                android.R.layout.simple_list_item_1,
                new String[]{"Placement"},
                new int[]{android.R.id.text1});
        mPlacementsView.setAdapter(adapter);
        mPlacementsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedPlacementsIndex = position;

                HashMap<String, Integer> item = (HashMap<String, Integer>) adapter.getItem(position);
                Integer placementId = item.get("Placement");
                Toast.makeText(getActivity(), placementId.toString(), Toast.LENGTH_LONG).show();
                mPlayer.setPlacementId(placementId);

            }
        });

        resetTrackInfo();

        if (mPlayer.hasPlay()) {
            updateTrackInfo(mPlayer.getPlay());
        }
        if (mPlayer.hasStationList()) {
            updateStations(mPlayer.getStationList());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mPlayer.registerNavListener(mNavigationListener);
        mPlayer.registerPlayerListener(mPlayerListener);
        mPlayer.registerSocialListener(mSocialListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        mPlayer.unregisterNavListener(mNavigationListener);
        mPlayer.unregisterPlayerListener(mPlayerListener);
        mPlayer.unregisterSocialListener(mSocialListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putIntArray(PLACEMENTS, mPlacements);
    }

    private Player.PlayerListener mPlayerListener = new Player.PlayerListener() {
        @Override
        public void onPlayerInitialized(PlayInfo playInfo) {
            updateTitle(playInfo);

            // This is going to be called every time the Player registers itself to the service.
            // For example when the app resumes.
            // A Track may already be playing when that happens.
            if (playInfo.getPlay() != null) {
                mNavigationListener.onTrackChanged(playInfo.getPlay());
            }
        }

        @Override
        public Player.NotificationBuilder getNotificationBuilder() {
            Player.NotificationBuilder notificationBuilder = new Player.NotificationBuilder() {
                @Override
                public Notification build(Context serviceContext, Play play) {
                    int stringId = getActivity().getApplicationInfo().labelRes;
                    String applicationName = getString(stringId);

                    Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                    intent.setAction(Intent.ACTION_MAIN);

                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pi = PendingIntent.getActivity(getActivity().getApplicationContext(), 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getActivity());
                    mBuilder.setContentIntent(pi);
                    mBuilder.setContentTitle(applicationName);
                    mBuilder.setContentText("Playing: " + play.getAudioFile().getTrack().getTitle());
                    mBuilder.setOngoing(true);
                    mBuilder.setSmallIcon(android.R.drawable.ic_media_play);

                    return mBuilder.build();
                }

                @Override
                public void destroy(Context serviceContext) {
                    NotificationManager mNotificationManager =
                            (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(CUSTOM_NOTIFICATION_ID);
                }

                @Override
                public int getNotificationId() {
                    return CUSTOM_NOTIFICATION_ID;
                }
            };
            return null; // Don't return a notification builder as we want to be using the one in PlayerView
        }

        @Override
        public void onPlaybackStateChanged(PlayInfo.State state) {
            /**
             * Handle each state change by updating the UI
             */
            switch (state) {
                case WAITING:
                    break;
                case READY:
                    break;
                case TUNING:
                    break;
                case TUNED:
                    break;
                case PAUSED:
                    break;
                case PLAYING:
                    break;
                case STALLED:
                    break;
                case COMPLETE:
                    break;
                case REQUESTING_SKIP:
                    break;
            }
        }

        @Override
        public void onSkipStatusChange(boolean skippable) {

        }

        @Override
        public void onError(PlayerError playerError) {
            // Display error
            Toast.makeText(getActivity(), playerError.toString(), Toast.LENGTH_LONG).show();
        }
    };


    private Player.NavListener mNavigationListener = new Player.NavListener() {
        @Override
        public void onPlacementChanged(Placement placement, List<Station> stationList) {
            mPlacementsView.setSelection(mSelectedPlacementsIndex);

            resetTrackInfo();
            updateStations(stationList);

        }

        @Override
        public void onStationChanged(Station station) {
            resetTrackInfo();
            mStationsView.setSelection(mSelectedStationIndex);
            Toast.makeText(getActivity(), String.format("Station set to: %s (%s)", station.getName(), station.getId()), Toast.LENGTH_LONG).show();

        }

        @Override
        public void onTrackChanged(Play play) {
            resetTrackInfo();

            updateTrackInfo(play);

        }

        @Override
        public void onSkipFailed() {
            Toast.makeText(getActivity(), "Cannot Skip Track", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onEndOfPlaylist() {
            Toast.makeText(getActivity(), "Reached end of Playlist", Toast.LENGTH_LONG).show();
            resetTrackInfo();
        }

        @Override
        public void onBufferUpdate(Play play, int percentage) {
            mProgressBar.setSecondaryProgress((percentage * play.getAudioFile().getDurationInSeconds()) / 100);
        }

        @Override
        public void onProgressUpdate(Play play, int elapsedTime, int totalTime) {
            mProgressBar.setProgress(elapsedTime);
            mTxtCurrentProgress.setText(TimeUtils.toProgressFormat(elapsedTime));
            mTxtDuration.setText(TimeUtils.toProgressFormat(totalTime));
        }
    };

    private Player.SocialListener mSocialListener = new Player.SocialListener() {
        @Override
        public void onLiked() {
            Toast.makeText(getActivity(), "Liked!", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onUnliked() {
            Toast.makeText(getActivity(), "UnLiked!", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDisliked() {
            Toast.makeText(getActivity(), "DisLiked!", Toast.LENGTH_LONG).show();
        }
    };

    private void resetTrackInfo() {
        int max = 0;
        int min = 0;
        mTxtCurrentProgress.setText(TimeUtils.toProgressFormat(min));
        mTxtDuration.setText(TimeUtils.toProgressFormat(max));

        mProgressBar.setProgress(min);
        mProgressBar.setSecondaryProgress(min);
        mProgressBar.setMax(min);

        mTxtTitle.setText("");
        mTxtArtist.setText("");
        mTxtAlbum.setText("");
    }

    @SuppressWarnings("unused")
    private View.OnClickListener tune = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPlayer.tune();
        }
    };

    private View.OnClickListener play = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPlayer.play();
        }
    };

    private View.OnClickListener pause = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPlayer.pause();
        }
    };

    private View.OnClickListener skip = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPlayer.skip();
        }
    };

    private View.OnClickListener like = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPlayer.like();
        }
    };

    private View.OnClickListener unlike = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPlayer.unlike();
        }
    };

    private View.OnClickListener dislike = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPlayer.dislike();
        }
    };

    private void updateTitle(PlayInfo playInfo) {
        final PackageManager pm = getActivity().getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(getActivity().getPackageName(), 0);

        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        getActivity().setTitle(applicationName + " (sdk: " + playInfo.getSdkVersion() + ")");
    }

    private void updateStations(List<Station> stationList) {
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for (Station s : stationList) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("Id", s.getId() + "");
            map.put("Station", s.getName());
            fillMaps.add(map);
        }

        final SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                fillMaps,
                R.layout.list_item,
                new String[]{"Id", "Station"},
                new int[]{R.id.id, R.id.name});
        mStationsView.setAdapter(adapter);
        mStationsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedStationIndex = position;

                HashMap<String, Integer> item = (HashMap<String, Integer>) adapter.getItem(position);
                Integer stationId = Integer.parseInt(String.valueOf(item.get("Id")));
                mPlayer.setStationId(stationId);
            }
        });
    }

    private void updateTrackInfo(Play play) {
        mProgressBar.setMax(play.getAudioFile().getDurationInSeconds());
        mTxtDuration.setText(TimeUtils.toProgressFormat(play.getAudioFile().getDurationInSeconds()));

        mTxtTitle.setText(play.getAudioFile().getTrack().getTitle());
        mTxtArtist.setText(play.getAudioFile().getArtist().getName());
        mTxtAlbum.setText(play.getAudioFile().getRelease().getTitle());
    }

}
