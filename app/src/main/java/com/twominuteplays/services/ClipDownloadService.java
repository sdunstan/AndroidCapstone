package com.twominuteplays.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.twominuteplays.model.Contributions;
import com.twominuteplays.model.Line;
import com.twominuteplays.model.Movie;
import com.twominuteplays.model.MovieState;
import com.twominuteplays.model.Part;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class ClipDownloadService extends IntentService {
    private static final String ACTION_DOWNLOAD_CLIPS = "com.twominuteplays.services.action.DOWNLOAD_CLIPS";

    private static final String EXTRA_MOVIE = "com.twominuteplays.video.extra.MOVIE";
    private static final String EXTRA_CONTRIBUTIONS = "com.twominuteplays.video.extra.CONTRIBUTIONS";

    private static final String TAG = ClipDownloadService.class.getName();


    public ClipDownloadService() {
        super("ClipDownloadService");
    }

    /**
     * <p>
     * After recording their part (or maybe during recording), the cloned movie will still have
     * the share ID in it. It is now time to start downloading the other guy's clips to the
     * this device. Once each one is downloaded and saved, they can be combined with the
     * locally recorded clips.
     * </p><p>
     * Given the destination movie, this service will download all the clips from the
     * Contributions object. The movie is updated with the local clip locations.
     * </p>
     */
    public static void startActionDownloadClips(final Context context, final Contributions contributions, final Movie destinationMovie) {
        if (contributions == null)
            throw new IllegalStateException("Contributions cannot be null");
        if (destinationMovie == null)
            throw new IllegalStateException("Movie cannot be null");
        Intent intent = new Intent(context, ClipDownloadService.class);
        intent.setAction(ACTION_DOWNLOAD_CLIPS);
        intent.putExtra(EXTRA_CONTRIBUTIONS, contributions);
        intent.putExtra(EXTRA_MOVIE, destinationMovie);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD_CLIPS.equals(action)) {
                Movie movie = intent.getParcelableExtra(EXTRA_MOVIE);
                final Contributions contributions = intent.getParcelableExtra(EXTRA_CONTRIBUTIONS);
                if (MovieState.SHARE_CLONED == movie.getState() || MovieState.CONTRIBUTED == movie.getState()) {
                    movie = movie.state.downloading(movie);
                    beginClipsDownload(contributions, movie);
                }
            }
        }
    }

    private Movie beginClipsDownload(final Contributions contributions, Movie movie) {
        try {
            Log.d(TAG, "Downloading for part " + contributions.getPartId() + " to movie " + movie.getId());
            final Part part = movie.findPart(contributions.getPartId());
            // Downloads the clips one at a time.
            for(Line line : part.getLines()) {
                File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                        movie.getId() + "-" + line.getId() + ".mp4");
                downloadClip(outputFile, contributions.getClips().get(line.getId()));
                movie = movie.addVideo(contributions.getPartId(), line.getId(), outputFile.getAbsolutePath());
            }
        }
        catch(Throwable t) {
            Log.e(TAG, "Error while downloading clips.", t);
        }
        finally {
            if (movie.isRecorded()) {
                Log.d(TAG, "Cleaning up. All lines recorded.");
                movie.state.downloaded(movie);
            }
            else {
                Log.d(TAG, "Something went wrong. Reverting downloading state.");
//                movie.state.revertDownloading(movie);
            }
        }
        return movie;
    }

    private boolean downloadClip(File outputFile, String downloadUrl) {
        Log.d(TAG, "Downloading " + downloadUrl + " to " + outputFile.getPath());
        boolean ok = false;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference clipsStorageReference = storage.getReferenceFromUrl(downloadUrl);
        FileDownloadTask task = clipsStorageReference.getFile(outputFile);
        try {
            Tasks.await(task);
            ok = task.isSuccessful();
        } catch (ExecutionException e) {
            Log.e(TAG, "Exception downloading file. " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interruption downloading file. " + e.getMessage(), e);
        }
        return ok;
    }

}
