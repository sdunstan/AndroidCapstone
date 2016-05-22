package com.twominuteplays.model;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.database.Exclude;
import com.twominuteplays.db.FirebaseStuff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Movie implements Parcelable {
    public static final String MOVIE_SHARE = "com.twominuteplays.model.MOVIE_SHARE";
    public static final String MOVIE_EXTRA = "movie";

    private final String id;
    private final String templateId;
    private final MovieState state;
    private final String title;
    private final String synopsis;
    private final String author;
    private final String scriptMarkup;
    private final String imageUrl;
    private final String movieUrl;
    private final List<Part> parts;

    public static final Parcelable.Creator<Movie> CREATOR =
            new Parcelable.Creator<Movie>() {

                @Override
                public Movie createFromParcel(Parcel parcel) {
                    return new Movie(parcel);
                }

                @Override
                public Movie[] newArray(int size) {
                    return new Movie[size];
                }
            };

    private Movie(String id,
                  String templateId,
                  MovieState movieState,
                  String title,
                  String synopsis,
                  String author,
                  String scriptMarkup,
                  String imageUrl,
                  String movieUrl,
                  List<Part> parts) {
        this.id = id;
        this.templateId = templateId;
        this.state = movieState == null ? MovieState.TEMPLATE : movieState;
        this.title = title;
        this.synopsis = synopsis;
        this.author = author;
        this.scriptMarkup = scriptMarkup;
        this.imageUrl = imageUrl;
        this.movieUrl = movieUrl;
        if (parts != null) {
            this.parts = Collections.unmodifiableList(parts);
        }
        else {
            this.parts = null;
        }
    }

    private Movie(Parcel in) {
        this.id = in.readString();
        this.templateId = in.readString();
        String movieStateString = in.readString();
        this.state = movieStateString == null ? MovieState.TEMPLATE : MovieState.valueOf(movieStateString);
        this.title = in.readString();
        this.synopsis = in.readString();
        this.author = in.readString();
        this.scriptMarkup = in.readString();
        this.imageUrl = in.readString();
        this.movieUrl = in.readString();
        List<Part> partsPrototype = new ArrayList<>();
        in.readList(partsPrototype, this.getClass().getClassLoader());
        this.parts = Collections.unmodifiableList(partsPrototype);
    }

    public String getId() {
        return id;
    }

    public String getTemplateId() { return templateId; }

    @Exclude
    public MovieState getState() { return state; }

    public String getMovieState() { return (state != null) ? state.toString() : null; }

    public String getTitle() {
        return title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public String getAuthor() {
        return author;
    }

    public String getScriptMarkup() {
        return scriptMarkup;
    }

    public List<Part> getParts() {
        return parts;
    }

    public String getImageUrl() { return imageUrl; }

    public String getMovieUrl() { return movieUrl; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(templateId);
        parcel.writeString(state.toString());
        parcel.writeString(title);
        parcel.writeString(synopsis);
        parcel.writeString(author);
        parcel.writeString(scriptMarkup);
        parcel.writeString(imageUrl);
        parcel.writeString(movieUrl);
        parcel.writeList(parts);
    }

    public void save() {
        FirebaseStuff.saveMovie(this);
    }

    public Movie select() {
        if (MovieState.TEMPLATE != getState())
            throw new IllegalStateException("Movie state must be TEMPLATE to select.");
        Movie selectedMovie = (new Builder())
                .withId(UUID.randomUUID().toString())
                .withTemplateId(getId())
                .withState(MovieState.SELECTED)
                .withTitle(getTitle())
                .withSynopsis(getSynopsis())
                .withAuthor(getAuthor())
                .withScriptMarkup(getScriptMarkup())
                .withImageUrl(getImageUrl())
                .withMovieUrl(getMovieUrl())
                .withParts(getParts())
                .build();
        selectedMovie.save();
        return selectedMovie;
    }

    private Movie copyWithState(MovieState newState) {
        Movie movie =  (new Builder())
                .withId(getId())
                .withTemplateId(getTemplateId())
                .withState(newState)
                .withTitle(getTitle())
                .withSynopsis(getSynopsis())
                .withAuthor(getAuthor())
                .withScriptMarkup(getScriptMarkup())
                .withImageUrl(getImageUrl())
                .withMovieUrl(getMovieUrl())
                .withParts(getParts())
                .build();
        movie.save();
        return movie;
    }

    public Movie selectPart(Part thePart) {
        if (!(MovieState.SELECTED == getState() || MovieState.RECORDING_STARTED == getState() || MovieState.RECORDED == getState()))
            throw new IllegalStateException("Movie state must be SELECTED, RECORDING STARTED, or RECORDED to select part. Is " + getState());
        Part selectedPart = thePart;
        selectedPart = (new Part.Builder())
                .withActorUid(FirebaseStuff.getUid())
                .withDescription(selectedPart.getDescription())
                .withCharacterName(selectedPart.getCharacterName())
                .withId(selectedPart.getId())
                .withLines(selectedPart.getLines())
                .build();
        Builder builder = new Builder();
        builder.withId(getId())
                .withTemplateId(getTemplateId())
                .withState(MovieState.PART_SELECTED)
                .withTitle(getTitle())
                .withSynopsis(getSynopsis())
                .withAuthor(getAuthor())
                .withScriptMarkup(getScriptMarkup())
                .withMovieUrl(getMovieUrl())
                .withImageUrl(getImageUrl());
        for (Part part : getParts()) {
            if (part.getId().equals(selectedPart.getId()))
                builder.addPart(selectedPart);
            else
                builder.addPart(part);
        }
        return builder.build();
    }

    public Movie startRecording() {
        if (MovieState.PART_SELECTED != getState())
            throw new IllegalStateException("Movie state must be PART SELECTED to start recording.");
        return copyWithState(MovieState.RECORDING_STARTED);
    }

    public Movie addVideo(String partId, String lineId, String videoUrl) {
        if (MovieState.RECORDING_STARTED != getState())
            throw new IllegalStateException("Movie state must be RECORDING STARTED to add video.");
        Builder builder = new Builder();
        builder.withId(getId())
                .withTemplateId(getTemplateId())
                .withState(getState())
                .withTitle(getTitle())
                .withSynopsis(getSynopsis())
                .withAuthor(getAuthor())
                .withScriptMarkup(getScriptMarkup())
                .withMovieUrl(getMovieUrl())
                .withImageUrl(getImageUrl());
        for (Part part : getParts()) {
            if (part.getId().equals(partId))
                builder.addPart(part.addVideo(lineId, videoUrl));
            else
                builder.addPart(part);
        }
        Movie movie = builder.build();
        movie.save();
        return movie;
    }

    public Movie recorded() {
        if (MovieState.RECORDING_STARTED != getState())
            throw new IllegalStateException("Movie state must be RECORDING STARTED to mark the movie as recorded.");
        return copyWithState(MovieState.RECORDED);
    }

    @SuppressWarnings("unused")
    public Movie shared() {
        if (MovieState.RECORDED != getState())
            throw new IllegalStateException("Movie state must be RECORDED to share the movie.");
        return copyWithState(MovieState.SHARED);
    }

    public String deepLink() {
        return "http://2mp.tv/play/" + FirebaseStuff.getUid() + "/" + getId();
    }

    public Movie addImageUrl(String absolutePath) {
        Builder builder = new Builder();
        builder.withId(getId())
                .withTemplateId(getTemplateId())
                .withState(getState())
                .withTitle(getTitle())
                .withSynopsis(getSynopsis())
                .withAuthor(getAuthor())
                .withScriptMarkup(getScriptMarkup())
                .withParts(getParts())
                .withMovieUrl(getMovieUrl())
                .withImageUrl(absolutePath);
        Movie movie = builder.build();
        movie.save();
        return movie;
    }

    public List<Line> assembleLines() {
        List<Line> lines = new java.util.ArrayList<>();
        for(Part part : getParts()) {
            lines.addAll(part.getLines());
        }
        Collections.sort(lines, new MovieLineSorter());
        return lines;
    }

    public Movie merged(String moviePath) {
        Builder builder = new Builder();
        builder.withId(getId())
                .withTemplateId(getTemplateId())
                .withState(MovieState.MERGED)
                .withTitle(getTitle())
                .withSynopsis(getSynopsis())
                .withAuthor(getAuthor())
                .withScriptMarkup(getScriptMarkup())
                .withParts(getParts())
                .withMovieUrl(moviePath)
                .withImageUrl(getImageUrl());
        return builder.build();
    }

    public void broadcastShare(Context context) {
        Intent shareIntent = new Intent(MOVIE_SHARE).putExtra(MOVIE_EXTRA, this);
        LocalBroadcastManager.getInstance(context).sendBroadcast(shareIntent);
    }

    @Exclude
    public boolean isRecorded() {
        boolean recorded = true;
        for (Part part : getParts()) {
            recorded = part.isRecorded();
            if (!recorded) break;
        }
        return recorded;
    }

    /**
     * Movie is not sharable if both parts have started their recording process.
     * @return
     */
    @Exclude
    public boolean isSharable() {
        // TODO: implement. State must be SHARED or RECORDED or MERGED
        return true;
    }

    public enum MovieState {
        TEMPLATE,
        SELECTED,
        PART_SELECTED,
        RECORDING_STARTED,
        CONTRIBUTE, // like RECORDING_STARTED except for when I accept a share
        RECORDED,
        CONTRIBUTED, // for when we detect someone has recorded their part
        SINGLE_USER, // for when you choose to record all the parts yourself
        SINGLE_USER_MERGED,
        SHARED,
        MERGED
    }

    public static class Builder {
        private String id;
        private String templateId;
        private MovieState movieState;
        private String title;
        private String synopsis;
        private String author;
        private String scriptMarkup;
        private String imageUrl;
        private List<Part> parts;
        private String movieUrl;

        public Builder withJson(Map<String, Object> jsonSnapshot) {
            this.id = (String)jsonSnapshot.get("id");
            this.templateId = (String)jsonSnapshot.get("templateId");
            this.movieState = marshalMovieState(jsonSnapshot.get("movieState"));
            this.title = (String)jsonSnapshot.get("title");
            this.synopsis = (String)jsonSnapshot.get("synopsis");
            this.author = (String)jsonSnapshot.get("author");
            this.scriptMarkup = (String)jsonSnapshot.get("scriptMarkup");
            this.imageUrl = (String)jsonSnapshot.get("imageUrl");
            this.movieUrl = (String)jsonSnapshot.get("movieUrl");
            marshalParts(jsonSnapshot);

            return this;
        }

        private void marshalParts(Map<String, Object> jsonSnapshot) {
            for(Object partObject : (Iterable)jsonSnapshot.get("parts")) {
                Map<String,Object> partMap = (Map<String, Object>) partObject;
                addPart(new Part.Builder().withJson(partMap).build());
            }
        }

        private MovieState marshalMovieState(Object movieState) {
            MovieState movieStateEnum = null;
            if (movieState != null) {
                movieStateEnum = MovieState.valueOf((String) movieState);
            }
            return movieStateEnum;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withState(MovieState movieState) {
            this.movieState = movieState;
            return this;
        }

        public Builder withTemplateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withSynopsis(String synopsis) {
            this.synopsis = synopsis;
            return this;
        }

        public Builder withAuthor(String author) {
            this.author = author;
            return this;
        }

        public Builder withScriptMarkup(String scriptMarkup) {
            this.scriptMarkup = scriptMarkup;
            return this;
        }

        public Builder withImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        private List<Part> getParts() {
            if (parts == null) {
                parts = new java.util.ArrayList<>();
            }
            return parts;
        }

        public Builder addPart(Part part) {
            getParts().add(part);
            return this;
        }

        public Builder withParts(List<Part> parts) {
            for(Part part : parts) {
                addPart(part);
            }
            return this;
        }

        public Movie build() {
            return new Movie(id, templateId, movieState, title, synopsis, author, scriptMarkup, imageUrl, movieUrl, getParts());
        }

        public Builder withMovieUrl(String movieUrl) {
            this.movieUrl = movieUrl;
            return this;
        }

    }


}
