package com.feedfm.android.playersdk.mocks;

import com.feedfm.android.playersdk.service.webservice.Webservice;
import com.feedfm.android.playersdk.service.webservice.model.ClientResponse;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlacementResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlayResponse;
import com.feedfm.android.playersdk.service.webservice.model.PlayStartResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.http.Field;
import retrofit.http.Header;
import retrofit.http.Path;

/**
 * Created by mharkins on 8/27/14.
 */
public class StubRestService implements Webservice.RestInterface {
    public ClientResponse mClientResponseMock;
    public PlacementResponse mPlacementResponseMock;
    public PlayResponse mPlayResponseMock;
    public PlayStartResponse mPlayStartResponseMock;
    public FeedFMResponse mFeedFMResponseMock;

    public FeedFMError mErrorMock = makeFeedFmError();

    public RetrofitError mRetrofitError;

    public static FeedFMError makeFeedFmError() {
        return new FeedFMError(4, "This is a message", 5);
    }

    @Override
    public ClientResponse getClientId(@Header("Authorization") String authorization) {
        return mClientResponseMock;
    }

    @Override
    public PlacementResponse setPlacementId(@Header("Authorization") String authorization, @Path("id") int id) {
        return mPlacementResponseMock;
    }

    @Override
    public PlayResponse getPlay(@Header("Authorization") String authorization, @Field("client_id") String clientId, @Field("placement_id") Integer placementId, @Field("station_id") Integer stationId, @Field("formats") String formats, @Field("max_bitrate") Integer maxBitrate) {
        return mPlayResponseMock;
    }

    @Override
    public PlayStartResponse playStarted(@Header("Authorization") String authorization, @Path("id") String playId) {
        return mPlayStartResponseMock;
    }

    @Override
    public FeedFMResponse skip(@Header("Authorization") String authorization, @Path("id") String playId) {
        return mFeedFMResponseMock;
    }

    @Override
    public FeedFMResponse playCompleted(@Header("Authorization") String authorization, @Path("id") String playId) {
        return mFeedFMResponseMock;
    }

    @Override
    public FeedFMResponse like(@Header("Authorization") String authorization, @Path("id") String playId) {
        return mFeedFMResponseMock;
    }

    @Override
    public FeedFMResponse unlike(@Header("Authorization") String authorization, @Path("id") String playId) {
        return mFeedFMResponseMock;
    }

    @Override
    public FeedFMResponse dislike(@Header("Authorization") String authorization, @Path("id") String playId) {
        return mFeedFMResponseMock;
    }
}
