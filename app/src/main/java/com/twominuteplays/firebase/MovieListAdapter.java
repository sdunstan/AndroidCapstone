package com.twominuteplays.firebase;

import android.app.Activity;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.twominuteplays.db.FirebaseStuff;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.MovieBuilder;

import java.util.Map;

public abstract class MovieListAdapter extends FirebaseListAdapter<Movie> {

    public MovieListAdapter(Activity activity, int modelLayout) {
        super(activity, Movie.class, modelLayout, FirebaseStuff.getMoviesRef());
    }

    @Override
    protected Movie parseSnapshot(DataSnapshot snapshot) {
        Map<String, Object> jsonSnapshot = (Map<String, Object>) snapshot.getValue();
        MovieBuilder builder = new MovieBuilder();
        return builder.withJson(jsonSnapshot).build();
    }
}
