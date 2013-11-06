package com.feedfm.api.Requests;

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


    private FMAuthenticator auth;
    private HashMap successBlock;
    private HashMap failureBlock;
    private URL urlRequest;
    private Boolean authRequired;
    private String httpMethod;
    private HashMap postParameters;
    private HashMap queryParameters;


    private FMSuccessResponse success;
    private FMFailureResponse failure;

    public FMAPIRequest(FMSuccessResponse success, FMFailureResponse failure) {

       this.success = success;
       this.failure = failure;

    }


    public static FMAPIRequest requestCUUID() {

        //ToDo
        return null;

    }
    public static FMAPIRequest requestServerTime() {

        //ToDo
        return null;
    }

    public static FMAPIRequest requestStationsForPlacement(String placementID) {
        //ToDo
        return null;
    }


    public static FMAPIRequest requestPlayInPlacement(String placementID, String stationID ) {
        //ToDo
        return null;

    }

    public static FMAPIRequest requestPlayInPlacement(String placementID, String stationID, String formatList, Double maxbitrate ) {
        //ToDo
        return null;

    }

    public static FMAPIRequest requestStart(String playid) {

        //ToDo
        return null;
    }

    public static FMAPIRequest requestElapse(String playid, Calendar time) {

        //ToDo
        return null;
    }

    public static FMAPIRequest requestSkip(String playid) {
        //ToDo
        return null;
    }

    public static FMAPIRequest  requestSkip(String playid, Calendar elapsedTime) {

        //Todo
        return null;
    }

    public static FMAPIRequest requestInvalidate(String playid) {

        //Todo
        return null;
    }

    public static FMAPIRequest requestComplete(String playid) {

        //ToDo
        return null;
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
}
