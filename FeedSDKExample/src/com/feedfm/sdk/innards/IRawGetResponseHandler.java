package com.feedfm.sdk.innards;

import org.apache.http.HttpResponse;

public interface IRawGetResponseHandler {
	
	public boolean isSuccess(HttpResponse response);
	
	public void handleSuccess(HttpResponse response);
	
	public void handleFailure(HttpResponse response);

}
