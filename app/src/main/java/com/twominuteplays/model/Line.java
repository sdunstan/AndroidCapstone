package com.twominuteplays.model;


import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Line implements Parcelable {

    public static final String LINE_RECORDED = "com.twominuteplays.model.LINE_RECORDED";
    public static final String LINE = "line";
    private final String id;
    private final Integer sortOrder;
    private final String line;
    private final String recordingPath;

    @JsonCreator
    private Line(@JsonProperty("id") String id,
                 @JsonProperty("sortOrder") Integer sortOrder,
                 @JsonProperty("line") String line,
                 @JsonProperty("recordingPath") String recordingPath) {
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

    public static class Builder {
        private String id;
        private Integer sortOrder;
        private String line;
        private String recordingPath;

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
