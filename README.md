
The Android SDK centers around an instance of the `fm.feed.android.playersdk.Player` class.
You can tell the `Player` instance to `play()` music, `pause()` music,
`like()` or `dislike()` the current song, or `skip()` to the next song.
The `Player` class does all of its work asynchronously, so you must register
`Listener` classes with it to receive notifications when various events occur.

Music playback requires an Internet connection and the client must
be within the United States. The player will check for these requirements
and notify your code via a Listener if they are not met and 
playback isn't available.

The basic flow to begin streaming music is:

1. Call `Player.setTokens` to assign your security credentials and
kick off a background request to Feed.fm to confirm the client can play
music. This should be done as early in your app's lifecycle as possible,
and only need be done once.

2. Use `Player.onPlayerAvailability` to be informed when the player is
available for playback or not.

3. Use the `Player.play()`, `Player.pause()`, `Player.skip()` methods 
to control music playback.

A minimal example, run from within an `ActionBarActivity.onCreate()` method
would look like the following:

```java

Player.setTokens("demo", "demo"); // or try "badgeo" to see what non-US clients experience

Player p = Player.getInstance();

p.onPlayerAvailability(new PlayerAvailabilityListener() {
    @Override
    public void onAvailable() {
      // .. enable player control buttons

      // optionally start loading a song in the background for 
      // future playback:
      p.tune();
    }

    @Override
    public void onUnavailable() {
      // .. hide player buttons because music isn't available
    }

});

// .... time passes ....

// user hits play button, music starts
p.play();

// user wants to skip to the next song
p.skip();

// user hits pause button, music stops
p.pause();
```

The SDK automatically takes care to register as a foreground service and
create a status bar Notification when playing music, so that when the user
switches to another app the music can continue playing and the user can
tap the notification to return to the app.
If you wish for music to not play when the app suspends or stops, you
must call `Player.pause()`. You can customize the look of the Notification
by making your own `fm.feed.android.playersdk.NotificationBuilder`
implementation and registering it with the Player.

The SDK will handle loss of audio focus: music will be paused or the volume
temporarily reduced.

The SDK will retry requests on network failure.

The current station, list of available stations, and current song being played
(if any) are available through various `Player` methods.

There are various _listeners_ that you can register with the player to
be informed of changes in state:

1. `fm.feed.android.playersdk.PlayerListener`

This interface will inform you when the player is initialized and
when it changes state (from playing to paused, for instance).
This will also receive any errors that the player wasn't able handle by itself.

2. `fm.feed.android.playersdk.SocialListener`

This interface will inform you when the user has liked, disliked, or
unliked the current song.

3. `fm.feed.android.playersdk.NavListener`

This interface will give you notifications of station/track changes,
skip failures, buffer and progress updates, and end of playlist notifications. 

There is a sample player UI in `fm.feed.android.playersdk.view.PlayerView`
that you can use as-is or as a starting point for your own project. There is
also a sample app (called, appropriately, 'app') that puts the sample UI
in some fragments and makes them available for display.

