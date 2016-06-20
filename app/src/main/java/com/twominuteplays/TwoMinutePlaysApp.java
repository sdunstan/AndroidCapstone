package com.twominuteplays;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.camera2.CameraManager;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

public class TwoMinutePlaysApp extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.INFO);

        context = this;
    }

    public static String getResourceString(int id) {
        if (context != null) {
            return context.getString(id);
        }
        else {
            return "";
        }
    }

    public static CameraManager getCameraManager() {
        return (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public static int getOrientation() {
        return context.getResources().getConfiguration().orientation;
    }

    public static ContentResolver getApplicationContentResolver() {
        return context.getContentResolver();
    }

}
