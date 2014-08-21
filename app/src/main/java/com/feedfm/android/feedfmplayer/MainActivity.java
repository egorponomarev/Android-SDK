package com.feedfm.android.feedfmplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;

import com.feedfm.android.playersdk.Player;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private BroadcastReceiver mPlayerBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Player.EVENT_CLIENT_ID_RECEIVED.equals(intent.getAction())) {
                    ((TextView) getView().findViewById(R.id.title)).setText(intent.getStringExtra(Player.EXTRA_CLIENT_ID));
                }
            }
        };

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();

            Player player = Player.getInstance(getActivity());
            try {
                player.setCredentials("d40b7cc98a001fc9be8dd3fd32c3a0c495d0db42", "b59c6d9c1b5a91d125f098ef9c2a7165dc1bd517");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            IntentFilter playerIntents = new IntentFilter();
            playerIntents.addAction(Player.EVENT_CLIENT_ID_RECEIVED);

            getActivity().registerReceiver(mPlayerBroadcastReceiver, playerIntents);
        }

        @Override
        public void onStop() {
            super.onStop();

            getActivity().unregisterReceiver(mPlayerBroadcastReceiver);
        }
    }
}
