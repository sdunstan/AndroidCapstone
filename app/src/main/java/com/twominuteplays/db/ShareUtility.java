package com.twominuteplays.db;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.twominuteplays.firebase.ShareCounter;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.MovieState;
import com.twominuteplays.model.Part;
import com.twominuteplays.model.Share;
import com.twominuteplays.services.ShareService;

import static com.twominuteplays.model.MovieState.RECORDED;

/**
 * All the sharing logic here.
 * 1. Increments the share counter and grabs the next value
 * 2. Saves the Share object with the initial data
 * 3. Saves the share ID to the Movie object
 * 4. Starts the share intent so the user can share via email, sms, whatever.
 * 5. Start intent to begin uploading owner clips to GCS
 */
public class ShareUtility {
    private static final String TAG = ShareUtility.class.getName();
    private static ShareUtility instance;
    private MyMoviesEventListener myMoviesEventListener;

    private ShareUtility() {
    }

    public static ShareUtility getInstance() {
        if (instance == null) {
            instance = new ShareUtility();
        }
        return instance;
    }

    private static boolean isSharableState(final Movie movie) {
        return MovieState.SHARED == movie.getState() || RECORDED == movie.getState();
    }

    public static void shareMovie(final Context context, final Movie movie) {
        final Part ownersPart = movie.findExclusiveRecordedPart();
        if (ownersPart == null || !isSharableState(movie)) {
            Log.e(TAG, "Movie must have exactly one part recorded.");
            throw new IllegalStateException("Movie must have exactly one part recorded.");
        }
        new ShareCounter(
                new ShareCounter.OnCounterIncremented() {
                    @Override
                    public void nextval(Long value) {
                        Log.d(TAG, "Sharing. Got share nextval of " + value);
                        Movie sharedMovie = saveShare(value, movie, ownersPart);
                        Log.d(TAG, "Sending share intent");
                        sendShareIntent(context, sharedMovie);
                        ShareService.saveOwnersClipsToGCS(context, sharedMovie.getShareId(), ownersPart);
                    }
                },
                new ShareCounter.OnFailure() {
                    @Override
                    public void error(DatabaseError databaseError) {
                        // TODO: how do I broadcast the error?
                        Log.e(TAG, "Unable to do the share, can't find a share nextval.");
                    }
                }
        ).nextval();
    }

    private static void sendShareIntent(Context context, Movie movie) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareText = String.format("ANDROID BETA ONLY. Congratulations! You've been cast to play a part in %s. Tap this: %s",
                movie.getTitle(),
                movie.deepLink());
        sharingIntent.putExtra("sms_body", shareText);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(movie.deepLink()));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sharingIntent.resolveActivity(context.getPackageManager());
        context.startActivity(Intent.createChooser(sharingIntent,"Share movie using"));
    }


    private static Movie saveShare(Long shareId, Movie movie, Part ownersPart) {
        Share share = new Share();
        share.setId(shareId);
        share.setMovieTemplateId(movie.getTemplateId());
        share.setOriginalMoviePath(FirebaseStuff.getMoviePath(movie));
        share.setOwnersPartId(ownersPart.getId());
        share.setContributorsPartId(movie.findPartIdOpposite(ownersPart));

        // TODO: s/b in a transaction
        FirebaseStuff.saveShare(share);
        Log.d(TAG, "Saving state SHARED to movie.");
        return movie.state.share(shareId, movie);
    }

    private void registerMovieListeners(final Context context) {
        if (myMoviesEventListener == null) {
            DatabaseReference db = FirebaseStuff.getMoviesRef();
            if (db == null) {
                Log.w(TAG, "Movies database reference is null. Cannot add listeners.");
                return;
            }
            myMoviesEventListener = new MyMoviesEventListener(context);
            db.addChildEventListener(myMoviesEventListener);
        }
    }

    /**
     * Call after the user has logged in from MainActivity
     */
    public static void registerShareListeners(final Context context) {
        getInstance().registerMovieListeners(context);
    }

}
