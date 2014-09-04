package fm.feed.android.testapp.fragment;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import fm.feed.android.testapp.MainActivity;
import fm.feed.android.testapp.R;
import fm.feed.android.testapp.util.TimeUtils;
import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.PlayerLibraryInfo;
import fm.feed.android.playersdk.model.Station;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mharkins on 8/22/14.
 */
public class PlayFragment extends Fragment implements Player.PlayerListener, Player.NavListener, Player.SocialListener {

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

    private int mSelectedStationIndex = -1;
    private int mSelectedPlacementsIndex = -1;

    public PlayFragment() {
    }

    public static PlayFragment newFragment(int[] placements) {
        PlayFragment fragment = new PlayFragment();
        fragment.mPlacements = placements;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

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

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
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
    }

    @Override
    public void onStart() {
        super.onStart();

        mPlayer = Player.getInstance(getActivity(), this, this, this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static final String PLACEMENTS = "save_placements";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putIntArray(PLACEMENTS, mPlacements);
    }

    private int mNotificationId = 1234532;

    public void createNotification(Play play) {
        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(getActivity().getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getActivity());
        mBuilder.setContentIntent(pi);
        mBuilder.setContentTitle("Feed.FM");
        mBuilder.setContentText("Playing: " + play.getAudioFile().getTrack().getTitle());
        mBuilder.setOngoing(true);
        mBuilder.setSmallIcon(android.R.drawable.ic_media_play);

        NotificationManager mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        // NOTIFICATION_ID allows you to update the notification later on.
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }

    public void clearNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mNotificationId);
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

    @Override
    public void onPlayerInitialized(PlayerLibraryInfo playerLibraryInfo) {
        mPlayer.setCredentials("d40b7cc98a001fc9be8dd3fd32c3a0c495d0db42", "b59c6d9c1b5a91d125f098ef9c2a7165dc1bd517");

        final PackageManager pm = getActivity().getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(getActivity().getPackageName(), 0);

        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        getActivity().setTitle(applicationName + " (sdk: " + playerLibraryInfo.versionName + ")");
    }

    @Override
    public void onPlacementChanged(Placement placement, List<Station> stationList) {
        mPlacementsView.setSelection(mSelectedPlacementsIndex);

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

    @Override
    public void onStationChanged(Station station) {
        mStationsView.setSelection(mSelectedStationIndex);
        Toast.makeText(getActivity(), String.format("Station set to: %s (%s)", station.getName(), station.getId()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTrackChanged(Play play) {
        mProgressBar.setMax(play.getAudioFile().getDurationInSeconds());
        mTxtDuration.setText(TimeUtils.toProgressFormat(play.getAudioFile().getDurationInSeconds()));

        mProgressBar.setSecondaryProgress(0);
        mTxtTitle.setText(play.getAudioFile().getTrack().getTitle());
        mTxtArtist.setText(play.getAudioFile().getArtist().getName());
        mTxtAlbum.setText(play.getAudioFile().getRelease().getTitle());

        createNotification(play);
    }

    @Override
    public void onPlaybackStateChanged(Placement placement, List<Station> stationList) {

    }

    @Override
    public void onSkipFailed() {

    }

    @Override
    public void onNotInUS() {

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

    @Override
    public void onLiked() {

    }

    @Override
    public void onUnliked() {

    }

    @Override
    public void onDisliked() {

    }
}
