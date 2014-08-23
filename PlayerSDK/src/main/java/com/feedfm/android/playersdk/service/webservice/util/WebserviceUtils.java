package com.feedfm.android.playersdk.service.webservice.util;

import android.util.Base64;

import com.feedfm.android.playersdk.service.bus.Credentials;

/**
 * Created by mharkins on 8/22/14.
 */
public class WebserviceUtils {
    private WebserviceUtils() {}


    public static String getAuthorization(Credentials credentials) {
        String concat = credentials.getToken() + ":" + credentials.getSecret();
        String base64Auth = Base64.encodeToString(concat.getBytes(), Base64.NO_WRAP | Base64.DEFAULT);
        return "Basic " + base64Auth;
    }

}
