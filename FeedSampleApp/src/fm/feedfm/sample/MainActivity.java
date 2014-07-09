package fm.feedfm.sample;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import fm.feedfm.sdk.BadRequestGenerationException;
import fm.feedfm.sdk.IResponseEvent;
import fm.feedfm.sdk.Play;
import fm.feedfm.sdk.Session;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

public class MainActivity extends Activity {
	
	private static final String TAG ="MainActivity";
	
	private Button start = null;
	private Button skip = null;
	private Button stop = null;
	private Button invalidate = null;
	private Button like = null;
	private Button unlike = null;
	private Button dislike = null;
	private Button start_session = null;
	
	//Data that is needed to establish a session with the FEED FM Server
	private static final String secret = "1977a15cd114fe2fc0cf85e9b57b47d93241d957";
	private static final String token = "89a91f25d4685efaafa4f01c62269bbd4842c67c";
	
	//The Initial CLIENT_ID is requested before a seesion is established.  The client should preserve 
	//This client id should be preserved between creation of sessions.  It should only be requested once.
	private static final String clientID = null;
	
	
	
	private Session the_session = null;
	
	
	MediaPlayer mediaPlayer = null;

	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        //this.the_session = new Session(token,secret, clientID);
        mediaPlayer = new MediaPlayer();
        
        
        
        
        
        
        
