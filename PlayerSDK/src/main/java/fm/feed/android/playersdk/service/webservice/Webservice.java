package fm.feed.android.playersdk.service.webservice;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.Executors;

import fm.feed.android.playersdk.R;
import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.webservice.adapter.FeedFMErrorDeserializer;
import fm.feed.android.playersdk.service.webservice.model.AudioFormat;
import fm.feed.android.playersdk.service.webservice.model.ClientResponse;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;
import fm.feed.android.playersdk.service.webservice.model.FeedFMResponse;
import fm.feed.android.playersdk.service.webservice.model.FeedFMUnkownRetrofitError;
import fm.feed.android.playersdk.service.webservice.model.PlacementResponse;
import fm.feed.android.playersdk.service.webservice.model.PlayResponse;
import fm.feed.android.playersdk.service.webservice.model.PlayStartResponse;
import fm.feed.android.playersdk.service.webservice.util.WebserviceUtils;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * Created by mharkins on 8/22/14.
 */
public class Webservice {
    private static final String TAG = Webservice.class.getSimpleName();

    private Credentials mCredentials;

    protected RestInterface mRestService;

    public Webservice(Context context) {
        String apiVersion = context.getString(R.string.api_version);
        String apiUrl = context.getString(R.string.api_url);

        OkHttpClient okHttpClient = new OkHttpClient();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(FeedFMError.class, new FeedFMErrorDeserializer()).create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setClient(new OkClient(okHttpClient))
                .setExecutors(Executors.newSingleThreadExecutor(), null)
                .setEndpoint(apiUrl + apiVersion)

                .setConverter(new GsonConverter(gson))
                .build();


        mRestService = restAdapter.create(RestInterface.class);
    }

    public void setCredentials(Credentials credentials) {
        mCredentials = credentials;
    }

    protected String getAuthStr() {
        return WebserviceUtils.getAuth(mCredentials);
    }

    public String getClientId() throws FeedFMError {
        RequestWrapper<ClientResponse, String> r = new RequestWrapper<ClientResponse, String>() {
            @Override
            public ClientResponse execute() throws RetrofitError {
                return mRestService.getClientId(
                        getAuthStr());
            }
        };
        return r.get();
    }

    public Placement getPlacementInfo() throws FeedFMError {
        RequestWrapper<PlacementResponse, Placement> r = new RequestWrapper<PlacementResponse, Placement>() {
            @Override
            public PlacementResponse execute() throws RetrofitError {
                return mRestService.getPlacementInfo(
                        getAuthStr());
            }
        };
        return r.get();
    }

    public Placement setPlacementId(final int placementId) throws FeedFMError {
        RequestWrapper<PlacementResponse, Placement> r = new RequestWrapper<PlacementResponse, Placement>() {
            @Override
            public PlacementResponse execute() throws RetrofitError {
                return mRestService.setPlacementId(
                        getAuthStr(),
                        placementId);
            }
        };
        return r.get();
    }

    public Play getPlay(final String clientId,
                        final Placement placement,
                        final Station station,
                        final AudioFormat[] audioFormats,
                        final Integer maxBitrate) throws FeedFMError {
        RequestWrapper<PlayResponse, Play> r = new RequestWrapper<PlayResponse, Play>() {
            @Override
            public PlayResponse execute() throws RetrofitError {
                String audioFormatStr = WebserviceUtils.getAudioFormatStr(audioFormats);
                Log.d(TAG, "Audio Format Str: " + audioFormatStr);

                Integer placementId = placement == null ? null : placement.getId();
                Integer stationId = station == null ? null : station.getId();

                return mRestService.getPlay(getAuthStr(),
                        clientId,
                        placementId,
                        stationId,
                        audioFormatStr,
                        maxBitrate);
            }
        };
        return r.get();
    }

    public Boolean playStarted(final String playId) throws FeedFMError {
        RequestWrapper<PlayStartResponse, Boolean> r = new RequestWrapper<PlayStartResponse, Boolean>() {
            @Override
            public PlayStartResponse execute() throws RetrofitError {
                return mRestService.playStarted(getAuthStr(), playId);
            }
        };
        return r.get();
    }

