package com.feedfm.android.feedfmplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.feedfm.android.feedfmplayer.R;
import com.feedfm.android.playersdk.Player;
import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mharkins on 8/22/14.
 */
public class PlayFragment extends Fragment implements Player.PlayerListener, Player.NavListener, Player.SocialListener {

    private Player mPlayer;

    private List<Integer> mPlacements;

    // Views
    private Button mBtnTune;
    private Button mBtnPlay;
    private Button mBtnPause;
    private Button mBtnSkip;
    private Button mBtnLike;
    private Button mBtnUnlike;
    private Button mBtnDislike;

    private ListView mStationsView;
    private ListView mPlacementsView;

    private int mSelectedStationIndex = -1;
    private int mSelectedPlacementsIndex = -1;

    public PlayFragment() {
    }

    public static PlayFragment newFragment(Integer[] placements) {
        PlayFragment fragment = new PlayFragment();
        fragment.mPlacements = Arrays.asList(placements);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mBtnTune = (Button) rootView.findViewById(R.id.tune);
        mBtnPlay = (Button) rootView.findViewById(R.id.play);
        mBtnPause = (Button) rootView.findViewById(R.id.pause);
        mBtnSkip = (Button) rootView.findViewById(R.id.skip);
        mBtnLike = (Button) rootView.findViewById(R.id.like);
        mBtnUnlike = (Button) rootView.findViewById(R.id.unlike);
        mBtnDislike = (Button) rootView.findViewById(R.id.dislike);

        mStationsView = (ListView) rootView.findViewById(R.id.stations);
        mPlacementsView = (ListView) rootView.findViewById(R.id.placements);

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
        for(Integer p: mPlacements) {
            HashMap<String, Integer> map= new HashMap<String, Integer>();
            map.put("Placement", p);
            fillMaps.add(map);
        }

        final SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                fillMaps,
                android.R.layout.simple_list_item_1,
                new String [] { "Placement"},
                new int[] {android.R.id.text1});
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

        mPlayer = Player.getInstance(getActivity(), this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @SuppressWarnings("unused")
    private View.OnClickListener tune = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Tune", Toast.LENGTH_LONG).show();
            mPlayer.tune();
        }
    };

    private View.OnClickListener play = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Play", Toast.LENGTH_LONG).show();
            mPlayer.play();
        }
    };

    private View.OnClickListener pause = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Pause", Toast.LENGTH_LONG).show();
            mPlayer.pause();
        }
    };

    private View.OnClickListener skip = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Skip", Toast.LENGTH_LONG).show();
            mPlayer.skip();
        }
    };

    private View.OnClickListener like = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Like", Toast.LENGTH_LONG).show();
            mPlayer.like();
        }
    };

    private View.OnClickListener unlike = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Unlike", Toast.LENGTH_LONG).show();
            mPlayer.unlike();
        }
    };

    private View.OnClickListener dislike = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Dislike", Toast.LENGTH_LONG).show();
            mPlayer.dislike();
        }
    };

    @Override
    public void onPlayerInitialized() {
        mPlayer.setCredentials("d40b7cc98a001fc9be8dd3fd32c3a0c495d0db42", "b59c6d9c1b5a91d125f098ef9c2a7165dc1bd517");
    }

    @Override
    public void onPlacementChanged(Placement placement, List<Station> stationList) {
        mPlacementsView.setSelection(mSelectedPlacementsIndex);

        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(Station s: stationList) {
            HashMap<String, String> map= new HashMap<String, String>();
            map.put("Id", s.getId() + "");
            map.put("Station", s.getName());
            fillMaps.add(map);
        }

        final SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                fillMaps,
                R.layout.list_item,
                new String [] { "Id", "Station"},
                new int[] {R.id.id, R.id.name});
        mStationsView.setAdapter(adapter);
        mStationsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedStationIndex = position;

                HashMap<String, Integer> item = (HashMap<String, Integer>) adapter.getItem(position);
                String stationId = String.valueOf(item.get("Id"));
                mPlayer.setStationId(stationId);
            }
        });
    }

    @Override
    public void onStationChanged(Station station) {
        mStationsView.setSelection(mSelectedStationIndex);
        Toast.makeText(getActivity(), String.format("Station set to: %s (%d)", station.getName(), station.getId()), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTrackChanged(Placement placement, List<Station> stationList) {

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
    public void onLiked() {

    }

    @Override
    public void onUnliked() {

    }

    @Override
    public void onDisliked() {

    }
}
