package com.twominuteplays.video;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.boxes.apple.AppleNameBox;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MetaDataRead {
    public static void main(String[] args) throws IOException {
        MetaDataRead cmd = new MetaDataRead();
        String xml = cmd.read();
        System.err.println(xml);
    }

    public String read() throws IOException {

        File videoFile = line("L3", "1464836517429");
        // File file2 = line("L4", "1464836633740");

        if (!videoFile.exists()) {
            throw new FileNotFoundException("File does not exist");
        }

        if (!videoFile.canRead()) {
            throw new IllegalStateException("No read permissions to file ");
        }
        DataSource ds =  new FileDataSourceImpl(videoFile);
        IsoFile isoFile = new IsoFile(ds);
        AppleNameBox nam = Path.getPath(isoFile, "/moov[0]/udta[0]/meta[0]/ilst/©nam");
        String xml = nam.getValue();
        isoFile.close();
        return xml;
    }

    private File line(String lineNumber, String id) {
        return movieFile("clips-" + lineNumber + "-" + id + ".mp4");
    }

    private File movieFile(String fileName) {
        return new File(System.getProperty("user.home"), fileName);
    }

}
