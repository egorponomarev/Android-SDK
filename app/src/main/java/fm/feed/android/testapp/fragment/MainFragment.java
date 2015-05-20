package fm.feed.android.testapp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.PlayerAvailabilityListener;
import fm.feed.android.testapp.MainActivity;
import fm.feed.android.testapp.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {
    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);

        // don't let the user interact with the buttons until the player is ready

        Player.getInstance().onPlayerAvailability(new PlayerAvailabilityListener() {
            @Override
            public void onAvailable() {
                // enable all the player buttons
                Button showTestUI = (Button) rootView.findViewById(R.id.show_test_ui);
                showTestUI.setEnabled(true);

                Button showcaseFullscreen = (Button) rootView.findViewById(R.id.showcase_fullscreen);
                showcaseFullscreen.setEnabled(true);

                Button showcaseHalfscreen = (Button) rootView.findViewById(R.id.showcase_halfscreen);
                showcaseHalfscreen.setEnabled(true);
            }

            @Override
            public void onUnavailable() {
                // show the 'sorry' message
                View unavailableText = rootView.findViewById(R.id.unavailable_text);
                unavailableText.setVisibility(View.VISIBLE);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Show the action bar
        ((ActionBarActivity) getActivity()).getSupportActionBar().show();
    }
}
