package com.twominuteplays.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.twominuteplays.db.FirebaseStuff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Part implements Parcelable {

    private final String id;
    private final String characterName;
    private final String description;
    private final List<Line> lines;
    private final String actorUid;

    private Part(String id,
                 String characterName,
                 String description,
                 List<Line> lines,
                 String actorUid) {
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

    @Exclude
    public boolean isRecorded() {
        boolean recorded = true;
        for (Line line : getLines()) {
            if (!line.hasMovieClip()) {
                recorded = false;
                break;
            }
        }
        return recorded;
    }

    @Exclude
    public boolean isLastLine(Line line) {
        boolean isLast = false;
        if (lines != null && !lines.isEmpty() && line != null) {
            Line lastLine = lines.get(lines.size()-1);
            isLast = lastLine.getId().equals(line.getId());
        }
        return isLast;
    }

    public Part clonePartForContribute() {
        return new Builder()
                .withActorUid(FirebaseStuff.getUid())
                .withCharacterName(getCharacterName())
                .withDescription(getDescription())
                .withId(getId())
                .withLines(cloneLinesForContribute())
                .build();
    }

    public Part cloneOwnerPart() {
        return new Builder()
                .withActorUid(getActorUid())
                .withCharacterName(getCharacterName())
                .withDescription(getDescription())
                .withId(getId())
                .withLines(cloneOwnerLines())
                .build();
    }

    private List<Line> cloneOwnerLines() {
        List<Line> linesClone = new ArrayList<>();
        for(final Line line : lines) {
            linesClone.add(line.cloneOwnerLine());
        }
        return linesClone;
    }

    private List<Line> cloneLinesForContribute() {
        List<Line> linesClone = new ArrayList<>();
        for(final Line line : lines) {
            linesClone.add(line.cloneLineForContribute());
        }
        return linesClone;
    }

    @Exclude
    public Line findLine(String lineId) {
        for(Line line : lines) {
            if (line.getId().equals(lineId)) {
                return line;
            }
        }
        return null;
    }

    @Exclude
    public Integer getCurrentLineIndex() {
        int currentLineIndex = 0;
        for(Line line : lines) {
            if(!line.hasMovieClip())
                break;
            currentLineIndex++;
        }
        if(currentLineIndex >= lines.size())
            throw new IllegalStateException("All lines have been recorded.");
        return currentLineIndex;
    }

    public static class Builder {
        public String id;
        public String characterName;
        public String description;
        public List<Line> lines;
        public String actorUid;

        public Builder withJson(Map<String, Object> partMap) {
            this.id = (String)partMap.get("id");
            this.characterName = (String)partMap.get("characterName");
            this.description = (String)partMap.get("description");
            this.actorUid = (String)partMap.get("actorUid");
            marshalLines(partMap);
            return this;
        }

        private void marshalLines(Map<String, Object> partMap) {
            for(Object lineObject : (Iterable)partMap.get("lines")) {
                Map<String,Object> lineMap = (Map<String, Object>) lineObject;
                addLine(new Line.Builder().withJson(lineMap).build());
            }
        }


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
