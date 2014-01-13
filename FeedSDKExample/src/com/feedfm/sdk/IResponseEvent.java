package com.feedfm.sdk;

import org.json.JSONObject;

public interface IResponseEvent {
	
	public void successEvent(JSONObject response_object);
	
	public void failureEvent(int statuscode,String reason, String data);
	
	

}
