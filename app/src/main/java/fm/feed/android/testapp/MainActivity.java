package fm.feed.android.testapp;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import fm.feed.android.playersdk.Player;
import fm.feed.android.playersdk.PlayerAvailabilityListener;
import fm.feed.android.playersdk.PlayerError;
import fm.feed.android.playersdk.service.PlayInfo;
import fm.feed.android.testapp.fragment.MainFragment;
import fm.feed.android.testapp.fragment.SlidingBottomFragment;
import fm.feed.android.testapp.fragment.SlidingFragment;
import fm.feed.android.testapp.fragment.TestFragment;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // tune in to our station
        Player.setTokens(getApplicationContext(), "demo", "demo"); // try "badgeo" to force the player to be unavailable
        player = Player.getInstance();

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            showFragment(new MainFragment(), MainFragment.class.getSimpleName());
        }

        /**
         * This makes the volume Hardware buttons control the Media Volume at all times (otherwise, during pauses in the playback, the ring volume would be set).
         */
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
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


    private void showFragment(Fragment fragment, String tag) {
        if (fragment == null) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (fragment instanceof SlidingFragment) {
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        }

        ft.replace(R.id.container, fragment, tag);
        ft.addToBackStack(tag);

        ft.commit();
    }

    private void addFragment(Fragment fragment, String tag) {
        if (fragment == null) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (fragment instanceof SlidingBottomFragment) {
            ft.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_top);
        }

        ft.add(R.id.container, fragment, tag);
        ft.addToBackStack(tag);

        ft.commit();
    }

    /**
     * "Showcase - Sliding Fullscreen Player" Button Callback
     * <p>
     * Shows a left sliding player
     * </p>
     *
     * @param v
     *         the Button
     */
    public void showSlidingPlayer(View v) {
        FragmentManager fm = getSupportFragmentManager();
        SlidingFragment fragment = (SlidingFragment) fm.findFragmentByTag(SlidingFragment.class.getSimpleName());

        if (fragment == null) {
            fragment = new SlidingFragment();
        }
        showFragment(fragment, SlidingFragment.class.getSimpleName());
    }

    /**
     * "Test UI" Button Callback
     * <p>
     * Shows a Testing UI
     * </p>
     *
     * @param v
     *         the Button
     */
    public void showTestUI(View v) {
        FragmentManager fm = getSupportFragmentManager();
        TestFragment fragment = (TestFragment) fm.findFragmentByTag(TestFragment.class.getSimpleName());

        if (fragment == null) {
            fragment = new TestFragment();
        }
        showFragment(fragment, TestFragment.class.getSimpleName());
    }

    /**
     * "Showcase - Sliding Bottom Player" Button Callback
     * <p>
     * Shows a bottom sliding player
     * </p>
     *
     * @param v
     *         the Button
     */
    public void showBottomPlayer(View v) {
        FragmentManager fm = getSupportFragmentManager();
        SlidingBottomFragment fragment = (SlidingBottomFragment) fm.findFragmentByTag(SlidingBottomFragment.class.getSimpleName());

        if (fragment == null) {
            fragment = new SlidingBottomFragment();
        }
        addFragment(fragment, SlidingBottomFragment.class.getSimpleName());
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }
}
