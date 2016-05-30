package com.twominuteplays;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import static com.twominuteplays.model.MovieState.TEMPLATE;

public class ClickableScriptCardViewHolder extends ScriptCardViewHolder {
    public ClickableScriptCardViewHolder(final View cardView) {
        super(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != movie) {
                    Context ctx;
                    ctx = view.getContext();
                    Intent intent = new Intent(ctx, PlayViewActivity.class);
                    if (TEMPLATE == movie.getState()) {
                        movie = movie.state.select(movie);
                    }
                    intent.putExtra("MOVIE", movie);
                    ctx.startActivity(intent);
                }
            }
        });
    }
}
