package com.feedfm.android.playersdk.service.webservice;

import android.content.Context;
import android.util.Pair;

import com.feedfm.android.playersdk.R;
import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Play;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.webservice.model.ClientResponse;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlacementResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlayResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlayStartResponse;
import com.feedfm.android.playersdk.service.webservice.util.WebserviceUtils;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by mharkins on 8/22/14.
 */
public class Webservice {
    public static final String EVENT_CLIENT_ID_RECEIVED = "com.feedfm.android.playersdk.event.clientid";
    public static final String EXTRA_CLIENT_ID = "com.feedfm.android.playersdk.extra.clientid";


    protected RestInterface mRestService;

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

    // TODO: add formats and max_bitrate
    public void tune(String clientId, Integer placementId, String stationId, final Webservice.Callback<Play> callback) {
        mRestService.tune(WebserviceUtils.getAuthorization(mCredentials), clientId, new retrofit.Callback<PlayResponse>() {
            @Override
            public void success(PlayResponse playResponse, Response response) {
                if (playResponse.isSuccess()) {
                    callback.onSuccess(playResponse.getPlay());
                } else {
                    callback.onFailure(playResponse.getError());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                FeedFMResponse feedFMResponse = (FeedFMResponse) error.getBody();
                callback.onFailure(feedFMResponse.getError());
            }
        });
    }

    public void playStarted(String playId, final Callback<Boolean> callback) {
        mRestService.playStarted(WebserviceUtils.getAuthorization(mCredentials), playId, new retrofit.Callback<PlayStartResponse>() {
            @Override
            public void success(PlayStartResponse playStartResponse, Response response) {
                if (playStartResponse.isSuccess()) {
                    callback.onSuccess(playStartResponse.canSkip());
                } else {
                    callback.onFailure(playStartResponse.getError());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                FeedFMResponse feedFMResponse = (FeedFMResponse) error.getBody();
                callback.onFailure(feedFMResponse.getError());
            }
        });
    }

    public void playCompleted(String playId, final Callback<Boolean> callback) {
        mRestService.playCompleted(WebserviceUtils.getAuthorization(mCredentials), playId, new retrofit.Callback<FeedFMResponse>() {
            @Override
            public void success(FeedFMResponse feedFMResponse, Response response) {
                if (feedFMResponse.isSuccess()) {
                    callback.onSuccess(true);
                } else {
                    callback.onFailure(feedFMResponse.getError());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                FeedFMResponse feedFMResponse = (FeedFMResponse) error.getBody();
                callback.onFailure(feedFMResponse.getError());
            }
        });
    }

    public void skip(String playId, final Callback<Boolean> callback) {
        mRestService.skip(WebserviceUtils.getAuthorization(mCredentials), playId, new retrofit.Callback<FeedFMResponse>() {
            @Override
            public void success(FeedFMResponse feedFMResponse, Response response) {
                if (feedFMResponse.isSuccess()) {
                    callback.onSuccess(true);
                } else {
                    callback.onFailure(feedFMResponse.getError());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (error != null) {
                    FeedFMResponse feedFMResponse = (FeedFMResponse) error.getBody();
                    callback.onFailure(feedFMResponse.getError());
                } else {
                    callback.onFailure(null);
                }
            }
        });
    }

    public interface RestInterface {
        @POST("/client")
        public void getClientId(@Header("Authorization") String authorization, retrofit.Callback<ClientResponse> callback);

        @GET("/placement/{id}")
        public void setPlacementId(@Header("Authorization") String authorization, @Path("id") int id, retrofit.Callback<PlacementResponse> callback);

        @FormUrlEncoded
        @POST("/play")
        public void tune(@Header("Authorization") String authorization, @Field("client_id") String clientId, retrofit.Callback<PlayResponse> callback);

        @POST("/play/{id}/start")
        public void playStarted(@Header("Authorization") String authorization, @Path("id") String playId, retrofit.Callback<PlayStartResponse> callback);

        @POST("/play/{id}/skip")
        public void skip(@Header("Authorization") String authorization, @Path("id") String playId, retrofit.Callback<FeedFMResponse> callback);

        @POST("/play/{id}/complete")
        public void playCompleted(@Header("Authorization") String authorization, @Path("id") String playId, retrofit.Callback<FeedFMResponse> callback);
    }

    public interface Callback<T> {
        public void onSuccess(T t);
        public void onFailure(FeedFMError error);
    }
}