    public Boolean playCompleted(final String playId) throws FeedFMError {
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.playCompleted(
                        getAuthStr(), playId);
            }
        };
        return r.get();
    }

    public Boolean skip(final String playId, final Integer elapsed, boolean forcing) throws FeedFMError {
        final int force = forcing ? 1 : 0;
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.skip(
                        getAuthStr(), playId, elapsed, force);
            }
        };
        return r.get();
    }

    public Boolean invalidate(final String playId) throws FeedFMError {
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.invalidate(
                        getAuthStr(), playId);
            }
        };
        return r.get();
    }

    public Boolean elapsed(final String playId, final Integer elapsed) throws FeedFMError {
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.elapsed(
                        getAuthStr(), playId, elapsed);
            }
        };
        return r.get();
    }

    public Boolean like(final String playId) throws FeedFMError {
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.like(
                        getAuthStr(), playId);
            }
        };
        return r.get();
    }

    public Boolean unlike(final String playId) throws FeedFMError {
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.unlike(
                        getAuthStr(), playId);
            }
        };
        return r.get();
    }

    public Boolean dislike(final String playId) throws FeedFMError {
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.dislike(
                        getAuthStr(), playId);
            }
        };
        return r.get();
    }

    public interface RestInterface {
        @POST("/client")
        public ClientResponse getClientId(@Header("Authorization") String authorization);

        @GET("/placement")
        public PlacementResponse getPlacementInfo(@Header("Authorization") String authorization);

        @GET("/placement/{id}")
        public PlacementResponse setPlacementId(@Header("Authorization") String authorization,
                                                @Path("id") int id);

        @FormUrlEncoded
        @POST("/play")
        public PlayResponse getPlay(@Header("Authorization") String authorization,
                                    @Field("client_id") String clientId,
                                    @Field("placement_id") Integer placementId,
                                    @Field("station_id") Integer stationId,
                                    @Field("formats") String formats,
                                    @Field("max_bitrate") Integer maxBitrate);

        @POST("/play/{id}/start")
        public PlayStartResponse playStarted(@Header("Authorization") String authorization,
                                             @Path("id") String playId);

        @FormUrlEncoded
        @POST("/play/{id}/skip")
        public FeedFMResponse skip(@Header("Authorization") String authorization,
                                   @Path("id") String playId,
                                   @Field("seconds") Integer elapsed,
                                   @Field("force") Integer force);

        @FormUrlEncoded
        @POST("/play/{id}/invalidate")
        public FeedFMResponse invalidate(@Header("Authorization") String authorization,
                                         @Path("id") String playId);

        @FormUrlEncoded
        @POST("/play/{id}/elapse")
        public PlayStartResponse elapsed(@Header("Authorization") String authorization,
                                         @Path("id") String playId,
                                         @Field("seconds") Integer elapsed);

        @POST("/play/{id}/complete")
        public FeedFMResponse playCompleted(@Header("Authorization") String authorization,
                                            @Path("id") String playId);

        @POST("/play/{id}/like")
        public FeedFMResponse like(@Header("Authorization") String authorization,
                                   @Path("id") String playId);

        @DELETE("/play/{id}/like")
        public FeedFMResponse unlike(@Header("Authorization") String authorization,
                                     @Path("id") String playId);

        @POST("/play/{id}/dislike")
        public FeedFMResponse dislike(@Header("Authorization") String authorization,
                                      @Path("id") String playId);
    }

    public interface Callback<T> {
        public void onSuccess(T t);

        public void onFailure(FeedFMError error);
    }

    abstract class RequestWrapper<T extends FeedFMResponse, R> {
        public R get() throws FeedFMError {
            try {
                T res = execute();
                if (res.isSuccess()) {
                    return parseResponse(res);
                } else {
                    handleError(res.getError());
                }
            } catch (RetrofitError retrofitError) {
                handleRetrofitError(retrofitError);
            }
            return null;
        }

        public R parseResponse(FeedFMResponse response) {
            return (R) response.getModel();
        }

        public abstract T execute() throws RetrofitError;

        protected void handleRetrofitError(RetrofitError retrofitError) throws FeedFMError {
            FeedFMError feedFMError = null;

//            try {
            Object body = retrofitError.getBody();
            if (body != null && body instanceof FeedFMResponse) {
                feedFMError = ((FeedFMResponse) body).getError();
            } else {
                retrofitError.printStackTrace();
            }

            handleError(feedFMError);
        }

        /**
         * Wrapper around a FeedFMError object. If {@code error} is {@code null} a generic FeedFMError will be generated.
         *
         * @param error
         *
         * @throws FeedFMError
         */
        protected void handleError(FeedFMError error) throws FeedFMError {
            if (error == null) {
                error = new FeedFMUnkownRetrofitError();
            }
            error.updateErrorType();
            throw error;
        }

    }

}
