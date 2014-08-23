package com.feedfm.android.playersdk.service;

/**
 * Created by mharkins on 8/22/14.
 */
public class Wrap <T>{
    private T mObject;

    public Wrap(T mObject) {
        this.mObject = mObject;
    }

    public T getObject() {
        return mObject;
    }
}
