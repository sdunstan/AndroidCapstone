package com.twominuteplays.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

public class FrameGrabber {

    private static final String TAG = FrameGrabber.class.getName();

    public static Bitmap getVideoFrame(Context context, Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            return retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "File name not found for rendering first image from movie.", ex);
        } catch (RuntimeException ex) {
            Log.e(TAG, "Problem rendering first image from movie.", ex);
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return null;
    }

    public static Bitmap getVideoFrame(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            return retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "File name not found for rendering first image from movie.", ex);
        } catch (RuntimeException ex) {
            Log.e(TAG, "Problem rendering first image from movie.", ex);
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return null;
    }

}
