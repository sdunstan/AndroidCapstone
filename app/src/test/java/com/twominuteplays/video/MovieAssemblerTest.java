package com.twominuteplays.video;

import android.util.Log;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.twominuteplays.model.Line;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(android.util.Log.class)
public class MovieAssemblerTest {

    @Before
    public void setup() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void testAssembleMovie() throws Exception {
        MovieAssembler movieAssembler = new MovieAssembler();
        List<Line> lines = new ArrayList<>();

//        lines.add(line("L1", "1464836509137"));
//        lines.add(line("L2", "1464836514173"));
        lines.add(line("L3", "1464836517429"));
        lines.add(line("L4", "conv"));
//        lines.add(line("L4", "1464836633740"));
//        lines.add(line("L5", "1464836638624"));

        File outputFile = new File(System.getProperty("user.home"), "movie.mp4");
        long size = movieAssembler.assembleMovie(lines, outputFile);

        assertEquals(6124305, size);
    }

//    private static void spitXml(File videoFile)){
//        DataSource ds =  new FileDataSourceImpl(videoFile);
//        IsoFile isoFile = new IsoFile(ds);
//        AppleNameBox nam = Path.getPath(isoFile, "/moov[0]/udta[0]/meta[0]/ilst/©nam");
//        String xml = nam.getValue();
//        isoFile.close();
//        return xml;
//    }

    private Line line(String lineNumber, String id) {
        String path = movieFile("clips-" + lineNumber + "-" + id + ".mp4");
        Line line = new Line.Builder()
                .withRecordingPath(path)
                .build();

        Movie parsedMovie = null;
        try {
            parsedMovie = MovieCreator.build(line.getRecordingPath());
            long timescale = parsedMovie.getTimescale();
            for (Track track : parsedMovie.getTracks()) {
                double w = track.getTrackMetaData().getWidth();
                double h = track.getTrackMetaData().getHeight();
                System.out.println("Line " + lineNumber + " Track data timescale " + timescale + " size " + w + "x" + h);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return line;
    }

    private String movieFile(String fileName) {
        File movieFile = new File(System.getProperty("user.home"), fileName);
        return movieFile.getAbsolutePath();
    }
}