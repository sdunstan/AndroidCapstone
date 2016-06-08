package com.twominuteplays.db;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.twominuteplays.exceptions.MappingError;
import com.twominuteplays.model.Contributions;
import com.twominuteplays.model.Movie;

public class ContributorsListener implements ChildEventListener {
    public static final String TAG = ContributorsListener.class.getName();

    private final Movie sharedMovie;

    public ContributorsListener(Movie movie) {
        sharedMovie = movie;
    }


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
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "Listener for contributions cancelled: " + databaseError.getMessage());
    }

    private void cloneMovieFromShareContributions(DataSnapshot dataSnapshot, final Movie sharedMovie) {
        final String contributorUid = dataSnapshot.getKey();
        try {
            Contributions contributions = Contributions.fromMap(dataSnapshot.getValue());
            cloneMovie(dataSnapshot, contributorUid, contributions, sharedMovie);
        } catch (MappingError mappingError) {
            Log.e(TAG, "Mapping error " + mappingError.getMessage(), mappingError);
        }
    }

    private void cloneMovie(DataSnapshot dataSnapshot, final String contributorUid, final Contributions contributions, final Movie sharedMovie) {
        Log.d(TAG, "Found contributions changes for " + sharedMovie.getId());
        if(contributions != null && contributions.getClips() != null && !contributions.isCloned()) {
            Log.d(TAG, "Got a live one! Try to clone clips.");
            dataSnapshot.getRef().runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    try {
                        Contributions currentValue = Contributions.fromMap(mutableData.getValue());
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
                        contributionsCurrentValue = Contributions.fromMap(dataSnapshot.getValue());
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

}
