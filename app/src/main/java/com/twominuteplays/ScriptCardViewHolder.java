package com.twominuteplays;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.twominuteplays.model.Movie;

public class ScriptCardViewHolder extends RecyclerView.ViewHolder {
    public Movie movie;
    public TextView scriptTitleTextView;
    public TextView synopsisTextView;
    public ImageView scriptImageView;
    public Button shareButton;
    public Button playButton;

    public ScriptCardViewHolder(final View cardView) {
        super(cardView);
        scriptTitleTextView = (TextView) cardView.findViewById(R.id.scriptTitle);
        synopsisTextView = (TextView) cardView.findViewById(R.id.synopsisTextView);
        scriptImageView = (ImageView) cardView.findViewById(R.id.scriptImageView);
        shareButton = (Button) cardView.findViewById(R.id.shareButton);
        playButton = (Button) cardView.findViewById(R.id.playButton);
    }
}
