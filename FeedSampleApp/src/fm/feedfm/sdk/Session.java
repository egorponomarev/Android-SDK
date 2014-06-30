package fm.feedfm.sdk;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.util.Base64;
import android.util.Log;

import fm.feedfm.sdk.innards.PostRequestTask;
import fm.feedfm.sdk.innards.RequestFactory;
import fm.feedfm.sdk.innards.UIResponseHandler;

/**
 * 
 * 
 * 
 * 
 */
public class Session implements ISession {
	
	
	private static final String TAG = "Session";
	
	public static  String FEED_BASE_URL =  "http://feed.fm";
	public static String FEED_SECURE_BASE_URL = "https://feed.fm"; 
	
	
	private String secret = null;
	private String token = null;

	private RequestFactory factory = null;
	private String client_id = null;
	private String auth_token;
	private String base_url;
	
	private String current_id = null;
	private String next_id = null;
	
	
	private String max_bitrates = null;
	private String formats = null;
	private String placement_id = null;
	private String station_id = null;
	
	
	private boolean started_playing = false;
	private boolean isTuned = false;
	
	
	/**
	 * Constructor
	 * 
	 * 
	 * 
	 * 
	 * @param secret
	 * @param token
	 */
	public Session(String token, String secret, String client_id ) throws IllegalArgumentException {
		
		
		if(secret != null) {
			
			this.secret = secret;
			
			if(token != null) {
				this.token = token;
				this.factory = new RequestFactory(this);
				this.started_playing = false;
				this.isTuned = false;
				this.base_url = FEED_SECURE_BASE_URL;
				this.secret = secret;
				if(client_id != null) {
					
					this.client_id = client_id;
				} else {
					
					
					if(this.current_id == null) {
						Log.e(TAG, "The client ID passed into this session was null");
						throw new IllegalArgumentException( "The client ID passed into this session was null");
					}
				}
				
			} else {
				Log.e(TAG,"The token passed in to the session was null");
				throw new IllegalArgumentException("The token passed in to the session was null");
				
			}
			
			
			
		} else {
			
			Log.e(TAG, "The session secret passed in was null");
			throw new IllegalArgumentException("The session secret passed in was null");
			
		}
			
	}

	public void setCurrentID(String song_id) {
		
		this.current_id = song_id;
	}
	

	public String  getClientID() {
		
		this.client_id= "tom";
		return this.client_id;
		
	}
	
	public static void requestClientID(String token, String secret, IResponseEvent event) throws BadRequestGenerationException, UnsupportedEncodingException {
		
		Log.d(TAG, "Attempting to perform ClientID Request");
		String url =FEED_SECURE_BASE_URL+ RequestFactory.CLIENT_REQUEST;
			
		//String url = "http://feed.fm:9000/api/v2/client";	
		HttpPost post = new HttpPost(url);	
		
		
		ArrayList<BasicNameValuePair> force = new ArrayList<BasicNameValuePair>();
		BasicNameValuePair force_pair = new BasicNameValuePair("force200", "1");
		force.add(force_pair);
		UrlEncodedFormEntity form_entity = new UrlEncodedFormEntity(force);
		post.setEntity(form_entity);
	
		String full_header = "Basic " + Session.generateAuthToken(token, secret);
		post.setHeader(RequestFactory.AUTHORIZATION_HEADER, full_header);

		
		Log.d(TAG, "Generated ClientID  now excecuting request");
		executePostRequest(event, post);;
		
		
	}


	public String getAuthToken() {
		
		if(this.auth_token == null) {
			
			this.auth_token = this.generateAuthToken();
		}
		
		return this.auth_token;
		
	}
	
	private String generateAuthToken() {
		return Session.generateAuthToken(this.token, this.secret);
	}
	
	
	
	private static String generateAuthToken(String token, String secret) {
		Log.i(TAG, "Generating the authorization token");
		String result = null;
		
		String concat_input = token + ":" + secret;
		
		result = Base64.encodeToString(concat_input.getBytes(),0,concat_input.getBytes().length, Base64.NO_WRAP);

		Log.i(TAG, "Authorization Token is " + result);
		
		return result;
	}


	@Override
	public void setBaseURL(String baseUrl) {
		Log.d(TAG, "Setting the base URL to " + baseUrl);
		
		this.base_url = baseUrl;
		
	}
	
	public String getBaseURL() {
		
		if(this.base_url != null) {
			Log.d(TAG,"Base URL is set to: " + this.base_url);
		} else {
			Log.e(TAG, "Base URL is set to NULL!");
		}
		
		return this.base_url;
	}