        start = (Button)this.findViewById(R.id.play);
        skip = (Button)this.findViewById(R.id.skip);
        stop = (Button)this.findViewById(R.id.stop);
        invalidate =(Button)this.findViewById(R.id.invalidate);
        like = (Button)this.findViewById(R.id.like);
        unlike = (Button)this.findViewById(R.id.unlike);
        dislike = (Button)this.findViewById(R.id.dislike);
        start_session = (Button)this.findViewById(R.id.start_session);
        
        
        start_session.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				startFeedSession(token, secret);
				
			}
        	
        });
        
        
        //OnClick Handler for the Start Play Button
        start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(the_session != null) {
					if(!the_session.isTuned()) {					
						playSong();
										
					}		
				} else {
					
					Log.e(TAG, "The Session object is null");
				}
			}      	
        });
        
        //OnClick Handler for the Skip Play Button
        skip.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				if(the_session != null) {
					if(the_session.isTuned()) {
						
						skipSong();
						
					} else {
						
						playSong();
					}	
				} else {
					
					Log.e(TAG, "The Session object is null");
				}
			}       	
        });
        
        //OnClickHandler for Stopping play of a song
        stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
        	
        	
        });
        
        invalidate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				invalidatePlay();
				
			}
        	
        });
        
        like.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				likePlay();
				
			}
        	
        });
        
        
        unlike.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				unlikePlay();
				
			}
        	
        	
        });
        
        dislike.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dislikePlay();
				
			}
        	
        	
        });
        
        
        
        
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
    
    /**
     * 
     * To be able to start a session with the Feed API 
     * 
     * you must get a CLIENT_ID.  This makes a request to the server for 
     * a client id.  You should preserve this CLIENT_ID and reuse it next time
     * the app starts.  Once a CLIENT_ID is generated you use it to initiate a 
     * Session.  which is done by calling the constructor for the session object.
     * 
     * 
     * 
     * @param token
     * @param secret
     */
    private void startFeedSession(final String token,final String secret) {
    	
    	try {
    		
			Session.requestClientID(token, secret,new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object)  {
					// TODO Auto-generated method stub
					Log.d(TAG, "Successfully Received a client ID call");
					
				
					
					try {
						
						String ClientID = response_object.getString("client_id");
						
						
						
						the_session = new Session(token,secret, ClientID);
						;
					} catch (JSONException e) {
						Log.e(TAG,"Error getting client ID out of successful call to feed fm for client id");
					}
					
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					Log.d(TAG, "Failure Recieving a client ID call");
					Log.d(TAG, "ClientID FAILURE status code " + statuscode + " " + reason);
				}
				
				
				
			});
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadRequestGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    /*
     * 
     * The FEED FM Session object is how you interact with the FEED FM server.  
     * 
     * You make invocations on a session object and you pass an instance of the IResponseEvent.  The
     * Session object takes care of all the threading issues and marshalling of the requests to the
     * UI thread.  Getting data from a request to the UI thread from a call on the Session object is done by making
     * the call with an Instance of the IResponseEvent.  It is up to the developer to write code to handle these events 
     * as shown below.  The IResponseEvent has too methods that will be called backed from the thread that made 
     * the request to the FEED FM Server.  The successEvent will return a JSONObject that contains all the data from the 
     * FEED FM sever.  For example a successful call to the the_session.play() object will contain the full JSONObject 
     * that comes back from the call to play.  The failureEvent method is called when the request to the FEED FM 
     * server fails.   It contains the status code of the request and the reason why the call failed and string of data
     * which should be a JSONObject containing error data.  All other method operate in a similar fashion. 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     */
    
    
    
    
    
    
    
    /**
     * 
     * This is code that is used to demonstrate in the UI 
     * or anywhere else on how to play a song. Using the FEED FM API
     * 
     *  A successful call will contain a JSON object that look similar to this
     *  
     *  {
				  "success": true,
				  "play": {
				    "id": "27555",
				    "station": {
				      "id": "599",
				      "name": "East Bay"
				    },
				    "audio_file": {
				      "id": "665",
				      "duration_in_seconds": "300",
				      "track": {
				          "id": "15224887",
				          "title": "3030"
				      },
				      "release": {
				          "id": "1483477",
				          "title": "Deltron 3030"
				      },
				      "artist": {
				          "id": "766824",
				          "name": "Del the Funky Homosapien"
				      },
				      "codec": "aac",
				      "bitrate": "128",
				      "url": "http://feed.fm/audiofile-665-original.aac"
				    }
				  }
				}
     * 
     * 
     * 
     * 
     */
    private void playSong() {
    	
    	
    	
		try {
			the_session.tune(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					
					Log.d(TAG, "Tune SUCCESS!!!");
					
					try {
						JSONObject play_json = (JSONObject) response_object.get("play");
						Play play = new Play(play_json);
						
						
						Play.AudioFile audio_file = play.getAudioFile();
						
						String song_id = play.getPlayID();
						String song_url = audio_file.getURL();
						try {
							mediaPlayer.setDataSource(song_url);
							mediaPlayer.prepareAsync();
							
							mediaPlayer.start();
							
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
										
						the_session.setCurrentID(song_id);
						if(song_url != null && song_url.length() > 0) {
							//Now that we have a url for the audio file we can set it 
							//to a media player and start playing.
							
							//Once play has been started we must inform the FEED Servers
							//Play has started.
							reportPlayStarted();
							
							
						}
												
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Tune FAILURE status code " + statuscode + " " + reason);
					
				}
										
			});
			
			
		} catch(Exception e) {
			
			Log.e(TAG,"There was an exception while attempting to start a play");
			Log.e(TAG,e.getMessage());
			
		}
	}
    
    
    /**
     * 
     * 
     * This demonstrates how to skip a song in the UI or 
     * anywhere else using the feed fm API.
     * 
     * A successful call will have JSONObject that looks like this.
     * 
     * {
		  "success" : true,
		  "can_skip" : true
		}
     */
    private void skipSong() {
    	
    	try {
    		
    		this.the_session.requestSkip(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Skip SUCCESS!!!");
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Skip FAILURE status code " + statuscode + " " + reason);
				}
    		   			
    		});
 
    	} catch(Exception e) {
    		Log.e(TAG, "There was an exception while attempt to skip a song");
    		Log.e(TAG, e.getMessage());
    		
    	}
    		
    }
    
    
    /**
     * 
     * Demonstrates how to stop play
     * 
     * Stopping of a play requires the stopping of the play from the media client
     * and then informing the server of completed play
     * 
     * 
     */
    private void stopPlay() {
    	
    
    }
    
    /**
     * 
     * Demonstrates how to dislike a play
     * 
     * {
		  "success" : true
		}
		   
     * 
     * 
     */
    private void dislikePlay() {
    	
    	try {
    		
    		this.the_session.dislikePlay(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Dislike SUCCESS!!!");
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Dislike FAILURE status code " + statuscode + " " + reason);
				}
    			
    			
    			
    		});
    		
    		
    	} catch(Exception e) {
    		
    		Log.e(TAG, "There was an exception while attempting to dislike a play");
    		Log.e(TAG, e.getMessage());
    			
    	}	
    }
    
    
    /**
     * 
     * Demonstates how to like a play
     * 
     * 
     * 
     * {
		  "success" : true
		}
   
     * 
     * 
     */
    private void likePlay() {
    	
    	try {
    		
    		this.the_session.likePlay(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Like SUCCESS!!!");
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Like FAILURE status code " + statuscode + " " + reason);
				}
    			
    			
    			
    			
    		});
    		
    	} catch (Exception e) {
    		Log.e(TAG, "There was an exception while attempting to report a like");
    		Log.e(TAG, e.getMessage());
    	}
    	
    	
    	
    }
    
    
    /**
     * 
     * Demonstrates how to invalidate a Play
     * 
     * {
		  "success" : true
		}
   
     * 
     */
    private void invalidatePlay() {
    	
    	try {
    		this.the_session.requestInvalidate(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					Log.d(TAG, "invalidate SUCCESS!!!");
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Invalidate FAILURE status code " + statuscode + " " + reason);
				}
    			
    		});
    		
    	} catch (Exception e) {
    		
    		Log.e(TAG, "There was an error while attempting to invalidate a play");
    		Log.e(TAG, e.getMessage());
    		
    	}
    	
    }
    
    
    /**
     * 
     * Demonstrates how to unlike a Play
     * 
     * 
     * {
		  "success" : true
		}
   
     * 
     */
    private void unlikePlay() {
    	
    	try {
    		
    		this.the_session.unlikePlay(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Unlike SUCCESS!!!");
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Unlike FAILURE status code " + statuscode + " " + reason);
				}
    					
    		});
    		
    	} catch(Exception e) {
    		
    		Log.e(TAG, "There was an error while attempting to unlike a Play");
    		Log.e(TAG,e.getMessage());
    	}
    	
    }
    
    
    /**
     * 
     * Demonstrates how to report a Play has started
     * 
     * {
		  "success" : true
		}
   
     * 
     */
    private void reportPlayStarted() {
    	
    	try {
    		
    		this.the_session.reportPlayStarted(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Play Started SUCCESS!!!");
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					Log.d(TAG, "PlayStarted FAILURE status code " + statuscode + " " + reason);
				}
    			
    			
    			
    		});
    		
    		
    		
    	} catch (Exception e) {
    		Log.e(TAG, "There was an exception attempt to report play started");
    		Log.e(TAG, e.getMessage());
    	}
    	
    	
    }
    
    /**
     * 
     * Demonstrates how to report the play time elapsed
     * 
     * 
     * {
		  "success" : true
		}
   
     * 
     * @param seconds
     */
    private void reportPlayElapsed(String seconds) {
    	
    	try {
    		
    		this.the_session.reportPlayElapsed(seconds, new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Play Elapsed SUCCESS!!!");
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					Log.d(TAG, "PlayElapsed FAILURE status code " + statuscode + " " + reason);
				}
    			
    			
    			
    		});
    		
    	} catch (Exception e) {
    		Log.e(TAG, "There was an exception attempt to report elapsed time");
    		Log.e(TAG, e.getMessage());
    	}
    }
    
    /**
     * 
     * Demonstrates how to report a play completed
     * 
     {
		  "success" : true
		}
   
     * 
     * 
     */
    private void reportPlayCompleted() {
    	try {
    			
    		this.the_session.reportPlayCompleted(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Play Completed SUCCESS!!!");
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					Log.d(TAG, "Play Completed FAILURE status code " + statuscode + " " + reason);
				}
    			
    		});
    		
    	} catch (Exception e) {
    		Log.e(TAG, "There was an exception attempt to report elapsed time");
    		Log.e(TAG, e.getMessage());
    	}
    	
    }
    
}

