package fm.feed.android.SampleApp;

import fm.feed.android.playersdk.Player;

/**
 * Created by ericlambrecht on 5/20/15.
 */
public class Application extends android.app.Application {

    public void onCreate() {
        super.onCreate();

        // initialize player
        Player.setTokens(this, "demo", "demo");
    }

}
