package com.feedfm.api.Requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.feedfm.api.FMSession;

import android.os.AsyncTask;
import android.util.Log;

public class FMRequestTask extends AsyncTask<String , String , String> {

	
	public static String TAG = "FMRequestTask";
	
	
	FMAPIRequest the_request = null;
	HttpClient client = null;
	String full_url_str= null;
	
	
	
	public FMRequestTask(FMAPIRequest request) {
		
		Log.v(TAG, "Constructor for FMRequestTask");
		
		
		if(request != null) {
			
			
			this.the_request = request;
			full_url_str = FMSession.baseURL + this.the_request.getHttpEndPoint();
			
			
			
			
		} else {
			
			throw new NullPointerException("The Request was null");
		}
		
		
		
		
	}

	protected HttpResponse doPOST() throws IOException, ClientProtocolException {
		
		Log.v(TAG, "do post");
		
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(this.full_url_str);
		
		try {
			
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			HashMap<String, String> params = this.the_request.getPostParameters();
			
			Set<String> keys = params.keySet();
			for(String key : keys) {
				
				String value = params.get(key);
				NameValuePair current = new BasicNameValuePair(key,value);
				nameValuePairs.add(current);
				
			}
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
	
			HttpResponse response = httpclient.execute(httppost);
			return response;
			
			
			
		} catch (ClientProtocolException e) {
			
			Log.e(TAG, "There was a error with the client protocol");
			throw e;
			
		} catch (IOException ioe) {
			
			Log.e(TAG, "There was an IOException");
			throw ioe;
		}
		
		
		
		
		
		
	}
	
	
	protected HttpResponse doGET() throws IOException, ClientProtocolException {
		
		Log.v(TAG, "do get");
		
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(this.full_url_str);
		
		
		
		try {
			
			
	
			HttpResponse response = httpclient.execute(httppost);
			return response;
			
			
			
		} catch (ClientProtocolException e) {
			
			Log.e(TAG, "There was a error with the client protocol");
			throw e;
			
		} catch (IOException ioe) {
			
			Log.e(TAG, "There was an IOException");
			throw ioe;
		}
		
		
	}
	
	
	
	public void executeRequest() throws IOException, ClientProtocolException, UnknownHttpMethod {
		this.client = null;
		
		if(this.the_request != null) {
			
			
			
			if(this.the_request.getHttpMethod().compareTo(FMAPIRequest.POST) == 0) {
				
				HttpResponse response = this.doPOST();
				
				
			} else {
				
				if(this.the_request.getHttpMethod().compareTo(FMAPIRequest.GET )== 0) {
					
					HttpResponse response = this.doGET();
					
					
						
				} else {
					
					Log.e(TAG,  "Unknown Http Method or was unsupported");
					throw new UnknownHttpMethod("The method was not GET or POST");
				}
			}
			
			
			
			
		} else {
			
			Log.e(TAG, "The request object was null and should not have been");
			throw new NullPointerException("The request object was null which it should not be");
		}
		
		
	}
	



	@Override
	protected String doInBackground(String... arg0) {
		
		Log.v(TAG, "DoInBackground");
		
		
		
		
		
		
		return null;
	}
	
	
	@Override
	protected void onPostExecute(String resutl) {
		
		
	}
	
	
	
	
	
}
