package com.feedfm.api.Requests;

import com.feedfm.api.FMFailureResponse;
import com.feedfm.api.FMSuccessResponse;

/**
 * Created by Thomas on 9/24/13.
 */
public class FMTimeRequest extends FMAPIRequest implements IFMAPIRequest {



    public FMTimeRequest(FMSuccessResponse success, FMFailureResponse failure) {

        super(success, failure);

    }



    public void send() {


    }



}
