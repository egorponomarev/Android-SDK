package com.feedfm.sdk.innards;

import org.apache.http.HttpResponse;

public interface IRawPostResponseHandler {
	
	public boolean isSuccess(HttpResponse response);
	
	public void handleFailure(HttpResponse response);
	
	public void handleSuccess(HttpResponse response);

}
