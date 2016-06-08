package com.twominuteplays.db;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.twominuteplays.R;
import com.twominuteplays.TwoMinutePlaysApp;
import com.twominuteplays.model.Movie;

import java.util.HashMap;
import java.util.Map;

public class ContributorsManager {

    private static final String TAG = ContributorsManager.class.getName();

    private Map<String,ContributorsListener> contributorsListenerMap;

    private Map<String,ContributorsListener> getContributorsListenerMap() {
        if (contributorsListenerMap == null) {
            contributorsListenerMap = new HashMap<>();
        }
        return contributorsListenerMap;
    }

    public synchronized void registerListener(Movie movie) {
        Long shareId = movie.getShareId();
        if(shareId != null && !getContributorsListenerMap().containsKey(shareId.toString())) {
            DatabaseReference db = FirebaseStuff.getShareRef(shareId.toString())
                    .child(TwoMinutePlaysApp.getResourceString(R.string.contributorsNode));

            Log.d(TAG, "Attempting to listen for contributions for " + db.toString());
            if (db == null) {
                Log.w(TAG, "Movies database reference is null. Cannot add listeners.");
                return;
            }
            ContributorsListener listener = new ContributorsListener(movie);
            getContributorsListenerMap().put(shareId.toString(), listener);
            db.addChildEventListener(listener);
        }
    }

    public synchronized void removeListener(Long shareId) {
        try {
            if (shareId != null && getContributorsListenerMap().containsKey(shareId.toString())) {
                DatabaseReference db = FirebaseStuff.getShareRef(shareId.toString())
                        .child(TwoMinutePlaysApp.getResourceString(R.string.contributorsNode));
                db.removeEventListener(getContributorsListenerMap().remove(shareId.toString()));
                getContributorsListenerMap().remove(shareId.toString());
            }
        }
        catch(Throwable t) {
            Log.e(TAG, "Error removing ContributorsListener from share. May have leaked.", t);
        }
    }

}
