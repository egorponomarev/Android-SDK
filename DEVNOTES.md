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
and secondaryQueue (TaskQueueManager). 

The TaskQueueManager is a list of what are basically AsyncTasks
that are executed serially, but different from an Android Looper
in that you can cancel all existing queued up tasks or cancel
the tasks based on priority. It has functions for you to be able
to add a new task to the list, and
remove other tasks that are at different priorities. Once you`ve 
added tasks to the Queue, you run 'next()' to make sure the first
task on the queue is started up. When a running, queued, task
is complete, it will removed itself from the queue and call 'next()'
on the Queue again to get the next task started.

The things that you can queue up are:

TuneTask - request a play from the feed.fm server, then create
  a MediaPlayer with the URL for the audio. Send the MediaPlayer
  instance to the TuneListener registered with this task.



It looks like when we are requesting a play from the server that
we don`t expect to immediately
commence, that is done in the tuningQueue. When we`re getting a play from
the server that we want to immediately commence, or we want to commence
a play that is in the tuningQueue, we throw the play in the mainQueue.
The secondaryQueue holds the next play that should commence upon
completion of the play in the mainQueue.



q`s:
  in PlayerService.tune() in the onMetaDataLoaded() handler
  when creating a TuneTask, why is setSkippable set to false by default?

Android notes:

Every thread gets a MessageQueue attached to it when Looper.prepare()
is called. When you run Looper.loop(), the system just repeatedly pulling
things off that queue and processing them. You can now create a
Handler instance that will post things (Runnable or Message instances)
to the queue. The Handler can throw things right on the queue to
be run on the next loop, or you can set a delay or specific time
so they run in the future.

The Android UI thread has a looper by default.

An AsyncTask is a task that runs a new asynchronous thread and
returns posts the results back to the UI thread. This class must be
instantiated in the UI thread, and the instance must be started 
in the UI thread. This class uses the Handler class to interact
with the UI thread. This class may run multiple tasks in parallel
or serially, depending on the SDK version - but you can use
.executeOnExecutor() to enforce the parallelization you want.


