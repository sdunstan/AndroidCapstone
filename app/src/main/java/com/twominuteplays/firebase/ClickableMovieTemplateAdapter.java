package com.twominuteplays.firebase;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.twominuteplays.ClickableScriptCardViewHolder;
import com.twominuteplays.MovieActivity;
import com.twominuteplays.R;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.MovieBuilder;
import com.twominuteplays.model.MovieState;

import java.util.Map;

public class ClickableMovieTemplateAdapter extends FirebaseRecyclerAdapter<Movie, ClickableScriptCardViewHolder> {

    public ClickableMovieTemplateAdapter(DatabaseReference moviesRef) {
        super(Movie.class, R.layout.script_card, ClickableScriptCardViewHolder.class, moviesRef);
    }

    private void setText(TextView textView, String text) {
        if (textView != null) {
            if (text!=null) {
                textView.setText(text);
            }
            else {
                textView.setText("");
            }
        }
    }

    @Override
    protected void populateViewHolder(ClickableScriptCardViewHolder scriptCardViewHolder, final Movie movie, int i) {
        scriptCardViewHolder.movie = movie;
        setText(scriptCardViewHolder.scriptTitleTextView, movie.getTitle());
        setText(scriptCardViewHolder.synopsisTextView, movie.getSynopsis());
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
        if (MovieState.RECORDED == movie.getState()) {
            scriptCardViewHolder.shareButton.setVisibility(View.VISIBLE);
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
        else {
            scriptCardViewHolder.shareButton.setVisibility(View.INVISIBLE);
        }

        if (MovieState.MERGED == movie.getState()) {
            scriptCardViewHolder.playButton.setVisibility(View.VISIBLE);
            scriptCardViewHolder.playButton.setTag(movie);
            scriptCardViewHolder.playButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Context ctx = view.getContext();
                            Intent intent = new Intent(ctx, MovieActivity.class);
                            intent.putExtra("MOVIE", movie);
                            ctx.startActivity(intent);
                        }
                    }
            );
        }
        else {
            scriptCardViewHolder.playButton.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected Movie parseSnapshot(DataSnapshot snapshot) {
        Map<String,Object> jsonSnapshot = (Map<String, Object>) snapshot.getValue();
        MovieBuilder builder = new MovieBuilder();
        return builder.withJson(jsonSnapshot).build();
    }
}
