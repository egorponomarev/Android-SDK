package fm.feed.android.playersdk.service.constant;

import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Feed Media, Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by mharkins on 9/15/14.
 */
public enum PlayerErrorEnum {
    // Generic Errors
    NO_NETWORK(1000, "No Internet Connection"),
    UNKNOWN(1001, "Unkown Error"),
    INVALID_CREDENTIALS(1002, "Credentials are not valid"),
    // Tuning Errors
    TUNE_UNKNOWN(1010, "Error preparing audio stream"),
    TUNE_IO_EXCEPTION(1011, "Error Tuning MediaPlayer (IOException)"),
    TUNE_MEDIA_PLAYER_ILLEGAL_STATE(1020, "Error Tuning MediaPlayer (IllegalStateException)"),

    // Retrofit Errors
    RETROFIT_UNKNOWN(1030, "Unkown Retrofit Error"),
    RETROFIT_NULL_REQ_SUCCESS(1031, "Retrofit error response is null - Request Response was positive"),
    RETROFIT_NULL_REQ_FAIL(1032, "Retrofit error response is null - Request Response was negative");

    private int code;
    private String message;

    PlayerErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static PlayerErrorEnum fromCode(int code) {
        for (PlayerErrorEnum apiError : values()) {
            if (apiError.getCode() == code) {
                return apiError;
            }
        }
        return null;
    }

    public static PlayerErrorEnum fromError(FeedFMError error) {
        if (error == null) {
            return null;
        }

        for (PlayerErrorEnum playerError : values()) {
            if (playerError.getCode() == error.getCode()) {
                return playerError;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("{type: \"Generic Error\", code: %d, message: \"%s\"}", getCode(), getMessage());
    }
}
