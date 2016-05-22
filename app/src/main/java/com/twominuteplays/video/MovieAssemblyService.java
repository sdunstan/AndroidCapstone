package com.twominuteplays.video;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.twominuteplays.model.Line;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieAssemblyService extends IntentService {

    private static final String TAG = MovieAssemblyService.class.getName();
    public static final String ASSEMBLE_MOVIE = "com.twominuteplays.video.action.ASSEMBLE_MOVIE";;

    public MovieAssemblyService() {
        super("Movie Assembler");
    }

    private long assembleMovie(List<Line> lines, File outputFile) throws IOException {
        // First, break apart tracks of individual clips by type
        Log.i(TAG, "Breaking apart tracks");
        Map<String, List<Track>> trackMap = new HashMap<>();
        for(Line line : lines) {
            if (line.getRecordingPath() != null) {
                Movie parsedMovie = MovieCreator.build(line.getRecordingPath());
                for (Track track : parsedMovie.getTracks()) {
                    addTrack(trackMap, track);
                }
            }
        }

        Log.i(TAG, "Creating resulting movie");
        // Then, reassemble the tracks into one movie by appending all like tracks to each other
        // and adding the result track to the finished movie.
        com.googlecode.mp4parser.authoring.Movie result = new com.googlecode.mp4parser.authoring.Movie();

        // Kind of lame that it must be done this way but the library expects that the second
        // track is the video track instead of checking for the type.
        Log.i(TAG, "Appending audio tracks.");
        List<Track> audioTracks = trackMap.get("soun");
        result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));

        Log.i(TAG, "Appending video tracks.");
        List<Track> videoTracks = trackMap.get("vide");
        result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));

        Log.i(TAG, "Writing movie to file " + outputFile.getAbsolutePath());
        // Finally, write the resulting movie to a file and return the resulting file size.
        FileOutputStream fos = new FileOutputStream(outputFile);
        Container out = (new DefaultMp4Builder()).build(result);
        out.writeContainer(fos.getChannel());
        long size = fos.getChannel().size();
        fos.close();
        return size;
    }

    // Collates tracks by type
    private void addTrack(Map<String, List<Track>> trackMap, Track track) {
        String type = track.getHandler();
        Log.d(TAG, "Adding track to collation map of type " + type);
        List<Track> trackList = trackMap.get(type);
        if(trackList == null) {
            trackList = new ArrayList<>();
            trackMap.put(type, trackList);
        }
        trackList.add(track);
    }

    // Suzanne sat patiently with me while I wrote this. She gets one share of stock as a reward.
    /*
    private void saveToGCS(String token, File movieFile, long size) throws IOException, GeneralSecurityException {
        FileInputStream is = new FileInputStream(movieFile);
        InputStreamContent content = new InputStreamContent("video/mp4", is);
        content.setLength(size);
        StorageObject objectMetadata = new StorageObject()
                // Set the destination object name
                .setName(movieFile.getName())
                // Set the access control list to publicly read-only
                .setAcl(Arrays.asList(
                        new ObjectAccessControl().setEntity("allUsers").setRole("READER")));
        Storage client = getService(token);
        Storage.Objects.Insert insertRequest = client.objects().insert(
                "2mp", objectMetadata, content);
        insertRequest.execute();
    }

    private Storage getService(String token) throws GeneralSecurityException, IOException {
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = new GoogleCredential();
        credential.setAccessToken(token);
        return new Storage.Builder(transport, jsonFactory, credential)
                .setApplicationName("Two Minute Plays")
                .build();
    }
    */

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!ASSEMBLE_MOVIE.equals(intent.getAction()))
            return;
        com.twominuteplays.model.Movie movie = intent.getParcelableExtra("movie");
        List<Line> lines = movie.assembleLines();
        try {
            File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "2mp-" + movie.getId() + ".mp4");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            long size = assembleMovie(lines, outputFile);
            Log.i(TAG, "Created finished move of size " + size);
            movie.merged(outputFile.getAbsolutePath()).save();
            Log.i(TAG, "Saved movie state and path to database.");

            // TODO: cleanup
            // TODO: send to GCS (needs size)
        } catch (IOException e) {
            Log.e(TAG, "Error creating movie file.", e);
        }
    }
}
