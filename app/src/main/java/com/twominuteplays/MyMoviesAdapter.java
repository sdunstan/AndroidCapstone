package com.twominuteplays;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.MovieBuilder;
import com.twominuteplays.model.MovieState;

import static com.twominuteplays.db.sql.MovieContract.Movie.*;

public class MyMoviesAdapter extends RecyclerView.Adapter<ClickableScriptCardViewHolder> {
    private Cursor cursor;
    private DataSetObserver dataSetObserver;

    public MyMoviesAdapter() {
        cursor = null;
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        if(cursor != null) {
            notifyDataSetChanged();
        }
        // TODO: this.cursor.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public ClickableScriptCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.script_card, parent, false);
        return new ClickableScriptCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClickableScriptCardViewHolder holder, int position) {
        if (cursor == null)
            return;
        cursor.moveToPosition(position);
        com.twominuteplays.model.Movie movie = new MovieBuilder()
                .withLocalFlag(true)
                .withId(getString(COLUMN_NAME_MOVIE_ID))
                .withContributor(getString(COLUMN_NAME_CONTRIBUTOR))
                .withAuthor(getString(COLUMN_NAME_AUTHOR))
                .withImageUrl(getString(COLUMN_NAME_IMAGE_URL))
                .withMovieUrl(getString(COLUMN_NAME_MOVIE_URL))
                .withScriptMarkup(getString(COLUMN_NAME_SCRIPT_MARKUP))
                .withState(MovieState.valueOf(getString(COLUMN_NAME_STATE)))
                .withSynopsis(getString(COLUMN_NAME_SYNOPSIS))
                .withTitle(getString(COLUMN_NAME_TITLE))
                .build();
        populateViewHolder(holder, movie);
    }

    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    private String getString(String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return cursor.getString(index);
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

    private void populateViewHolder(ClickableScriptCardViewHolder scriptCardViewHolder, final Movie movie) {
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
        if (MovieState.RECORDED == movie.getState() || MovieState.SHARED == movie.getState()) {
            scriptCardViewHolder.shareButton.setVisibility(View.VISIBLE);
            scriptCardViewHolder.shareButton.setTag(movie);
            scriptCardViewHolder.shareButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MovieInflater movieInflater = new MovieInflater();
                            movieInflater.broadcastShare(movie, view.getContext());
                        }
                    }
            );
        }
        else {
            scriptCardViewHolder.shareButton.setVisibility(View.INVISIBLE);
        }

        if (MovieState.DOWNLOADED == movie.getState()) {
            scriptCardViewHolder.playButton.setVisibility(View.VISIBLE);
            scriptCardViewHolder.playButton.setTag(movie);
            scriptCardViewHolder.playButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Context ctx = view.getContext();
                            Intent intent = new Intent(ctx, MovieActivity.class);
                            MovieInflater movieInflater = new MovieInflater();
                            movieInflater.startActivity(movie, ctx, intent);
                        }
                    }
            );
        }
        else {
            scriptCardViewHolder.playButton.setVisibility(View.INVISIBLE);
        }

    }

    public void cleanup() {

    }
}
