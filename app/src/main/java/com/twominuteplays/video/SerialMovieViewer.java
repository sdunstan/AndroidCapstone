package com.twominuteplays.video;

import android.media.MediaPlayer;
import android.util.Log;
import android.widget.VideoView;

import com.twominuteplays.model.Line;
import com.twominuteplays.model.Movie;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * This class will play a movie's clips given two VideoViews. It will alternate between
 * the two video views so that the clips can be played seamlessly.
 */
public class SerialMovieViewer {
    private static final String TAG = SerialMovieViewer.class.getName();

    private final Movie movie;
    private final VideoView view;

    public SerialMovieViewer(final Movie movie, final VideoView view) {
        this.movie = movie;
        this.view = view;
    }

    public void play() {
        List<Line> linesList = movie.assembleLines();
        Log.d(TAG, "Playing lines. Count is " + linesList.size());

        Iterator<Line> lines = linesList.iterator();

        view.setOnCompletionListener(new MovieFinishedListener(view, lines));
        view.setOnPreparedListener(new MoviePreparedListener());
        view.setOnErrorListener(new MovieErrorListener());
        File file = new File(lines.next().getRecordingPath());
        Log.d(TAG, "Next line is " + file.getAbsolutePath() + " exists? " + file.exists());
        view.setVideoPath(file.getAbsolutePath()); // starts up the process.
    }

    private class MovieFinishedListener implements MediaPlayer.OnCompletionListener {
        private final VideoView view;
        private final Iterator<Line> lines;

        public MovieFinishedListener(VideoView view, Iterator<Line> lines) {
            this.view = view;
            this.lines = lines;
        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.i(TAG, "Clip done. Loading next line.");
            if (lines.hasNext()) {
                mediaPlayer.reset();
                String nextVideo = lines.next().getRecordingPath();
                Log.i(TAG, "Queuing next line. " + nextVideo);
                view.setVideoPath(nextVideo);
            }
        }
    }

    private class MovieErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            Log.e(TAG, "Error loading file!");
            return false;
        }
    }

    private class MoviePreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.i(TAG, "movie is ready to go. Playing.");
            mediaPlayer.start();
        }
    }

}
