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

public class ContributedManager {
    private static final String TAG = ContributorsManager.class.getName();

    private Map<String,ValueEventListener> contributionsListenerMap;

    private Map<String,ValueEventListener> getContributionsListenerMap() {
        if (contributionsListenerMap == null) {
            contributionsListenerMap = new HashMap<>();
        }
        return contributionsListenerMap;
    }

    private String getKey(Long shareId, String contributor) {
        return shareId.toString() + "/" + contributor;
    }

    public synchronized void registerListener(final Movie movie, final Context context) {
        Long shareId = movie.getShareId();
        String contributor = movie.getContributor();
        if(shareId != null && !getContributionsListenerMap().containsKey(getKey(shareId, contributor))) {
            DatabaseReference db = FirebaseStuff.getShareRef(shareId.toString())
                    .child("contributors").child(contributor);
            if (db == null) {
                Log.w(TAG, "Movies database reference is null. Cannot add listeners.");
                return;
            }
            Log.d(TAG, "Looking for contributions to " + db.toString());

            ValueEventListener listener =
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "Found contributor contributions for my share clone. Downloading to " + movie.getId());
                            try {
                                Contributions contributions = Contributions.fromMap(dataSnapshot.getValue());
                                ClipDownloadService.startActionDownloadClips(context, contributions, movie);
                            } catch (MappingError mappingError) {
                                Log.e(TAG, "Error mapping contribution: " + mappingError.getMessage(), mappingError);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
            getContributionsListenerMap().put(getKey(shareId, contributor), listener);
            db.addValueEventListener(listener);
        }
    }

    public synchronized void removeListener(Long shareId, String contributor) {
        try {
            if (shareId != null && contributor != null &&
                    getContributionsListenerMap().containsKey(getKey(shareId, contributor))) {
                DatabaseReference db = FirebaseStuff.getShareRef(shareId.toString())
                        .child("contributors").child(contributor);
                db.removeEventListener(getContributionsListenerMap().get(getKey(shareId, contributor)));
            }
        }
        catch(Throwable t) {
            Log.e(TAG, "Error removing ContributedListener from share. May have leaked.", t);
        }
    }
}
