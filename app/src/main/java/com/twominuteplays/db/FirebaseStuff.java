package com.twominuteplays.db;

import com.firebase.client.Firebase;
import com.twominuteplays.BuildConfig;
import com.twominuteplays.model.Movie;

public class FirebaseStuff {

    public static final String FIREBASE_URL = BuildConfig.FIREBASE_URL;

    public static void saveMovie(Movie movie) {
        Firebase firebase = new Firebase(FIREBASE_URL);
        String uid = firebase.getAuth().getUid();
        if (uid == null) {
            throw new IllegalStateException("User must be logged in to save movie.");
        }
        firebase.child("movies/" + uid + "/" + movie.getId()).setValue(movie);
    }

    public static String getUid() {
        Firebase firebase = new Firebase(FIREBASE_URL);
        if (firebase.getAuth() == null)
            return null;
        return firebase.getAuth().getUid();
    }

    public static Firebase getMovieRef(String uid, String movieId) {
        Firebase firebase = new Firebase(FIREBASE_URL + "movies/" + uid + "/" + movieId);
        return firebase;
    }

    public static Firebase getMovieRef(String movieId) {
        String uid = getUid();
        if (uid == null)
            return null;
        return getMovieRef(uid, movieId);
    }
}
