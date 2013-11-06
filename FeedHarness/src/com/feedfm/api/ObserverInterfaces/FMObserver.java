package com.feedfm.api.ObserverInterfaces;

/**
 * Created by Thomas on 9/24/13.
 */
public interface FMObserver {



    public void postFMSessionNextItemAvailableNotification();

    public void postFMSessionCurrentItemDidChangeNotification();

    public void postFMSessionActivePlacementDidChangeNotification();

    public void postFMSessionActiveStationiDidChangeNotification();









}
