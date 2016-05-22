package com.twominuteplays.firebase;

import android.view.View;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.twominuteplays.ClickableScriptCardViewHolder;
import com.twominuteplays.R;
import com.twominuteplays.model.Movie;

import java.util.Map;

public class ClickableMovieTemplateAdapter extends FirebaseRecyclerAdapter<Movie, ClickableScriptCardViewHolder> {

    public ClickableMovieTemplateAdapter(DatabaseReference moviesRef) {
        super(Movie.class, R.layout.script_card, ClickableScriptCardViewHolder.class, moviesRef);
    }

    @Override
    protected void populateViewHolder(ClickableScriptCardViewHolder scriptCardViewHolder, final Movie movie, int i) {
        scriptCardViewHolder.movie = movie;
        scriptCardViewHolder.scriptTitleTextView.setText(movie.getTitle());
        scriptCardViewHolder.synopsisTextView.setText(movie.getSynopsis());
        if (movie.getImageUrl() != null) {
            Glide.with(scriptCardViewHolder.scriptImageView.getContext())
                    .load(movie.getImageUrl())
                    .into(scriptCardViewHolder.scriptImageView);
        }
        else {
            Glide.with(scriptCardViewHolder.scriptImageView.getContext())
                    .load(R.mipmap.card_bg)
                    .into(scriptCardViewHolder.scriptImageView);
        }
        scriptCardViewHolder.shareButton.setTag(movie);
        scriptCardViewHolder.shareButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        movie.broadcastShare(view.getContext());
                    }
                }
        );
    }

    @Override
    protected Movie parseSnapshot(DataSnapshot snapshot) {
        Map<String,Object> jsonSnapshot = (Map<String, Object>) snapshot.getValue();
        Movie.Builder builder = new Movie.Builder();
        return builder.withJson(jsonSnapshot).build();
    }
}
