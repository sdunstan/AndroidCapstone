package com.twominuteplays.video;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import com.twominuteplays.model.Line;
import com.twominuteplays.model.Movie;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class will play a movie's clips given two VideoViews. It will alternate between
 * the two video views so that the clips can be played seamlessly.
 */
public class MovieViewer {
    private static final String TAG = MovieViewer.class.getName();

    private final Movie movie;
    private final VideoView view1;
    private final VideoView view2;
    private Iterator<Line> view1Lines;
    private Iterator<Line> view2Lines;
    private final Lock moviePlayingLock;

    public MovieViewer(final Movie movie, final VideoView view1, final VideoView view2) {
        this.movie = movie;
        this.view1 = view1;
        this.view2 = view2;
        moviePlayingLock = new ReentrantLock();
    }

    public void play() {
        view1Lines = movie.getParts().get(0).getLines().iterator();
        view2Lines = movie.getParts().get(1).getLines().iterator();

        view1.setOnCompletionListener(new MovieFinishedListener());
        view2.setOnCompletionListener(new MovieFinishedListener());
        view1.setOnPreparedListener(new MoviePreparedListener(view1, view2, view2Lines));
        view2.setOnPreparedListener(new MoviePreparedListener(view2, view1, view1Lines));
        MovieErrorListener errorListener = new MovieErrorListener();
        view1.setOnErrorListener(errorListener);
        view2.setOnErrorListener(errorListener);

        // Assumes both parts have at least one line.
        view1.setVideoPath(view1Lines.next().getRecordingPath()); // starts up the process.
    }

    private class MovieFinishedListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.reset();
            Log.i(TAG, "Movie done. Unlocking to play next line.");
            moviePlayingLock.unlock(); // Not having an OnErrorListener will cause completion listener to be called so we are safe.
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
        private final VideoView myView;
        private final VideoView otherView;
        private final Iterator<Line> otherViewLines;

        public MoviePreparedListener(VideoView myView, VideoView otherView, Iterator<Line> otherViewLines) {
            this.myView = myView;
            this.otherView = otherView;
            this.otherViewLines = otherViewLines;
        }

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.i(TAG, "Waiting for movie to finish playing...");
            moviePlayingLock.lock(); // Blocks until the movie finishes playing.
            try {
                Log.i(TAG, "movie is ready to go. Playing.");
                otherView.setVisibility(View.INVISIBLE);
                myView.setVisibility(View.VISIBLE);
                mediaPlayer.start();
            }
            catch(Throwable t) {
                Log.e(TAG, "Got error attempting to play the clip.", t);
                moviePlayingLock.unlock();
            }
            if (otherViewLines.hasNext()) {
                String nextVideo = otherViewLines.next().getRecordingPath();
                Log.i(TAG, "Queuing next line. " + nextVideo);
                otherView.setVideoPath(nextVideo);
            }
        }
    }

}
