package fm.feed.android.playersdk.service.webservice.model;

import com.google.gson.annotations.SerializedName;

import fm.feed.android.playersdk.service.constant.ApiErrorEnum;
import fm.feed.android.playersdk.service.constant.PlayerErrorEnum;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by mharkins on 8/21/14.
 */
public class FeedFMError extends Throwable {

    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;
    @SerializedName("status")
    private int status;

    private PlayerErrorEnum mPlayerError;
    private ApiErrorEnum mApiError;

    private static enum Type {
        Player,
        Api
    }

    private Type type;

    public FeedFMError(int code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;

        setPlayerError(code);
        if (mPlayerError != null) {
            this.type = Type.Player;
        }

        setApiError(code);
        if (mApiError != null) {
            this.type = Type.Api;
        }
    }

    public FeedFMError(ApiErrorEnum errorEnum) {
        this.mApiError = errorEnum;

        this.code = mApiError.getCode();
        this.message = mApiError.getMessage();
        this.status = mApiError.getStatus();

        this.type = Type.Api;
    }

    public FeedFMError(PlayerErrorEnum errorEnum) {
        this.mPlayerError = errorEnum;

        this.code = mPlayerError.getCode();
        this.message = mPlayerError.getMessage();
        this.status = -1;

        this.type = Type.Player;
    }

    public void updateErrorType() {
        setPlayerError(code);
        setApiError(code);
    }

    private void setPlayerError(int code) {
        mPlayerError = PlayerErrorEnum.fromCode(code);
    }

    private void setApiError(int code) {
        mApiError = ApiErrorEnum.fromCode(code);
    }


    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public PlayerErrorEnum getPlayerError() {
        return mPlayerError;
    }

    public ApiErrorEnum getApiError() {
        return mApiError;
    }

    public boolean isPlayerError() {
        if (mPlayerError != null) {
            return true;
        }
        setPlayerError(code);
        return mPlayerError != null;
    }

    public boolean isApiError() {
        if (mApiError != null) {
            return true;
        }
        setApiError(code);
        return mApiError != null;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("{type: \"%s Error\", code: %d, message: \"%s\", status: %s}", getType(), getCode(), getMessage(), getStatus());
    }
}
