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
import com.twominuteplays.model.MovieBuilder;
import com.twominuteplays.model.MovieState;
import com.twominuteplays.model.Part;
import com.twominuteplays.model.Share;

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
                showLoadingDialog();
                movieRef.addListenerForSingleValueEvent(new LoadMovieEventListener());
            }
        }
    }

    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Grabbing your movie...");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.show();
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
            return;
        }

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data == null) {
            Log.i(TAG, "No intent data. PlayView will not be initialized.");
            return;
        }

        String path[] = data.getPath().split("/");

        if (path.length <= 0) {
            Log.i(TAG, "No path information in data part of Uri. PlayView will not be initialized. Data path is " + data.getPath());
            return;
        }

        String shareId = path[path.length-1];
        Log.i(TAG, "Grabbing movie for Share ID " + shareId);
        DatabaseReference shareRef = FirebaseStuff.getShareRef(shareId);
        Log.i(TAG, "Getting movie for " + shareRef.toString());
        shareRef.addListenerForSingleValueEvent(new LoadShareEventListener());
    }

    private void configurePartButton(Button button, final Part part) {
        button.setText(part.getCharacterName());
        if (!part.isRecorded() && movie.state.isRecordable()) {
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
        if (MovieState.MERGED == movie.getState()) playFab.setVisibility(View.VISIBLE);
        else playFab.setVisibility(View.INVISIBLE);
    }

    private void onPartClicked(Part part) {
        Intent intent = new Intent(this, RecorderActivity.class);
        movie = movie.state.selectPart(movie, part);
        movie = movie.state.startRecording(movie);
        intent.putExtra("MOVIE", movie);
        intent.putExtra("PART", part);
        startActivity(intent);
    }

    public void onPlay(View view) {
        Intent intent = new Intent(this, MovieActivity.class);
        intent.putExtra("MOVIE", movie);
        startActivity(intent);
    }

    private class LoadShareEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Share share = dataSnapshot.getValue(Share.class);
            DatabaseReference movieRef = FirebaseStuff.getFirebase().child(share.getOriginalMoviePath());
            showLoadingDialog();
            movieRef.addListenerForSingleValueEvent(new LoadMovieForShareEventListener(share));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            dismissLoadingDialog();
            Log.e(TAG, "Could not load share. " + databaseError.getMessage(), databaseError.toException());
            // TODO: show an error an pop to the main view
        }
    }

    private class LoadMovieForShareEventListener implements ValueEventListener {
        private final Share share;

        public LoadMovieForShareEventListener(Share share) {
            this.share = share;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Map<String,Object> jsonSnapshot = (Map<String, Object>) dataSnapshot.getValue();
            MovieBuilder builder = new MovieBuilder();
            movie = builder.withJson(jsonSnapshot).build();
            Log.i(TAG, "Loaded movie from database. State is " + movie.getState());
            movie = movie.state.contribute(movie, share);
            setViewFromMovie();
            dismissLoadingDialog();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            dismissLoadingDialog();
            Log.e(TAG, "Could not load movie. " + databaseError.getMessage(), databaseError.toException());
            // TODO: show an error an pop to the main view
        }
    }

    private class LoadMovieEventListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Map<String,Object> jsonSnapshot = (Map<String, Object>) dataSnapshot.getValue();
            MovieBuilder builder = new MovieBuilder();
            movie = builder.withJson(jsonSnapshot).build();
            Log.i(TAG, "Loaded movie from database. State is " + movie.getState());
            setViewFromMovie();
            dismissLoadingDialog();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            dismissLoadingDialog();
            Log.e(TAG, "Could not load movie. " + databaseError.getMessage(), databaseError.toException());
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
