package com.feedfm.android.playersdk.service.webservice;

import com.feedfm.android.playersdk.service.webservice.model.FeedFMError;
import com.feedfm.android.playersdk.service.webservice.model.FeedFMResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by mharkins on 8/29/14.
 */
public abstract class DefaultRetrofitCallback <T extends FeedFMResponse, R> implements Callback <T> {
    private Webservice.Callback mServiceCallback;

    protected DefaultRetrofitCallback(Webservice.Callback serviceCallback) {
        this.mServiceCallback = serviceCallback;
    }

    public abstract R parseResponse(T response);

    @Override
    public void success(T feedFMResponse, Response response) {
        if (feedFMResponse.isSuccess()) {
            mServiceCallback.onSuccess(parseResponse(feedFMResponse));
        } else {
            FeedFMError feedFMError = feedFMResponse.getError();
            if (feedFMError == null) {
                feedFMError = new FeedFMError(-1, "Retrofit error response is null - Request Response was positive", -1);
            }
            mServiceCallback.onFailure(feedFMError);
        }
    }

    @Override
    public void failure(RetrofitError error) {
        FeedFMResponse feedFMResponse = (FeedFMResponse) error.getBody();
        FeedFMError feedFMError = feedFMResponse.getError();
        if (feedFMError == null) {
            feedFMError = new FeedFMError(-1, "Retrofit error response is null - Request Response was negative", -1);
        }
        mServiceCallback.onFailure(feedFMError);
    }
}
