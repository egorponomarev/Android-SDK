package com.feedfm.api.Requests;

import android.util.Log;

import com.feedfm.api.FMAuthenticator;
import com.feedfm.api.FMFailureResponse;
import com.feedfm.api.FMSuccessResponse;
import com.feedfm.api.Requests.FMTimeRequest;



import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;


/**
 * Created by Thomas on 9/16/13.
 */
public class FMAPIRequest {

	
	public static String TAG = "FMAPIRquest";
	
	
	
	public static String POST = "POST";
	public static String GET = "GET";
	public static String CLIENT = "client";
	public static String OAUTH = "oauth/time";
	public static String PLAY = "play";
	
	
	public static String POST_PLACEMENTID = "placement_id";
	public static String POST_STATIONID = "station_id";
	public static String  POST_FORMATS = "formats";
	public static String POST_MAX_BITRATE = "max_bitrate";
	public static String POST_SECONDS = "seconds";
	

    private FMAuthenticator auth;
    private HashMap successBlock;
    private HashMap failureBlock;
    private URL urlRequest;
    private Boolean authRequired;
    private String httpMethod;
    private String httpEndPoint;
   


	private HashMap<String, String> postParameters;
    private HashMap<String, String> queryParameters;


    private FMSuccessResponse success;
    private FMFailureResponse failure;

    public FMAPIRequest(FMSuccessResponse success, FMFailureResponse failure) {

       this();
       this.success = success;
       this.failure = failure;

    }
    
    public FMAPIRequest() {
    	
    	this.postParameters = new HashMap<String, String>();
    	this.queryParameters = new HashMap<String, String>();
    		
    }


    public static FMAPIRequest requestCUUID() {
    	
    	Log.v(TAG, "Creating a CUUID request");

        FMAPIRequest request = new FMAPIRequest();
        
        request.setHttpMethod(FMAPIRequest.POST);
        request.setHttpEndPoint(FMAPIRequest.CLIENT);
        request.setAuthRequired(true);
     
        return request;

    }
    public static FMAPIRequest requestServerTime() {
    	
    	Log.v(TAG, "Creating a requestServerTime request");

    	FMAPIRequest request = new FMAPIRequest();
    	request.setHttpMethod(FMAPIRequest.GET);
        request.setHttpEndPoint(FMAPIRequest.OAUTH);
        request.setAuthRequired(false);
    	
    	
        return request;
    }

    public static FMAPIRequest requestStationsForPlacement(String placementID) throws BADRequestParam {
    	
    	Log.v(TAG, "Creating a StationForPlacement request");
    	
    	FMAPIRequest request = null;
    	if(placementID != null && placementID.length() > 0) {
    		request = new FMAPIRequest();
    		request.setHttpMethod(FMAPIRequest.GET);
    	
    		String endpoint = "placement/" + placementID + "/station"; 
    	
    		
        	request.setHttpEndPoint(endpoint);
    		request.setAuthRequired(true);
    	} else {
    		Log.e(TAG, "The placementID for the StationsForPlacementRequest was null");
    		throw new BADRequestParam("The placement ID was null");
    	}
    		
    	return request;
    }

    
    public static FMAPIRequest requestPlayInPlacement(String placementID) throws BADRequestParam{
    	
    	return FMAPIRequest.requestPlayInPlacement(placementID, null);
    }
    

    public static FMAPIRequest requestPlayInPlacement(String placementID, String stationID ) throws BADRequestParam {
        
    	return FMAPIRequest.requestPlayInPlacement(placementID, stationID, null, null);
   

    }

