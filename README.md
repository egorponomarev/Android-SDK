
The Android SDK centers around an instance of the 'Player' class.
You can tell the Player instance to `play()` music, `pause()` music,
or `skip()` to the next song. The Player class does all of its work
asynchronously, so you must register various 'Listener' classes with
it to receive notifications when various events occur.

The basic flow for using the Player is as follows:

```
Player p = Player.getInstance(??);

p.registerPlayerListener(new PlayerListener() {
  public void onPlayerInitialized(PlayInfo playInfo) {
    p.setCredentials(TOKEN, SECRET);
    p.tune();
  }

  public void onNotInUS() {
    // app is not located in the US, so hide player functionality
  }
});

// .... time passes ....

// user hits play button, music starts immediatedly
p.play();

// user wants to skip to the next song
p.skip();

// user hits pause button to pause music playback
p.pause();
```

The SDK automatically takes care to register as a foreground
service, so that when user switches to another app the music
can continue playing and the user can return to the music app
via the status bar.

The SDK will also handle loss of audio focus: music will be
paused or the volume temporarily reduced.



