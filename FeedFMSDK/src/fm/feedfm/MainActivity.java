package fm.feedfm;

import org.json.JSONObject;

import fm.feedfm.sdk.IResponseEvent;
import fm.feedfm.sdk.Session;
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
	
	
	private static final String secret = "";
	private static final String token = "";
	private static final String clientID = "";
	
	
	
	private Session the_session = null;

	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        this.the_session = new Session(token,secret, clientID);
        
        
        
        start = (Button)this.findViewById(R.id.play);
        skip = (Button)this.findViewById(R.id.skip);
        stop = (Button)this.findViewById(R.id.stop);
        
        //OnClick Handler for the Start Play Button
        start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if(!the_session.isTuned()) {					
					playSong();
									
				}				
			}      	
        });
        
        //OnClick Handler for the Skip Play Button
        skip.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				if(the_session.isTuned()) {
					
					skipSong();
					
				} else {
					
					playSong();
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
        
        
        
        
        
        
        
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /**
     * 
     * This is code that is used to demonstrate in the UI 
     * or anywhere else on how to play a song. Using the FEED FM API
     * 
     * 
     */
    private void playSong() {
		try {
			the_session.tune(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					
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
     * 
     */
    private void skipSong() {
    	
    	try {
    		
    		this.the_session.requestSkip(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					
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
     */
    private void stopPlay() {
    	
    
    }
    
    /**
     * 
     * Demonstrates how to dislike a play
     * 
     * 
     */
    private void dislikePlay() {
    	
    	try {
    		
    		this.the_session.dislikePlay(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					
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
     */
    private void likePlay() {
    	
    	try {
    		
    		this.the_session.likePlay(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					
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
     */
    private void invalidatePlay() {
    	
    	try {
    		this.the_session.requestInvalidate(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					
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
     */
    private void unlikePlay() {
    	
    	try {
    		
    		this.the_session.unlikePlay(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					
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
     */
    private void reportPlayStarted() {
    	
    	try {
    		
    		this.the_session.reportPlayStarted(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					
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
     * @param seconds
     */
    private void reportPlayElapsed(String seconds) {
    	
    	try {
    		
    		this.the_session.reportPlayElapsed(seconds, new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					
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
     * 
     */
    private void reportPlayCompleted() {
    	try {
    			
    		this.the_session.reportPlayCompleted(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason,
						String data) {
					// TODO Auto-generated method stub
					
				}
    			
    		});
    		
    	} catch (Exception e) {
    		Log.e(TAG, "There was an exception attempt to report elapsed time");
    		Log.e(TAG, e.getMessage());
    	}
    	
    }
    
}
