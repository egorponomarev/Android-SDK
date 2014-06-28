package fm.feedfm.sdk;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpPost;

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
	public Session(String secret, String token, String client_id ) {
		
		
		if(secret != null) {
			
			this.secret = secret;
			
			if(token != null) {
				
				if(client_id != null) {
					this.token = token;
					this.factory = new RequestFactory(this);
					this.started_playing = false;
					this.isTuned = false;
					this.current_id = client_id;
				} else {
					Log.e(TAG, "The client ID passed into this session was null");
					throw new IllegalArgumentException( "The client ID passed into this session was null");
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


	public String getClientID() {
		return client_id;
	}


	public String getAuthToken() {
		
		if(this.auth_token == null) {
			
			this.auth_token = this.generateAuthToken();
		}
		
		return this.auth_token;
		
	}
	
	private String generateAuthToken() {
		
		String result = null;
		
		String concat_input = token + ":" + secret;
		
		result = Base64.encodeToString(concat_input.getBytes(),0,concat_input.getBytes().length, Base64.DEFAULT);
		
		
		return result;
	}


	@Override
	public void setBaseURL(String baseUrl) {
		this.base_url = baseUrl;
		
	}
	
	public String getBaseURL() {
		return this.base_url;
	}


	@Override
	public void setCredentials(String token, String secret) {
		this.token = token;
		this.secret = secret;
	}


	@Override
	public void setPlacementID(String placementID) {
	
		this.placement_id = placementID;
	}


	@Override
	public void setStationID(String StationID) {
		this.station_id = StationID;
		
	}


	@Override
	public void setFormats(String formats) {
		this.formats = formats;
		
	}


	@Override
	public void setMaxBitRate(String bitrate) {
		this.max_bitrates = bitrate;
		
	}


	@Override
	public void tune(IResponseEvent event) throws BadRequestGenerationException{
		
		try {
			
			HttpPost post = this.factory.createPlayRequest(this.placement_id, this.station_id, this.formats, this.max_bitrates, null);
			this.executePostRequest(event, post);;
			
			
		} catch (UnsupportedEncodingException e) {
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
	

		try {
			
			HttpPost post = this.factory.createPlayStartRequest(this.current_id);
			this.executePostRequest(event, post);;
			
			
		} catch (UnsupportedEncodingException e) {
			throw new BadRequestGenerationException("Unsupported Encoding of URL");
		}
		
		
		
	}



	public void reportPlayElapsed(String seconds,IResponseEvent event )throws BadRequestGenerationException {

		try {
			
			HttpPost post = this.factory.createPlayElapseRequest( this.current_id, seconds);
			this.executePostRequest(event, post);
			
			
		} catch (UnsupportedEncodingException e) {
			throw new BadRequestGenerationException("Unsupported Encoding of URL");
		}
		
		
		
	}



	public void reportPlayCompleted(IResponseEvent event) throws BadRequestGenerationException{
try {
			
			HttpPost post = this.factory.createPlayCompleteRequest(this.current_id);
			this.executePostRequest(event, post);
			
			
		} catch (UnsupportedEncodingException e) {
			throw new BadRequestGenerationException("Unsupported Encoding of URL");
		}
	}



	public void requestSkip(IResponseEvent event) throws BadRequestGenerationException {
		try {
			
			HttpPost post = this.factory.createSkipRequest(this.current_id);
			this.executePostRequest(event, post);
			
			
		} catch (UnsupportedEncodingException e) {
			throw new BadRequestGenerationException("Unsupported Encoding of URL");
		}
		
	}



	public void requestInvalidate(IResponseEvent event) throws BadRequestGenerationException{
		try {
			
			HttpPost post = this.factory.createPlayInvalidate(this.current_id);
			this.executePostRequest(event, post);
			
			
		} catch (UnsupportedEncodingException e) {
			throw new BadRequestGenerationException("Unsupported Encoding of URL");
		}
		
	}


	public void likePlay(IResponseEvent event) throws BadRequestGenerationException {
		try {
			
			HttpPost post = this.factory.createLikeRequest(this.current_id);
			this.executePostRequest(event, post);
			
			
		} catch (UnsupportedEncodingException e) {
			throw new BadRequestGenerationException("Unsupported Encoding of URL");
		}
		
	}



	public void unlikePlay(IResponseEvent event) throws BadRequestGenerationException {
		try {
			
			HttpPost post = this.factory.createUnlikeRequest(this.current_id);
			this.executePostRequest(event, post);
			
			
		} catch (UnsupportedEncodingException e) {
			throw new BadRequestGenerationException("Unsupported Encoding of URL");
		}
		
	}



	public void dislikePlay(IResponseEvent event) throws BadRequestGenerationException {
		try {
			
			HttpPost post = this.factory.createDislikeRequest(this.current_id);
			this.executePostRequest(event, post);
			
			
		} catch (UnsupportedEncodingException e) {
			throw new BadRequestGenerationException("Unsupported Encoding of URL");
		}
		
	}

	private void executePostRequest(IResponseEvent event, HttpPost post) throws BadRequestGenerationException{
		
		if(event != null && post != null) {
			UIResponseHandler handler = new UIResponseHandler(event);
			PostRequestTask task = new PostRequestTask(post, handler);
			task.execute();
		} else {
			
			throw new BadRequestGenerationException("Either the response event object was null or the HttpPost request object was null");
		}
		
		
	}


	@Override
	public void reportPlayElapsed(IResponseEvent handler, String seconds)
			throws BadRequestGenerationException {
		// TODO Auto-generated method stub
		
	}


	

}