	@Override
	public void setCredentials(String token, String secret) {
		
		Log.d(TAG, "Setting the Creditenials");
		if(token != null && secret != null) {
			this.token = token;
			this.secret = secret;
		} else {
			
			Log.e(TAG,  "Either the secret or token passed in was null");
		}
	}


	@Override
	public void setPlacementID(String placementID) {
		if( placementID != null) {
			Log.d(TAG, "Setting the placement ID to" + placementID);
			this.placement_id = placementID;
		} else {
			Log.e(TAG, "The PlacementID passed in was null not setting placement ID");
		}
	}


	@Override
	public void setStationID(String StationID) {
		if(StationID != null) {
			this.station_id = StationID;
		} else {
			
			Log.e(TAG, "The Station ID was null");
		}
		
	}


	@Override
	public void setFormats(String formats) {
		
		if(formats != null) {
			this.formats = formats;
		} else {
			
			Log.e(TAG, "The formats passed in were null");
		}
		
	}


	@Override
	public void setMaxBitRate(String bitrate) {
		if(bitrate != null) {
			this.max_bitrates = bitrate;
		} else {
			Log.e(TAG, "The max bitrate passed in was null");
		}
		
	}


	@Override
	public void tune(IResponseEvent event) throws BadRequestGenerationException{
		Log.d(TAG, "Attempting to perfrom Tune Reqeuest");
		try {
			
			HttpPost post = this.factory.createPlayRequest(this.placement_id, this.station_id, this.formats, this.max_bitrates, null);
			Log.d(TAG, "Generated Tune Request now excecuting request");
			executePostRequest(event, post);;
			
			
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,"Unsupported Encoding of URL for Tune request" );
			throw new BadRequestGenerationException("Unsupported Encoding of URL");
		}
				
	}


	@Override
	public Placement getActivePlacement() {
		if(this.placement_id != null) {
			return new Placement(placement_id);
		}
		return null;
	}


	@Override
	public Play getActivePlay() {
		if(this.current_id != null) {
			return new Play(this.current_id);
		}
		return null;
	}


	@Override
	public boolean hasActivePlayStarted() {
		return this.started_playing;
	}


	public void setStarted_playing(boolean started_playing) {
		this.started_playing = started_playing;
	}


	public void setTuned(boolean isTuned) {
		this.isTuned = isTuned;
	}


	@Override
	public boolean isTuned() {
		return this.isTuned;
	}



	public void reportPlayStarted(IResponseEvent event) throws BadRequestGenerationException {
	
		Log.d(TAG, "Attempeting to report a play has started");
		if(event != null) {
			try {
				Log.d(TAG, "Generating Play Started Reqeuest");
				HttpPost post = this.factory.createPlayStartRequest(this.current_id);
				Log.d(TAG, "Executing Play Started Request");
				executePostRequest(event, post);;
				
				
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG,"Unsupported Encoding of URL for play started request");
				throw new BadRequestGenerationException("Unsupported Encoding of URL");
			}
		} else {
			Log.e(TAG, "The IResponseEvent object passed in was null cannot execute request");
			throw new BadRequestGenerationException("The IResponseEvent passed was null");
		}
		
		
		
	}



	public void reportPlayElapsed(String seconds,IResponseEvent event )throws BadRequestGenerationException {

		Log.d(TAG, "Attempting to perform PlayElapsed Request");
		if(event != null) {
			try {
				if(seconds != null) {
					Log.d(TAG, "Generating the PlayElapsed Request");
					HttpPost post = this.factory.createPlayElapseRequest( this.current_id, seconds);
					Log.d(TAG, "Executing the PlayElapsed Request");
					executePostRequest(event, post);
				} else {
					Log.e(TAG, "The seconds reported were null not performing request");
					throw new BadRequestGenerationException("the seconds reported were null not performing request");
				}
				
				
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unsupported Encoding of URL for Play Elasped Request");
				throw new BadRequestGenerationException("Unsupported Encoding of URL");
			}
		}  else {
			Log.e(TAG, "The IResponseEvent object passed in was null cannot execute request");
			throw new BadRequestGenerationException("The IResponseEvent passed was null");
		}
		
		
		
	}



	public void reportPlayCompleted(IResponseEvent event) throws BadRequestGenerationException{
		Log.d(TAG, "Attempting to perform Play Completed Request");
		if(event != null) {
			try {
				Log.d(TAG, "Generating the Play Completed Request");
				HttpPost post = this.factory.createPlayCompleteRequest(this.current_id);
				Log.d(TAG, "Executing the Play Completed Request");
				executePostRequest(event, post);
				
				
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unsupported Encoding of URL for Play Completed Request");
				throw new BadRequestGenerationException("Unsupported Encoding of URL");
			}
		} else {
			Log.e(TAG, "The IResponseEvent object passed in was null cannot execute request");
			throw new BadRequestGenerationException("The IResponseEvent passed was null");
		}
	}



	public void requestSkip(IResponseEvent event) throws BadRequestGenerationException {
		Log.d(TAG, "Attempting to perform Skip Request");
		if(event != null) {
			try {
				Log.d(TAG, "Generating the Skip Request");
				HttpPost post = this.factory.createSkipRequest(this.current_id);
				Log.d(TAG, "Executing the Skip Request");
				executePostRequest(event, post);
				
				
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unsupported Encoding of URL for Skip Request");
				throw new BadRequestGenerationException("Unsupported Encoding of URL");
			}
		
			
		} else {
			Log.e(TAG, "The IResponseEvent object passed in was null cannot execute request");
			throw new BadRequestGenerationException("The IResponseEvent passed was null");
		}
	
		
	}



	public void requestInvalidate(IResponseEvent event) throws BadRequestGenerationException{
		Log.d(TAG, "Attempting to perform Invalidate Request");
		if(event != null) {
			try {
				Log.d(TAG, "Generating the Invalidate Request");
				HttpPost post = this.factory.createPlayInvalidate(this.current_id);
				Log.d(TAG, "Executing the Invalidate Request");
				executePostRequest(event, post);
				
				
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unsupported Encoding of URL for Invalidate Request");
				throw new BadRequestGenerationException("Unsupported Encoding of URL");
			}
		} else {
			Log.e(TAG, "The IResponseEvent object passed in was null cannot execute request");
			throw new BadRequestGenerationException("The IResponseEvent passed was null");
		}
		
	}


	public void likePlay(IResponseEvent event) throws BadRequestGenerationException {
		Log.d(TAG, "Attempting to perform Like Play Request");
		if(event != null) {
			try {
				Log.d(TAG, "Generating the Like Play Request");
				HttpPost post = this.factory.createLikeRequest(this.current_id);
				Log.d(TAG, "Executing the Like Play Request");
				executePostRequest(event, post);
				
				
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unsupported Encoding of URL for Like Play Request");
				throw new BadRequestGenerationException("Unsupported Encoding of URL");
			}
		} else {
			Log.e(TAG, "The IResponseEvent object passed in was null cannot execute request");
			throw new BadRequestGenerationException("The IResponseEvent passed was null");
		}
		
	}



	public void unlikePlay(IResponseEvent event) throws BadRequestGenerationException {
		Log.d(TAG, "Attempting to perform Unlike Request");
		if(event != null) {
			try {
				Log.d(TAG, "Generating the Unlike Play Request");
				HttpPost post = this.factory.createUnlikeRequest(this.current_id);
				Log.d(TAG, "Executing the Unlike Play Request");
				executePostRequest(event, post);
				
				
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unsupported Encoding of URL for UnLike Play Request");
				throw new BadRequestGenerationException("Unsupported Encoding of URL");
			}
		} else {
			Log.e(TAG, "The IResponseEvent object passed in was null cannot execute request");
			throw new BadRequestGenerationException("The IResponseEvent passed was null");
		}
		
	}



	public void dislikePlay(IResponseEvent event) throws BadRequestGenerationException {
		
	
		Log.d(TAG, "Attempting to perform Dislike Request");
		if(event != null) {
			try {
				Log.d(TAG, "Generating the Dislike Play Request");
				HttpPost post = this.factory.createDislikeRequest(this.current_id);
				Log.d(TAG, "Executing the DislikePlay Request");
				this.executePostRequest(event, post);
				
				
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unsupported Encoding of URL for DisLike Play Request");
				throw new BadRequestGenerationException("Unsupported Encoding of URL");
			}
		} else {
			Log.e(TAG, "The IResponseEvent object passed in was null cannot execute request");
			throw new BadRequestGenerationException("The IResponseEvent passed was null");
		}
		
	}

	protected static void executePostRequest(IResponseEvent event, HttpPost post) throws BadRequestGenerationException{
		Log.d(TAG, "Executing Post Request");
		if(event != null && post != null) {
			UIResponseHandler handler = new UIResponseHandler(event);
			PostRequestTask task = new PostRequestTask(post, handler);
			task.execute();
		} else {
			Log.e(TAG, "Could not execute post request due to Bad Request Generation Exception");
			throw new BadRequestGenerationException("Either the response event object was null or the HttpPost request object was null");
		}
		
		
	}


	


	

}
