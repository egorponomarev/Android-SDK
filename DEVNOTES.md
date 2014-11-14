Notes on how this is structured:

While clients interact with the Player class, all the real work
is done in the webservice.PlayerService class, which is an
Android 'Service'.

The PlayerService class does not allow binding. Instead, it
communicates with the app through a shared singleton bus (created with
'Otto', by the folks at Square) referred to as the 'event bus'. 
The Player and PlayerService classes register listeners and
post events to the bus. Much of the stuff passed over the bus
is defined in fm.feed.android.playersdk.service.bus.

The Player sends the following events to the PlayerService
over the bus, which the PlayerService responds to:

  Credentials - client credentials (token, secret)
  OutPlacementWrap - set new current placement
  OutStationWrap - set new current station
  PlayerAction - one of the various events the user initiated 
                 (tune, play, pause, skip, like, unlike, dislike)

and the Player listens (via the PlayerServiceListener) for the
following things to be posted, which it passes on to clients.

  PlayInfo - update on currently playing song
  EventMessage - an update on the status of things, such as:
                skip failed, skip status update, like, unlike, dislike,
                end of playlist, status updated.
  Placement - placement info received from server
  Station - station info received from server
  Play - new track started playing
  BufferUpdate - update on status of playback buffer
  ProgressUpdate - update on playback progress
  FeedFMError - something bad happened in the PlayerService


PlayerService holds on to an instance of PlayerInfo that basically
denotes the state of the player (current placement, station, song, e.t.c).
PlayerService updates that object as things change.  When the PlayerService
starts up or resumes, it posts the PlayerInfo instance on the bus so the
Player class has a reference to it.

The PlayerService handles all the communication with the Feed.fm
REST API through an instance of webservice.Webservice, which uses
the Square 'OkHttpClient' library that does request construction
and response parsing.

The PlayerService executes network communication with the server
through the use of Android AsyncTask instances. Those tasks are
tracked through a couple queues:

  TaskQueueManager - a queue of tasks, with some
    prioritization knowledge built in. Tasks that are of lower
    priority can be cancelled. The priority order, from highest
    to lowest is:
      ClientIdTask
      PlacementIdTask, StationIdTask
      all others
  TuningQueue extends TaskQueueManager - adds an extra
    isTuning() method that indicates a non-cancelled Tune task at
    the head of the queue.
  MainQueue extends TuningQueue - adds extra hasActivePlayTask()
    hasPlayTask(), and removeAllPlayTasks() methods for managing
    PlayTasks.

There is an instance of each queue: tuningQueue, mainQueue (MainQueue),
and secondaryQueue (TaskQueueManager). It looks like when we are
requesting a play from the server that we don`t expect to immediately
commence, that is done in the tuningQueue. When we`re getting a play from
the server that we want to immediately commence, or we want to commence
a play that is in the tuningQueue, we throw the play in the mainQueue.
The secondaryQueue holds the next play that should commence upon
completion of the play in the mainQueue.


q`s:
  in PlayerService.tune() in the onMetaDataLoaded() handler
  when creating a TuneTask, why is setSkippable set to false by default?

Android notes:

An AsyncTask is a task that gets started in the UI thread, then
run in a background thread with periodict update notifications, then
run again in the UI thread for post execution.

A Looper instance is attached to a MessageQueue and a Thread. It will
repeatedly pull things from the MessageQueue and execute them. You can
throw things into the MessageQueue with a Handler. The handler has
methods to append things to the queue, put them in the front, schedule
them to run in the future at a specific time or after some delay.


