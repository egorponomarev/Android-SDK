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
    public void getClientId(@Header("Authorization") String authorization, Callback<ClientResponse> callback) {
        if (mClientResponseMock == null) {
            callback.failure(null);
        } else {
            callback.success(mClientResponseMock, null);
        }
    }

    @Override
    public void setPlacementId(@Header("Authorization") String authorization, @Path("id") int id, Callback<PlacementResponse> callback) {
        if (mPlacementResponseMock == null) {
            callback.failure(null);
        } else {
            callback.success(mPlacementResponseMock, null);
        }
    }

    @Override
    public void tune(String authorization,
                     String clientId,
                     Integer placementId,
                     String stationId,
                     String audioFormats,
                     Integer maxBitrate,
                     retrofit.Callback<PlayResponse> callback) {
        if (mPlayResponseMock == null) {
            callback.failure(null);
        } else {
            callback.success(mPlayResponseMock, null);
        }
    }

    @Override
    public void playStarted(@Header("Authorization") String authorization, @Path("id") String playId, Callback<PlayStartResponse> callback) {
        if (mPlayStartResponseMock == null) {
            callback.failure(null);
        } else {
            callback.success(mPlayStartResponseMock, null);
        }
    }

    @Override
    public void skip(@Header("Authorization") String authorization, @Path("id") String playId, Callback<FeedFMResponse> callback) {
        if (mFeedFMResponseMock == null) {
            callback.failure(null);
        } else {
            callback.success(mFeedFMResponseMock, null);
        }
    }

    @Override
    public void playCompleted(@Header("Authorization") String authorization, @Path("id") String playId, Callback<FeedFMResponse> callback) {
        if (mFeedFMResponseMock == null) {
            callback.failure(null);
        } else {
            callback.success(mFeedFMResponseMock, null);
        }
    }

    @Override
    public void like(@Header("Authorization") String authorization, @Path("id") String playId, Callback<FeedFMResponse> callback) {
        if (mFeedFMResponseMock == null) {
            callback.failure(null);
        } else {
            callback.success(mFeedFMResponseMock, null);
        }
    }

    @Override
    public void unlike(@Header("Authorization") String authorization, @Path("id") String playId, Callback<FeedFMResponse> callback) {
        if (mFeedFMResponseMock == null) {
            callback.failure(null);
        } else {
            callback.success(mFeedFMResponseMock, null);
        }
    }

    @Override
    public void dislike(@Header("Authorization") String authorization, @Path("id") String playId, Callback<FeedFMResponse> callback) {
        if (mFeedFMResponseMock == null) {
            callback.failure(null);
        } else {
            callback.success(mFeedFMResponseMock, null);
        }
    }
}
