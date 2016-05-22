package com.twominuteplays.db;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.twominuteplays.BuildConfig;
import com.twominuteplays.model.Movie;

public class FirebaseStuff {

    public static final String FIREBASE_URL = BuildConfig.FIREBASE_URL;

    public static DatabaseReference getFirebase() {
        return FirebaseDatabase.getInstance().getReferenceFromUrl(FIREBASE_URL);
    }

    public static void saveMovie(Movie movie) {
        String uid = getUid();
        if (uid == null) {
            throw new IllegalStateException("User must be logged in to save movie.");
        }
        getFirebase().child("movies/" + uid + "/" + movie.getId()).setValue(movie);
    }

    public static String getUid() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null)
            return null;
        return currentUser.getUid();
    }

    public static DatabaseReference getMovieRef(String movieId) {
        DatabaseReference moviesRef = getMoviesRef();
        if (moviesRef == null)
            return null;
        return moviesRef.child(movieId);
    }

    public static DatabaseReference getMoviesRef() {
        String uid = getUid();
        if (uid == null)
            return null;
        return getFirebase().child("movies").child(uid);
    }
}
