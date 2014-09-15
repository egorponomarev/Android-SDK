
The Android SDK centers around an instance of the `fm.feed.android.playersdk.Player` class.
You can tell the `Player` instance to `play()` music, `pause()` music,
or `skip()` to the next song. The `Player` class does all of its work
asynchronously, so you must register `Listener` classes with it to
receive notifications when various events occur.

Note that this library requires an Internet connection for playback,
and the Feed.fm service is only licensed for use in the United States.

The basic flow to begin streaming music is:

1. create an implementation of `Player.PlayListener` to receive notifications 
  from the player
2. call `p = Player.getInstance(..)` to pass in authentication credentials,
  begin initialization, and get a reference to the player
3. use the `p.play()`, `p.pause()`, `p.skip()` instance methods 
  to control music playback.

A minimal example, run from within a `Fragment.onCreateView()` method
would look like the following:

```java

PlayerListener pl = new PlayerListener() {
  public void onPlayerInitialized(PlayInfo playInfo) {
    /* Player is ready to rock. */

    /* Display the player interface here (note that
     * we might be reconnecting to the music player because
     * the app is resuming).
     */
    // displayPlayer(playInfo);

    /* Start pulling down the first song so playback is immediate
     * when 'play' is called.
     */
    p.tune();

    /* (or just start music playback immediately) */
    // p.play();
  }

  public void onError() {
    /* Unrecoverable error, such as:
     *    - the client isn't in the US
     *    - bad authentication tokens
     *    - server not responding as expected
     * Bad network connections are retried before they are
     * ever passed down to here.
     */
  }

  public void OnPlaybackStateChanged(PlayInfo.State state) {
    /* The state of the player has changed, so rerender
     * as needed.
     */
  }

  public void onNotificationWillShow(int notificationId) {
    /* Called when the persistant notification will show.
     * Use the passed in id if you want to override the default
     * notification layout, otherwise do nothing here.
     */
  }

};

// You pick a unique id for your app for notifications
static final int CUSTOM_NOTIFICATION_ID = 313377367;

Player p = Player.getInstance(getActivity(), pl, token, secret, CUSTOM_NOTIFICATION_ID);

// .... time passes ....

// user hits play button, music starts
p.play();

// user wants to skip to the next song
p.skip();

// user hits pause button, music stops
p.pause();
```

The SDK automatically takes care to register as a foreground service, so that
when user switches to another app the music can continue playing and the
user can return to the music app via the status bar. If you wish for music to
not play when the app suspends or stops, you should call `Player.pause()`.

The SDK will handle loss of audio focus: music will be paused or the volume
temporarily reduced.

The SDK will retry requests on network failure.

If you are building an interactive player, you can register a
`fm.feed.android.playersdk.Player.NavListener`
implementation with your `Player` instance. This interface will give you
notifications of placement/station/track changes, skip failures, buffer and progress
updates, and end of playlist notifications. The current placement, list of
available stations, current station, and current song being played (if any) is
available through various `Player` methods.





