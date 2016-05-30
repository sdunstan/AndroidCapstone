package com.twominuteplays.model;


import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Line implements Parcelable {

    public static final String LINE_RECORDED = "com.twominuteplays.model.LINE_RECORDED";
    public static final String LINE = "line";
    private final String id;
    private final Integer sortOrder;
    private final String line;
    private final String recordingPath;

    private Line(String id,
                 Integer sortOrder,
                 String line,
                 String recordingPath) {
        this.id = id;
        this.sortOrder = sortOrder;
        this.line = line;
        this.recordingPath = recordingPath;
    }


    public String getId() {
        return id;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public String getLine() {
        return line;
    }

    public String getRecordingPath() { return recordingPath; }

    public Line addVideoUrl(String videoUrl) {
        Builder builder = new Builder();
        return builder.withLine(getLine())
                .withSortOrder(getSortOrder())
                .withId(getId())
                .withRecordingPath(videoUrl)
                .build();
    }

    public void broadcastRecorded(Context context) {
        Intent broadcastRecordedIntent = new Intent(LINE_RECORDED);
        broadcastRecordedIntent.putExtra(LINE, this);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastRecordedIntent);
    }

    public Line cloneLineForContribute() {
        return new Builder()
                .withLine(getLine())
                .withRecordingPath(null)
                .withSortOrder(getSortOrder())
                .withId(getId())
                .withSortOrder(getSortOrder())
                .build();
    }

    @Exclude
    public boolean hasMovieClip() {
        return (recordingPath != null && !recordingPath.isEmpty());
    }

    public Line cloneOwnerLine() {
        return new Builder()
                .withLine(getLine())
                .withRecordingPath("-")
                .withSortOrder(getSortOrder())
                .withId(getId())
                .withSortOrder(getSortOrder())
                .build();
    }

    public static class Builder {
        private String id;
        private Integer sortOrder;
        private String line;
        private String recordingPath;

        public Builder withJson(Map<String, Object> partMap) {
            this.id = (String)partMap.get("id");
            this.sortOrder = ((Long)partMap.get("sortOrder")).intValue();
            this.line = (String)partMap.get("line");
            this.recordingPath = (String)partMap.get("recordingPath");
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder withLine(String line) {
            this.line = line;
            return this;
        }

        public Builder withRecordingPath(String recordingPath) {
            this.recordingPath = recordingPath;
            return this;
        }

        public Line build() {
            return new Line(id, sortOrder, line, recordingPath);
        }

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeValue(this.sortOrder);
        dest.writeString(this.line);
        dest.writeString(this.recordingPath);
    }

    protected Line(Parcel in) {
        this.id = in.readString();
        this.sortOrder = (Integer) in.readValue(Integer.class.getClassLoader());
        this.line = in.readString();
        this.recordingPath = in.readString();
    }

    public static final Parcelable.Creator<Line> CREATOR = new Parcelable.Creator<Line>() {
        @Override
        public Line createFromParcel(Parcel source) {
            return new Line(source);
        }

        @Override
        public Line[] newArray(int size) {
            return new Line[size];
        }
    };
}
