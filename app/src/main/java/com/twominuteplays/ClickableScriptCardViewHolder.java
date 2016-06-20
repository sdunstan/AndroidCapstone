package com.twominuteplays;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.twominuteplays.db.FirebaseStuff;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.MovieBuilder;

import java.util.Map;

import static com.twominuteplays.model.MovieState.TEMPLATE;

public class ClickableScriptCardViewHolder extends ScriptCardViewHolder {
    public ClickableScriptCardViewHolder(final View cardView) {
        super(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != movie && null != movie.getId()) {
                    if(movie.isLocal()) {
                        // Local database does not store the parts, lines, etc. Need to inflate from
                        // Firebase, then launch activity.
                        inflateMovie(view.getContext(),  movie.getId());
                    }
                    else {
                        launchPlayView(view.getContext(), movie);
                    }
                }
            }

            private void inflateMovie(final Context context, String movieId) {
                DatabaseReference db = FirebaseStuff.getMovieRef(movieId);
                if (db != null) {
                    db.addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> jsonSnapshot = (Map<String, Object>) dataSnapshot.getValue();
                                    MovieBuilder builder = new MovieBuilder();
                                    Movie loadedMovie = builder.withJson(jsonSnapshot).build();
                                    launchPlayView(context, loadedMovie);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            }
                    );
                }
            }

            private void launchPlayView(final Context context, Movie movie) {
                Intent intent = new Intent(context, PlayViewActivity.class);
                if (TEMPLATE == movie.getState()) {
                    movie = movie.state.select(movie);
                }
                intent.putExtra("MOVIE", movie);
                context.startActivity(intent);
            }
        });
    }
}
