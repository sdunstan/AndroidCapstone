package com.twominuteplays.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.twominuteplays.R;
import com.twominuteplays.db.FirebaseStuff;
import com.twominuteplays.model.Contributions;
import com.twominuteplays.model.Line;
import com.twominuteplays.model.Part;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service to asynchronously upload video clips to Google Cloud Storage.
 */
public class ShareService extends IntentService {

    private static final String ACTION_SHARE_OWNER_PART = "com.twominuteplays.video.action.SHARE_OWNER_PART";
    private static final String ACTION_SHARE_CONTRIBUTOR_PART = "com.twominuteplays.video.action.SHARE_CONTRIBUTOR_PART";
    private static final String EXTRA_SHARE_ID = "com.twominuteplays.video.extra.SHARE_ID";
    private static final String EXTRA_CONTRIBUTOR_UID = "com.twominuteplays.video.extra.CONTRIBUTOR_UID";
    private static final String EXTRA_PART = "com.twominuteplays.video.extra.PART";
    private static final String TAG = ShareService.class.getName();

    public ShareService() {
        super("ShareService");
    }

    /**
     * Starts this service to upload the owner's video clips to GCS and then update the Share
     * data structure.
     */
    public static void saveOwnersClipsToGCS(Context context, final Long shareId, final Part ownerPart) {
        Log.d(TAG, "Starting share owner clips service");
        Intent intent = new Intent(context, ShareService.class);
        intent.setAction(ACTION_SHARE_OWNER_PART);
        intent.putExtra(EXTRA_SHARE_ID, shareId);
        intent.putExtra(EXTRA_PART, ownerPart);
        context.startService(intent);
    }

    /**
     * Starts this service to upload a contributors's video clips to GCS and then update the Share
     * data structure.
     */
    public static void saveContributorClipsToGCS(Context context, final Long shareId, final String contributorUid, final Part contributorPart) {
        Log.d(TAG, "Starting share owner clips service");
        Intent intent = new Intent(context, ShareService.class);
        intent.setAction(ACTION_SHARE_CONTRIBUTOR_PART);
        intent.putExtra(EXTRA_SHARE_ID, shareId);
        intent.putExtra(EXTRA_CONTRIBUTOR_UID, contributorUid);
        intent.putExtra(EXTRA_PART, contributorPart);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Handling share intent...");
        if (intent != null) {
            final Long shareId = intent.getLongExtra(EXTRA_SHARE_ID, -1);
            final String uid = intent.getStringExtra(EXTRA_CONTRIBUTOR_UID);
            final Part part = intent.getParcelableExtra(EXTRA_PART);
            final String action = intent.getAction();
            if (shareId == -1) {
                Log.w(TAG, "No share ID was passed in.");
                return;
            }
            try {
                if (ACTION_SHARE_OWNER_PART.equals(action)) {
                    Log.d(TAG, "Uploading owner clips.");
                    shareOwnerClips(shareId.toString(), part);
                    return;
                }
                if (ACTION_SHARE_CONTRIBUTOR_PART.equals(action)) {
                    shareContributorClips(shareId.toString(), uid, part);
                    return;
                }
            }
            catch (ExecutionException e) {
                Log.e(TAG, "Error during clip upload.", e);
            }
            catch (InterruptedException e) {
                Log.e(TAG, "Interrupted during clip upload.", e);
            }
            Log.w(TAG, "No matching action in intent.");
        }
    }

    private void shareOwnerClips(final String shareId, final Part part) throws ExecutionException, InterruptedException {
        DatabaseReference clipCollectionRef = FirebaseStuff.getShareRef(shareId)
                .child(getString(R.string.ownersClipsNode));
        storeVideo(clipCollectionRef, part);
        Log.d(TAG, "All clips uploaded for share ID " + shareId);
    }

    private void shareContributorClips(final String shareId, final String uid, final Part part) throws ExecutionException, InterruptedException {
        DatabaseReference clipCollectionRef = FirebaseStuff.getShareRef(shareId)
                .child(getString(R.string.contributorsNode))
                .child(uid);
        storeVideo(clipCollectionRef, part);
    }

    private void storeVideo(final DatabaseReference clipDBReference, Part part) throws ExecutionException, InterruptedException {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // TODO: externalize this ref to GCS
        StorageReference clipsStorageReference = storage.getReferenceFromUrl("gs://twominuteplays.appspot.com")
                .child(getString(R.string.clipsNode));

        Map<String, UploadTask> uploadTasks = new HashMap<>();
        for(Line line : part.getLines()) {
            if(line.getRecordingPath() != null) {
                UploadTask task = uploadClip(clipsStorageReference, line.getRecordingPath());
                if (task != null)
                    uploadTasks.put(line.getId(), task);
            }
        }

        Log.d(TAG, "Waiting for " + uploadTasks.size() + " upload task(s) to complete.");
        Contributions contributions = new Contributions();
        for(String lineId : uploadTasks.keySet()) {
            UploadTask task = uploadTasks.get(lineId);
            Tasks.await(task);
            Uri uri = task.getResult().getDownloadUrl();
            if (uri != null) {
                Log.d(TAG, "Adding contribution for line id " + lineId + " " + uri.toString());
                contributions.getClips().put(lineId, uri.toString());
            }
        }
        clipDBReference.setValue(contributions);

    }

    private synchronized UploadTask uploadClip(final StorageReference clipsStorageReference, final String recordingPath) {
        try {
            File clipFile = new File(recordingPath);
            InputStream inputStream = new FileInputStream(clipFile);
            return clipsStorageReference.child(clipFile.getName()).putStream(inputStream);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Clip not found", e);
        }
        return null;
    }

}
