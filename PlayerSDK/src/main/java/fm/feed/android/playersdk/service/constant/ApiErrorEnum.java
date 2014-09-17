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
public enum ApiErrorEnum {
    INVALID_CREDENTIALS(5, "Invalid credentials. Credentials are missing or invalid.", 401),
    FORBIDDEN(6, "Forbidden. Access forbidden to the requested resource.", 401),
    SKIP_LIMIT_REACHED(7, "User has reached their skip limit and may not skip this song.", 200),
    END_OF_PLAYLIST(9, "End of available music. There is no more music that can be played from this station.", 200),
    CANT_SKIP_NO_PLAY(12, "This play is not currently being played so we can't determine skippability.", 200),
    INVALID_PARAMETER(15, "Invalid parameter value. See error message for details.", 400),
    MISSING_PARAMETER(16, "400	Missing required parameter. See error message for details.", 400),
    NO_SUCH_OBJECT(17, "No such object. The requested resource couldn't be found.", 404),
    UNHANDLED_INTERNAL_ERROR(18, "Unhandled internal error.", 500),
    NOT_IN_US(19, "The client's IP address does not map to one in the United States, and so cannot play music.", 403),
    PLAYBACK_ALREADY_STARTED(20, "Playback of this song has already started.", 403);

    private int code;
    private String message;
    private int status;

    ApiErrorEnum(int code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
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

    public static ApiErrorEnum fromCode(int code) {
        for (ApiErrorEnum apiError : values()) {
            if (apiError.getCode() == code) {
                return apiError;
            }
        }
        return null;
    }

    public static ApiErrorEnum fromError(FeedFMError error) {
        if (error == null) {
            return null;
        }

        for (ApiErrorEnum apiError : values()) {
            if (apiError.getCode() == error.getCode()) {
                return apiError;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("{type: \"Api Error\", code: %d, message: \"%s\", status: %s}", getCode(), getMessage(), getStatus());
    }
}
