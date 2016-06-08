package com.twominuteplays.db;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.MovieBuilder;

import java.util.Map;

class MyMoviesEventListener implements ChildEventListener {
    private static final String TAG = MyMoviesEventListener.class.getName();
    private final Context context;

    private final ContributorsManager contributorsManager = new ContributorsManager();
    private final OwnerContributionsManager ownerContributionsManager = new OwnerContributionsManager();
    private final ContributedManager contributedManager = new ContributedManager();

    public MyMoviesEventListener(Context context) {
        this.context = context;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        // Called once initially, then again for each movie added
        Map<String,Object> jsonSnapshot = (Map<String, Object>) dataSnapshot.getValue();
        MovieBuilder builder = new MovieBuilder();
        Movie movie = builder.withJson(jsonSnapshot).build();

        if (movie != null) {
            Log.d(TAG, "Listening for changes to my added movie " + movie.getId());
            onMovieChange(context, movie);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        // Same as onChildAdded
        Map<String,Object> jsonSnapshot = (Map<String, Object>) dataSnapshot.getValue();
        MovieBuilder builder = new MovieBuilder();
        Movie movie = builder.withJson(jsonSnapshot).build();

        if (movie != null) {
            Log.d(TAG, "Listening for changes to my modified movie " + movie.getId());
            onMovieChange(context, movie);
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Map<String,Object> jsonSnapshot = (Map<String, Object>) dataSnapshot.getValue();
        MovieBuilder builder = new MovieBuilder();
        Movie movie = builder.withJson(jsonSnapshot).build();

        Log.d(TAG, "Removing movie " + movie.getId());
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
        // Dont' care
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "Listener for movies cancelled: " + databaseError.getMessage());
    }

    private void onMovieChange(Context context, final Movie movie) {
        if (movie.getShareId() != null && movie.getState() != null) {
            switch (movie.getState()) {
                case SHARED:
                    listenForContributions(context, movie);
                    break;
                case TEMPLATE:
                    break;
                case SELECTED:
                    break;
                case PART_SELECTED:
                    break;
                case RECORDING_STARTED:
                    break;
                case CONTRIBUTE:
                    break;
                case RECORDED:
                    break;
                case CONTRIBUTED:
                    listenForOwnerContributions(context, movie);
                    break;
                case SINGLE_USER:
                    break;
                case SINGLE_USER_MERGED:
                    break;
                case SHARE_CLONED:
                    listenForContributed(context, movie);
                    break;
                case DOWNLOADING_OWNER:
                    break;
                case DOWNLOADING_CONTRIBUTOR:
                    break;
                case DOWNLOADED:
                    removeListeners(movie);
                    break;
                case MERGED:
                    break;
            }
        }
    }

    private void removeListeners(Movie movie) {
        ownerContributionsManager.removeListener(movie);
    }

    /**
     * If a movie is shared and then clips are uploaded for it, we need to create a SHARE_CLONED copy.
     */
    private synchronized void listenForContributions(final Context context, final Movie sharedMovie) {
        contributorsManager.registerListener(sharedMovie);
    }

    /**
     * Download clips when an owner contributes their own clips.
     */
    private synchronized void listenForOwnerContributions(final Context context, final Movie contributedMovie) {
        ownerContributionsManager.registerListener(contributedMovie, context);
    }


    /**
     * Download clips when they are uploaded from a contributor.
     */
    private synchronized void listenForContributed(final Context context, final Movie movie) {
        contributedManager.registerListener(movie, context);
    }



}
