package com.twominuteplays;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.twominuteplays.db.FirebaseStuff;
import com.twominuteplays.db.ShareUtility;
import com.twominuteplays.model.Line;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.MovieState;
import com.twominuteplays.model.Part;
import com.twominuteplays.services.ShareService;
import com.twominuteplays.video.FrameGrabber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Iterator;

public class LineActivity extends BaseActivity {
    private static final String TAG = LineActivity.class.getName();

    private static final int LINE_RECORDED = 31415;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private Movie mMovie = null;
    private Part mPart = null;
    private Line mCurrentLine = null;
    private Button mCurrentButton = null;
    private File mOutputFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line);

        mPart = getIntent().getParcelableExtra("PART");
        mMovie = getIntent().getParcelableExtra("MOVIE");

        TextView partTextView = (TextView) findViewById(R.id.partTextView);
        if (partTextView != null) {
            partTextView.setText(mPart.getCharacterName());
        }

        setupButtons(mPart.getLines().iterator(),
                R.id.line1Button,
                R.id.line2Button,
                R.id.line3Button,
                R.id.line4Button,
                R.id.line5Button,
                R.id.line6Button);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("MOVIE", mMovie);
        outState.putParcelable("PART", mPart);

        super.onSaveInstanceState(outState);
    }

    private void setupButtons(Iterator<Line> lines, Integer...ids) {
        for(Integer id : ids) {
            Button button = (Button) findViewById(id);
            if (button != null) {
                if (lines.hasNext()) {
                    button.setVisibility(View.VISIBLE);
                    Line line = lines.next();
                    button.setText(line.getLine());
                    if (line.hasMovieClip()) {
                        button.setEnabled(false);
                    }
                    else {
                        button.setEnabled(true);
                        button.setOnClickListener(new RecordVideoClickHandler(line));
                    }
                }
                else {
                    button.setVisibility(View.INVISIBLE);
                }
            }
        }
        if (lines.hasNext()) {
            Log.e(TAG, "Line view cannot handle all the lines for this movie!");
        }
    }

    @Override
    public void postLoginCreate() {

    }

    private boolean cameraPermissionCheck() {
        boolean ok = false;
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showCameraPermissionsDialog();
            }
            else {
                // Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ok = true;
        }


        return ok;
    }

    private void recordLine() {
        if (mCurrentLine == null)
            return;
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // EXTRA_OUTPUT, EXTRA_VIDEO_QUALITY, EXTRA_SIZE_LIMIT, EXTRA_DURATION_LIMIT
        cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 20);
        StringBuilder fileName = new StringBuilder(mCurrentLine.getId());
        fileName.append("-").append(new Date().getTime()).append(".mp4");
        mOutputFile = new File(getMediaDirectory(), fileName.toString());
        Uri outputFileUri = Uri.fromFile(mOutputFile);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(cameraIntent, LINE_RECORDED);

    }

    private File getMediaDirectory() {
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "2mp");
        if (!dir.exists()) {
            if (! dir.mkdirs() ) {
                Log.e(TAG, "Could not create directory " + dir.toString());
                // TODO: fallback
            }
        }
        return dir;
    }

    private class RecordVideoClickHandler implements Button.OnClickListener {
        private final Line line;

        public RecordVideoClickHandler(Line line) {
            this.line = line;
        }

        @Override
        public void onClick(View view) {
            mCurrentButton = (Button) view;
            mCurrentLine = line;
            if (!cameraPermissionCheck())
                return;
            recordLine();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (LINE_RECORDED == requestCode && resultCode == RESULT_OK) {
            Log.d(TAG, "mOutputFile " + mOutputFile);
            Log.d(TAG, "File exists? " + mOutputFile.exists());
            if (data != null) { // doesn't come back on crappy phones
                Uri filePath = data.getData();
                Log.d(TAG, "Got result for " + filePath);
                if (filePath != null && mCurrentLine != null) {
                    mMovie = mMovie.addVideo(mPart.getId(), mCurrentLine.getId(), mOutputFile.getAbsolutePath())
                            .save();
                    addImage(filePath); // If the current movie does not have an image, this will add one.
                    mPart = mMovie.findPart(mPart.getId());
                    mCurrentButton.setEnabled(false);
                    completeCheck();
                }
                Log.d(TAG, "extra output " + data.getParcelableExtra(MediaStore.EXTRA_OUTPUT));
            }
            else if (mOutputFile != null && mOutputFile.exists()) {
                mMovie = mMovie.addVideo(mPart.getId(), mCurrentLine.getId(), mOutputFile.getAbsolutePath())
                        .save();
                addImage(mOutputFile); // If the current movie does not have an image, this will add one.
                mPart = mMovie.findPart(mPart.getId());
                mCurrentButton.setEnabled(false);
                completeCheck();
            }
        }

    }

    private void addImage(File clipFile) {
        if (mMovie.getImageUrl() == null) {
            File pngPath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "coverart-" + mMovie.getId() + ".png");
            if (pngPath != null) {
                Bitmap frameBitamp = FrameGrabber.getVideoFrame(clipFile.getAbsolutePath());
                try {
                    if (frameBitamp != null && frameBitamp.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(pngPath))) {
                        mMovie = mMovie.addImageUrl(pngPath.getAbsolutePath()).save();
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found when trying to create and save movie image.");
                }
            }
        }
    }

    private void addImage(Uri videoUri) {
        if (mMovie.getImageUrl() == null) {
            File pngPath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "coverart-" + mMovie.getId() + ".png");
            if (pngPath != null) {
                Bitmap frameBitamp = FrameGrabber.getVideoFrame(this, videoUri);
                try {
                    if (frameBitamp != null && frameBitamp.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(pngPath))) {
                        mMovie = mMovie.addImageUrl(pngPath.getAbsolutePath()).save();
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found when trying to create and save movie image.");
                }
            }
        }
    }

    private void completeCheck() {
        if (mPart.isRecorded()) {
            if (mMovie.getState() == MovieState.RECORDING_STARTED) {
                mMovie = mMovie.state.recorded(mMovie);
                ShareUtility.shareMovie(this, mMovie);
            }

            if (mMovie.getState() == MovieState.CONTRIBUTE) {
                mMovie = mMovie.state.contributed(mMovie);
                ShareService.saveContributorClipsToGCS(this, mMovie.getShareId(), FirebaseStuff.getUid(), mPart);
            }
            finish();
        }
    }

    private void showCameraPermissionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("To record your part, you need to use your device's camera. Allow Two Minute Plays to access the camera?");
        builder.setTitle("Camera Permissions Check");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int ID) {
                cameraPermissionCheck();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // ?
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recordLine();
                }
                break;
            default:
                break;
        }
    }
}
