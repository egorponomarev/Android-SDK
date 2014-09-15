
The Android SDK centers around an instance of the 'Player' class.
You can tell the Player instance to `play()` music, `pause()` music,
or `skip()` to the next song. The Player class does all of its work
asynchronously, so you must register 'Listener' classes with it to
receive notifications when various events occur.

Note that this library requires an Internet connection for playback,
and the Feed.fm service is only licensed for use in the United States.

The basic flow for using the Player is to:

1. create an implementation of Player.PlayListener to receive notifications 
  from the player
2. call Player.getInstance(..) to pass in authentication credentials and
  begin initialization
3. use the play(), pause(), skip() methods as requested by the user.

A minimal example, run from within a Fragment.onCreateView() method
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

// Unique id needed for foreground music service
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
user can return to the music app via the status bar.

The SDK will handle loss of audio focus: music will be paused or the volume
temporarily reduced.

The SDK will retry requests on network failure.

If you are building an interactive player, you can register a Player.NavListener
implementation with the Player instance. This interface will give you
notifications of placement/station/track changes, skip failures, buffer and progress
updates, and end of playlist notifications.


