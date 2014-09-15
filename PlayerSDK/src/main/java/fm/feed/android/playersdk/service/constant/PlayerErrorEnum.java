package fm.feed.android.playersdk.service.constant;

import fm.feed.android.playersdk.service.webservice.model.FeedFMError;

/**
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
    RETROFIT_NULL_REQ_FAIL(1032, "Retrofit error response is null - Request Response was negative"),

    ;

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
