package fm.feed.android.playersdk.service.webservice;

import fm.feed.android.playersdk.service.constant.PlayerErrorEnum;
import fm.feed.android.playersdk.service.webservice.model.FeedFMError;
import fm.feed.android.playersdk.service.webservice.model.FeedFMResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by mharkins on 8/29/14.
 */
public abstract class DefaultRetrofitCallback<T extends FeedFMResponse, R> implements Callback<T> {
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
                feedFMError = new FeedFMError(PlayerErrorEnum.RETROFIT_NULL_REQ_SUCCESS);
            }
            mServiceCallback.onFailure(feedFMError);
        }
    }

    @Override
    public void failure(RetrofitError error) {
        FeedFMResponse feedFMResponse = (FeedFMResponse) error.getBody();
        FeedFMError feedFMError = feedFMResponse.getError();
        if (feedFMError == null) {
            feedFMError = new FeedFMError(PlayerErrorEnum.RETROFIT_NULL_REQ_FAIL);
        }
        mServiceCallback.onFailure(feedFMError);
    }
}
