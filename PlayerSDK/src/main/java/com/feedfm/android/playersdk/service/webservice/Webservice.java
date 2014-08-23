package com.feedfm.android.playersdk.service.webservice;

import android.content.Context;
import android.util.Pair;

import com.feedfm.android.playersdk.R;
import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.webservice.model.ClientResponse;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlacementResponse;
import com.feedfm.android.playersdk.service.webservice.util.WebserviceUtils;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by mharkins on 8/22/14.
 */
public class Webservice {
    public static final String EVENT_CLIENT_ID_RECEIVED = "com.feedfm.android.playersdk.event.clientid";
    public static final String EXTRA_CLIENT_ID = "com.feedfm.android.playersdk.extra.clientid";


    private RestInterface mRestService;

    /**
     * an android.app.Service object Context
     */
    private Context mContext;

    private Credentials mCredentials;

    public Webservice(Context context) {
        mContext = context;

        String apiVersion = context.getString(R.string.api_version);

        OkHttpClient okHttpClient = new OkHttpClient();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new OkClient(okHttpClient))
                .setEndpoint("https://feed.fm/api/" + apiVersion)
                .build();

        mRestService = restAdapter.create(RestInterface.class);
    }

    public void setCredentials(Credentials credentials) {
        mCredentials = credentials;
    }

    public void getClientId(final Webservice.Callback<String> callback) {
        mRestService.getClientId(WebserviceUtils.getAuthorization(mCredentials), new retrofit.Callback<ClientResponse>() {
            @Override
            public void success(ClientResponse clientResponse, Response response) {
                if (clientResponse.isSuccess()) {
                    callback.onSuccess(clientResponse.getClientId());
                } else {
                    callback.onFailure(clientResponse.getError());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                FeedFMResponse feedFMResponse = (FeedFMResponse) error.getBody();
                callback.onFailure(feedFMResponse.getError());
            }
        });
    }

    public void setPlacementId(int placementId, final Webservice.Callback<Pair<Placement, List<Station>>> callback) {
        mRestService.setPlacementId(WebserviceUtils.getAuthorization(mCredentials), placementId, new retrofit.Callback<PlacementResponse>() {
            @Override
            public void success(PlacementResponse placementResponse, Response response) {
                if (placementResponse.isSuccess()) {
                    callback.onSuccess(new Pair(placementResponse.getPlacement(), placementResponse.getStations()));
                } else {
                    callback.onFailure(placementResponse.getError());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                FeedFMResponse feedFMResponse = (FeedFMResponse) error.getBody();
                callback.onFailure(feedFMResponse.getError());
            }
        });
    }

    interface RestInterface {
        @POST("/client")
        public void getClientId(@Header("Authorization") String authorization, retrofit.Callback<ClientResponse> callback);

        @GET("/placement/{id}")
        public void setPlacementId(@Header("Authorization") String authorization, @Path("id") int id, retrofit.Callback<PlacementResponse> callback);
    }

    public interface Callback<T> {
        public void onSuccess(T t);
        public void onFailure(FeedFMError error);
    }
}
