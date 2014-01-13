package com.feedfm.sdk.innards;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;


import com.feedfm.sdk.IResponseEvent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;



/**
 * 
 * This handles the responses from a request and does the marshalling of the data
 * from the AsyncTask thread back to the UI thread. 
 * 
 * 
 * @author tdebenning
 *
 */
public class UIResponseHandler extends Handler implements IRawPostResponseHandler {
	
	private static final String TAG ="UIPostHandler";
	
	private IResponseEvent event;

	

	/**
	 * 
	 * This is the constructor it takes an IResponseEvent which for UI handling of the success on the UI
	 * Thread.
	 * 
	 * 
	 * @param event
	 */
	public UIResponseHandler(IResponseEvent event) {
		
		
		if(event !=null) {
			
			this.event = event;
			
			
		} else {
			
			Log.e(TAG, "The response event passed in was null must be something");
		}
		
		
	}
	
	

	
	/**
	 * 
	 * Handles that marshalling of the data back onto the UI thread from the asynctask thread.
	 * 
	 */
	@Override
	public void handleMessage(Message msg) {
		
		Bundle data = msg.getData();
		if(data.getBoolean("success")) {
			//The request was a success
			
			String json_response = data.getString("json");
			
			JSONObject obj;
			try {
				obj = new JSONObject(json_response);
				this.event.successEvent(obj);
			} catch (JSONException e) {
				int statuscode = data.getInt("statuscode");
				this.event.failureEvent(statuscode, "Could not parse JSON from string", json_response);
			}
			
			
			
		} else {
			//The request was a failure
			int statuscode = data.getInt("statuscode");
			this.event.failureEvent(statuscode,"Error status from server", null);
			
		}
			
	}
	
	
	/**
	 * 
	 * Convenience function to determine if the Http response was a success or not based upon 
	 * the status code.
	 * 
	 */
	@Override
	public boolean isSuccess(HttpResponse response) {
		
		if(response.getStatusLine().getStatusCode() == 200) {
			
			return true;
		}

		return false;
		
	}

	
	/**
	 * 
	 * Since the response was a success on the server this function handles the reading of the
	 * data off of the HttpResponse's data stream and into a string.
	 * 
	 * 
	 */
	@Override
	public void handleSuccess(HttpResponse response) {

		InputStream stream;
		StringBuffer result_buffer = new StringBuffer();
		int statuscode = response.getStatusLine().getStatusCode();
		try {
			stream = response.getEntity().getContent();
			
			byte[] buffer = new byte[100];
			int bytes_read = stream.read(buffer);
			
			while(bytes_read > 0) {
				String work_buf = new String(buffer);
				result_buffer.append(work_buf);
				 buffer = new byte[100];
				bytes_read = stream.read(buffer);
			}
			
			Bundle data = new Bundle();
			data.putBoolean("success", true);
			data.putInt("statuscode", statuscode);
			data.putString("json", result_buffer.toString());
			
			
			Message response_msg = new Message();
			response_msg.setData(data);
	
			this.sendMessage(response_msg);
			
		
			
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			
			Log.e(TAG, "IllegalState Exeception in UIPostHandler:handleSuccess " + e.getMessage());

		} catch (IOException e) {
			Log.e(TAG, "IOException in UIPostHandler:handleSuccess " + e.getMessage());
		}
		
		
	}

	
	/**
	 * 
	 * This handles the failure case 
	 * 
	 * Doesn't read from the response stream but does pass along the status code to the UI 
	 * Thread.
	 * 
	 * 
	 */
	@Override
	public void handleFailure(HttpResponse response) {
		// TODO Auto-generated method stub
		
		int statuscode = response.getStatusLine().getStatusCode();
		Bundle data = new Bundle();
		data.putBoolean("success", false);
		data.putInt("statuscode", statuscode);
		
		
		Message msg = new Message();
		msg.setData(data);
		this.sendMessage(msg);
		
	
	}

}
