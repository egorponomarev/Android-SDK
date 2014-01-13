package com.feedfm.sdk.innards;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.util.Base64;
import android.util.Log;


import com.feedfm.sdk.Session;

import org.apache.http.Header;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class RequestFactory {
	
	private static final String TAG = "RequestFactory";

	private static final String HttpPost = null;
	
	public static final String SERVER_TIME = "/api/v2/oauth/time";
	public static final String CLIENT_REQUEST = "/api/v2/client";
	public static final String PLACEMENT_REQUEST = "/api/v2/placement";
	
	//used with PLACEMENT ABOVE;
	public static final String STATION_REQUEST = "/station"; 
	
	public static final String PLAY_REQUEST = "api/v2/play";
	//All the rest are used with PLAY_REQUEST
	public static final String PLAY_START ="/start";
	public static final String PLAY_ELAPSED = "/elapsed";
	public static final String PLAY_SKIPPED = "/skip";
	public static final String PLAY_INVALIDATE_REQUEST = "/invalidate";
	public static final String PLAY_COMPLETE_REQUEST = "/complete";
	public static final String PLAY_LIKE_REQUEST = "/like";
	public static final String PLAY_DISLIKE_REQUEST = "/dislike";
	public static final String PLAY_UNLIKE_REQUEST = "/like";
	
	
	public static final String AUTHORIZATION_HEADER = "Authorization";
	
	
	public static final String CLIENT_ID_PARAM = "client_id";
	public static final String PLACEMENT_ID_PARAM = "placement_id";
	public static final String STATION_ID_PARAM = "station_id";
	public static final String FORMATS_PARAM = "formats";
	public static final String MAX_BITRATE_PARAM = "max_bitrate";
	public static final String SECONDS_PARAM = "seconds";
	
	

	private Session session;
	
	
	/**
	 * 
	 * Convenience Class for creating HTTPPost and HTTPGET requests to perform specific tasks
	 * 
	 * 
	 * @param session
	 */
	public RequestFactory(Session session) {
		
		
		if(session != null) {
			
			this.session = session;
			
			
		} else {
			
			Log.e(TAG, "The session object passed into the result factory was null");
			throw new IllegalArgumentException();
		}
		
		
	}
	
	//result.setHeader("Authorization", "Basic " + Base64.encodeToString("user:password".getBytes(), Base64.NO_WRAP));
	
	/**
	 * 
	 * Generates the must have param for post requests.
	 * 
	 * @return
	 */
	private BasicNameValuePair createClientIDParam() {
		
		BasicNameValuePair result = new BasicNameValuePair(RequestFactory.CLIENT_ID_PARAM, this.session.getClientid());
		return result;
		
	}
	
	/**
	 * 
	 * Handles creating the params for Post requests deals with optional params
	 * 
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @param seconds
	 * @return
	 */
	private  List<BasicNameValuePair> generateBaseParams(String placement_id, String station_id, String formats, String max_bitrate, String seconds) {
		
		ArrayList<BasicNameValuePair> result = new ArrayList<BasicNameValuePair>();
		result.add(this.createClientIDParam());
		
		if(placement_id != null) {
			BasicNameValuePair placement_pair = new BasicNameValuePair(RequestFactory.PLACEMENT_ID_PARAM, placement_id);
			result.add(placement_pair);
		}
		
		if(station_id != null) {
			
			BasicNameValuePair station_pair = new BasicNameValuePair(RequestFactory.STATION_ID_PARAM, station_id);
			result.add(station_pair);
		}
		
		if(formats != null) {
			
			BasicNameValuePair  format_pair = new BasicNameValuePair(RequestFactory.FORMATS_PARAM, formats);
			result.add(format_pair);
		}
		
		if(max_bitrate != null) {
			
			BasicNameValuePair bitrate_pair = new BasicNameValuePair(RequestFactory.MAX_BITRATE_PARAM, max_bitrate);
			result.add(bitrate_pair);
		}
		
		if(seconds != null) {
			
			BasicNameValuePair seconds_pair = new BasicNameValuePair(RequestFactory.SECONDS_PARAM, max_bitrate);
			result.add(seconds_pair);
		}
		
		return result;
	}
	
	
	/**
	 * 
	 * Creates the baseline request used by all request invocations
	 * 
	 * @param full_url
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @param seconds
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private HttpPost createBasicPostRequest(String full_url, String placement_id, String station_id, String formats, String max_bitrate, String seconds) throws UnsupportedEncodingException {
		
		
		HttpPost result = new HttpPost(full_url);		
		
		result.setHeader(RequestFactory.AUTHORIZATION_HEADER, "Basic " + this.session.getAuthToken());
		List<BasicNameValuePair> params = this.generateBaseParams(placement_id, station_id, formats, max_bitrate, seconds);
		
		result.setEntity((new UrlEncodedFormEntity(params)));
		
		return result;

		
		
		
	}
	
	
	/**
	 * 
	 * Creates a start request
	 * 
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public HttpPost createPlayRequest(String placement_id, String station_id, String formats, String max_bitrate, String seconds) throws UnsupportedEncodingException {
		
		String url = this.session.getSbaseurl() + RequestFactory.PLAY_REQUEST;
	
					
		HttpPost result = this.createBasicPostRequest(url,placement_id, station_id, formats, max_bitrate,seconds);
		return result;
		
		
		
	}
	
	/**
	 * 
	 * Creates a Request that notifies the server that a play has started. 
	 * 
	 * @param id
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @param seconds
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public HttpPost createPlayStartRequest(String id, String placement_id, String station_id, String formats, String max_bitrate, String seconds) throws UnsupportedEncodingException {
		String url = this.session.getSbaseurl() + RequestFactory.PLAY_REQUEST + "/" + id + RequestFactory.PLAY_START;
		
		HttpPost result = this.createBasicPostRequest(url,placement_id, station_id, formats, max_bitrate, seconds);
		return result;
	
	}
	
	/**
	 * Creates a request that notifies the server how many seconds has elapsed since a play has started. 
	 * 
	 * 
	 * @param id
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @param seconds
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public HttpPost createPlayElapseRequest(String id,String placement_id, String station_id, String formats, String max_bitrate, String seconds) throws UnsupportedEncodingException {
		
		String url = this.session.getSbaseurl() + RequestFactory.PLAY_REQUEST + "/" + id + RequestFactory.PLAY_ELAPSED;
		
		HttpPost result = this.createBasicPostRequest(url,placement_id, station_id, formats, max_bitrate, seconds);
		return result;
		
	}
	
	/**
	 * 
	 * Creates a Skip Request
	 * 
	 * @param id
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @param seconds
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public HttpPost createSkipRequest(String id,String placement_id, String station_id, String formats, String max_bitrate, String seconds) throws UnsupportedEncodingException {
		
		
		
		String url = this.session.getSbaseurl() + RequestFactory.PLAY_REQUEST + "/" + id + RequestFactory.PLAY_SKIPPED;
		
		HttpPost result = this.createBasicPostRequest(url,placement_id, station_id, formats, max_bitrate, seconds);
		return result;
	}
	
	
	
	/**
	 * 
	 * Creates an Invalidate Request
	 * 
	 * @param id
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @param seconds
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public HttpPost createPlayInvalidate(String id, String placement_id, String station_id, String formats, String max_bitrate, String seconds) throws UnsupportedEncodingException {
		
		
		String url = this.session.getSbaseurl() + RequestFactory.PLAY_REQUEST + "/" + id + RequestFactory.PLAY_SKIPPED;
		
		HttpPost result = this.createBasicPostRequest(url,placement_id, station_id, formats, max_bitrate, seconds);
		return result;
		
	}
	
	/**
	 * 
	 * Creates a PlayCompleteRequest
	 * 
	 * @param id
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @param seconds
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public HttpPost createPlayCompleteRequest(String id, String placement_id, String station_id, String formats, String max_bitrate, String seconds) throws UnsupportedEncodingException {
		
		String url = this.session.getSbaseurl() + RequestFactory.PLAY_REQUEST + "/" + id + RequestFactory.PLAY_COMPLETE_REQUEST;
		
		HttpPost result = this.createBasicPostRequest(url,placement_id, station_id, formats, max_bitrate,seconds);
		return result;
		
		
	}
	
	
	/**
	 * 
	 * 
	 * Creates a Like Request
	 * 
	 * @param id
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @param seconds
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public HttpPost createLikeRequest(String id, String placement_id, String station_id, String formats, String max_bitrate, String seconds) throws UnsupportedEncodingException {
		
		String url = this.session.getSbaseurl() + RequestFactory.PLAY_REQUEST + "/" + id + RequestFactory.PLAY_LIKE_REQUEST;
		
		HttpPost result = this.createBasicPostRequest(url,placement_id, station_id, formats, max_bitrate, seconds);
		return result;
		
	}
	
	
	/**
	 * 
	 * 
	 * Creates a DislikeRequest
	 * 
	 * @param id
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @param seconds
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public HttpPost createDislikeRequest(String id, String placement_id, String station_id, String formats, String max_bitrate, String seconds) throws UnsupportedEncodingException {
		String url = this.session.getSbaseurl() + RequestFactory.PLAY_REQUEST + "/" + id + RequestFactory.PLAY_DISLIKE_REQUEST;
		
		HttpPost result = this.createBasicPostRequest(url,placement_id, station_id, formats, max_bitrate,seconds);
		return result;
		
		
	}
	
	/**
	 * 
	 * Creates a UnlikeRequest
	 * 
	 * @param id
	 * @param placement_id
	 * @param station_id
	 * @param formats
	 * @param max_bitrate
	 * @param seconds
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public HttpPost  createUnlikeRequest(String id, String placement_id, String station_id, String formats, String max_bitrate, String seconds) throws UnsupportedEncodingException {
		
		String url = this.session.getSbaseurl() + RequestFactory.PLAY_REQUEST + "/" + id + RequestFactory.PLAY_UNLIKE_REQUEST;
		
		HttpPost result = this.createBasicPostRequest(url,placement_id, station_id, formats, max_bitrate,seconds);
		return result;
	}
	
	
	
	
	
	
	

}
