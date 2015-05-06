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
Player class gets a reference to it.

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
and secondaryQueue (TaskQueueManager). 

The TaskQueueManager is a list of what are basically AsyncTasks
that are executed serially, but different from an Android Looper
in that you can cancel all existing queued up tasks or cancel
the tasks based on priority. It has functions for you to be able
to add a new task to the list, and remove other tasks that are at
different priorities. Once you`ve added tasks to the queue, you
run 'next()' to make sure the next task on the queue is started
up. When a running, queued, task is complete, it will remove
itself from the queue and call 'next()' on the queue again to
get the next task started. If a task is failing, it can duplicate
itself and put the copy back at the front of the queue.

The things that you can queue up are:

TuneTask - request a play from the feed.fm server, then prepare
  a MediaPlayer with the play`s audio URL. When the MediaPlayer
  is ready for playback, throw it in the 'tuned' MediaPlayerPool
  so it can be retrieved for playback.

PlayTask - grab a tuned MediaPlayer from the MediaPlayerPool and
  play it while monitoring connectivity. 

....

The mainQueue contains things that affect what the user perceives
(such as the current play or current song). The tuningQueue runs
TuneTasks to get music queued up for when the current song completes.
The secondaryQueue runs tasks that might be visible by the user
but should run parallel to the primary queue (such as liking/disliking
the current song or reporting elapsed time to the feed server)


