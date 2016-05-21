package com.twominuteplays.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Part implements Parcelable {

    private final String id;
    private final String characterName;
    private final String description;
    private final List<Line> lines;
    private final String actorUid;

    @JsonCreator
    private Part(@JsonProperty("id") String id,
                 @JsonProperty("characterName") String characterName,
                 @JsonProperty("description") String description,
                 @JsonProperty("lines") List<Line> lines,
                 @JsonProperty("actorUid") String actorUid) {
        this.id = id;
        this.characterName = characterName;
        this.description = description;
        this.lines = lines;
        this.actorUid = actorUid;
    }

    private Part(Parcel in) {
        id = in.readString();
        characterName = in.readString();
        description = in.readString();
        List<Line> linesPrototype = new ArrayList<>();
        in.readList(linesPrototype, this.getClass().getClassLoader());
        lines = Collections.unmodifiableList(linesPrototype);
        actorUid = in.readString();
    }

    public static final Parcelable.Creator<Part> CREATOR =
            new Parcelable.Creator<Part>() {

                @Override
                public Part createFromParcel(Parcel parcel) {
                    return new Part(parcel);
                }

                @Override
                public Part[] newArray(int size) {
                    return new Part[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(characterName);
        parcel.writeString(description);
        parcel.writeList(lines);
        parcel.writeString(actorUid);
    }

    public String getId() {
        return id;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getDescription() {
        return description;
    }

    public List<Line> getLines() { return lines; }

    public String getActorUid() { return actorUid; }

    public Part addVideo(String lineId, String videoUrl) {
        Builder builder = new Builder();
        builder.withDescription(getDescription())
                .withId(getId())
                .withCharacterName(getCharacterName())
                .withActorUid(getActorUid());
        for(Line line : getLines()) {
            if (line.getId().equals(lineId)) {
                builder.addLine(line.addVideoUrl(videoUrl));
            }
            else {
                builder.addLine(line);
            }
        }
        return builder.build();
    }

    @JsonIgnore
    public boolean isRecorded() {
        for (Line line : getLines()) {
            if (line.getRecordingPath() == null)
                return false;
        }
        return true;
    }

    public static class Builder {
        public String id;
        public String characterName;
        public String description;
        public List<Line> lines;
        public String actorUid;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withCharacterName(String name) {
            this.characterName = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        private List<Line> getLines() {
            if (lines == null) {
                lines = new ArrayList<>();
            }
            return lines;
        }

        public Builder addLine(Line line) {
            getLines().add(line);
            return this;
        }

        public Builder withActorUid(String actorUid) {
            this.actorUid = actorUid;
            return this;
        }

        public Part build() {
            return new Part(id, characterName, description, getLines(), actorUid);
        }

        public Builder withLines(List<Line> lines) {
            for (Line line : lines) {
                getLines().add(line);
            }
            return this;
        }
    }


}
