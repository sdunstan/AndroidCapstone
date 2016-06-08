package com.twominuteplays.camera;

import android.Manifest;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

public class CameraHelper {

    private static final String TAG = CameraHelper.class.getName();

    public static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    public static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    public static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    public static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    public static final int REQUEST_VIDEO_PERMISSIONS = 1;
    public static final String FRAGMENT_DIALOG = "PERMISSIONS_DIALOG_TAG";

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

    public static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            Log.d(TAG, "Found video size " + size);
            if (size.getWidth() == 320 && size.getHeight() == 240) {
                return size;
            }
            if (size.getWidth() == 240 && size.getHeight() == 320) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size. using " + choices[choices.length - 1]);
        return choices[choices.length - 1];
    }

    public static String findFallbackCamera(CameraManager manager) throws CameraAccessException {
        for (String cameraId : manager.getCameraIdList()) {
            CameraCharacteristics characteristics
                    = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                continue;
            }
            return cameraId;
        }
        return null;
    }

    public static String findFrontFacingCamera(CameraManager manager) throws CameraAccessException {
        for (String cameraId : manager.getCameraIdList()) {
            CameraCharacteristics characteristics
                    = manager.getCameraCharacteristics(cameraId);

            // Only use front facing camera.
            if (!isFrontFacing(characteristics)) {
                continue;
            }

            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                continue;
            }

            return cameraId;
        }
        return null;
    }

    public static boolean isFrontFacing(CameraCharacteristics characteristics) {
        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        return (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT);
    }
}
