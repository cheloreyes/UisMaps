package com.proyecto.uis.uismaps;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * Esta clase registra UISMaps en GoogleAnalytics para tener la información del número de personas que la utilizan.
 * Created by CheloReyes on 1/05/15.
 */
public class MyApp extends Application {
    private static final String PROPERTY_ID = "UA-62503510-1";

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
    }
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            //Tracker t = analytics.newTracker(PROPERTY_ID);
            if( trackerId == TrackerName.APP_TRACKER )
            {
                Log.v("Analytics", "Registra analytics");
                Tracker tracker = analytics.newTracker(PROPERTY_ID);
                tracker.enableExceptionReporting(true);
                mTrackers.put(trackerId, tracker);
            }
        }
        return mTrackers.get(trackerId);
    }
}
