package fm.feedfm.feedsdkexample;

import org.json.JSONObject;

import com.feedfm.feedsdkexample.R;

import fm.feedfm.sdk.BadRequestGenerationException;
import fm.feedfm.sdk.IResponseEvent;
import fm.feedfm.sdk.Session;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;

public class PlayerActivity extends Activity {

	
	EditText result;
	
	Button btnTune;
	Button btnPlay;
	Button btnSkip;
	Button btnLike;
	Button btnDislike;
	Button btnElapsed;
	MediaController controller;
	
	
	
	public String token = "";
	public String secret = "";
	
	private Session session;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        
        btnTune = (Button)this.findViewById(R.id.btnTune);
        btnPlay = (Button)this.findViewById(R.id.btnStart);
        btnSkip = (Button)this.findViewById(R.id.btnSkip);
        btnElapsed = (Button)this.findViewById(R.id.btnElapsed);
        btnLike = (Button)this.findViewById(R.id.btnLike);
        btnDislike =(Button)this.findViewById(R.id.btnDislike);
        
        controller = (MediaController)this.findViewById(R.id.mediaController1);
        
        result = (EditText) this.findViewById(R.id.textView1);
        
        
        this.session = new Session(token,secret);
        
        
        btnTune.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
        	
        	
        });
        
        btnPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
        	
        	
        });
        
        
        btnSkip.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
        	
        	
        });
        
        
        btnElapsed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
        	
        	
        });
        
        btnLike.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
        	
        	
        });
        
        btnDislike.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
        	
        	
        });
        
        
        
        
        
    }
    
    
    public void handleTuneClick() {
    	
    	
    	try {
			this.session.tune(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason, String data) {
					// TODO Auto-generated method stub
					
				}
				
				
			});
		} catch (BadRequestGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public void handlePlayStartClick() {
    	
    	try {
			this.session.reportPlayStarted(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason, String data) {
					// TODO Auto-generated method stub
					
				}
				
				
			});
			
			
		} catch (BadRequestGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	
    	
    }
    
    public void handleSkipClick() {
    	
    	try {
			this.session.requestSkip(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason, String data) {
					// TODO Auto-generated method stub
					
				}
				
				
			});
		} catch (BadRequestGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public void handleElapsedClick() {
    	
    	try {
			this.session.reportPlayElapsed(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason, String data) {
					// TODO Auto-generated method stub
					
				}
				
				
			}, "0");
		} catch (BadRequestGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    		
    	
    }
    
    public void handleLikeClick() {
    	
    	try {
			this.session.likePlay(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason, String data) {
					// TODO Auto-generated method stub
					
				}
				
				
			});
		} catch (BadRequestGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	
    }
    
    public void handleDisLikeClick() {
    	
    	
    	try {
			this.session.dislikePlay(new IResponseEvent() {

				@Override
				public void successEvent(JSONObject response_object) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void failureEvent(int statuscode, String reason, String data) {
					// TODO Auto-generated method stub
					
				}
				
				
			});
		} catch (BadRequestGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.player, menu);
        return true;
    }
    
}
