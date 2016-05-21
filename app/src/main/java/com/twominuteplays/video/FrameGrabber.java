package com.twominuteplays.video;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

public class FrameGrabber {

    public static Bitmap getVideoFrame(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            return retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return null;
    }

}
