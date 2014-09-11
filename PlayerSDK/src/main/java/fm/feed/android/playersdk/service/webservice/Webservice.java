package fm.feed.android.playersdk.service.webservice;

import android.content.Context;

import fm.feed.android.playersdk.R;
import fm.feed.android.playersdk.model.Placement;
import fm.feed.android.playersdk.model.Play;
import fm.feed.android.playersdk.model.Station;
import fm.feed.android.playersdk.service.bus.Credentials;
import fm.feed.android.playersdk.service.webservice.model.AudioFormat;
import fm.feed.android.playersdk.service.webservice.model.ClientResponse;
import fm.feed.android.playersdk.service.webservice.model.FeedFMResponse;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;
import fm.feed.android.playersdk.service.webservice.model.PlacementResponse;
import fm.feed.android.playersdk.service.webservice.model.PlayResponse;
import fm.feed.android.playersdk.service.webservice.model.PlayStartResponse;
import fm.feed.android.playersdk.service.webservice.util.WebserviceUtils;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
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
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setClient(new OkClient(okHttpClient))
                .setExecutors(mExecutorService, null)
                .setEndpoint(apiUrl + apiVersion)
                .build();

        mRestService = restAdapter.create(RestInterface.class);
    }

    public void setCredentials(Credentials credentials) {
        mCredentials = credentials;
    }

    public String getClientId() throws FeedFMError {
        RequestWrapper<ClientResponse, String> r = new RequestWrapper<ClientResponse, String>() {
            @Override
            public ClientResponse execute() throws RetrofitError {
                return mRestService.getClientId(
                        WebserviceUtils.getAuth(mCredentials));
            }
        };
        return r.get();
    }

    public Placement getPlacementInfo() throws FeedFMError {
        RequestWrapper<PlacementResponse, Placement> r = new RequestWrapper<PlacementResponse, Placement>() {
            @Override
            public PlacementResponse execute() throws RetrofitError {
                return mRestService.getPlacementInfo(
                        WebserviceUtils.getAuth(mCredentials));
            }
        };
        return r.get();
    }

    public Placement setPlacementId(final int placementId) throws FeedFMError {
        RequestWrapper<PlacementResponse, Placement> r = new RequestWrapper<PlacementResponse, Placement>() {
            @Override
            public PlacementResponse execute() throws RetrofitError {
                return mRestService.setPlacementId(
                        WebserviceUtils.getAuth(mCredentials),
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
                Integer placementId = placement == null ? null : placement.getId();
                Integer stationId = station == null ? null : station.getId();

                return mRestService.getPlay(WebserviceUtils.getAuth(mCredentials),
                        clientId,
                        placementId,
                        stationId,
                        audioFormatStr,
                        192);
            }
        };
        return r.get();
    }

    public Boolean playStarted(final String playId) throws FeedFMError {
        RequestWrapper<PlayStartResponse, Boolean> r = new RequestWrapper<PlayStartResponse, Boolean>() {
            @Override
            public PlayStartResponse execute() throws RetrofitError {
                return mRestService.playStarted(WebserviceUtils.getAuth(mCredentials), playId);
            }
        };
        return r.get();
    }

    public Boolean playCompleted(final String playId) throws FeedFMError {
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.playCompleted(
                        WebserviceUtils.getAuth(mCredentials), playId);
            }
        };
        return r.get();
    }

    public Boolean skip(final String playId, boolean forcing) throws FeedFMError {
        final int force = forcing ? 1 : 0;
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.skip(
                        WebserviceUtils.getAuth(mCredentials), playId, force);
            }
        };
        return r.get();
    }

    public Boolean like(final String playId) throws FeedFMError {
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.like(
                        WebserviceUtils.getAuth(mCredentials), playId);
            }
        };
        return r.get();
    }

    public Boolean unlike(final String playId) throws FeedFMError {
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.unlike(
                        WebserviceUtils.getAuth(mCredentials), playId);
            }
        };
        return r.get();
    }

    public Boolean dislike(final String playId) throws FeedFMError {
        RequestWrapper<FeedFMResponse, Boolean> r = new RequestWrapper<FeedFMResponse, Boolean>() {
            @Override
            public FeedFMResponse execute() throws RetrofitError {
                return mRestService.dislike(
                        WebserviceUtils.getAuth(mCredentials), playId);
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
                                   @Field("force") Integer force);

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

        private void handleRetrofitError(RetrofitError retrofitError) throws FeedFMError {
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
         * @throws FeedFMError
         */
        private void handleError(FeedFMError error) throws FeedFMError {
            if (error == null) {
                error = new FeedFMError(-1, "Retrofit error response is null or can't be parsed", -1);
            }
            throw error;
        }

    }
}
