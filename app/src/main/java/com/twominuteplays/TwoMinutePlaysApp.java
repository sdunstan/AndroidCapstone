package com.twominuteplays;

import android.app.Application;

import com.firebase.client.Firebase;
import com.firebase.client.Logger;

public class TwoMinutePlaysApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setLogLevel(Logger.Level.DEBUG);
    }
}
