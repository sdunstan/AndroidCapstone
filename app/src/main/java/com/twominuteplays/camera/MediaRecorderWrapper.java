package com.twominuteplays.camera;

import android.media.MediaRecorder;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

public class MediaRecorderWrapper {

    private static final String TAG = MediaRecorderWrapper.class.getName();

    private final MediaRecorder mMediaRecorder;
    private MediaRecorderState mState = MediaRecorderState.INITIAL;

    private enum MediaRecorderState {
        INITIAL,
        ERROR,
        INITIALIZED,
        DATA_SOURCE_CONFIGURED,
        PREPARED,
        RECORDING,
        RELEASED;
    }

    public MediaRecorderWrapper() {
        mMediaRecorder = new MediaRecorder();
        mState = MediaRecorderState.INITIAL;
    }

    private synchronized void logAndThrowStateException(MediaRecorderState...allowedStates) {
        StringBuilder msg = new StringBuilder("State must be in ");
        for (MediaRecorderState state : allowedStates) {
            msg.append(state.name()).append(" ");
        }
        msg.append(". But found ").append(mState.name());
        Log.e(TAG, msg.toString());
        throw new IllegalStateException(msg.toString());
    }

    public synchronized Surface getSurface() {
        Log.d(TAG, "Get Surface");
        if (mState == MediaRecorderState.PREPARED) {
            return mMediaRecorder.getSurface();
        }
        else {
            logAndThrowStateException(MediaRecorderState.PREPARED);
        }
        return null;
    }

    public synchronized void setAudioVideoSource() {
        Log.d(TAG, "Set AV Source");
        if (mState == MediaRecorderState.INITIAL || mState == MediaRecorderState.INITIALIZED) {
            try {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // before setOutputFormat, recording parameters or encoders. First.
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE); // ditto above
                mState = MediaRecorderState.INITIALIZED;
            }
            catch (Throwable t) {
                Log.e(TAG, "Error while SETTING AV sources.", t);
                mState = MediaRecorderState.ERROR;
            }
        }
        else {
            logAndThrowStateException(MediaRecorderState.INITIAL, MediaRecorderState.INITIALIZED);
        }
    }

    public synchronized void setOutputFormat() {
        Log.d(TAG, "Set output format.");
        if (mState == MediaRecorderState.INITIALIZED) {
            try {
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // after setAudio/VideoSource, before prepare
                mState = MediaRecorderState.DATA_SOURCE_CONFIGURED;
            }
            catch (Throwable t) {
                Log.e(TAG, "Error while setting output format.", t);
                mState = MediaRecorderState.ERROR;
            }
        }
        else {
            logAndThrowStateException(MediaRecorderState.INITIALIZED);
        }
    }

    public synchronized void configureDataSource(Size mVideoSize, int sensorOrientation, String outputFileName) {
        Log.d(TAG, "Configure data source.");
        if (mState == MediaRecorderState.DATA_SOURCE_CONFIGURED) {
            try {
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); // after setOutputFormat, before prepare
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // ditto above

                mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight()); // after setVideoSource, setOutFormat, before prepare
                mMediaRecorder.setVideoFrameRate(24); // after setVideoSource, setOutFormat, before prepare
                mMediaRecorder.setOutputFile(outputFileName); // after setOutputFormat, before prepare
                mMediaRecorder.setVideoEncodingBitRate(1228800); // just before prepare (LD == 350Kbps == 35840, SD == 1200 == 1228800)

                int rotation = Surface.ROTATION_0; // activity.getWindowManager().getDefaultDisplay().getRotation();
                switch (sensorOrientation) {
                    case CameraHelper.SENSOR_ORIENTATION_DEFAULT_DEGREES:
                        mMediaRecorder.setOrientationHint(CameraHelper.DEFAULT_ORIENTATIONS.get(rotation)); // before prepare
                        break;
                    case CameraHelper.SENSOR_ORIENTATION_INVERSE_DEGREES:
                        mMediaRecorder.setOrientationHint(CameraHelper.INVERSE_ORIENTATIONS.get(rotation)); // before prepare
                        break;
                }
            }
            catch (Throwable t) {
                Log.e(TAG, "Error configuring data source.", t);
                mState = MediaRecorderState.ERROR;
            }
        }
        else {
            logAndThrowStateException(MediaRecorderState.DATA_SOURCE_CONFIGURED);
        }
    }

    public synchronized void reset() {
        Log.d(TAG, "Reset");
        if (mState == MediaRecorderState.INITIAL) {
            Log.w(TAG, "Ignoring reset, state is INITIAL");
            return;
        }

        if (mState != MediaRecorderState.RELEASED) {
            try {
                mMediaRecorder.reset();
                mState = MediaRecorderState.INITIAL;
            }
            catch (Throwable t) {
                Log.e(TAG, "Error while resetting media recorder.", t);
                mState = MediaRecorderState.ERROR;
            }
        }
        else {
            logAndThrowStateException(MediaRecorderState.INITIAL, MediaRecorderState.RELEASED);
        }
    }

    public synchronized void start() {
        Log.d(TAG, "Start");
        if (mState == MediaRecorderState.PREPARED) {
            try {
                mMediaRecorder.start();
                mState = MediaRecorderState.RECORDING;
            }
            catch (Throwable t) {
                Log.e(TAG, "Error while starting media recorder.", t);
                mState = MediaRecorderState.ERROR;
            }
        }
        else {
            logAndThrowStateException(MediaRecorderState.PREPARED);
        }
    }

    public synchronized void stop() {
        Log.d(TAG, "Stop");
        if (mState == MediaRecorderState.RECORDING) {
            try {
                mMediaRecorder.stop();
                mState = MediaRecorderState.INITIAL;
            }
            catch (Throwable t) {
                Log.e(TAG, "Error while stopping media recorder.", t);
                mState = MediaRecorderState.ERROR;
            }
        }
        else {
            logAndThrowStateException(MediaRecorderState.RECORDING);
        }
    }

    public synchronized void release() {
        Log.d(TAG, "Release");
        if (mState == MediaRecorderState.INITIAL) {
            try {
                mMediaRecorder.release();
                mState = MediaRecorderState.RELEASED;
            }
            catch (Throwable t) {
                Log.e(TAG, "Error while releasing media recorder.", t);
                mState = MediaRecorderState.ERROR;
            }
        }
        else {
            logAndThrowStateException(MediaRecorderState.INITIAL);
        }
    }

    public synchronized void prepare() {
        Log.d(TAG, "Prepare");
        if (mState == MediaRecorderState.DATA_SOURCE_CONFIGURED) {
            try {
                mMediaRecorder.prepare();
                mState = MediaRecorderState.PREPARED;
            }
            catch (Throwable t) {
                Log.e(TAG, "Error while preparing media recorder.", t);
                mState = MediaRecorderState.ERROR;
            }
        }
        else {
            throw new IllegalStateException("State must be DATA_SOURCE_CONFIGURED. Is " + mState.name());
        }
    }

}
