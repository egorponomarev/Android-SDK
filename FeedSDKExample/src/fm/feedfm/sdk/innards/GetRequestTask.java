package fm.feedfm.sdk.innards;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;


/**
 * 
 * Lowest level object dealing with GET Requests in the SDK.
 * 
 * 
 * @author tdebenning
 *
 */
public class GetRequestTask extends AsyncTask<Void, Void, Void> {
	
	
	private static final String TAG = "PostRequestTask";
	
	
	private HttpGet request = null;
	private IRawResponseHandler response_handler;
	
	
	/**
	 * 
	 * 
	 * 
	 * @param request
	 * @param response_handler
	 */
	public GetRequestTask(HttpGet request,IRawResponseHandler response_handler ){
		
		if(request != null) {
			
			this.request = request;
			if(response_handler != null) {
				this.response_handler = response_handler;
			} else {
				Log.e(TAG, "The RawGetResponseHandler passed into the GetRequestTask was null");
				throw new IllegalArgumentException("The RawGetResponseHandler passed into the GetRequestTask was null");
				
			}
		} else {
			Log.e(TAG, "The GetRequest Object passed into the GetRequestTask was null");
			throw new IllegalArgumentException("The HttpGet Object passed into the GetRequestTask was null");
			
		}
		
		
	}
	

	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		
		HttpClient httpClient = new DefaultHttpClient();
		
		try {
			HttpResponse response = httpClient.execute(this.request);
			if(this.response_handler.isSuccess(response)) {
				//Request was a success
				
				this.response_handler.handleSuccess(response);
				
			} else {
				//Request was a failure
				this.response_handler.handleFailure(response);
				
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

}
