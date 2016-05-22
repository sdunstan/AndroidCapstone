package com.twominuteplays.video;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.twominuteplays.db.FirebaseStuff;
import com.twominuteplays.model.Line;
import com.twominuteplays.model.Merge;
import com.twominuteplays.model.Part;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Service to asynchronously upload video clips to Google Cloud Storage.
 */
public class ShareService extends IntentService {

    private static final String ACTION_SHARE_PART = "com.twominuteplays.video.action.SHARE_PART";
    private static final String EXTRA_PART = "com.twominuteplays.video.extra.PART";
    private static final String TAG = ShareService.class.getName();

    public ShareService() {
        super("ShareService");
    }

    /**
     * Starts this service to create a merge data structure on Firebase and upload the video
     * for the given part to GCS.
     */
    public static void startActionShare(Context context, Part part) {
        Log.d(TAG, "Starting share service");
        Intent intent = new Intent(context, ShareService.class);
        intent.setAction(ACTION_SHARE_PART);
        intent.putExtra(EXTRA_PART, part);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Handling share intent...");
        if (intent != null) {
            final String action = intent.getAction();
            if (!ACTION_SHARE_PART.equals(action)) {
                Log.w(TAG, "Can't handle this action: " + action);
                return;
            }

            final Part part = intent.getParcelableExtra(EXTRA_PART);
            share(part);
        }
    }

    private void share(final Part part) {

        FirebaseStuff.getFirebase().child("mergeCounter").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long nextval = (Long) mutableData.getValue();
                if (nextval == null) {
                    mutableData.setValue(1000L);
                }
                else {
                    mutableData.setValue(nextval + 1L);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean success, DataSnapshot dataSnapshot) {
                if (success) {
                    shareWithMergeId(dataSnapshot.getValue(Long.class), part);
                }
                else {
                    Log.e(TAG, "Unable to get nextval. " + databaseError.getMessage(), databaseError.toException());
                }
            }
        });



    }

    private void shareWithMergeId(final Long mergeId, final Part part) {
        Log.d(TAG, "Creating merge with ID " + mergeId);
        Merge merge = new Merge();
        merge.setMovieId("movieid");
        merge.setOwner("steve");
        merge.getContributors().put("steve", part);
        FirebaseStuff.getFirebase().child("merges").child(mergeId.toString()).setValue(merge);
        storeVideo(part);
    }

    private void storeVideo(Part part) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference clipsReference = storage.getReferenceFromUrl("gs://twominuteplays.appspot.com").child("clips");

        for(Line line : part.getLines()) {
            if(line.getRecordingPath() != null) {
                uploadClip(clipsReference, line.getRecordingPath());
            }
        }
    }

    private void uploadClip(StorageReference clipsReference, String recordingPath) {
        try {
            File clipFile = new File(recordingPath);
            InputStream inputStream = new FileInputStream(clipFile);
            UploadTask uploadTask = clipsReference.child(clipFile.getName()).putStream(inputStream);
            uploadTask.addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Failed to upload clip.", e);
                        }
                    }
            );
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Clip not found", e);
        }

    }

}
