package com.twominuteplays;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.twominuteplays.db.FirebaseStuff;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.MovieBuilder;

import java.util.Map;

public class MovieInflater {

    private interface InflatedMovieAction {
        void action(final Movie movie, final Context context);
    }

    public synchronized void broadcastShare(final Movie movie, final Context context) {
        if(movie.isLocal()) {
            // Local database does not store the parts, lines, etc. Need to inflate from
            // Firebase, then launch activity.
            inflateMovie(context,  movie.getId(), new InflatedMovieAction() {
                @Override
                public void action(Movie loadedMovie, Context context) {
                    loadedMovie.broadcastShare(context);
                }
            });
        }
        else {
            movie.broadcastShare(context);
        }
    }

    public synchronized void startActivity(final Movie movie, final Context context, final Intent intent) {
        if(movie.isLocal()) {
            // Local database does not store the parts, lines, etc. Need to inflate from
            // Firebase, then launch activity.
            inflateMovie(context,  movie.getId(), new InflatedMovieAction() {
                @Override
                public void action(Movie loadedMovie, Context context) {
                    intent.putExtra("MOVIE", loadedMovie);
                    context.startActivity(intent);
                }
            });
        }
        else {
            intent.putExtra("MOVIE", movie);
            context.startActivity(intent);
        }
    }

    private void inflateMovie(final Context context, String movieId, final InflatedMovieAction inflatedMovieAction) {
        DatabaseReference db = FirebaseStuff.getMovieRef(movieId);
        if (db != null) {
            db.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> jsonSnapshot = (Map<String, Object>) dataSnapshot.getValue();
                            MovieBuilder builder = new MovieBuilder();
                            Movie loadedMovie = builder.withJson(jsonSnapshot).build();
                            inflatedMovieAction.action(loadedMovie, context);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    }
            );
        }
    }

}
