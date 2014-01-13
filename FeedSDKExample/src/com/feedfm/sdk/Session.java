package com.feedfm.sdk;

import android.util.Base64;
import android.util.Log;

import com.feedfm.sdk.innards.RequestFactory;

public class Session implements ISession {
	
	
	private static final String TAG = "Session";
	
	
	private String secret = null;
	private String token = null;
	private String baseurl =  "http://feed.fm";
	private String sbaseurl = "https://feed.fm"; 
	private RequestFactory factory = null;
	private String clientid = null;
	private String auth_token;
	
	public Session(String secret, String token) {
		
		
		if(secret != null) {
			
			this.secret = secret;
			
			if(token != null) {
				this.token = token;
				this.factory = new RequestFactory(this);
				
			} else {
				Log.e(TAG,"The token passed in to the session was null");
				throw new IllegalArgumentException();
				
			}
			
			
			
		} else {
			
			Log.e(TAG, "The session secret passed in was null");
			throw new IllegalArgumentException("The session secret passed in was null");
			
		}
		
		
		
	}
	
	
	public String getBaseurl() {
		return baseurl;
	}


	public String getSbaseurl() {
		return sbaseurl;
	}


	public String getClientid() {
		return clientid;
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
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setCreditials(String token, String secret) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setPlacementID(String placementID) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setStationID(String StationID) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setFormats(String formats) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setMaxBitRate(String bitrate) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void tune() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Placement getActivePlacement() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Play getActivePlay() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean hasActivePlayStarted() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isTuned() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void reportPlayStarted() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void reportPlayElapsed(String seconds) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void reportPlayCompleted() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void requestSkip() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void requestInvalidate() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void likePlay() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void unlikePlay() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void dislikePlay() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void Unsuspend() {
		// TODO Auto-generated method stub
		
	}

	


}
