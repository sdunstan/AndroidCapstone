package com.twominuteplays.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.twominuteplays.model.Line;
import com.twominuteplays.model.Movie;
import com.twominuteplays.video.MovieAssembler;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MovieAssemblyService extends IntentService {

    private static final String TAG = MovieAssemblyService.class.getName();
    private static final String ASSEMBLE_MOVIE = "com.twominuteplays.service.action.ASSEMBLE_MOVIE";;
    private static final String EXTRA_MOVIE = "com.twominuteplays.services.extra.MOVIE";

    public MovieAssemblyService() {
        super("Movie Assembler");
    }

    public static void startMovieAssemblyService(Context context, Movie movie) {
        if (movie == null)
            throw new IllegalStateException("Movie cannot be null");
        Intent intent = new Intent(context, MovieAssemblyService.class);
        intent.setAction(ASSEMBLE_MOVIE);
        intent.putExtra(EXTRA_MOVIE, movie);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!ASSEMBLE_MOVIE.equals(intent.getAction()))
            return;
        com.twominuteplays.model.Movie movie = intent.getParcelableExtra(EXTRA_MOVIE);

        List<Line> lines = movie.assembleLines();
        try {
            File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "2mp-" + movie.getId() + ".mp4");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            MovieAssembler movieAssembler = new MovieAssembler();
            movieAssembler.assembleMovie(lines, outputFile);
            Log.d(TAG, "Created finished move of size ");
            movie.state.merged(outputFile.getAbsolutePath(), movie).save();
            Log.d(TAG, "Saved movie state and path to database.");
        } catch (IOException e) {
            Log.e(TAG, "Error creating movie file.", e);
        }

        // Cleanup local files.
        // TODO: re-enable after debugging
        /* commented while I debug
        for(final Line line : lines) {
            try {
                File file = new File(line.getRecordingPath());
                if (file.exists() && file.canWrite())
                    file.delete();
            }
            catch(Throwable t) {
                Log.w(TAG, "Clip file could not be deleted.");
            }
        }
        */
    }


}
