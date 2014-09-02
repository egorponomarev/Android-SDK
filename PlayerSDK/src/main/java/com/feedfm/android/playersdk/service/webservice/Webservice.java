package com.feedfm.android.playersdk.service.webservice;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.feedfm.android.playersdk.R;
import com.feedfm.android.playersdk.model.Placement;
import com.feedfm.android.playersdk.model.Play;
import com.feedfm.android.playersdk.model.Station;
import com.feedfm.android.playersdk.service.bus.Credentials;
import com.feedfm.android.playersdk.service.webservice.model.AudioFormat;
import com.feedfm.android.playersdk.service.webservice.model.ClientResponse;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlacementResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlayResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlayStartResponse;
import com.feedfm.android.playersdk.service.webservice.util.WebserviceUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.RestAdapter;
import retrofit.android.MainThreadExecutor;
import retrofit.client.OkClient;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by mharkins on 8/22/14.
 */
public class Webservice {
    private Credentials mCredentials;

    protected RestInterface mRestService;
    private ExecutorService mExecutorService;

    public Webservice(Context context) {
        String apiVersion = context.getString(R.string.api_version);
        String apiUrl = context.getString(R.string.api_url);

        OkHttpClient okHttpClient = new OkHttpClient();

        mExecutorService = Executors.newSingleThreadExecutor();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new OkClient(okHttpClient))
                .setExecutors(mExecutorService, null)
                .setEndpoint(apiUrl + apiVersion)
                .build();

        mRestService = restAdapter.create(RestInterface.class);
    }

    public void setCredentials(Credentials credentials) {
        mCredentials = credentials;
    }

    public void getClientId(Webservice.Callback<String> callback) {
        mRestService.getClientId(
                WebserviceUtils.getAuth(mCredentials),
                new DefaultRetrofitCallback<ClientResponse, String>(callback) {
                    public String parseResponse(ClientResponse response) {
                        return response.getClientId();
                    }
                });
    }

    public void setPlacementId(int placementId,
                               Webservice.Callback<Pair<Placement, List<Station>>> serviceCallback) {
        mRestService.setPlacementId(
                WebserviceUtils.getAuth(mCredentials),
                placementId,
                new DefaultRetrofitCallback<PlacementResponse, Pair<Placement, List<Station>>>(serviceCallback) {
                    @Override
                    public Pair<Placement, List<Station>> parseResponse(PlacementResponse response) {
                        return new Pair(response.getPlacement(), response.getStations());
                    }
                });
    }

    public void tune(String clientId,
                     Placement placement,
                     Station station,
                     AudioFormat[] audioFormats,
                     Integer maxBitrate,
                     Webservice.Callback<Play> serviceCallback) {
        String audioFormatStr = WebserviceUtils.getAudioFormatStr(audioFormats);
        Integer placementId = placement == null ? null : placement.getId();
        Integer stationId = station == null ? null : station.getId();

        mRestService.tune(WebserviceUtils.getAuth(mCredentials),
                clientId,
                placementId,
                stationId,
                audioFormatStr,
                maxBitrate,
                new DefaultRetrofitCallback<PlayResponse, Play>(serviceCallback) {
                    @Override
                    public Play parseResponse(PlayResponse response) {
                        return response.getPlay();
                    }
                });
    }

    public void playStarted(String playId, Callback<Boolean> serviceCallback) {
        mRestService.playStarted(
                WebserviceUtils.getAuth(mCredentials), playId, new DefaultRetrofitCallback<PlayStartResponse, Boolean>(serviceCallback) {
                    @Override
                    public Boolean parseResponse(PlayStartResponse response) {
                        return response.canSkip();
                    }
                });
    }

    public void playCompleted(String playId, Callback<Boolean> serviceCallback) {
        mRestService.playCompleted(
                WebserviceUtils.getAuth(mCredentials), playId, new DefaultRetrofitCallback<FeedFMResponse, Boolean>(serviceCallback) {
                    @Override
                    public Boolean parseResponse(FeedFMResponse response) {
                        return true;
                    }
                });
    }

    public void skip(String playId, Callback<Boolean> serviceCallback) {
        mRestService.skip(
                WebserviceUtils.getAuth(mCredentials), playId, new DefaultRetrofitCallback<FeedFMResponse, Boolean>(serviceCallback) {
                    @Override
                    public Boolean parseResponse(FeedFMResponse response) {
                        return true;
                    }
                });
    }

    public void like(String playId, Callback<Boolean> serviceCallback) {
        mRestService.like(
                WebserviceUtils.getAuth(mCredentials), playId, new DefaultRetrofitCallback<FeedFMResponse, Boolean>(serviceCallback) {
                    @Override
                    public Boolean parseResponse(FeedFMResponse response) {
                        return true;
                    }
                });
    }

    public void unlike(String playId, Callback<Boolean> serviceCallback) {
        mRestService.unlike(
                WebserviceUtils.getAuth(mCredentials), playId, new DefaultRetrofitCallback<FeedFMResponse, Boolean>(serviceCallback) {
                    @Override
                    public Boolean parseResponse(FeedFMResponse response) {
                        return true;
                    }
                });
    }

    public void dislike(String playId, Callback<Boolean> serviceCallback) {
        mRestService.dislike(
                WebserviceUtils.getAuth(mCredentials), playId, new DefaultRetrofitCallback<FeedFMResponse, Boolean>(serviceCallback) {
                    @Override
                    public Boolean parseResponse(FeedFMResponse response) {
                        return true;
                    }
                });
    }

    public interface RestInterface {
        @POST("/client")
        public void getClientId(@Header("Authorization") String authorization,
                                retrofit.Callback<ClientResponse> callback);

        @GET("/placement/{id}")
        public void setPlacementId(@Header("Authorization") String authorization,
                                   @Path("id") int id,
                                   retrofit.Callback<PlacementResponse> callback);

        @FormUrlEncoded
        @POST("/play")
        public void tune(@Header("Authorization") String authorization,
                         @Field("client_id") String clientId,
                         @Field("placement_id") Integer placementId,
                         @Field("station_id") Integer stationId,
                         @Field("formats") String formats,
                         @Field("max_bitrate") Integer maxBitrate,
                         retrofit.Callback<PlayResponse> callback);

        @POST("/play/{id}/start")
        public void playStarted(@Header("Authorization") String authorization,
                                @Path("id") String playId,
                                retrofit.Callback<PlayStartResponse> callback);

        @POST("/play/{id}/skip")
        public void skip(@Header("Authorization") String authorization,
                         @Path("id") String playId,
                         retrofit.Callback<FeedFMResponse> callback);

        @POST("/play/{id}/complete")
        public void playCompleted(@Header("Authorization") String authorization,
                                  @Path("id") String playId,
                                  retrofit.Callback<FeedFMResponse> callback);

        @POST("/play/{id}/like")
        public void like(@Header("Authorization") String authorization,
                         @Path("id") String playId,
                         retrofit.Callback<FeedFMResponse> callback);

        @DELETE("/play/{id}/like")
        public void unlike(@Header("Authorization") String authorization,
                           @Path("id") String playId,
                           retrofit.Callback<FeedFMResponse> callback);

        @POST("/play/{id}/dislike")
        public void dislike(@Header("Authorization") String authorization,
                            @Path("id") String playId,
                            retrofit.Callback<FeedFMResponse> callback);
    }

    public interface Callback<T> {
        public void onSuccess(T t);

        public void onFailure(FeedFMError error);
    }
}
