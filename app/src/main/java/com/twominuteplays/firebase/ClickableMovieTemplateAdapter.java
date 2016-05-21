package com.twominuteplays.firebase;

import android.view.View;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.twominuteplays.ClickableScriptCardViewHolder;
import com.twominuteplays.R;
import com.twominuteplays.model.Movie;

public class ClickableMovieTemplateAdapter extends FirebaseRecyclerAdapter<Movie, ClickableScriptCardViewHolder> {

    public ClickableMovieTemplateAdapter(Firebase moviesRef) {
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
}
