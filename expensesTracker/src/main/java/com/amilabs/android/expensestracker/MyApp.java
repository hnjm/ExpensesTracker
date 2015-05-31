package com.amilabs.android.expensestracker;

import com.amilabs.android.expensestracker.database.DatabaseHandler;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.acra.*;
import org.acra.annotation.*;

import android.app.Application;

import java.util.HashMap;

@ReportsCrashes(formKey = "", // not used
                mailTo = "amilien.labs@gmail.com",
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.crash_toast_text)
public class MyApp extends Application {

    private static final String PROPERTY_ID = "UA-50430786-1";
    private HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    @Override
    public void onCreate() {
        ACRA.init(this); 
        ACRA.getErrorReporter().checkReportsOnApplicationStart();
        DatabaseHandler.getInstance(this);
        super.onCreate();
    }
    
    synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
}