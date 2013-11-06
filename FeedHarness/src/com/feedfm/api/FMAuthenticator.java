package com.feedfm.api;

import com.feedfm.api.Requests.FMAPIRequest;

import java.net.URL;

/**
 * Created by Thomas on 9/16/13.
 */
public interface FMAuthenticator {

    public URL authenticatedURLRequest(FMAPIRequest request);


}
