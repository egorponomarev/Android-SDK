package com.feedfm.api;

/**
 * Created by Thomas on 9/16/13.
 */
public enum FMError {

    FMErrorCodeRequestFailed(-4),
    FMErrorCodeUnexpectedReturnType(-1),
    FMErrorCodeInvalidCredentials(5),
    FMErrorCodeAccessForbidden(6),
    FMErrorCodeSkipLimitExceeded(7),
    FMErrorCodeNoAvailableMusic(9),
    FMErrorCodeInvalidSkip(12),
    FMErrorCodeInvalidParameter(15),
    FMErrorCodeMissingParameter(16),
    FMErrorCodeNoSuchResource(17),
    FMErrorCodeInternal(18),
    FMErrorCodeGeoBlocked(19);


    FMError(int i) {
    }


}
