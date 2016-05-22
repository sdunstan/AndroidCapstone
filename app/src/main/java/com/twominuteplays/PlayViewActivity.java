package com.twominuteplays;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.twominuteplays.db.FirebaseStuff;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.Part;

import java.util.Map;

public class PlayViewActivity extends BaseActivity {

    private static final String TAG = PlayViewActivity.class.getName();

    private Movie movie;
    private ProgressDialog progressDialog;

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "Restarting, attempting to refresh movie state.");
        if (movie != null) {
            DatabaseReference movieRef = FirebaseStuff.getMovieRef(movie.getId());
            if (movieRef != null) {
                loadMovie(movieRef);
            }
        }
    }

    private void loadMovie(DatabaseReference movieRef) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Grabbing your movie...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();

        movieRef.addListenerForSingleValueEvent(new LoadMovieEventListener());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_view);

        movie = getIntent().getParcelableExtra("MOVIE");
    }

    @Override
    public void postLoginCreate() {
        if (movie != null) {
            setViewFromMovie();
        } else {
            Intent intent = getIntent();
            Uri data = intent.getData();
            String path[] = data.getPath().split("/");
            String uid = null;
            String movieId = null;
            if (path.length >= 4 && "play".equals(path[1])) {
                // got 2mp.tv intent
                uid = path[2];
                movieId = path[3];
            } else if ("twominuteplays".equals(data.getScheme())) {
                uid = path[1];
                movieId = path[2];
            }
            // TODO: lookup real UID from fake mapping passed in on uid parameter,
            // as is this is only single user
            Log.i(TAG, "Grabbing movie for UID/movieID " + uid + " " + movieId);
            DatabaseReference movieRef = FirebaseStuff.getMovieRef(movieId);
            Log.i(TAG, "Getting movie for " + movieRef.toString());

            loadMovie(movieRef);
        }
    }

    private void configurePartButton(Button button, final Part part) {
        button.setText(part.getCharacterName());
        if (!part.isRecorded() ) { // TODO: isRecorded is kind of a dumb way to check if the button should be available, use movie state?
            button.setEnabled(true);
            button.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onPartClicked(part);
                        }
                    }
            );
        } else {
            button.setEnabled(false);
        }
    }

    private void setViewFromMovie() {
        TextView playTitle = (TextView) findViewById(R.id.play_title_textView);
        assert playTitle != null;
        playTitle.setText(movie.getTitle());
        TextView synopsis = (TextView) findViewById(R.id.synopsisTextView);
        assert synopsis != null;
        synopsis.setText(movie.getSynopsis());

        Part part1 = movie.getParts().get(0);
        Button part1Button = (Button) findViewById(R.id.part1_Button);
        configurePartButton(part1Button, part1);


        Part part2 = movie.getParts().get(1);
        Button part2Button = (Button) findViewById(R.id.part2_Button);
        configurePartButton(part2Button, part2);

        TextView part1Description = (TextView) findViewById(R.id.partDescription1_textView);
        assert part1Description != null;
        part1Description.setText(part1.getDescription());

        TextView part2Description = (TextView) findViewById(R.id.partDescription2_textView);
        assert part2Description != null;
        part2Description.setText(part2.getDescription());

        TextView playContent = (TextView) findViewById(R.id.playContentTextView);
        assert playContent != null;
        playContent.setMovementMethod(new ScrollingMovementMethod());
        playContent.setText(Html.fromHtml(movie.getScriptMarkup()));

        // TODO: other state based view adaptations
        FloatingActionButton playFab = (FloatingActionButton) findViewById(R.id.playMovieFAB);
        assert playFab != null;
        if (Movie.MovieState.MERGED == movie.getState()) playFab.setVisibility(View.VISIBLE);
        else playFab.setVisibility(View.INVISIBLE);
    }

    private void onPartClicked(Part part) {
        Intent intent = new Intent(this, RecorderActivity.class);
        movie = movie.selectPart(part);
        movie = movie.startRecording();
        intent.putExtra("MOVIE", movie);
        intent.putExtra("PART", part);
        startActivity(intent);
    }

    public void onPlay(View view) {
        Intent intent = new Intent(this, MovieActivity.class);
        intent.putExtra("MOVIE", movie);
        startActivity(intent);
    }

    private class LoadMovieEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Map<String,Object> jsonSnapshot = (Map<String, Object>) dataSnapshot.getValue();
            Movie.Builder builder = new Movie.Builder();
            movie = builder.withJson(jsonSnapshot).build();
            Log.i(TAG, "Loaded movie from database. State is " + movie.getState());
            setViewFromMovie();
            dismissLoadingDialog();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            dismissLoadingDialog();
            // TODO: show an error an pop to the main view
        }
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