    public static FMAPIRequest requestPlayInPlacement(String placementID, String stationID, String formatList, String maxbitrate ) throws BADRequestParam {
    	
     	Log.v(TAG, "Creating PlayInPlacement request");
     	FMAPIRequest request = new FMAPIRequest();
     	request.setHttpMethod(FMAPIRequest.POST);
		request.setHttpEndPoint(FMAPIRequest.PLAY);
    	
    	if(placementID != null && placementID.length() > 0) {
    		
    		request.addPostParameter(FMAPIRequest.POST_PLACEMENTID, placementID);
    			
    		
    	} else {
    		
    		Log.e(TAG, "The PlacementID was either null or had a zero length");
    		
    		throw new BADRequestParam("The PlacementID was either null or had a zero length");
    	}
    	
    	
    	if(stationID != null && stationID.length() > 0) {
			
			
			if(stationID != null && stationID.length() > 0) {
			
				request.addPostParameter(FMAPIRequest.POST_STATIONID, stationID);
				
			}
			
			
		} else {
			
			Log.e(TAG, "The StationID was either null or had a zero length");
			
			throw new BADRequestParam("The StationID was either null or had a zero length");
		}
    	
    	if(formatList != null) {
    	
    		
    		request.addPostParameter(POST_FORMATS, formatList);
		
    		
    	} else {
    	
    		Log.e(TAG, "The formatlist was null or had zero length");
    		throw new BADRequestParam("The formatlist was null or had zero length");
    		
    	}
    	
    	if(maxbitrate != null && maxbitrate.length() > 0) {
    		
    		request.addPostParameter(FMAPIRequest.POST_MAX_BITRATE, maxbitrate);
    		
    		
    	} else {
    	
    		Log.e(TAG, "The maxbitrate was null or had a zero length");
    		throw new BADRequestParam("The maxbitrate was null or had a zero length");
    	}
    	
    	request.setAuthRequired(true);
    	
    	
        return request;

    }

    public static FMAPIRequest requestStart(String playID) throws BADRequestParam {

    	Log.v(TAG, "Creating a request start request");
    	
        FMAPIRequest request = new FMAPIRequest();
        
        if(playID != null || playID.length() > 0) {
        	request.setHttpMethod(POST);
            
            String endpoint = "play/" + playID + "/start";
            request.setHttpEndPoint(endpoint);
            request.setAuthRequired(true);
        	
        	
        }else  {
        	Log.e(TAG,"The playID was either null or had a zero length.");
        	
        	throw new BADRequestParam("The playID was either null or had a zero length.");
        }
        
        
        return request;
    	
    	
    }

    public static FMAPIRequest requestElapse(String playID, Double time) throws BADRequestParam {

        
    	Log.v(TAG, "Creating a Elapse request");
    	
    	FMAPIRequest request = new FMAPIRequest();
    	if(playID != null && playID.length() >0 ) {
    	
    		if(time != null) {
    		
		    	request.setHttpMethod(FMAPIRequest.POST);
		    	
		    	String endpoint = "play/" + playID + "/elapse";
		    	
		    	request.setHttpEndPoint(endpoint);
		    	String the_time = time.toString();
		    	
		    	request.addPostParameter(FMAPIRequest.POST_SECONDS, the_time);
    		} else {
    			Log.e(TAG, "The time was null");
    			throw new BADRequestParam("The time was null");
    		}
    	} else {
    		Log.e(TAG,"The playID was either null or of zero length");
    		throw new BADRequestParam("The playID was either null or of zero length");
    	}
    	
    	
        return request;
    }

    public static FMAPIRequest requestSkip(String playID) {
       
    	return FMAPIRequest.requestSkip(playID);
    }

    public static FMAPIRequest  requestSkip(String playID, Double elapsedTime) throws BADRequestParam {
    	
    	Log.v(TAG, "Creating a  Skip request");

    	FMAPIRequest request = new FMAPIRequest();
    	
    	if(playID != null && playID.length() > 0) {
    		
    		if(elapsedTime != null) {
    	
		    	request.setHttpMethod(FMAPIRequest.POST);
		    	String endpoint = "play/" + playID + "/skip";
		    	request.setHttpEndPoint(endpoint);
		    	request.setAuthRequired(true);
		    	String str_seconds = elapsedTime.toString();
		    	request.addPostParameter(POST_SECONDS, str_seconds);
    		} else {
    			Log.e(TAG, "Elaspsed Time was null");
    			throw new BADRequestParam("Elaspsed Time was null");
    			
    		}
    	} else {
    		
    		Log.e(TAG, "PlayID was null or of zero length");
    		throw new BADRequestParam("PlayID was null or of zero length");
    	}
    
        
        return request;
    }

