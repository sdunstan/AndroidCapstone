package com.twominuteplays.camera;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.twominuteplays.R;
import com.twominuteplays.model.Line;

public class VideoFragment extends Fragment implements FragmentCompat.OnRequestPermissionsResultCallback,
        VideoCameraManager.PermissionsCallback,
        VideoCameraManager.RecordingStateCallback {
    private static final String TAG = VideoFragment.class.getName();

    private Line currentLine = null;
    private VideoCameraManager mVideoCameraManager;
    private boolean mIsRecordingVideo;

    public VideoFragment() {
        super();
    }

    public static VideoFragment newInstance() {
        return new VideoFragment();
    }

    private final TextureViewManager mTextureViewManager = new TextureViewManager();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        AutoFitTextureView textureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mTextureViewManager.setTextureView(getActivity(), textureView);
        mVideoCameraManager = new VideoCameraManager(mTextureViewManager);
        mVideoCameraManager.setPermissionsCallback(this);
        mVideoCameraManager.setRecordingStateCallback(this);
        mIsRecordingVideo = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "Resuming Video Fragment.");
        mVideoCameraManager.beginPreview();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "Pausing Video Fragment");
        mVideoCameraManager.pause();
        super.onPause();
    }

    public void toggleRecording(Line line) {
        this.currentLine = line;

        if (mIsRecordingVideo) {
            mVideoCameraManager.stopRecordingVideo(true);
        } else {
            try {
                mVideoCameraManager.startRecordingVideo(getVideoFilePath());
                mIsRecordingVideo = true;
            }
            catch(IllegalStateException e) {
                error("Camera is not ready. Let's wait a bit.");
            }
        }
    }

    private String getVideoFilePath() {
        return getActivity().getExternalFilesDir(null).getAbsolutePath() + "/" + currentLine.getId() + "-" +
                + System.currentTimeMillis() + ".mp4"; // TODO: find a better system for naming the clip.
    }

    @Override
    public boolean hasPermissions() {
        if (!hasPermissionsGranted(CameraHelper.VIDEO_PERMISSIONS)) {
            if (shouldShowRequestPermissionRationale(CameraHelper.VIDEO_PERMISSIONS)) {
                new ConfirmationDialog().show(getChildFragmentManager(), CameraHelper.FRAGMENT_DIALOG);
            } else {
                requestPermissions(CameraHelper.VIDEO_PERMISSIONS, CameraHelper.REQUEST_VIDEO_PERMISSIONS);
            }
            return false;
        }
        return true;
    }

    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == CameraHelper.REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == CameraHelper.VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "User did not grant permissions. Bye! " + result);
                        break;
                    }
                }
            } else {
                ErrorDialog.newInstance(getString(R.string.permission_request))
                        .show(getChildFragmentManager(), CameraHelper.FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void recordingStopped(String clipPath) {
        Line recordedLine = currentLine.addVideoUrl(clipPath);
        recordedLine.broadcastRecorded(getContext());
        mIsRecordingVideo = false;
    }

    @Override
    public void cameraConfigured() {
        Log.d(TAG, "Camera configured. Start recording on UI thread...");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Start recording
                mVideoCameraManager.startMediaRecorder();
            }
        });

    }

    @Override
    public void configurationFailed() {
        Activity activity = getActivity();
        if (null != activity) {
            Toast.makeText(activity, "Camera session configuration failed.", Toast.LENGTH_SHORT).show();
        }
        // retry?
        // mVideoCameraManager.startRecordingVideo(getVideoFilePath());
    }

    @Override
    public void error(String errorText) {
        Activity activity = getActivity();
        if (null != activity) {
            Toast.makeText(activity, errorText, Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }
}
