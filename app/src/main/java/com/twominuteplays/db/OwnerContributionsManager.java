package com.twominuteplays.db;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.twominuteplays.exceptions.MappingError;
import com.twominuteplays.model.Contributions;
import com.twominuteplays.model.Movie;
import com.twominuteplays.services.ClipDownloadService;

import java.util.HashMap;
import java.util.Map;

public class OwnerContributionsManager {
    private static final String TAG = OwnerContributionsManager.class.getName();

    private Map<String,ValueEventListener> ownerContributionsListenerMap;

    private Map<String,ValueEventListener> getOwnerContributionsListenerMap() {
        if (ownerContributionsListenerMap == null) {
            ownerContributionsListenerMap = new HashMap<>();
        }
        return ownerContributionsListenerMap;
    }


    public synchronized void registerListener(final Movie movie, final Context context) {
        if(!getOwnerContributionsListenerMap().containsKey(movie.getId())) {
            DatabaseReference db = FirebaseStuff.getShareRef(movie.getShareId().toString()).child("ownersClips");
            if (db == null) {
                Log.w(TAG, "Movies database reference is null. Cannot add listeners.");
                return;
            }
            ValueEventListener listener =
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "Found owner contributions for my contributed movie. Downloading to " + movie.getId());
                            try {
                                Contributions ownerContributions = Contributions.fromMap(dataSnapshot.getValue());
                                ClipDownloadService.startActionDownloadClips(context, ownerContributions, movie);
                            } catch (MappingError mappingError) {
                                Log.e(TAG, "Error mapping owner contributions: " + mappingError.getMessage(), mappingError);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
            getOwnerContributionsListenerMap().put(movie.getId(), listener);
            db.addValueEventListener(listener);
        }
    }

    public synchronized void removeListener(Movie movie) {
        if(getOwnerContributionsListenerMap().containsKey(movie.getId())) {
            DatabaseReference db = FirebaseStuff.getShareRef(movie.getShareId().toString()).child("ownersClips");
            db.removeEventListener(getOwnerContributionsListenerMap().get(movie.getId()));
        }
    }
}
