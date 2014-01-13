package com.feedfm.sdk.innards;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

/**
 * 
 * This is the lowest level object in the SDK for post request
 * implemented as an asynchronous task. 
 * 
 * 
 * @author tdebenning
 *
 */
public class PostRequestTask extends AsyncTask<Void, Void, Void> {

	private static final String TAG = "PostRequestTask";
	
	
	private HttpPost request = null;
	private IRawPostResponseHandler response_handler;
	
	
	/**
	 * Constructor
	 * 
	 * @param request  Fully constructed HttpPost request
	 * @param response_handler response_processor that determines if the operation was a success
	 * or not and then makes the correct call on what to do with the response based on success 
	 * or failure
	 */
	public PostRequestTask(HttpPost request, IRawPostResponseHandler response_handler ) {
		
		if(request != null) {
			
			this.request = request;
			if(response_handler != null) {
				
				this.response_handler = response_handler;
			} else {
				Log.e(TAG, "The raw post response handler was null");
				throw new IllegalArgumentException("The RawPostResponseHandler was null");
			}
			
			
		} else {
			Log.e(TAG, "The HttpPost request passed to the PostRequestTask was null");
			throw new IllegalArgumentException("The post request passed in was null");
		}
		
		
		
		
	}
	
	
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpResponse response = httpClient.execute(this.request);
			if(this.response_handler.isSuccess(response)) {
				//The request succeeded
				this.response_handler.handleSuccess(response);
			} else {
				//The request failed
				this.response_handler.handleFailure(response);
			}
			
			
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "There was a Client protocol exception when trying to execute a post reqeust");
			
			e.printStackTrace();
		} catch (IOException e) {
			
			Log.e(TAG, "There was an IOException when trying to execture a post request");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