    public static FMAPIRequest requestInvalidate(String playID) throws BADRequestParam {

        Log.v(TAG, "Creating Invalidate request");
    	
        FMAPIRequest request = new FMAPIRequest();
        if(playID != null || playID.length() != 0) {
        
        	request.setHttpMethod(FMAPIRequest.POST);
        	
        	String endpoint = "play/" + playID + "/invalidate";
        	
        	request.setHttpEndPoint(endpoint);
        	request.setAuthRequired(true);
        	   	
        } else {
        	
        	Log.e(TAG, "The PlayID was null or of zero length");
        	throw new BADRequestParam("The playID was null or of zero length");
        	
        }
        	
        return request;
    }

    public static FMAPIRequest requestComplete(String playID) throws BADRequestParam {

     Log.v(TAG, "Creating Complete request");
    	
        FMAPIRequest request = new FMAPIRequest();
        if(playID != null || playID.length() != 0) {
        
        	request.setHttpMethod(FMAPIRequest.POST);
        	
        	String endpoint = "play/" + playID + "/invalidate";
        	
        	request.setHttpEndPoint(endpoint);
        	request.setAuthRequired(true);
        	    	
        	
        } else {
        	
        	Log.e(TAG, "The PlayID was null or of zero length");
        	throw new BADRequestParam("The playID was null or of zero length");
        	
        }
        
    	
        return request;
    }

    public void send() {

        //Todo
    }

    public void cancel() {

        //ToDo
    }

    public void setAuth(FMAuthenticator auth) {
        this.auth = auth;
    }

    public void setFailureBlock(HashMap failureBlock) {
        this.failureBlock = failureBlock;
    }

    public void setSuccessBlock(HashMap successBlock) {

        this.successBlock = successBlock;
    }

    public URL getUrlRequest() {
        return urlRequest;
    }

    public Boolean getAuthRequired() {
        return authRequired;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public HashMap getPostParameters() {
        return postParameters;
    }

    public HashMap getQueryParameters() {
        return queryParameters;
    }

    public static IFMAPIRequest requestServerTime(FMSuccessResponse success, FMFailureResponse failure) {

        FMTimeRequest request = new FMTimeRequest(success, failure);

        return request;
    }

	public FMSuccessResponse getSuccess() {
		return success;
	}

	public void setSuccess(FMSuccessResponse success) {
		this.success = success;
	}

	public FMFailureResponse getFailure() {
		return failure;
	}

	public void setFailure(FMFailureResponse failure) {
		this.failure = failure;
	}

	public FMAuthenticator getAuth() {
		return auth;
	}

	public HashMap getSuccessBlock() {
		return successBlock;
	}

	public HashMap getFailureBlock() {
		return failureBlock;
	}

	public void setUrlRequest(URL urlRequest) {
		this.urlRequest = urlRequest;
	}

	public void setAuthRequired(Boolean authRequired) {
		this.authRequired = authRequired;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public void setPostParameters(HashMap postParameters) {
		this.postParameters = postParameters;
	}

	public void setQueryParameters(HashMap queryParameters) {
		this.queryParameters = queryParameters;
	}
    
	 public String getHttpEndPoint() {
			return httpEndPoint;
	}
	 
	public void setHttpEndPoint(String httpEndPoint) {
		this.httpEndPoint = httpEndPoint;
	}

	
	public void addPostParameter(String key, String value) {
		
		Log.v(TAG, "Adding a Postparamter to the POST Paramenter hash");
		
		if(this.postParameters == null) {
			
			Log.e(TAG, "The post parameter hashmap was null. Creating");
			this.postParameters = new HashMap<String, String>();
			
		} else {
			if(key != null) {
				
				if(value != null) {
					this.postParameters.put(key, value);
					
				} else {
				
					Log.e(TAG, "The value was null not placing key " + key + " into hashmap");
					
				}
			
				
			} else {
				
				Log.e(TAG, "The key for the post paramater was null not adding");
			}
		}
	}
	
    
}
