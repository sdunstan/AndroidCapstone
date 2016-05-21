package com.twominuteplays.camera;

import android.Manifest;
import android.util.SparseIntArray;
import android.view.Surface;

public class CameraConstants {

    public static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    public static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    public static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    public static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    public static final int REQUEST_VIDEO_PERMISSIONS = 1;
    public static final String FRAGMENT_DIALOG = "dialog";

    public static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

}
