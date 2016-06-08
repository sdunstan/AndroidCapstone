package com.twominuteplays.camera;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextureViewManager implements TextureView.SurfaceTextureListener {

    private static final String TAG = TextureViewManager.class.getName();

    private AutoFitTextureView mTextureView;
    private Size mPreviewSize;
    private Activity mActivity;
    private CameraCallback mCameraCallback;

    public interface CameraCallback {
        void openCamera(int width, int height);
    }

    public void setCameraCallback(CameraCallback cameraCallback) {
        mCameraCallback = cameraCallback;
    }

    private void callOpenCameraCallback(int width, int height) {
        if (mCameraCallback != null) {
            mCameraCallback.openCamera(width, height);
        }
    }

    /**
     * Called when the texture view changes shape.
     */
    public void configureTransform() {
        if (null == mTextureView) {
            return;
        }
        configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
    }
    public void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize || null == mActivity) {
            return;
        }
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    public void setTextureView(Activity activity, AutoFitTextureView textureView) {
        this.mTextureView = textureView;
        this.mTextureView.setSurfaceTextureListener(this);
        this.mActivity = activity;
    }

    // Surface Texture Listener methods
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        callOpenCameraCallback(width, height);
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    public boolean isAvailable() {
        return mTextureView.isAvailable();
    }

    public int getWidth() {
        return mTextureView.getWidth();
    }

    public int getHeight() {
        return mTextureView.getHeight();
    }

    public void setOrientation(int orientation, int width, int height) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        } else {
            mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
        }
        configureTransform(width, height);
    }

    public Surface getSurface() {
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        return new Surface(texture);
    }

    public void setPreviewSize(Size size) {
        this.mPreviewSize = size;
    }

    public Size getPreviewSize() {
        return this.mPreviewSize;
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values, and whose aspect
     * ratio matches with the specified value.
     *
     * @param choices     The list of sizes that the camera supports for the intended output class
     * @param targetMinWidth       The minimum desired width
     * @param targetMinHeight      The minimum desired height
     * @param aspectRatio The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    public void setOptimalPreviewSize(Size[] choices, int targetMinWidth, int targetMinHeight, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> matchingAspectRatioSizes = new ArrayList<Size>();
        List<Size> minimumDensitySizes = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        MathContext mc = new MathContext(3, RoundingMode.UP);
        BigDecimal targetAspectRatio = new BigDecimal(h, mc);
        targetAspectRatio = targetAspectRatio.divide(new BigDecimal(w, mc), mc);
        int targetMinDensity = targetMinHeight*targetMinWidth;
        Log.d(TAG, "Target aspect ratio is " + targetAspectRatio + " looking for min of " + targetMinWidth + "x" + targetMinHeight);
        for (Size option : choices) {
            int density = option.getHeight() * option.getWidth();
            BigDecimal optionAspectRatio = (new BigDecimal(option.getHeight(), mc))
                    .divide(new BigDecimal(option.getWidth(), mc), mc);
            Log.d(TAG, "Found preview size of " + option.toString() + " has a ratio of " + optionAspectRatio);
            if (targetAspectRatio.equals(optionAspectRatio)) {
                matchingAspectRatioSizes.add(option);
                if (density >= targetMinDensity) {
                    minimumDensitySizes.add(option);
                }
            }
        }

        if (minimumDensitySizes.size() > 0) { // If any match the minimum size requirement, and the aspect ratio requirement, use the smallest
            Size size = Collections.min(minimumDensitySizes, new CompareSizesByArea());
            Log.i(TAG, "Using a good match for preview size. " + size.toString());
            setPreviewSize(size);
            return;
        }
        // Otherwise use the largest
        if (matchingAspectRatioSizes.size() > 0) {
            Size size = Collections.max(matchingAspectRatioSizes, new CompareSizesByArea());
            Log.w(TAG, "No preview sizes match the size of the screen, some scaling will occur. " + size.toString());
            setPreviewSize(size);
            return;
        }
        // Otherwise, use the first one
        Log.w(TAG, "Couldn't find any suitable preview size. Using " + choices[0].toString());
        setPreviewSize(choices[0]);
    }
}
