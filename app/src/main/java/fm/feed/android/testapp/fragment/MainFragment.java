package fm.feed.android.testapp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Show the action bar
        ((ActionBarActivity) getActivity()).getSupportActionBar().show();
    }
}
