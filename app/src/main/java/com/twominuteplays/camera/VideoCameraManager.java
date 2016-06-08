package com.twominuteplays.camera;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.twominuteplays.TwoMinutePlaysApp;
import com.twominuteplays.camera.TextureViewManager.CameraCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class VideoCameraManager implements CameraCallback {
    private static final String TAG = VideoCameraManager.class.getName();

    private CameraDevice mCameraDevice;
    private TextureViewManager mTextureViewManager;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CaptureRequest.Builder mPreviewBuilder;
    private PermissionsCallback mPermissionsCallback;
    private String mClipPath;
    private RecordingStateCallback mRecordingStateCallback;
    private Integer mSensorOrientation;
    private Size mVideoSize;
    private MediaRecorderWrapper mMediaRecorder;

    public VideoCameraManager(TextureViewManager textureViewManager) {
        this.mTextureViewManager = textureViewManager;
        mTextureViewManager.setCameraCallback(this);
    }

    /**
     * Call after cameraConfigured calls you.
     */
    public void startMediaRecorder() {
        mMediaRecorder.start();
    }

    public interface PermissionsCallback {
        boolean hasPermissions();
    }

    public interface RecordingStateCallback {
        void recordingStopped(String fileName);
        void cameraConfigured();
        void configurationFailed();
        void error(String s);
    }

    public void setPermissionsCallback(PermissionsCallback callback) {
        this.mPermissionsCallback = callback;
    }

    public void setRecordingStateCallback(RecordingStateCallback recordingStateCallback) {
        this.mRecordingStateCallback = recordingStateCallback;
    }

    public void beginPreview() {
        startBackgroundThread();
        if (mTextureViewManager.isAvailable()) {
            // This is the entry point
            openCamera(mTextureViewManager.getWidth(), mTextureViewManager.getHeight());
        }
    }

    public void pause() {
        closeCamera();
        stopBackgroundThread();
    }

//    public void stopRecordingVideo() {
//        Log.d(TAG, "Stopping recording.");
//        // Stop recording
//        try {
//            mMediaRecorder.stop();
//            if (mRecordingStateCallback != null) {
//                mRecordingStateCallback.recordingStopped(mClipPath);
//            }
//        }
//        catch (IllegalStateException e) {
//            mMediaRecorder.reset();
//            reportError("Didn't catch that. Try again.");
//        }
//    }
    public void stopRecordingVideo(boolean start) {
        Log.d(TAG, "Stopping recording.");
        // Stop recording
        try {
            mMediaRecorder.stop();
            if (mRecordingStateCallback != null) {
                mRecordingStateCallback.recordingStopped(mClipPath);
            }
        }
        finally {
            closeCamera();
            if(start)
                openCamera(mTextureViewManager.getWidth(), mTextureViewManager.getHeight());
        }
    }

    public synchronized void startRecordingVideo(String path) {
        Log.d(TAG, "Attempting to start recording.");
        if (null == mCameraDevice || !mTextureViewManager.isAvailable() || null == mTextureViewManager.getPreviewSize()) {
            Log.w(TAG, "Camera or texture not ready. Bailing out of attempt to start recording.");
            Log.d(TAG, "Camera device is null? " + (null == mCameraDevice));
            Log.d(TAG, "Texture view manager is available " + mTextureViewManager.isAvailable() + " has preview size of " + mTextureViewManager.getPreviewSize());
            throw new IllegalStateException("Camera to texture not ready.");
        }
        try {
            mClipPath = path;
            mMediaRecorder.reset();
            setUpMediaRecorder();

            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface textureViewSurface = mTextureViewManager.getSurface();
            surfaces.add(textureViewSurface);
            mPreviewBuilder.addTarget(textureViewSurface);

            // Set up Surface for the MediaRecorder
            Surface mRecorderSurface = mMediaRecorder.getSurface();
            surfaces.add(mRecorderSurface);
            mPreviewBuilder.addTarget(mRecorderSurface);

            Log.d(TAG, "Surfaces all configured, trying to start video capture session.");
            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    updatePreview(cameraCaptureSession);
                    Log.d(TAG, "Everything's ok for now, recording started.");
                    mRecordingStateCallback.cameraConfigured();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e(TAG, "Camera device capture session configuration failed.");
                    mRecordingStateCallback.configurationFailed();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Could not access camera. Reason: " + e.getReason(), e);
        } catch (IOException e) {
            Log.e(TAG, "IO Error while spinning up camera.", e);
        }
    }

    private void reportError(String message) {
        Log.e(TAG, message);
        if (mRecordingStateCallback != null) {
            mRecordingStateCallback.error(message);
        }
    }

    private void reportError(String message, Throwable t) {
        Log.e(TAG, message, t);
        reportError(message);
    }

    private void startPreview() {
        if (null == mCameraDevice || !mTextureViewManager.isAvailable() || null == mTextureViewManager.getPreviewSize()) {
            return;
        }
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Surface previewSurface = mTextureViewManager.getSurface();
            mPreviewBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    updatePreview(cameraCaptureSession);
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Log.e(TAG, "Configure camera failed.");  // TODO: now what?
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "The camera is no longer connected or has encountered a fatal error", e);
        }
    }

    private final class CameraStateCallback extends CameraDevice.StateCallback {
        // Camera State Callback Methods
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            mTextureViewManager.configureTransform();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Log.e(TAG, "Camera error " + error); // TODO: bail out of activity? Recover?
        }

        @Override
        public void onClosed(CameraDevice camera) {
            Log.i(TAG, "Camera closed.");
            stopBackgroundThread();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void updatePreview(CameraCaptureSession cameraCaptureSession) {
        if (null == mCameraDevice || cameraCaptureSession == null) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            cameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    @Override
    public void openCamera(int width, int height) {
        if (mPermissionsCallback == null || !mPermissionsCallback.hasPermissions())
            return;

        CameraManager manager = TwoMinutePlaysApp.getCameraManager();
        try {
            Log.d(TAG, "tryAcquire");
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId = CameraHelper.findFrontFacingCamera(manager);
            if (cameraId == null) {
                cameraId = CameraHelper.findFallbackCamera(manager);
            }

            if (cameraId == null) {
                reportError("Could not find a suitable camera to use.");
                return;
            }

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            mVideoSize = CameraHelper.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            mTextureViewManager.setOptimalPreviewSize(map.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);
            mTextureViewManager.setOrientation(TwoMinutePlaysApp.getOrientation(), width, height);
            mMediaRecorder = new MediaRecorderWrapper();
            manager.openCamera(cameraId, new CameraStateCallback(), null); // This is how mCameraDevice is created
        } catch (CameraAccessException e) {
            reportError("Cannot access the camera.", e);
        } catch (NullPointerException e) {
            Log.e(TAG, "Currently an NPE is thrown when the Camera2API is used but not supported on the device.", e);
            reportError("Cannot access the camera on your device.");
        } catch (InterruptedException e) {
            reportError("Cannot access the camera on your device.");
        } catch (SecurityException e) {
            reportError("Two Minute Plays does not have access the video camera.");
        }

    }


    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                try {
                    mMediaRecorder.release();
                }
                finally {
                    mMediaRecorder = null;
                }
            }
        } catch (InterruptedException e) {
            reportError("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Where the bulk of the media recorder is configured. Specific state machine rules apply.
     */
    private void setUpMediaRecorder() throws IOException {
        mMediaRecorder.setAudioVideoSource();
        mMediaRecorder.setOutputFormat();
        mMediaRecorder.configureDataSource(mVideoSize, mSensorOrientation, mClipPath);
        mMediaRecorder.prepare(); // after sources, eccoders, etc., before start()
    }

}
