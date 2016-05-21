package com.twominuteplays;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.twominuteplays.camera.VideoFragment;
import com.twominuteplays.model.Line;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.Part;
import com.twominuteplays.video.FrameGrabber;
import com.twominuteplays.video.MovieAssemblyService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class RecorderActivity extends BaseActivity {

    private Movie movie;
    private Part part;
    private Integer currentLineIndex = null;
    private String token=null;

    private final String TAG = RecorderActivity.class.getName();
    private ImageView recordImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        recordImageView = (ImageView) findViewById(R.id.recordImageView);
        assert recordImageView != null;
        recordImageView.setVisibility(View.INVISIBLE);

        currentLineIndex = 0;
        Intent intent = getIntent();
        movie = intent.getParcelableExtra("MOVIE");
        part = intent.getParcelableExtra("PART");
        if (movie == null || movie.getState() != Movie.MovieState.RECORDING_STARTED) {
            Log.e(TAG, "Movie is not in the correct state for this activity.");
            finish();
        }
        new LineAdvancedAction().call(currentLineIndex);

        VideoFragment cameraFragment = VideoFragment.newInstance();
        // when line recorded by cameraFragment, call LineRecordedAction
        IntentFilter lineRecordedIntentFilter = new IntentFilter(Line.LINE_RECORDED);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Line line = intent.getParcelableExtra(Line.LINE);
                new LineRecordedAction().call(line);
            }
        },
        lineRecordedIntentFilter);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.cameraFragment, cameraFragment);
        tx.commitAllowingStateLoss();
    }

    @Override
    public void postLoginCreate() {
        token = mFirebaseRef.getAuth().getToken();
    }

    @SuppressWarnings("unused")
    public void toggleRecordStop(View view) {
        Log.d(TAG, "Toggle record/stop");
        Button button = (Button) view;
        VideoFragment cameraFragment = (VideoFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
        cameraFragment.toggleRecording(part.getLines().get(currentLineIndex));
        if (button.getText().equals(getResources().getString(R.string.record))) {
            button.setText(getResources().getString(R.string.stop));
            recordImageView.setVisibility(View.VISIBLE);
        }
        else {
            Log.d(TAG, "Stopping recording... total lines is " + part.getLines().size());
            recordImageView.setVisibility(View.INVISIBLE);
            if ((currentLineIndex+1) < part.getLines().size()) {
                new LineAdvancedAction().call(++currentLineIndex);
                Log.d(TAG, "Advancing line to " + currentLineIndex);
                button.setText(getResources().getString(R.string.record));
            }
            else {
                movie = movie.recorded();
                if (token != null) {
                    Intent assemblyServiceIntent = new Intent(this, MovieAssemblyService.class);
                    assemblyServiceIntent.putExtra("movie", movie);
                    assemblyServiceIntent.putExtra("token", token);
                    startService(assemblyServiceIntent);
                }
                finish();
            }
        }
    }

    public final class LineAdvancedAction {
        public void call(Integer lineIndex) {
            Log.d(TAG, "IT WORKS! Got callback for currentLineIndex");
            Line currentLine = part.getLines().get(lineIndex);
            TextView lineTextView = (TextView) findViewById(R.id.currentLineTextView);
            lineTextView.setText(currentLine.getLine());
        }
    }

    public final class LineRecordedAction {
        public void call(Line line) {
            // TODO: move this off the UI thread
            movie = movie.addVideo(part.getId(), line.getId(), line.getRecordingPath());
            if (movie.getImageUrl() == null) {
                File pngPath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "coverart-" + movie.getId() + ".png");
                if (pngPath != null) {
                    Bitmap frameBitamp = FrameGrabber.getVideoFrame(line.getRecordingPath());
                    try {
                        if (frameBitamp.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(pngPath))) {
                            movie = movie.addImageUrl(pngPath.getAbsolutePath());
                        }
                    } catch (FileNotFoundException e) {
                    }
                }
            }
        }
    }

}
