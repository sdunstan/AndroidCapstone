package com.twominuteplays.db;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.twominuteplays.R;
import com.twominuteplays.exceptions.MappingError;
import com.twominuteplays.firebase.ShareCounter;
import com.twominuteplays.model.Contributions;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.MovieBuilder;
import com.twominuteplays.model.Part;
import com.twominuteplays.model.Share;
import com.twominuteplays.services.ClipDownloadService;
import com.twominuteplays.services.MovieAssemblyService;
import com.twominuteplays.services.ShareService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.twominuteplays.model.MovieState.RECORDED;
import static com.twominuteplays.model.MovieState.SHARE_CLONED;

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
    private Map<String,Movie> movieMap;

    private ShareUtility() {
    }

    public static ShareUtility getInstance() {
        if (instance == null) {
            instance = new ShareUtility();
        }
        return instance;
    }

    public Map<String,Movie> getMovieMap() {
        if (movieMap == null) {
            movieMap = Collections.synchronizedMap(new HashMap<String,Movie>());
        }
        return movieMap;
    }

    public static void shareMovie(final Context context, final Movie movie) {
        final Part ownersPart = movie.findExclusiveRecordedPart();
        if (ownersPart == null || RECORDED != movie.getState()) {
            Log.e(TAG, "Movie must have exactly one part recorded.");
            throw new IllegalStateException("Movie must have exactly one part recorded.");
        }
        ShareCounter shareCounter = new ShareCounter(
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
        );
        shareCounter.nextval();
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

    /**
     * Call after the user has logged in from MainActivity
     */
    public static void registerShareListeners(final Context context) {
        DatabaseReference db = FirebaseStuff.getMoviesRef();
        if (db == null) {
            Log.w(TAG, "Movies database reference is null. Cannot add listeners.");
            return;
        }
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // Called once initially, then again for each movie added
                Map<String,Object> jsonSnapshot = (Map<String, Object>) dataSnapshot.getValue();
                MovieBuilder builder = new MovieBuilder();
                Movie movie = builder.withJson(jsonSnapshot).build();

                if (movie != null) {
                    Log.d(TAG, "Listening for changes to my added movie " + movie.getId());
                    ShareUtility.getInstance().getMovieMap().put(movie.getId(), movie);
                    listenForChanges(context, movie);
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
                    ShareUtility.getInstance().getMovieMap().put(movie.getId(), movie);
                    listenForChanges(context, movie);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Map<String,Object> jsonSnapshot = (Map<String, Object>) dataSnapshot.getValue();
                MovieBuilder builder = new MovieBuilder();
                Movie movie = builder.withJson(jsonSnapshot).build();

                Log.d(TAG, "Removing movie " + movie.getId());
                ShareUtility.getInstance().getMovieMap().remove(movie.getId());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Dont' care
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Listener for movies cancelled: " + databaseError.getMessage());
            }
        });
    }

    private static void listenForChanges(Context context, final Movie movie) {
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
                    merge(context,movie);
                    break;
                case MERGED:
                    removeListeners(movie);
                    break;
            }
        }
    }

    private static void merge(Context context, Movie movie) {
        MovieAssemblyService.startMovieAssemblyService(context, movie);
    }

    // If I am share cloned, try to download the contributor clips.
    private static void listenForContributed(final Context context, final Movie movie) {
        if (movie.state != SHARE_CLONED)
            return;
        DatabaseReference db = FirebaseStuff.getShareRef(movie.getShareId().toString()).child("contributors").child(movie.getContributor());
        if (db == null) {
            Log.w(TAG, "Movies database reference is null. Cannot add listeners.");
            return;
        }
        Log.d(TAG, "Looking for contributions to " + db.toString());
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Found contributor contributions for my share clone. Downloading to " + movie.getId());
                try {
                    Contributions contributions = mapContributions(dataSnapshot.getValue());
                    ClipDownloadService.startActionDownloadClips(context, contributions, movie);
                } catch (MappingError mappingError) {
                    Log.e(TAG, "Error mapping contribution: " + mappingError.getMessage(), mappingError);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private static void removeListeners(Movie movie) {
        // TODO: remove listeners after the movie has been merged
    }

    private static void listenForOwnerContributions(final Context context, final Movie contributedMovie) {

        DatabaseReference db = FirebaseStuff.getShareRef(contributedMovie.getShareId().toString()).child("ownersClips");
        if (db == null) {
            Log.w(TAG, "Movies database reference is null. Cannot add listeners.");
            return;
        }
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Found owner contributions for my contributed movie. Downloading to " + contributedMovie.getId());
                try {
                    Contributions ownerContributions = mapContributions(dataSnapshot.getValue());
                    ClipDownloadService.startActionDownloadClips(context, ownerContributions, contributedMovie);
                } catch (MappingError mappingError) {
                    Log.e(TAG, "Error mapping owner contributions: " + mappingError.getMessage(), mappingError);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * If a movie is shared and then clips are uploaded for it, we need to create a SHARE_CLONED copy.
     */
    private static void listenForContributions(final Context context, final Movie sharedMovie) {
        DatabaseReference db = FirebaseStuff.getShareRef(sharedMovie.getShareId().toString())
                .child(context.getString(R.string.contributorsNode));

        Log.d(TAG, "Attempting to listen for contributions for " + db.toString());
        if (db == null) {
            Log.w(TAG, "Movies database reference is null. Cannot add listeners.");
            return;
        }
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                cloneMovieFromShareContributions(dataSnapshot, sharedMovie);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                cloneMovieFromShareContributions(dataSnapshot, sharedMovie);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Don't care
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // Not possible
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Listener for contributions cancelled: " + databaseError.getMessage());
            }
        });
    }

    private static void cloneMovieFromShareContributions(DataSnapshot dataSnapshot, final Movie sharedMovie) {
        final String contributorUid = dataSnapshot.getKey();
        try {
            Contributions contributions = mapContributions(dataSnapshot.getValue());
            cloneIt(dataSnapshot, contributorUid, contributions, sharedMovie);
        } catch (MappingError mappingError) {
            Log.e(TAG, "Mapping error " + mappingError.getMessage(), mappingError);
        }
//        for (DataSnapshot contribSnapshot : dataSnapshot.getChildren()) {
//            Log.i(TAG, "Found possible contributions from " + contributorUid);
//            try {
//                Contributions contributions = mapContributions(contribSnapshot.getValue());
//                cloneIt(contribSnapshot, contributorUid, contributions, sharedMovie);
//            } catch (MappingError mappingError) {
//                Log.e(TAG, "Mapping error " + mappingError.getMessage(), mappingError);
//            }
//        }
    }

    private static void cloneIt(DataSnapshot dataSnapshot, final String contributorUid, final Contributions contributions, final Movie sharedMovie) {
        Log.d(TAG, "Found contributions changes for " + sharedMovie.getId());
        if(contributions != null && contributions.getClips() != null && !contributions.isCloned()) {
            Log.d(TAG, "Got a live one! Try to clone clips.");
            dataSnapshot.getRef().runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    try {
                        Contributions currentValue = mapContributions(mutableData.getValue());
                        Log.d(TAG, "Found candidate for cloning from contributor " + contributorUid);
                        if (currentValue != null && !currentValue.isCloned()) {
                            Log.d(TAG, "Contribution has not yet spawned a movie clone. Cloning...");
                            currentValue.setCloned(true);
                            mutableData.setValue(currentValue);
                            return Transaction.success(mutableData);
                        }
                    } catch (MappingError mappingError) {
                        Log.e(TAG, "Error cloning: " + mappingError.getMessage(), mappingError);
                    }
                    return Transaction.abort();
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                    Log.i(TAG, "Possible clone begin.");
                    Contributions contributionsCurrentValue = null;
                    try {
                        contributionsCurrentValue = mapContributions(dataSnapshot.getValue());
                    }
                    catch (Throwable t) {
                        Log.w(TAG, "Could not read contributions from data snapshot while cloning shared movie.", t);
                    }
                    if (committed && contributionsCurrentValue != null && contributionsCurrentValue.isCloned()) {
                        sharedMovie.state.shareClone(contributorUid, sharedMovie);
                    }
                }
            });
        }
    }

    private static Contributions mapContributions(Object value) throws MappingError {
        if (value != null && value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            if (!map.containsKey("cloned"))
                throw new MappingError("Contributions map must have a cloned property.");
            return new Contributions(map);
        }
        throw new MappingError("Value is not an instance of Map.");
    }

}
