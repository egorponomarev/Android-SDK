package com.feedfm.api;

import android.content.Context;
import android.util.Log;

import com.feedfm.api.ObserverInterfaces.FMObserver;
import com.feedfm.api.Requests.FMAPIRequest;
import com.feedfm.api.Requests.IFMAPIRequest;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Thomas on 9/24/13.
 */
public class FMSession {



    public static String TAG = "FMSession";
    
    public static String baseURL = "http://feed.fm";

    private static String session_token = null;
    private static String session_secret = null;

    private static FMSession sharedSession;


    public static String FMAuthStoragePath = "FeedMedia/";
    public static String FMAuthStorageName = "FMAuth.plist";

    public static Integer SessionDefaultBitrate = 48;

    private LinkedList<FMAPIRequest> queuedRequests;
    private LinkedList<FMAPIRequest> requestsInProgress;
    protected ArrayList<FMAudioFormat> supportedAudioFormats;

    protected ArrayList<FMObserver> observers;

    protected FMStation activeStation;
    protected String activePlacementID;
    protected Integer maxBitRate;
    protected FMAudioItem currentItem;
    protected FMAudioItem nextItem;
    protected FMAuth auth;


    public FMSession() {

        this.queuedRequests = new LinkedList<FMAPIRequest>();
        this.requestsInProgress = new LinkedList<FMAPIRequest>();

        this.supportedAudioFormats = new ArrayList<FMAudioFormat>();
        this.supportedAudioFormats.add(FMAudioFormat.AAC);
        this.supportedAudioFormats.add(FMAudioFormat.MP3);

        this.maxBitRate = SessionDefaultBitrate;
        this.auth = this.authFromDisk();

        this.observers = new ArrayList<FMObserver>();

        if(this.auth != null) {
            this.auth = new FMAuth();
        }

        //ToDo need to implement the Notification center portion of this constructor

    }

    public void addFMObserver(FMObserver observer) {


        if(this.observers == null) {

            this.observers = new ArrayList<FMObserver>();
        }

        if(observer != null) {

            this.observers.add(observer);
        }

    }

    public static FMSession sharedSession(String token, String secret) {

        if(sharedSession == null) {

            FMSession.sharedSession = new FMSession();
            FMSession.sharedSession.setClientToken(token, secret);
        }

        return FMSession.sharedSession;


    }


    public FMAuth authFromDisk() {

        //Todo implement
    	
    	return null;
    }




    public static  void setClientToken(String token, String secret) {


        if(token != null || token.length() < 1 || secret != null || secret.length() < 1 ) {

            Log.e(TAG, "FMSession must be initialized with a token and secret");
            return;
        }




        FMSession.session_token = token;
        FMSession.session_secret = secret;


    }

    public void cancelOutstandingRequests() {

        if(queuedRequests != null ) {

             this.queuedRequests.removeAll(null);
        }

        if(this.requestsInProgress != null) {
            for(FMAPIRequest request :this.requestsInProgress) {

                request.cancel();
            }
            this.requestsInProgress.removeAll(null);

        }



    }

    public FMAuth getAuth() {
        return auth;
    }

    public void setAuth(FMAuth auth) {
        this.auth = auth;
    }

    public FMStation getActiveStation() {
        return activeStation;
    }

    public void setActiveStation(FMStation activeStation) {
        this.activeStation = activeStation;
    }

    public String getActivePlacementID() {
        return activePlacementID;
    }

    public void setActivePlacementID(String activePlacementID) {
        this.activePlacementID = activePlacementID;
    }

    public ArrayList getSupportedAudioFormats() {
        return supportedAudioFormats;
    }

    public void setSupportedAudioFormats(ArrayList supportedAudioFormats) {
        this.supportedAudioFormats = supportedAudioFormats;
    }

    public Integer getMaxBitRate() {
        return maxBitRate;
    }

    public void setMaxBitRate(Integer maxBitRate) {
        this.maxBitRate = maxBitRate;
    }

    public FMAudioItem getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(FMAudioItem currentItem) {
        this.currentItem = currentItem;

        if(currentItem == null && this.currentItem == null) {
            return;
        } else {

            if(currentItem == this.currentItem) {
                return;
            }
        }

        this.currentItem = currentItem;

        if(this.observers != null) {

            for(FMObserver observer : this.observers) {
                observer.postFMSessionCurrentItemDidChangeNotification();
            }
        }

    }

    public FMAudioItem getNextItem() {
        return nextItem;
    }

    public void setNextItem(FMAudioItem nextItem) {
        if(this.nextItem == nextItem) {
            return;
        }

        this.nextItem = nextItem;
        if(this.nextItem != null) {

            Log.e(TAG, "Next Item Set calling Observers");
            if(this.observers != null) {
              for(FMObserver observer : this.observers) {
                  observer.postFMSessionCurrentItemDidChangeNotification();
              }
            }
        }
    }

    public void setPlacement(String activePlacementID) {

        if(activePlacementID == null && this.activePlacementID == null) {
            return;
        }

        if(activePlacementID.compareTo(this.activePlacementID)== 0) {
            return;
        }

        this.activePlacementID = activePlacementID;
        if(this.observers != null) {

            for(FMObserver observer : this.observers){

                observer.postFMSessionActivePlacementDidChangeNotification();
            }
        }

    }

    public void setStation(FMStation station) {

        if(this.activeStation == null && station == null) {

            return;
        }

        this.activeStation = station;

        this.cancelOutstandingRequests();
        this.currentItem = null;
        this.nextItem = null;

        if(this.observers != null) {

            for(FMObserver observer : this.observers) {

                observer.postFMSessionActiveStationiDidChangeNotification();
            }
        }


    }

   public String saveDirectory(Context ctx) {

       //Todo Implement saveDirectory

       return null;
   }

    public void timeUpdate() {
        this.updateServerTime();
    }


    public void updateServerTime() {

        FMSuccessResponse success = new FMSuccessResponse();
        FMFailureResponse failure = new FMFailureResponse();

        IFMAPIRequest timeRequest = FMAPIRequest.requestServerTime(success, failure);
        timeRequest.send();


    }


    /**
     Returns whether or not the session is ready for requests, e.g. a valid client token, secret, and placementId has been set.
     */
    public Boolean canRequestItems() {
        //Todo Implement canRequestItems

        Boolean result = Boolean.FALSE;


        return result;
    }

    /**
     Requests the next item for the current placement/station, which will populate the `nextItem` property and trigger the `FMSessionNextItemAvailableNotification` notification on success. Only has effect if `nextItem` is nil.
     */
    public void requestNextItem() {
        //Todo implment

    }

    /**
    Moves the nextItem into the currentItem position and notifies the server that the play began. If a previous item is playing, `-playCompleted` or `-requestSkip` must be called first.
    */
    public void playStarted() {
        //Todo implement


    }

/**
 Notifies the server of how much of the current item has been played for reporting purposes. Can be called periodically or specifically on events such as when playback is paused.

 @param elapsedTime The amount of time the currentItem has already been played
  */
    public void updatePlay(Double elapsedTime) {

        //Todo implement
    }

    /**
     Notifies the server that playthrough completed successfully. It nils out the currentItem in preparation for the next play to be started.
     */
    public void playCompleted() {

        //ToDo Implement

    }

    /**
     If successful, `-requestSkip` will behave like `-playCompleted` by nilling out the currentItem in preparation for the next `-playStarted` call.
     May fail if the user is out of skips, in which case the delegate will be notified and the failure block (if any) will be called.

     //@param success Optional block to be called if the server grants the skip
     //@param failure Optional block to be called if the server rejects the skip
     */
    public void requestSkip() {
        //ToDo Implement


    }

    /**
     Use only to resolve system issues, e.g. unplayable content.
     Automatically requests a new item.

     @param item The item that failed. Should be either the FMSession's currentItem or nextItem, otherwise the call will be ignored.
     */
    public void rejectItem(FMAudioItem item) {
        //ToDo Implement

    }


}
